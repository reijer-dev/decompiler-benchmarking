package nl.ou.debm.common.feature5;

import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.LLVMIRBaseListener;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.Map;

import static java.lang.System.exit;

public class IndirectionLLVMListener extends LLVMIRBaseListener {

    private String m_strCurrentFunctionName;
    final private Map<Long, SwitchInfo> m_si;

    public IndirectionLLVMListener(Map<Long, SwitchInfo> info){
        m_si=info;
    }

    @Override
    public void enterFuncDef(LLVMIRParser.FuncDefContext ctx) {
        super.enterFuncDef(ctx);
    }

    @Override
    public void enterFuncHeader(LLVMIRParser.FuncHeaderContext ctx) {
        super.enterFuncHeader(ctx);
        m_strCurrentFunctionName = ctx.GlobalIdent().toString();
    }

    @Override
    public void enterBasicBlock(LLVMIRParser.BasicBlockContext ctx) {
        super.enterBasicBlock(ctx);
        if (ctx.LabelIdent()!=null) {
            System.out.println("BB: " + Misc.strSafeLeftString(ctx.getText(), 20));
        }
    }

    @Override
    public void enterSwitchTerm(LLVMIRParser.SwitchTermContext ctx) {
        super.enterSwitchTerm(ctx);
        System.out.println(m_strCurrentFunctionName);
        System.out.println(ctx.getText());

        // claim switch info storage
        SwitchInfo si = new SwitchInfo();

        // analyze branches
        for (var case_ : ctx.case_()){
            //System.out.println("  " + case_.getText());
            var ci = new SwitchInfo.CaseInfo();
            ci.m_lngBranchValue = Misc.lngRobustStringToLong(case_.typeConst().constant().getText());       // get branch value
            ci.m_strLLVMLabel = case_.label().LocalIdent().getText().substring(1);                // remove % from label

            System.out.println("  " + ci.m_lngBranchValue + " --> " + ci.m_strLLVMLabel);

            si.m_caseInfo.add(ci);
        }


        exit(0);
    }
}
