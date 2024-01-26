package nl.ou.debm.assessor;

public enum ETestCategories {
    FEATURE1_AGGREGATED,
        FEATURE1_NUMBER_OF_LOOPS_GENERAL,
        FEATURE1_NUMBER_OF_UNROLLED_LOOPS,

    FEATURE2_AGGREGATED,

    FEATURE3_AGGREGATED;

    public String strTestDescription(){
        switch (this){
            case FEATURE1_AGGREGATED -> {               return "Loops, aggregated score";            }
            case FEATURE2_AGGREGATED -> {               return "Datastructures, aggregated score";   }
            case FEATURE3_AGGREGATED -> {               return "Function analysis, aggregated score";}
            case FEATURE1_NUMBER_OF_LOOPS_GENERAL -> {  return "Number of loops found";              }
            case FEATURE1_NUMBER_OF_UNROLLED_LOOPS -> { return "Number of unrolled loops found";     }
        }
        return "";
    }

    public String strTestUnit(){
        switch (this){

            case FEATURE1_AGGREGATED -> {               return "unit1";                             }
            case FEATURE2_AGGREGATED -> {               return "unit2";                             }
            case FEATURE3_AGGREGATED -> {               return "unit3";                             }
            case FEATURE1_NUMBER_OF_LOOPS_GENERAL -> {  return "loop";                              }
            case FEATURE1_NUMBER_OF_UNROLLED_LOOPS -> { return "unrolled loop";                     }
        }
        return "";
    }

    public boolean bIsAggregatedScore(){
        switch (this){
            case FEATURE1_AGGREGATED, FEATURE2_AGGREGATED, FEATURE3_AGGREGATED -> { return true;}
        }
        return false;
    }

    public int iFeatureNumber(){
        switch (this) {
            case FEATURE1_AGGREGATED,
                 FEATURE1_NUMBER_OF_LOOPS_GENERAL,
                 FEATURE1_NUMBER_OF_UNROLLED_LOOPS -> {
                return 1;
            }
            case FEATURE2_AGGREGATED -> {
                return 2;
            }
            case FEATURE3_AGGREGATED -> {
                return 3;
            }
        }
        return 0;
    }
}
