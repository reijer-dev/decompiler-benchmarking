package nl.ou.debm.common.feature5;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.LLVMIRBaseListener;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;

public class IndirectionLLVMListener extends LLVMIRBaseListener {

    private static class IndirectionCMinBasicBlockInfo{
        public IndirectionsCodeMarker icm;
        public boolean bFirstInstruction;
        public IndirectionCMinBasicBlockInfo(IndirectionsCodeMarker icm, boolean fi){
            this.icm=icm;
            this.bFirstInstruction=fi;
        }
        public String toString(){
            return Misc.cBooleanToChar(bFirstInstruction) + " -- " + icm.toString();
        }
    }

    /** name of the current function we're in */private String m_strCurrentFunctionName;
    /** info on al switches, key=switchID */ private final Map<Long, SwitchInfo> m_si;
    /** map block ID to info on first indirection code marker in the block */ private final Map<Long, IndirectionCMinBasicBlockInfo> m_idmPerBlock = new HashMap<>();

    /** map code marker ID to code marker info */ private final Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_basicLLVMInfo = new HashMap<>();
    /** map LLVM_ID to code marker ID */ private final Map<String, Long> m_LLVMIDtoCodeMarkerID = new HashMap<>();

    public IndirectionLLVMListener(Map<Long, SwitchInfo> info, LLVMIRParser parser){
        m_si=info;
        CodeMarker.getCodeMarkerInfoFromLLVM(parser, m_basicLLVMInfo, m_LLVMIDtoCodeMarkerID);
    }

    @Override
    public void enterFuncDef(LLVMIRParser.FuncDefContext ctx) {
        super.enterFuncDef(ctx);

        // probe all basic blocks and try to find indirection code markers
        m_idmPerBlock.clear();
        for (var basicBlock : ctx.funcBody().basicBlock()){     // block loop
            if (basicBlock.LabelIdent() != null){               // only when labeled (looking for a case)
                long lngBlockID = Misc.lngRobustStringToLong(Misc.strSafeLeftString(basicBlock.LabelIdent().getText(), -1));    // get block ID, remove : at the end
                boolean bFirstLine = true;
                for (var ins : basicBlock.instruction()){
                    if (ins.valueInstruction()!=null){
                        var call = ins.valueInstruction().callInst();
                        if (call != null){                      // we've found a call!
                            // is it a call to an indirections code marker?
                            var icm = extractIndirectionsCodeMarkerFromCall(call);
                            if (icm!=null){
                                m_idmPerBlock.put(lngBlockID, new IndirectionCMinBasicBlockInfo(icm, bFirstLine));
                            }
                        }
                        // no more first line...
                        bFirstLine=false;
                    }
                }
            }
        }

        System.out.println(ctx.funcHeader().getText());
        for (var q: m_idmPerBlock.entrySet()){
            System.out.println(q);
        }
    }

    private IndirectionsCodeMarker extractIndirectionsCodeMarkerFromCall(LLVMIRParser.CallInstContext call){
        // get all globals
        List<String> glob = Misc.getGlobalsFromLLVMString(call.getText());

        for (var s: glob){
            // check if this global is a code marker
            Long LngCMID = m_LLVMIDtoCodeMarkerID.get(s);
            if (LngCMID != null){
                // valid code marker, check type
                var cm = m_basicLLVMInfo.get(LngCMID);
                if (cm.codeMarker instanceof IndirectionsCodeMarker icm){
                    // correct type, so return it
                    return icm;
                }
            }
        }
        // no icm found
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
