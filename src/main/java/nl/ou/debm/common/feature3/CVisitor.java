package nl.ou.debm.common.feature3;

import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CParser;

import java.util.HashMap;

public class CVisitor extends CBaseVisitor {
    public HashMap<String, FoundFunction> functions = new HashMap<>();

    @Override
    public Object visitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        var result = new FoundFunction();
        if (ctx.declarator().directDeclarator().Identifier() != null)
            result.name = ctx.declarator().directDeclarator().Identifier().getText();
        else if (ctx.declarator().directDeclarator().directDeclarator().Identifier() != null)
            result.name = ctx.declarator().directDeclarator().directDeclarator().Identifier().getText();
        if(ctx.declarationSpecifiers().declarationSpecifier(0).typeSpecifier().structOrUnionSpecifier() != null) {
            if(ctx.declarationSpecifiers().declarationSpecifier(0).typeSpecifier().structOrUnionSpecifier().structOrUnion().Struct() != null)
                result.type = "%struct." + ctx.declarationSpecifiers().declarationSpecifier(0).typeSpecifier().structOrUnionSpecifier().Identifier().getText();
        }
        else if(ctx.declarationSpecifiers().declarationSpecifier(0).typeSpecifier().atomicTypeSpecifier() != null)
            result.type = ctx.declarationSpecifiers().declarationSpecifier(0).typeSpecifier().atomicTypeSpecifier().Atomic().getText();
        else
            result.type = ctx.declarationSpecifiers().declarationSpecifier(0).typeSpecifier().getText();
        if(ctx.declarator().directDeclarator().parameterTypeList() != null) {
            for (var param : ctx.declarator().directDeclarator().parameterTypeList().parameterList().parameterDeclaration()) {
                //type void falls in declarationSpecifier2
                if(param.declarationSpecifiers() != null)
                    result.addParameter(param.declarationSpecifiers().getText());
                else if(param.declarationSpecifiers2() != null)
                    result.addParameter(param.declarationSpecifiers2().getText());
            }
        }
        functions.put(result.getSignature(), result);
        return super.visitFunctionDefinition(ctx);
    }
}
