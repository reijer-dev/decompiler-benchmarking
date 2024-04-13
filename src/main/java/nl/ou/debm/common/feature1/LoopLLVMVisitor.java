package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.LLVMIRBaseVisitor;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LLVM visitor for loops
 * This visitor checks for unrolled loops
 */
public class LoopLLVMVisitor extends LLVMIRBaseVisitor {

    /**
     * struct to store data about LLVM-IR instructions
     */
    private static class LLVMInstruction{
        /** all the tokens of the instruction, tokenID + text */    public final List<Misc.ANTLRParsedElement> instructionData;
        /** stringBuilder containing interpreted info */            public final StringBuilder sb = new StringBuilder();
        /** if instruction is body code marker: ID of the loop*/    public final long lngBodyCodeMarkerID;

        public LLVMInstruction(List<Misc.ANTLRParsedElement> instructionData, long lngBodyCodeMarkerID) {
            this.instructionData = instructionData;
            this.lngBodyCodeMarkerID = lngBodyCodeMarkerID;
        }
    }

    LoopLLVMVisitor(Map<Long, CodeMarker.CodeMarkerLLVMInfo> CMMap, Map<String, Long> IDMap){
        m_CMMap=CMMap;
        m_IDMap=IDMap;
    }

    /** instructions in a block */                      private final List<LLVMInstruction> m_ins = new ArrayList<>();
    /** general code marker info, by codemarker ID*/    private final Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_CMMap;
    /** maps LLVM'IDs (@.str.299) to my ID's */         private final Map<String, Long> m_IDMap;

    /** result: which loops really are unrolled */      private final List<Long> m_lngAcceptedAsUnrolledLoops = new ArrayList<>();

    /**
     * Get list of unrolled loops
     * @return the list
     */
    public List<Long> getIDsOfUnrolledLoops() {
        return m_lngAcceptedAsUnrolledLoops;
    }

    @Override
    public Object visitFuncDef(LLVMIRParser.FuncDefContext ctx) {
        // work per function, so first reset instructin table
        m_ins.clear();

        // visit all instructions
        var visitor = new LoopLLVMInstructionVisitor();
        for (var child : ctx.children){
            visitor.visit(child);
        }

        // assess unrolled loops
        m_lngAcceptedAsUnrolledLoops.addAll(findUnrolledLoops());

        // no deeper visit, go to the next function
        return null;
    }

    private List<Long> findUnrolledLoops(){
        // find unrolled loops in data
        //
        // an unrolled loop has a pattern:
        // body code marker
        // ins 1
        // ...
        // ins n
        // body code marker
        // ins 1
        // ...
        // ins n
        //
        // etc.
        //
        // we search for a body code marker. we check if we've seen it before.
        //     if we had previously accepted it, we renounce it, because apparently, there
        //     are more instructions between the last found one and this new marker, so
        //     it's not neatly unrolled
        //     if we already rejected it, we do nothing
        //
        //  ---> we haven't seen it before
        //     we search for the next loop body code marker with the same ID.
        //         --> not found? no unrolled loop
        //         --> found? we try to find repeated bodies
        //       in repeated bodies we accept *local* variables to change, but all others (lits etc.) must
        //       remain the same. There's no problem in the printing of loop variable's values in the code
        //       marker, as those change in the loop body code marker and not in the instructions between the markers.

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
                        }
                        else {
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
                            }
                            else {
                                acceptedAsUnrolled.add(lngCurrentBodyID);
                            }
                            base = basecopy;
                        }
                    }
                }
            }
        }
        return acceptedAsUnrolled;
    }

    /**
     * Class to get instruction info
     */
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
                    Long CMID = m_IDMap.get(token.strText);
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
            // replace local vars with tabs; this makes sure the comparison between two instructions in two
            // bodies is not hampered by different local variables
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
