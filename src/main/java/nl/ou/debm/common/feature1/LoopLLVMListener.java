package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.antlr.LLVMIRBaseListener;
import nl.ou.debm.common.antlr.LLVMIRParser;

public class LoopLLVMListener extends LLVMIRBaseListener {
    @Override
    public void enterGlobalDef(LLVMIRParser.GlobalDefContext ctx) {
        super.enterGlobalDef(ctx);

        var gcm = CodeMarker.findInGlobalDef(EFeaturePrefix.CONTROLFLOWFEATURE, ctx.getText());
        if (gcm==null){
            return;
        }

        var cm = (LoopCodeMarker) gcm;

        System.out.println(cm);
    }
}
