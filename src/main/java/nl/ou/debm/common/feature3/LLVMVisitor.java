package nl.ou.debm.common.feature3;

import nl.ou.debm.common.antlr.LLVMIRBaseVisitor;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.HashMap;
import java.util.Objects;

public class LLVMVisitor extends LLVMIRBaseVisitor {
    public HashMap<String, FoundFunction> functions = new HashMap<>();
    private FoundFunction lastFunction;

    @Override
    public Object visitFuncDef(LLVMIRParser.FuncDefContext ctx) {
        if (ctx.funcHeader().GlobalIdent().getSymbol().getText() != null) {
            var result = new FoundFunction();
            result.name = removeLeadingAts(ctx.funcHeader().GlobalIdent().getSymbol().getText());
            result.type = ctx.funcHeader().type().getText();
            for(var param : ctx.funcHeader().params().param()){
                result.addParameter(param.type().getText());
            }
            functions.put(result.getSignature(), result);
            lastFunction = result;
        }
        return super.visitFuncDef(ctx);
    }

    String removeLeadingAts(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() > 1 && sb.charAt(0) == '@') {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    @Override
    public Object visitLocalDefInst(LLVMIRParser.LocalDefInstContext ctx){
        if(Objects.equals(ctx.LocalIdent().getText(), "%retval")) {
            if(ctx.valueInstruction().allocaInst() != null && lastFunction != null) {
                lastFunction.type = ctx.valueInstruction().allocaInst().type().getText();
            }
        }
        return super.visitLocalDefInst(ctx);
    }
}
