package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.LLVMIRBaseListener;
import nl.ou.debm.common.antlr.LLVMIRParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoopLLVMListener extends LLVMIRBaseListener {

    private class LLVMElement {
        public String strText;

        LLVMElement(String s){
            strText=s;
        }
    }

    private final Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_defMap;

    private final List<LLVMElement> m_element = new ArrayList<>();

    public LoopLLVMListener(Map<Long, CodeMarker.CodeMarkerLLVMInfo> defaultMap){
        m_defMap = defaultMap;
    }

    @Override
    public void enterInstruction(LLVMIRParser.InstructionContext ctx) {
        super.enterInstruction(ctx);
        System.out.println("---" + ctx.children.size() + " : " + ctx.getText());
        m_element.clear();
        DepthFirst(ctx.getChild(0), " ");
        System.out.println();
        ParserRuleContext prc = ctx;

//
//        for (ParseTree o : ctx.children) {
//            if (o instanceof TerminalNode) {
//                TerminalNode tnode = (TerminalNode) o;
//                Token symbol = tnode.getSymbol();
//                System.out.println("---" + symbol.getText());
//                if (symbol.getType() == ttype) {
//                    if (tokens == null) {
//                        tokens = new ArrayList();
//                    }
//
//                    tokens.add(tnode);
//                }
//            }
        }

    void DepthFirst(ParseTree tree, String strInd){
        for (int ch = 0; ch <tree.getChildCount(); ch++){
            ParseTree pt = tree.getChild(ch);
            if (pt instanceof TerminalNode x){
                System.out.println("---- "  + Misc.strGetNumberWithPrefixZeros(x.getSymbol().getType(),4) + "  " + strInd + x.getSymbol().getText() );

//                System.out.print(((TerminalNode) pt).getSymbol().getText());
                m_element.add(new LLVMElement(((TerminalNode) pt).getSymbol().getText()));
            }
            else {
                DepthFirst(pt, strInd + "  ");
            }
        }
    }

}
