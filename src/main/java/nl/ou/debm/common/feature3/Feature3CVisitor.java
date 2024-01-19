package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.EFeaturePrefix;
import org.antlr.v4.runtime.RuleContext;

import java.util.*;
import java.util.regex.Pattern;

public class Feature3CVisitor extends CBaseVisitor<Object> {
    public HashMap<Integer, FoundFunction> functions = new HashMap<>();
    public HashMap<String, FoundFunction> functionsByName = new HashMap<>();
    public HashMap<Long, FunctionCodeMarker> markersById = new HashMap<>();
    private Pattern functionCallPattern = Pattern.compile("([a-zA-Z]+\\S+?)\\(", Pattern.CASE_INSENSITIVE);

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
        var argumentNames = arguments.stream().map(x -> Optional.ofNullable(x.declarator()).map(RuleContext::getText).orElse(null)).filter(Objects::nonNull).toList();

        var statements = ctx.compoundStatement().blockItemList().blockItem();
        var numberOfActualBodyStatements = 0;
        if (statements.size() > 0) {
            var textPerStatement = new HashMap<CParser.BlockItemContext, String>();
            for (CParser.BlockItemContext statement : statements) {
                var statementText = statement.getText();
                textPerStatement.put(statement, statementText);
                var functionCallsMatch = functionCallPattern.matcher(statementText);
                var anyFunctionFound = false;
                while(functionCallsMatch.find()){
                    anyFunctionFound = true;
                    for(var i = 0; i < functionCallsMatch.groupCount(); i++){
                        var function = functionsByName.getOrDefault(functionCallsMatch.group(1), null);
                        if(function != null)
                            function.addCalledFromFunction(result.getName());
                    }
                }

                var marker = anyFunctionFound ? (FunctionCodeMarker) CodeMarker.findInStatement(EFeaturePrefix.FUNCTIONFEATURE, statementText) : null;

                if (marker != null) {
                    marker.functionId = functionId;
                    marker.positionInFunction = numberOfActualBodyStatements;
                    markersById.put(marker.lngGetID(), marker);
                    result.addMarker(marker);
                    numberOfActualBodyStatements++;
                } else {
                    if (numberOfActualBodyStatements > 0 || !isPrologueStatement(statementText, statement, argumentNames))
                        numberOfActualBodyStatements++;
                }
            }

            for (var i = statements.size() - 1; i >= 0 && !CodeMarker.isInStatement(EFeaturePrefix.FUNCTIONFEATURE, textPerStatement.get(statements.get(i))) && isEpilogueStatement(textPerStatement.get(statements.get(i)), statements.get(i)); i--) {
                numberOfActualBodyStatements--;
            }
        }

        result.setNumberOfStatements(numberOfActualBodyStatements);
        return null;
    }

    private boolean isPrologueStatement(String statementText, CParser.BlockItemContext blockItem, List<String> argumentNames) {
        if (statementText.contains(CodeMarker.STRCODEMARKERGUID))
            return true;
        var assignmentInitializerText = Optional.of(blockItem)
                .map(CParser.BlockItemContext::declaration)
                .map(CParser.DeclarationContext::initDeclaratorList)
                .map(x -> x.initDeclarator(0))
                .map(CParser.InitDeclaratorContext::initializer)
                .map(CParser.InitializerContext::assignmentExpression)
                .map(RuleContext::getText)
                .orElse(null);
        if (assignmentInitializerText != null) {
            if (argumentNames.stream().anyMatch(x -> x.equals(assignmentInitializerText)))
                return true;
        }
        return false;
    }

    private boolean isEpilogueStatement(String statementText, CParser.BlockItemContext blockItem) {
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
