package nl.ou.debm.assessor;

/**
 * Enum containing descriptions of all test performed <br>
 * <br>
 * Use _AGGREGATED for final scores on your own feature<br>
 * Use child categories for details.<br>
 * <br>
 * Do not forget to add details in strTestDescription and strTestUnit.<br>
 * <br>
 * As sorting of lists of test results is done using the enumeration as primary key,
 * take care where you put your new tests!
 *
 */
public enum ETestCategories {
    FEATURE1_AGGREGATED,
        FEATURE1_NUMBER_OF_LOOPS_GENERAL,
        FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP,
        FEATURE1_LOOP_BEAUTY_SCORE_OVERALL,
        FEATURE1_LOOP_BEAUTY_SCORE_NORMAL,
        FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED,
        FEATURE1_TOTAL_NUMBER_OF_GOTOS,
        FEATURE1_NUMBER_OF_UNWANTED_GOTOS,

    FEATURE2_AGGREGATED,

    FEATURE3_AGGREGATED,
        FEATURE3_FUNCTION_IDENTIFICATION,
        FEATURE3_FUNCTION_START,
        FEATURE3_FUNCTION_PROLOGUE_RATE,
        FEATURE3_FUNCTION_EPILOGUE_RATE,
        FEATURE3_FUNCTION_END,
        FEATURE3_RETURN,
        FEATURE3_PERFECT_BOUNDARIES,
        FEATURE3_UNREACHABLE_FUNCTION,
        FEATURE3_TOTAL_FUNCTION_CALLS,
        FEATURE3_FUNCTION_CALLS,
        FEATURE3_VARIADIC_FUNCTION;

    public String strTestDescription(){
        switch (this){
            case FEATURE1_AGGREGATED -> {                      return "Loops, aggregated score";                            }
            case FEATURE2_AGGREGATED -> {                      return "Datastructures, aggregated score";                   }
            case FEATURE3_AGGREGATED -> {                      return "Function analysis, aggregated score";                }

            case FEATURE1_NUMBER_OF_LOOPS_GENERAL -> {         return "Number of loops found";                              }
            case FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP -> {return "Number of unrolled loops identified as loop";        }
            case FEATURE1_LOOP_BEAUTY_SCORE_OVERALL -> {       return "Loop quality score - all loops";                     }
            case FEATURE1_LOOP_BEAUTY_SCORE_NORMAL  -> {       return "Loop quality score - normal loops";                  }
            case FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED -> {      return "Loop quality score - unrolled loops";                }
            case FEATURE1_TOTAL_NUMBER_OF_GOTOS -> {           return "Total number of goto's found";                       }
            case FEATURE1_NUMBER_OF_UNWANTED_GOTOS -> {        return "Total number of unwanted goto's found";              }

            case FEATURE3_FUNCTION_IDENTIFICATION -> {  return "Number of found functions";     }
            case FEATURE3_FUNCTION_START -> {           return "Number of found function starts";     }
            case FEATURE3_FUNCTION_PROLOGUE_RATE -> {   return "Number of prologue statements";     }
            case FEATURE3_FUNCTION_EPILOGUE_RATE -> {   return "Number of epilogue statements";     }
            case FEATURE3_FUNCTION_END -> {             return "Number of function ends";     }
            case FEATURE3_RETURN -> {                   return "Number of found return statements";     }
            case FEATURE3_PERFECT_BOUNDARIES -> {       return "Number of functions with perfectly found boundaries";     }
            case FEATURE3_UNREACHABLE_FUNCTION -> {     return "Number of unreachable functions found";     }
            case FEATURE3_TOTAL_FUNCTION_CALLS -> {     return "Number of correctly identified total calls";     }
            case FEATURE3_VARIADIC_FUNCTION -> {        return "F1-score for variadic functions";     }
        }
        return "";
    }

    public String strTestUnit(){
        switch (this){

            case FEATURE1_AGGREGATED -> {                       return "unit1";                             }
            case FEATURE2_AGGREGATED -> {                       return "unit2";                             }
            case FEATURE3_AGGREGATED -> {                       return "unit3";                             }
            case FEATURE1_NUMBER_OF_LOOPS_GENERAL, FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP, FEATURE1_TOTAL_NUMBER_OF_GOTOS,
                 FEATURE1_NUMBER_OF_UNWANTED_GOTOS -> {         return "#";                                 }
            case FEATURE1_LOOP_BEAUTY_SCORE_OVERALL, FEATURE1_LOOP_BEAUTY_SCORE_NORMAL, FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED ->
                                               {                return "school mark";        }
            case FEATURE3_FUNCTION_IDENTIFICATION -> {  return "functions";    }
            case FEATURE3_FUNCTION_START -> {           return "starts";    }
            case FEATURE3_FUNCTION_PROLOGUE_RATE -> {   return "%";    }
            case FEATURE3_FUNCTION_EPILOGUE_RATE -> {   return "%";    }
            case FEATURE3_FUNCTION_END -> {             return "ends";    }
            case FEATURE3_RETURN -> {                   return "returns";    }
            case FEATURE3_PERFECT_BOUNDARIES -> {       return "boundaries";    }
            case FEATURE3_UNREACHABLE_FUNCTION -> {     return "functions";    }
            case FEATURE3_TOTAL_FUNCTION_CALLS -> {     return "calls";    }
            case FEATURE3_VARIADIC_FUNCTION -> {        return "functions";    }
        }
        return "";
    }

    /**
     * Is test result the aggregated feature score? (helps to comprise and lay out tables)
     * @return true if test result is a feature-aggregated one.
     */
    public boolean bIsAggregatedScore(){
        switch (this){
            case FEATURE1_AGGREGATED, FEATURE2_AGGREGATED, FEATURE3_AGGREGATED -> { return true;}
        }
        return false;
    }
}
