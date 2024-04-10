package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.LLVMIRBaseVisitor;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoopLLVMVisitor extends LLVMIRBaseVisitor {

    private static class LLVMInstruction{
        public final List<Misc.ANTLRParsedElement> instructionData;
        public final StringBuilder sb = new StringBuilder();
        public final long lngBodyCodeMarkerID;

        public LLVMInstruction(List<Misc.ANTLRParsedElement> instructionData, long lngBodyCodeMarkerID) {
            this.instructionData = instructionData;
            this.lngBodyCodeMarkerID = lngBodyCodeMarkerID;
        }
    }

    LoopLLVMVisitor(Map<Long, CodeMarker.CodeMarkerLLVMInfo> CMMap, Map<String, Long> IDMap){
        m_CMMap=CMMap;
        m_IDMap=IDMap;
    }

    private final List<LLVMInstruction> m_ins = new ArrayList<>();
    private final Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_CMMap;
    private final Map<String, Long> m_IDMap;

    private final List<Long> m_lngAcceptedAsUnrolledLoops = new ArrayList<>();

    public List<Long> getIDsOfUnrolledLoops(){
        return m_lngAcceptedAsUnrolledLoops;
    }

    @Override
    public Object visitFuncDef(LLVMIRParser.FuncDefContext ctx) {
        m_ins.clear();

//        System.out.println(">>>>" + ctx.getText());


        // do the visit
        var visitor = new LoopLLVMInstructionVisitor();
        for (var child : ctx.children){
            visitor.visit(child);
        }

        // assess unrolled loops
        m_lngAcceptedAsUnrolledLoops.addAll(findUnrolledLoops());


//        for (var w : m_ins){
//            System.out.println(w.lngBodyCodeMarkerID + "  " + w.sb);
//        }

//        System.out.println("--------------------------------------" );

        return null;
    }

    private List<Long> findUnrolledLoops(){
        final List<Long> rejectedAsUnrolled = new ArrayList<>();
        final List<Long> acceptedAsUnrolled = new ArrayList<>();

        for (int base = 0; base < m_ins.size(); ++base){
            var baseline = m_ins.get(base);
            long lngCurrentBodyID = baseline.lngBodyCodeMarkerID;
            if ((lngCurrentBodyID>-1)){
                if (!rejectedAsUnrolled.contains(lngCurrentBodyID)) {
                    if (acceptedAsUnrolled.contains(lngCurrentBodyID)){
                        // found a non-wanted extra body marker -- not unrolled after all
                        acceptedAsUnrolled.remove(lngCurrentBodyID);
                        rejectedAsUnrolled.add(lngCurrentBodyID);
                    }
                    else {
                        // new body code marker found, not yet in rejected list
                        int last;
                        for (last = base + 1; last < m_ins.size(); ++last) {
                            if (m_ins.get(last).lngBodyCodeMarkerID == lngCurrentBodyID) {
                                break;
                            }
                        }
                        // we now know the whole loops unrolled body, or now it is not there...
                        if (last >= m_ins.size()) {
                            // no next body code marker found, so the loop was not unrolled
                            rejectedAsUnrolled.add(lngCurrentBodyID);
                        } else {
                            // found a second loop body marker -- try to match as many bodies as possible
                            int basecopy = last;
                            int iNBodyLines = last - base - 1;
                            boolean bMisMatch = false;
                            while (m_ins.get(basecopy).lngBodyCodeMarkerID == lngCurrentBodyID) {
                                for (int offset = 1; offset <= iNBodyLines; offset++) {
                                    if (!(m_ins.get(base + offset).sb.compareTo(m_ins.get(basecopy + offset).sb) == 0)) {
                                        bMisMatch = true;
                                        break;
                                    }
                                }
                                if (bMisMatch) {
                                    break;
                                }
                                basecopy += (iNBodyLines + 1);
                            }
                            if (bMisMatch) {
                                rejectedAsUnrolled.add(lngCurrentBodyID);
                            } else {
                                acceptedAsUnrolled.add(lngCurrentBodyID);
                            }
                            base = basecopy;
                        }
                    }
                }
            }
        }

//        if ((acceptedAsUnrolled.size() + rejectedAsUnrolled.size())>0) {
//            System.out.println("Unrolled:  " + acceptedAsUnrolled);
//            System.out.println("Preserved: " + rejectedAsUnrolled);
//        }

        return acceptedAsUnrolled;
    }



    private class LoopLLVMInstructionVisitor extends LLVMIRBaseVisitor {

        @Override
        public Object visitInstruction(LLVMIRParser.InstructionContext ctx) {
            var tokens = Misc.getAllTerminalNodes(ctx);

            // search for code marker
            long lngLoopBodyID = -1;
            boolean bCallFound = false;
            for (var token : tokens){
                if (token.iTokenID == 180){     // token ID for a call inst (source: LLVMIR.tokens)
                    bCallFound = true;
                }
                else if (token.iTokenID==493){  // token ID for a global variable
                    Long CMID = null;
                    CMID = m_IDMap.get(token.strText);
                    if (CMID!=null){
                        // code marker string!
                        var cmi = m_CMMap.get(CMID);
                        if (cmi!=null) { // non-loop-codeMarkers are not in the map
                            if (cmi.codeMarker instanceof LoopCodeMarker lcm) { // keep the compiler happy with extra test
                                if (lcm.getLoopCodeMarkerLocation() == ELoopMarkerLocationTypes.BODY) {
                                    lngLoopBodyID = lcm.lngGetLoopID();
                                }
                            }
                        }
                    }
                }
            }
            if (!bCallFound){
                lngLoopBodyID = -1;
            }
            var ins = new LLVMInstruction(tokens, lngLoopBodyID);
            m_ins.add(ins);
            for (var token : tokens){
                if (token.iTokenID == 494) {
                    ins.sb.append("\t");
                } else {
                    ins.sb.append(token.strText);
                }
            }

            return null;
        }
    }
}
