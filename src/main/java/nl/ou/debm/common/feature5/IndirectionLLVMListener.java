package nl.ou.debm.common.feature5;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.LLVMIRBaseListener;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.ou.debm.common.feature5.IndirectionsProducer.ICASEINDEXFORDEFAULTBRANCH;

public class IndirectionLLVMListener extends LLVMIRBaseListener {

    /** storage class for info on indirection code markers in a LLVM block */
    private static class IndirectionCMinBasicBlockInfo{
        /** the code marker object */                                       public IndirectionsCodeMarker icm;
        /** the code marker is the first instruction in the block */        public boolean bFirstInstruction;
        public IndirectionCMinBasicBlockInfo(IndirectionsCodeMarker icm, boolean fi){
            this.icm=icm;
            this.bFirstInstruction=fi;
        }
        public String toString(){
            return Misc.cBooleanToChar(bFirstInstruction) + " -- " + icm.toString();
        }
    }

    /** name of the current function we're in */                                    private String m_strCurrentFunctionName;
    /** info on al switches, key=switchID */                                        private final Map<Long, SwitchInfo> m_si;
    /** map block ID to info on first indirection code marker in the block */       private final Map<String, IndirectionCMinBasicBlockInfo> m_idmPerBlock = new HashMap<>();
    /** map a code marker ID to code marker info in the LLVM */                     private final Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_basicLLVMInfo = new HashMap<>();
    /** map LLVM_ID to code marker ID */                                            private final Map<String, Long> m_LLVMIDtoCodeMarkerID = new HashMap<>();

    /**
     * constructor
     * @param info      the listener will return information in this map;
     *                  will be cleared before beginning
     * @param parser    the parser object for the LLVM file
     */
    public IndirectionLLVMListener(Map<Long, SwitchInfo> info, LLVMIRParser parser){
        m_si=info;
        m_si.clear();
        CodeMarker.getCodeMarkerInfoFromLLVM(parser, m_basicLLVMInfo, m_LLVMIDtoCodeMarkerID);
        parser.reset();
    }

    @Override
    public void enterFuncDef(LLVMIRParser.FuncDefContext ctx) {
        super.enterFuncDef(ctx);

        // probe all basic blocks and try to find indirection code markers
        m_idmPerBlock.clear();
        for (var basicBlock : ctx.funcBody().basicBlock()){     // block loop
            if (basicBlock.LabelIdent() != null){               // only when labeled (looking for a case)
                String strBlockID = Misc.strSafeLeftString(basicBlock.LabelIdent().getText(), -1);    // get block ID, remove : at the end
                boolean bFirstLine = true;
                for (var ins : basicBlock.instruction()){
                    if (ins.valueInstruction()!=null){
                        var call = ins.valueInstruction().callInst();
                        if (call != null){                      // we've found a call!
                            // is it a call to an indirections code marker?
                            var icm = extractIndirectionsCodeMarkerFromCall(call);
                            if (icm!=null){
                                m_idmPerBlock.put(strBlockID, new IndirectionCMinBasicBlockInfo(icm, bFirstLine));
                                break;
                            }
                        }
                        // no more first line...
                        bFirstLine=false;
                    }
                }
            }
        }
    }

    /**
     * try to extract an indirection code marker from a call-context
     * @param call  the LLVM call instruction context
     * @return indirection code marker, null if none was found
     */
    private IndirectionsCodeMarker extractIndirectionsCodeMarkerFromCall(LLVMIRParser.CallInstContext call){
        // get all globals
        List<String> globals = Misc.getGlobalsFromLLVMString(call.getText());

        for (var global: globals){
            // check if this global is a code marker
            Long LngCMID = m_LLVMIDtoCodeMarkerID.get(global);
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
    public void enterSwitchTerm(LLVMIRParser.SwitchTermContext ctx) {
        super.enterSwitchTerm(ctx);

        // claim switch info storage
        SwitchInfo si = new SwitchInfo();
        si.setLLVMFunctionName(m_strCurrentFunctionName);

        // analyze branches
        long lngSwitchID = -1;
        for (var case_ : ctx.case_()){
            // basic branch info: value and jump label
            var ci = new SwitchInfo.LLVMCaseInfo();
            ci.m_lngBranchValue = Misc.lngRobustStringToLong(case_.typeConst().constant().getText());       // get branch value
            ci.m_strLLVMLabel = case_.label().LocalIdent().getText().substring(1);                // remove start-% from label
            si.LLVMCaseInfo().add(ci);
            // determine switch ID from code marker in switch
            var icmi = m_idmPerBlock.get(ci.m_strLLVMLabel);
            if (icmi!=null) {
                // get switch ID
                long lngCSwitchID = icmi.icm.lngGetSwitchID();
                // process it
                if (lngSwitchID == -1) {
                    // first branch to contain a switch code marker -- beautiful!
                    lngSwitchID = lngCSwitchID;
                } else {
                    // code marker ID set before, so it must be the same
                    if (lngCSwitchID != lngSwitchID) {
                        throw new RuntimeException("Different switch ID's come up in the same switch! Previous ID found = " +
                                lngSwitchID + ", current ID found = " + lngCSwitchID);
                    }
                }
            }
        }

        // search before switch code marker
        for (var cm : m_basicLLVMInfo.entrySet()){
            if (cm.getValue().codeMarker instanceof IndirectionsCodeMarker icm){
                if (icm.lngGetSwitchID()==lngSwitchID){
                    si.setICM(icm);
                    break;
                }
            }
        }

        // by analysing the branches, we've determined the switchID. If it could not be determined,
        // we consider this switch not to be one of ours. This means we don't have to analyse further.
        if (lngSwitchID==-1){
            return;
        }

        // let's see if there is a default branch
        String strDefaultLabel = ctx.label().LocalIdent().getText().substring(1);                // remove start-% from label
        var icmi = m_idmPerBlock.get(strDefaultLabel);
        if (icmi.icm.getCodeMarkerLocation()==EIndirectionMarkerLocationTypes.CASEBEGIN){
            // this is a jump to a default branch, so add this branch to the list of branches
            var ci = new SwitchInfo.LLVMCaseInfo();
            ci.m_lngBranchValue = ICASEINDEXFORDEFAULTBRANCH;
            ci.m_strLLVMLabel = strDefaultLabel;
            si.LLVMCaseInfo().add(ci);
        }

        // group branches together, thus recognizing things like:
        // switch (){
        //   case 1:
        //   case 2:
        //   case 3:
        //      <code>
        //   cade 4:
        //      <code>
        // }
        // cases 1-3 are a group, implemented as one case only, with 3 jumps pointing at it
        for (var thisCase : si.LLVMCaseInfo()){
            for (var otherCase: si.LLVMCaseInfo()){
                if (thisCase!=otherCase){
                    if (otherCase.m_strLLVMLabel.equals(thisCase.m_strLLVMLabel)){
                        thisCase.m_otherBranchValues.add(otherCase.m_lngBranchValue);
                    }
                }
            }
        }

        // save switch info
        m_si.put(lngSwitchID, si);
    }
}
