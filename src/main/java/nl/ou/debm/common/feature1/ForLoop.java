package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;

public class ForLoop extends Loop{



    @Override
    public CodeMarker toCodeMarker() {

        /*
        This implementation is just a stub to show the idea
         */

        var out = new CodeMarker();

        out.setProperty("Looptype", "For");
        out.setProperty("LoopInit", m_strInitExpression);
        out.setProperty("LoopCont", m_strContinueExpression);
        out.setProperty("LoopUpdate", m_strUpdateExpression);

        return out;
    }

    @Override
    public void fromCodeMarker(CodeMarker cm) {

    }


    public ForLoop(String strInitExpression, String strContinueExpression, String strUpdateExpression){
        SetInitExpression(strInitExpression);
        SetContinueExpression(strContinueExpression);
        SetUpdateExpression(strUpdateExpression);
    }

    public String strGetForStatement(){
        return "for (" +
                m_strInitExpression + ";" +
                m_strContinueExpression + ";" +
                m_strUpdateExpression +
                ")";
    }

    public String strGetForStatement(boolean bIncludeOpeningBrace){
        if (bIncludeOpeningBrace){
            return strGetForStatement() + "{";
        }
        return strGetForStatement();
    }
}
