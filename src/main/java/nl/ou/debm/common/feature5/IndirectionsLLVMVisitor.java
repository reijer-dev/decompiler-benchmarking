package nl.ou.debm.common.feature5;

import nl.ou.debm.common.antlr.LLVMIRBaseVisitor;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.HashMap;

public class IndirectionsLLVMVisitor extends LLVMIRBaseVisitor<Object> {

    /*
    Het idee lijkt me: door de instructions heen gaan.
    Als je een switch codemarker ziet (instruction), dan die als huidige codemarker instellen.
    Als je dan een switchterm tegenkomt (is geen instruction, maar een terminator), dan kun je de codemarker aanvullen
     */

    public IndirectionsLLVMVisitor(){
        //Fill dummy switch table
        for(var i = 0L; i < 400; i++)
            switches.put(i, new SwitchInfo());
    }

    private final HashMap<Long, SwitchInfo> switches = new HashMap<>();
    private IndirectionsCodeMarker currentSwitchCodeMarker;

    @Override
    public Object visitSwitchTerm(LLVMIRParser.SwitchTermContext ctx){

        return null;
    }

    @Override
    public Object visitInstruction(LLVMIRParser.InstructionContext ctx) {

        return null;
    }

    public HashMap<Long, SwitchInfo> getSwitches() {
        return switches;
    }
}
