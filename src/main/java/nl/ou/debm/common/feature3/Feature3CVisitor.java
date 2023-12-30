package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.producer.EFeaturePrefix;
import org.antlr.v4.runtime.RuleContext;

import java.util.*;

public class Feature3CVisitor extends CBaseVisitor{
    public HashMap<Integer, FoundFunction> functions = new HashMap<>();
    public HashMap<String, FoundFunction> functionsByName = new HashMap<>();
    public HashMap<String, FunctionCodeMarker> markersById = new HashMap<>();

    public Object visitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        /*
        Ghidra creates empty structs, with even incorrect C code.
        ANTLR sees these lines as function definitions.
        Therefore, we return when no function body is found
        */
        if(ctx.compoundStatement() == null || ctx.compoundStatement().blockItemList() == null)
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
        if(statements.size() > 0) {
            for (CParser.BlockItemContext statement : statements) {
                for(var function : functions.values()){
                    if(statement.getText().contains(function.getName()+"("))
                    {
                        function.addCalledFromFunction(result.getName());
                    }
                }
                var codeMarker = CodeMarker.findInStatement(EFeaturePrefix.FUNCTIONFEATURE, statement.getText());
                if (codeMarker != null) {
                    var marker = new FunctionCodeMarker(codeMarker);
                    marker.functionId = functionId;
                    marker.positionInFunction = numberOfActualBodyStatements;
                    markersById.put(marker.rawMarker.getID(), marker);
                    result.addMarker(marker);
                    numberOfActualBodyStatements++;
                } else {
                    if (numberOfActualBodyStatements > 0 || !isPrologueStatement(statement, argumentNames))
                        numberOfActualBodyStatements++;
                }
            }

            for (var i = statements.size() - 1; i >= 0 && !CodeMarker.isInStatement(EFeaturePrefix.FUNCTIONFEATURE, statements.get(i).getText()) && isEpilogueStatement(statements.get(i)); i--) {
                numberOfActualBodyStatements--;
            }
        }
        result.setNumberOfStatements(numberOfActualBodyStatements);
        return super.visitFunctionDefinition(ctx);
    }

    private boolean isPrologueStatement(CParser.BlockItemContext blockItem, List<String> argumentNames){
        var assignmentInitializerText = Optional.of(blockItem)
                .map(CParser.BlockItemContext::declaration)
                .map(CParser.DeclarationContext::initDeclaratorList)
                .map(x -> x.initDeclarator(0))
                .map(CParser.InitDeclaratorContext::initializer)
                .map(CParser.InitializerContext::assignmentExpression)
                .map(RuleContext::getText)
                .orElse(null);
        if(assignmentInitializerText != null) {
            if (argumentNames.stream().anyMatch(x -> x.equals(assignmentInitializerText)))
                return true;
        }
        return false;
    }

    private boolean isEpilogueStatement(CParser.BlockItemContext blockItem){
        if(blockItem.getText().startsWith("__asm"))
            return true;
        var returnStatement = Optional.of(blockItem)
                .map(CParser.BlockItemContext::statement)
                .map(CParser.StatementContext::jumpStatement)
                .map(CParser.JumpStatementContext::Return);
        if(returnStatement.isPresent()) {
            return true;
        }
        return false;
    }
}
