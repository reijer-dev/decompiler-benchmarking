package nl.ou.debm.common.feature5;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.LLVMIRBaseListener;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;

public class IndirectionLLVMListener extends LLVMIRBaseListener {

    /** name of the current function we're in */private String m_strCurrentFunctionName;
    /** info on al switches, key=switchID */ private final Map<Long, SwitchInfo> m_si;
    /** block "key" starts with indirections code marker "idm" */ private final Map<Long, IndirectionsCodeMarker> m_idmPerBlock = new HashMap<>();

    /** */ private Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_basicLLVMInfo = new HashMap<>();
    /** */ private Map<String, Long> m_LLVMIDtoCodeMarkerID = new HashMap<>();

    public IndirectionLLVMListener(Map<Long, SwitchInfo> info, LLVMIRParser parser){
        m_si=info;


        CodeMarker.getCodeMarkerInfoFromLLVM(parser, m_basicLLVMInfo, m_LLVMIDtoCodeMarkerID);
        parser.reset();
    }

    @Override
    public void enterFuncDef(LLVMIRParser.FuncDefContext ctx) {
        super.enterFuncDef(ctx);

        int cnt=0;

        // probe all basic blocks and try to find indirection code markers
        boolean bQuit = false;
        for (var basicBlock : ctx.funcBody().basicBlock()){     // block loop
            if (basicBlock.LabelIdent() != null){               // only when labeled (looking for a case)
                if (!basicBlock.instruction().isEmpty()) {      // only with instructions
                    var ins = basicBlock.instruction().get(0);  // get first instruction
                    if (ins.valueInstruction()!=null){
                        var call = ins.valueInstruction().callInst();
                        if (call != null){                      // we've found a call!
                            // so, we have a block that starts with a call
                            // does the call have an indirections code marker?
                            var icm = extractIndirectionsCodeMarkerFromCall(call);
                            if (icm!=null){
                                System.out.println(icm);
                            }
                        }
                    }
                    if (ins.getText().contains("__CM_printf")){
                        bQuit=true;
                    }
                }
            }
        }

        if (bQuit) {exit (0);}
    }

    private IndirectionsCodeMarker extractIndirectionsCodeMarkerFromCall(LLVMIRParser.CallInstContext call){
        // get all globals
        List<String> glob = Misc.getGlobalsFromLLVMString(call.getText());

        for (var s: glob){
            
        }

        return null;
    }

    @Override
    public void enterFuncHeader(LLVMIRParser.FuncHeaderContext ctx) {
        super.enterFuncHeader(ctx);
        m_strCurrentFunctionName = ctx.GlobalIdent().toString();
    }

    @Override
    public void enterBasicBlock(LLVMIRParser.BasicBlockContext ctx) {
        super.enterBasicBlock(ctx);
//        if (ctx.LabelIdent()!=null) {
//            System.out.println("BB: " + Misc.strSafeLeftString(ctx.getText(), 20));
//        }
    }

    @Override
    public void enterSwitchTerm(LLVMIRParser.SwitchTermContext ctx) {
        super.enterSwitchTerm(ctx);
        System.out.println(m_strCurrentFunctionName);
        System.out.println(ctx.getText());

        // claim switch info storage
        SwitchInfo si = new SwitchInfo();
        si.setFunctionName(m_strCurrentFunctionName);

        // analyze branches
        for (var case_ : ctx.case_()){
            //System.out.println("  " + case_.getText());
            var ci = new SwitchInfo.CaseInfo();
            ci.m_lngBranchValue = Misc.lngRobustStringToLong(case_.typeConst().constant().getText());       // get branch value
            ci.m_strLLVMLabel = case_.label().LocalIdent().getText().substring(1);                // remove % from label

            System.out.println("  " + ci.m_lngBranchValue + " --> " + ci.m_strLLVMLabel);

            si.m_caseInfo.add(ci);


            m_si.put((long)0, si);
        }



        exit(0);
    }
}
