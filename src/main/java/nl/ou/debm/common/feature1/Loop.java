package nl.ou.debm.common.feature1;

import nl.ou.debm.common.ICodeMarker;

public abstract class Loop implements ICodeMarker {
    public String strGetInitExpression() {
        return m_strInitExpression;
    }

    public String strGetContinueExpression() {
        return m_strContinueExpression;
    }

    public String strGetUpdateExpression() {
        return m_strUpdateExpression;
    }

    public void SetInitExpression(String strInitExpression) {
        this.m_strInitExpression = strInitExpression;
        if (this.m_strInitExpression == null) { this.m_strInitExpression = "";}
    }

    public void SetContinueExpression(String strContinueExpression) {
        this.m_strContinueExpression = strContinueExpression;
        if (this.m_strContinueExpression == null) { this.m_strContinueExpression = "";}
    }

    public void SetUpdateExpression(String strUpdateExpression) {
        this.m_strUpdateExpression = strUpdateExpression;
        if (this.m_strUpdateExpression == null) { this.m_strUpdateExpression = "";}
    }

    protected String m_strUpdateExpression;
    protected String m_strInitExpression;
    protected String m_strContinueExpression;


}
