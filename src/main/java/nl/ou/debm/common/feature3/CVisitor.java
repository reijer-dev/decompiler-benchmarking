package nl.ou.debm.common.feature3;

import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CParser;
import org.antlr.v4.runtime.RuleContext;

import java.util.*;
import java.util.regex.Pattern;

public class CVisitor extends CBaseVisitor {
    public HashMap<Integer, FoundFunction> functions = new HashMap<>();
    public HashMap<String, FunctionCodeMarker> markersById = new HashMap<>();
    private Pattern _pattern;

    public CVisitor() {
        _pattern = Pattern.compile(".+\\(\"" + FunctionProducer.FunctionMarkerPrefix + "(.+)\"", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Object visitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        var functionId = functions.size();
        var result = new FoundFunction();
        if (ctx.declarator().directDeclarator().Identifier() != null)
            result.setName(ctx.declarator().directDeclarator().Identifier().getText());
        else if (ctx.declarator().directDeclarator().directDeclarator().Identifier() != null)
            result.setName(ctx.declarator().directDeclarator().directDeclarator().Identifier().getText());

        var arguments = Optional.of(ctx)
                .map(CParser.FunctionDefinitionContext::declarator)
                .map(CParser.DeclaratorContext::directDeclarator)
                .map(CParser.DirectDeclaratorContext::parameterTypeList)
                .map(CParser.ParameterTypeListContext::parameterList)
                .map(CParser.ParameterListContext::parameterDeclaration)
                .orElse(new ArrayList<>());
        var argumentNames = arguments.stream().map(x -> Optional.ofNullable(x.declarator()).map(RuleContext::getText).orElse(null)).filter(Objects::nonNull).toList();

        var statements = ctx.compoundStatement().blockItemList().blockItem();
        var numberOfActualBodyStatements = 0;
        if(statements.size() > 0) {
            var lastStatement = statements.get(statements.size() - 1).statement();
            var hasReturn = lastStatement != null && lastStatement.jumpStatement() != null && lastStatement.jumpStatement().Return() != null;
            result.setNumberOfStatements(hasReturn ? statements.size() - 1 : statements.size());
            for (var i = 0; i < statements.size(); i++) {
                var matcher = _pattern.matcher(statements.get(i).getText());
                if (matcher.find()) {
                    var marker = new FunctionCodeMarker(matcher.group(1), functionId, numberOfActualBodyStatements);
                    markersById.put(marker.getID(), marker);
                    result.addMarker(marker);
                    numberOfActualBodyStatements++;
                }else{
                    if(numberOfActualBodyStatements > 0 || !isPrologueStatement(statements.get(i), argumentNames))
                        numberOfActualBodyStatements++;
                }
            }

            for (var i = statements.size() - 1; i >= 0 && !_pattern.matcher(statements.get(i).getText()).find() && isEpilogueStatement(statements.get(i)); i--) {
                numberOfActualBodyStatements--;
            }
        }
        result.setNumberOfStatements(numberOfActualBodyStatements);
        functions.put(functionId, result);
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
