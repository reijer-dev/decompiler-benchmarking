package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.antlr.LLVMIRBaseListener;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.Map;

public class LoopLLVMListener extends LLVMIRBaseListener {

    private final Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_defMap;

    public LoopLLVMListener(Map<Long, CodeMarker.CodeMarkerLLVMInfo> defaultMap){
        m_defMap = defaultMap;
    }

    @Override
    public void enterInstruction(LLVMIRParser.InstructionContext ctx) {
        super.enterInstruction(ctx);
        System.out.println(ctx.getText());

    }

}
