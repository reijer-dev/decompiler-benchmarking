package nl.ou.debm.common.feature5;

import nl.ou.debm.common.antlr.LLVMIRBaseVisitor;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.HashMap;

public class IndirectionsLLVMVisitor extends LLVMIRBaseVisitor<Object> {

    private final HashMap<Long, SwitchInfo> switches = new HashMap<>(){
        { put(121L, new SwitchInfo()); }
    };
    @Override
    public Object visitFuncBody(LLVMIRParser.FuncBodyContext ctx){

        for(var block : ctx.basicBlock()){

            for(var instruction : block.instruction()){
                if(instruction.getText().contains("switch")){
                    System.out.println("Switch!");
                }
            }
        }
        return null;
    }

    public HashMap<Long, SwitchInfo> getSwitches() {
        return switches;
    }
}
