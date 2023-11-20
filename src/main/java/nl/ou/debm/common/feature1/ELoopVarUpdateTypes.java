package nl.ou.debm.common.feature1;

public enum ELoopVarUpdateTypes {
    INCREASE_BY_ONE,                        // ++
    DECREASE_BY_ONE,                           // --
    INCREASE_OTHER,                         // += <?>
    DECREASE_OTHER,                         // -= <?>
    MULTIPLY,                               // *= <?>
    DIVIDE,                                 // /= <?>
    INCREASE_BY_INPUT,                      // +=getchar()
    DECREASE_BY_INPUT                       // -=getchar()
}
