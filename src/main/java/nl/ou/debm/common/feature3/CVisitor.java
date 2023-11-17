package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        var statements = ctx.compoundStatement().blockItemList().blockItem();
        if(statements.size() > 0) {
            var lastStatement = statements.get(statements.size() - 1).statement();
            var hasReturn = lastStatement != null && lastStatement.jumpStatement() != null && lastStatement.jumpStatement().Return() != null;
            result.setNumberOfStatements(hasReturn ? statements.size() - 1 : statements.size());
            for (var i = 0; i < statements.size(); i++) {
                var matcher = _pattern.matcher(statements.get(i).getText());
                if (matcher.find()) {
                    var marker = new FunctionCodeMarker(matcher.group(1), functionId, i);
                    markersById.put(marker.getID(), marker);
                    result.addMarker(marker);
                }
            }
        }

        functions.put(functionId, result);
        return super.visitFunctionDefinition(ctx);
    }
}
