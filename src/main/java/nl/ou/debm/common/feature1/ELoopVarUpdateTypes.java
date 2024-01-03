package nl.ou.debm.common.feature1;

import static nl.ou.debm.common.Misc.rnd;
import static nl.ou.debm.common.Misc.strFloatTrailer;
import static nl.ou.debm.common.feature1.LoopProducer.*;

public enum ELoopVarUpdateTypes {
    INCREASE_BY_ONE,                        // ++
    DECREASE_BY_ONE,                        // --
    INCREASE_OTHER,                         // += <?>
    DECREASE_OTHER,                         // -= <?>
    MULTIPLY,                               // *= <?>
    DIVIDE,                                 // /= <?>
    INCREASE_BY_INPUT,                      // +=getchar()
    DECREASE_BY_INPUT;                      // -=getchar()

    public final static String STRPROPERTYNAME = "update";

    public String strShortCode(){
        String out = "xx";
        switch (this){
            case INCREASE_BY_ONE ->    out = "++";
            case DECREASE_BY_ONE ->    out = "--";
            case INCREASE_OTHER ->     out = "+=";
            case DECREASE_OTHER ->     out = "-=";
            case MULTIPLY ->           out = "*=";
            case DIVIDE ->             out = "/=";
            case INCREASE_BY_INPUT ->  out = "+?";
            case DECREASE_BY_INPUT ->  out = "-?";
        }
        return out;
    }

    public String strPropertyValue(){
        return strShortCode();
    }

    public boolean bIsIncreasing(){
        return switch (this) {
            case INCREASE_BY_ONE, INCREASE_OTHER, MULTIPLY, INCREASE_BY_INPUT -> true;
            default -> false;
        };
    }
    public boolean bIsDecreasing(){
        return switch (this) {
            case DECREASE_BY_ONE, DECREASE_OTHER, DIVIDE, DECREASE_BY_INPUT -> true;
            default -> false;
        };
    }

    public String strGetUpdateExpression(boolean bIsFloat){
        String out = "";
        switch (this){
            case INCREASE_BY_ONE ->   out = "++";
            case DECREASE_BY_ONE ->   out = "--";
            case INCREASE_OTHER ->    out = "+=" + rnd.nextInt(ILOOPUPDATEIFNOTONELOWBOUND, ILOOPUPDATEIFNOTONEHIGHBOUND) + strFloatTrailer(bIsFloat);
            case DECREASE_OTHER ->    out = "-=" + rnd.nextInt(ILOOPUPDATEIFNOTONELOWBOUND, ILOOPUPDATEIFNOTONEHIGHBOUND) + strFloatTrailer(bIsFloat);
            case MULTIPLY ->          out = "*=" + rnd.nextInt(IMULTIPLYLOWBOUND, IMULTIPLYHIGHBOUND) + strFloatTrailer(bIsFloat);
            case DIVIDE ->            out = "/=" + rnd.nextInt(IDIVIDELOWBOUND, IDIVIDEHIGHBOUND) + strFloatTrailer(bIsFloat);
            case INCREASE_BY_INPUT -> out = "+=getchar()";
            case DECREASE_BY_INPUT -> out = "-=getchar()";
        }
        return out;
    }

    public static ELoopVarUpdateTypes intToType(int i){
        return switch (i) {
            case 1 ->       DECREASE_BY_ONE;
            case 2 ->       INCREASE_OTHER;
            case 3 ->       DECREASE_OTHER;
            case 4 ->       MULTIPLY;
            case 5 ->       DIVIDE;
            case 6 ->       INCREASE_BY_INPUT;
            case 7 ->       DECREASE_BY_INPUT;
            default  ->     INCREASE_BY_ONE;
        };
    }
}
