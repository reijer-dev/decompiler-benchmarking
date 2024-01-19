package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.EFeaturePrefix;
import org.antlr.v4.runtime.RuleContext;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Feature3CVisitor extends CBaseVisitor<Object> {
    public HashMap<Integer, FoundFunction> functions = new HashMap<>();
    public HashMap<String, FoundFunction> functionsByName = new HashMap<>();
    public HashMap<Long, FunctionCodeMarker> markersById = new HashMap<>();

    private Pattern functionCallPattern = Pattern.compile("([a-zA-Z]+\\S+?)\\(", Pattern.CASE_INSENSITIVE);

    //^(\S+?)=([^;\"+\-&]*)[^a-zA-Z0-9]*(a1|a2|a3|a4|a5|a6|a7|a8)[^a-zA-Z0-9]
    private Pattern localVariableInitPattern = Pattern.compile("^[a-zA-Z_][0-9a-zA-Z_]+;");

    Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    //^[a-z_][0-9a-z_]+;
    private HashMap<String, Pattern> copyInPatterns = new HashMap<>();
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
        if(!isSourceVisitor){
            System.out.println("test");
        }
        if (ctx.compoundStatement() == null || ctx.compoundStatement().blockItemList() == null)
            return null;

        var functionId = functions.size();
        var result = new FoundFunction();
        if (ctx.declarator().directDeclarator().Identifier() != null)
            result.setName(ctx.declarator().directDeclarator().Identifier().getText());
        else if (ctx.declarator().directDeclarator().directDeclarator().Identifier() != null)
            result.setName(ctx.declarator().directDeclarator().directDeclarator().Identifier().getText());

        functions.put(functionId, result);
        functionsByName.put(result.getName(), result);

        var parameterTypeList = Optional.of(ctx)
                .map(CParser.FunctionDefinitionContext::declarator)
                .map(CParser.DeclaratorContext::directDeclarator)
                .map(CParser.DirectDeclaratorContext::parameterTypeList);

        result.setIsVariadic(parameterTypeList
                .map(CParser.ParameterTypeListContext::Ellipsis)
                .isPresent());

        var arguments = parameterTypeList
                .map(CParser.ParameterTypeListContext::parameterList)
                .map(CParser.ParameterListContext::parameterDeclaration)
                .orElse(new ArrayList<>());

        var argumentNames = new ArrayList<>(arguments.stream().map(x -> Optional.ofNullable(x.declarator()).map(RuleContext::getText).orElse(null)).filter(Objects::nonNull).toList());

        var statements = ctx.compoundStatement().blockItemList().blockItem();
        var actualCodeStarted = false;
        var actualCodeEndIndex = statements.size() - 1;
        if (statements.size() > 0) {
            var textPerStatement = new HashMap<CParser.BlockItemContext, String>();
            for (CParser.BlockItemContext statement : statements) {
                var statementText = statement.getText();
                textPerStatement.put(statement, statementText);
            }

            while (actualCodeEndIndex >= 0 && isEpilogueStatement(textPerStatement.get(statements.get(actualCodeEndIndex)), statements.get(actualCodeEndIndex))) {
                actualCodeEndIndex--;
            }

            for(var i = 0; i < statements.size(); i++){
                var statement = statements.get(i);
                var statementText = textPerStatement.get(statement);
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
                    marker.functionId = functionId;
                    marker.isAtFunctionStart = !actualCodeStarted;
                    marker.isAtFunctionEnd = i >= actualCodeEndIndex;
                    markersById.put(marker.lngGetID(), marker);
                    result.addMarker(marker);
                } else {
                    if (!actualCodeStarted && !isPrologueStatement(statementText, statement, argumentNames))
                        actualCodeStarted = true;
                }
            }
        }

        return null;
    }

    private boolean isPrologueStatement(String statementText, CParser.BlockItemContext statement, ArrayList<String> argumentNames) {
        if(isSourceVisitor)
            return false;
        if (statementText.startsWith("__asm"))
            return true;
        if (statementText.contains(CodeMarker.STRCODEMARKERGUID))
            return true;

        var localVariableInitMatcher = localVariableInitPattern.matcher(statementText);
        if(localVariableInitMatcher.find()) {
            var localVariableName = Optional.of(statement)
                    .map(CParser.BlockItemContext::declaration)
                    .map(CParser.DeclarationContext::declarationSpecifiers)
                    .map(x -> x.declarationSpecifier(1))
                    .map(RuleContext::getText)
                    .orElse(null);
            if(localVariableName != null)
                argumentNames.add(localVariableName);
            return true;
        }

        var argumentNamesConcatenated = String.join("|", argumentNames.stream().map(this::regexEscaped).toList());
        if(argumentNamesConcatenated.contains("*")){
            System.out.println("ouch");
        }
        for(var argumentName : argumentNames) {
            var copyInPattern = copyInPatterns.getOrDefault(argumentName, Pattern.compile("^(\\S+?)=([^;\"+\\-&]*)[^a-zA-Z0-9]*(" + argumentNamesConcatenated + ")[^a-zA-Z0-9]"));
            var matcher = copyInPattern.matcher(statementText);
            if(matcher.find())
                return true;
            if(!copyInPatterns.containsKey(argumentName))
                copyInPatterns.put(argumentName, copyInPattern);
        }
        return false;
    }

    private String regexEscaped(String str){
        return SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0");
    }

    private boolean isEpilogueStatement(String statementText, CParser.BlockItemContext blockItem) {
        if(isSourceVisitor)
            return false;
        if (statementText.startsWith("__asm"))
            return true;
        var returnStatement = Optional.of(blockItem)
                .map(CParser.BlockItemContext::statement)
                .map(CParser.StatementContext::jumpStatement)
                .map(CParser.JumpStatementContext::Return);
        if (returnStatement.isPresent()) {
            return true;
        }
        return false;
    }
}
