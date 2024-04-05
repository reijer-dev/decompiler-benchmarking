package nl.ou.debm.common.feature1;

import nl.ou.debm.common.Misc;

public class LoopTestInfo {
    private String m_strLeftArg = "";
    private String m_strRightArg = "";
    private String m_strOperator = "";

    public boolean bContainsGetChar() {
        return m_strLeftArg.equals("getchar()") || m_strRightArg.equals("getchar()");
    }

    public LoopTestInfo(String strLeftArgument, String strComparisonOperator, String strRightArgument) {
        setValues(strLeftArgument, strComparisonOperator, strRightArgument);
    }
    public void setValues(String strLeftArgument, String strComparisonOperator, String strRightArgument) {
        m_strLeftArg = strLeftArgument;
        m_strOperator = strComparisonOperator;
        m_strRightArg = strRightArgument;
    }

    @Override
    public String toString() {
        return m_strLeftArg + "   " + m_strOperator + "   " + m_strRightArg +
                (bContainsGetChar() ? "  --- XXXX" : "");
    }

    /**
     * Try to match a found comparison to what we like to find
     * @param rhs  String, must start with comparator and end with numeral value, e.g.: ">10", "!=-10",
     *                           may contain nothing else
     * @return true if expressions are equal
     */
    public boolean equalsTestExpression(String rhs){
        // process rhs
        // operator
        ELoopVarTestTypes rhsOperator = ELoopVarTestTypes.stringStartToType(rhs);
        assert rhsOperator != ELoopVarTestTypes.UNUSED;
        // value
        var rhsValue = new Misc.ConvertCNumeral(rhs.substring(rhsOperator.strCOperator().length()));
        assert rhsValue.bIsNumeral();

        // check lhs for comparator format
        var lhsOperator = ELoopVarTestTypes.stringStartToType(m_strOperator);
        if (lhsOperator==ELoopVarTestTypes.UNUSED){
            return false;
        }

        // check if only one side of the lhs operator is a numeral
        int iNNumerals = 0;
        Misc.ConvertCNumeral lhsValue = null;
        var n1 = new Misc.ConvertCNumeral(m_strLeftArg);
        var n2 = new Misc.ConvertCNumeral(m_strRightArg);
        if (n1.bIsNumeral()) {iNNumerals++; lhsValue = n1;}
        if (n2.bIsNumeral()) {iNNumerals++; lhsValue = n2;}
        if (iNNumerals!=1){
            return false;
        }

        // when necessary, change lhs operator direction:
        // a>b  <--> b<a
        if (lhsValue == n1){        // this is truly intended as an object pointer comparison
            lhsOperator = lhsOperator.mirrorOperator();
        }

        // compare values and operator
        if (lhsOperator == rhsOperator){
            // equal operators, compare values
            return lhsValue.equalsIgnoringValueTypes(rhsValue);
        }

        // try special cases, but only if integers are compared
        if ((lhsValue.bIsFloat()) || (rhsValue.bIsFloat())){
            return false;
        }

        // x > y  <---> x >= (y+1)
        if ((lhsOperator==ELoopVarTestTypes.GREATER_THAN) && (rhsOperator==ELoopVarTestTypes.GREATER_OR_EQUAL)){
            return lhsValue.increaseByOne().equalsIgnoringValueTypes(rhsValue);
        }

        // x < y  <---> x <= (y-1)
        if ((lhsOperator==ELoopVarTestTypes.SMALLER_THAN) && (rhsOperator==ELoopVarTestTypes.SMALLER_OR_EQUAL)){
            return lhsValue.decreaseByOne().equalsIgnoringValueTypes(rhsValue);
        }

        // x >= y  <---> x > (y-1)
        if ((lhsOperator==ELoopVarTestTypes.GREATER_OR_EQUAL) && (rhsOperator==ELoopVarTestTypes.GREATER_THAN)){
            return lhsValue.decreaseByOne().equalsIgnoringValueTypes(rhsValue);
        }

        // x <= y  <---> x < (y+1)
        if ((lhsOperator==ELoopVarTestTypes.SMALLER_OR_EQUAL) && (rhsOperator==ELoopVarTestTypes.SMALLER_THAN)){
            return lhsValue.increaseByOne().equalsIgnoringValueTypes(rhsValue);
        }

        // no special case, no match, done
        return false;
    }
}
