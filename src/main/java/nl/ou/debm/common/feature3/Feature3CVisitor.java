package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.EFeaturePrefix;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;
import java.util.regex.Pattern;

public class Feature3CVisitor extends CBaseVisitor<Object> {
    public HashMap<Integer, FoundFunction> functions = new HashMap<>();
    public HashMap<String, FoundFunction> functionsByName = new HashMap<>();
    public HashMap<Long, FunctionCodeMarker> markersById = new HashMap<>();

    private static final Pattern functionCallPattern = Pattern.compile("(?:return)*(_*[a-zA-Z][a-zA-Z0-9_]+)\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern variableDeclarationPatterns = Pattern.compile("^([^=]+)[^)];", Pattern.CASE_INSENSITIVE);

    Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");
    private HashMap<CParser.FunctionDefinitionContext, Pattern> registerHomingPatterns = new HashMap<>();
    private boolean isSourceVisitor;

    public Feature3CVisitor(boolean isSourceVisitor){
        this.isSourceVisitor = isSourceVisitor;
    }

    public Object visitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        /*
        Ghidra creates empty structs, with even incorrect C code.
        ANTLR sees these lines as function definitions.
        Therefore, we return when no function body is found
        */
        if (ctx.compoundStatement() == null || ctx.compoundStatement().blockItemList() == null)
            return null;

        var functionId = functions.size();
        var result = new FoundFunction();
        var name = Optional.of(ctx.declarator())
                .map(CParser.DeclaratorContext::directDeclarator)
                .map(CParser.DirectDeclaratorContext::directDeclarator)
                .map(CParser.DirectDeclaratorContext::Identifier)
                .map(ParseTree::getText)
                .orElse(Optional.of(ctx.declarator())
                        .map(CParser.DeclaratorContext::directDeclarator)
                        .map(CParser.DirectDeclaratorContext::Identifier)
                        .map(ParseTree::getText)
                        .orElse(null));
        if(name == null)
            return null;

        result.setName(name);

        var parameterTypeList = Optional.of(ctx)
                .map(CParser.FunctionDefinitionContext::declarator)
                .map(CParser.DeclaratorContext::directDeclarator)
                .map(CParser.DirectDeclaratorContext::parameterTypeList);

        if(parameterTypeList.isEmpty())
            return null;

        functions.put(functionId, result);
        functionsByName.put(result.getName(), result);

        result.setIsVariadic(parameterTypeList
                .map(CParser.ParameterTypeListContext::Ellipsis)
                .isPresent());

        var arguments = parameterTypeList
                .map(CParser.ParameterTypeListContext::parameterList)
                .map(CParser.ParameterListContext::parameterDeclaration)
                .orElse(new ArrayList<>());
        var argumentNames = new ArrayList<>(arguments.stream().map(x -> Optional.ofNullable(x.declarator()).map(RuleContext::getText).map(this::regexEscaped).orElse(null)).filter(Objects::nonNull).toList());
        if(!argumentNames.isEmpty())
            registerHomingPatterns.put(ctx, compileRegisterHomingPattern(argumentNames));

        var statements = ctx.compoundStatement().blockItemList().blockItem();
        result.setNumberOfStatements(statements.size());
        if (!statements.isEmpty()) {
            var textPerStatement = new HashMap<CParser.BlockItemContext, String>();
            for (CParser.BlockItemContext statement : statements) {
                var statementText = statement.getText();
                textPerStatement.put(statement, statementText);
            }

            var actualCodeEndIndex = statements.size() - 1;
            while (actualCodeEndIndex >= 0) {
                var statement = statements.get(actualCodeEndIndex);
                var statementText = textPerStatement.get(statement);

                if(statementText.contains(CodeMarker.STRCODEMARKERGUID))
                    break;

                actualCodeEndIndex--;
            }

            var printfFound = false;
            var indexOfLastEndMarker = 0;
            var variableDeclarationsBeforeStartMarker = 0;
            var registerHomingBeforeStartMarker = 0;
            var endWithReturn = false;

            for(var i = 0; i < statements.size(); i++){
                var statement = statements.get(i);
                var statementText = textPerStatement.get(statement);
                if(statementText.startsWith("return"))
                    result.registerReturnStatement();
                var functionCallsMatch = functionCallPattern.matcher(statementText);
                var anyFunctionFound = false;
                while(functionCallsMatch.find()){
                    anyFunctionFound = true;
                    var function = functionsByName.getOrDefault(functionCallsMatch.group(1), null);
                    if(function != null)
                        function.addCalledFromFunction(result.getName());
                }

                var marker = anyFunctionFound ? (FunctionCodeMarker) CodeMarker.findInStatement(EFeaturePrefix.FUNCTIONFEATURE, statementText) : null;

                if (marker != null) {
                    if(marker.isEndMarker())
                        indexOfLastEndMarker = i;
                    marker.functionId = functionId;
                    markersById.put(marker.lngGetID(), marker);
                    result.addMarker(marker);
                }

                if(!printfFound){
                    if(isVariableDeclaration(statementText))
                        variableDeclarationsBeforeStartMarker++;

                    var registerHomingPattern = registerHomingPatterns.get(ctx);
                    if(registerHomingPattern != null && registerHomingPattern.matcher(statementText).find())
                        registerHomingBeforeStartMarker++;

                    if(statementText.contains(CodeMarker.STRCODEMARKERGUID)) {
                        //Prologue statements = number of statements before the first code marker
                        result.setNumberOfPrologueStatements(i);
                        result.setVariableDeclarationsBeforeStartMarker(variableDeclarationsBeforeStartMarker);
                        result.setRegisterHomingBeforeStartMarker(registerHomingBeforeStartMarker);
                        printfFound = true;
                    }
                }
                if(i == statements.size() - 1 && statementText.startsWith("return"))
                    endWithReturn = true;
            }

            var epilogueSize = statements.size() - indexOfLastEndMarker;
            if(endWithReturn)
                epilogueSize--;
            result.setNumberOfEpilogueStatements(epilogueSize);
        }

        return null;
    }

    private boolean isVariableDeclaration(String line){
        return variableDeclarationPatterns.matcher(line).find();
    }

    private Pattern compileRegisterHomingPattern(List<String> argumentNames){
        return Pattern.compile("^(\\S+?)=([^;\"+\\-&]*)[^a-zA-Z0-9]*(" + String.join("|", argumentNames) + ")[^a-zA-Z0-9]");
    }

    private String regexEscaped(String str){
        return SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0")
                .replaceAll("\\*", "")
                .replaceAll("\\\\", "");
    }
}
