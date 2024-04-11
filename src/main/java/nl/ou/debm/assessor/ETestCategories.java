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
        FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED,
        FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP,
        FEATURE1_LOOP_BEAUTY_SCORE_OVERALL,
        FEATURE1_LOOP_BEAUTY_SCORE_NORMAL,
        FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED,
        FEATURE1_TOTAL_NUMBER_OF_GOTOS,
        FEATURE1_NUMBER_OF_UNWANTED_GOTOS,

    FEATURE2_AGGREGATED,
        FEATURE2_FOUND_CODEMARKERS,
        FEATURE2_VARIABLES_IDENTIFIED,
        FEATURE2_LOCAL_BUILTIN,
        FEATURE2_LOCAL_STRUCT,
        FEATURE2_LOCAL_ARRAY,
        FEATURE2_LOCAL_PTR,
        FEATURE2_GLOBAL_BUILTIN,
        FEATURE2_GLOBAL_STRUCT,
        FEATURE2_GLOBAL_ARRAY,
        FEATURE2_GLOBAL_PTR,
        FEATURE2_HEAP_BUILTIN,
        FEATURE2_HEAP_STRUCT,
        FEATURE2_HEAP_ARRAY,
        FEATURE2_HEAP_PTR,

    FEATURE3_AGGREGATED,
        FEATURE3_FUNCTION_IDENTIFICATION,
        FEATURE3_FUNCTION_START,
        FEATURE3_FUNCTION_PROLOGUE_RATE,
        FEATURE3_FUNCTION_EPILOGUE_RATE,
        FEATURE3_FUNCTION_END,
        FEATURE3_RETURN,
        FEATURE3_UNREACHABLE_FUNCTION,
        FEATURE3_FUNCTION_CALLS,
        FEATURE3_VARIADIC_FUNCTION;

    public String strTestDescription(){
        switch (this){
            case FEATURE1_AGGREGATED -> {                      return "Loops, aggregated score";                            }
            case FEATURE2_AGGREGATED -> {                      return "Datastructures, aggregated score";                   }
            case FEATURE3_AGGREGATED -> {                      return "Function analysis, aggregated score";                }

            case FEATURE1_NUMBER_OF_LOOPS_GENERAL -> {         return "Number of loops found as loop - all loops";          }
            case FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED -> {    return "Number of loops found as loop - only normal loops";  }
            case FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP -> {return "Number of loops found as loop - only unrolled loops";}
            case FEATURE1_LOOP_BEAUTY_SCORE_OVERALL -> {       return "Loop quality score - all loops";                     }
            case FEATURE1_LOOP_BEAUTY_SCORE_NORMAL  -> {       return "Loop quality score - normal loops";                  }
            case FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED -> {      return "Loop quality score - unrolled loops";                }
            case FEATURE1_TOTAL_NUMBER_OF_GOTOS -> {           return "Total number of goto's found";                       }
            case FEATURE1_NUMBER_OF_UNWANTED_GOTOS -> {        return "Total number of unwanted goto's found";              }
            case FEATURE2_FOUND_CODEMARKERS -> {        return "Datastructure codemarkers found"; }
            case FEATURE2_VARIABLES_IDENTIFIED -> {     return "Datastructure variables identified"; }

            case FEATURE3_FUNCTION_IDENTIFICATION -> {  return "Found functions";     }
            case FEATURE3_FUNCTION_START -> {           return "Number of found function starts";     }
            case FEATURE3_FUNCTION_PROLOGUE_RATE -> {   return "Number of prologue statements";     }
            case FEATURE3_FUNCTION_EPILOGUE_RATE -> {   return "Number of epilogue statements";     }
            case FEATURE3_FUNCTION_END -> {             return "Number of function ends";     }
            case FEATURE3_RETURN -> {                   return "Number of found return statements";     }
            case FEATURE3_UNREACHABLE_FUNCTION -> {     return "Number of unreachable functions found";     }
            case FEATURE3_FUNCTION_CALLS -> {           return "Number of correctly identified calls";     }
            case FEATURE3_VARIADIC_FUNCTION -> {        return "Variadic functions";     }
        }
        return "";
    }

    public String strTestUnit(){
        switch (this){

            case FEATURE1_AGGREGATED -> {                       return "unit1";                             }
            case FEATURE2_AGGREGATED -> {                       return "unit2";                             }
            case FEATURE3_AGGREGATED -> {                       return "unit3";                             }
            case FEATURE1_NUMBER_OF_LOOPS_GENERAL, FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP, FEATURE1_TOTAL_NUMBER_OF_GOTOS,
                 FEATURE1_NUMBER_OF_UNWANTED_GOTOS, FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED -> {
                                                                return "#";                                 }
            case FEATURE1_LOOP_BEAUTY_SCORE_OVERALL, FEATURE1_LOOP_BEAUTY_SCORE_NORMAL, FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED ->
                                               {                return "school mark";        }
            case FEATURE3_FUNCTION_IDENTIFICATION -> {  return "recall";    }
            case FEATURE3_FUNCTION_START -> {           return "recall";    }
            case FEATURE3_FUNCTION_PROLOGUE_RATE -> {   return "%";    }
            case FEATURE3_FUNCTION_EPILOGUE_RATE -> {   return "%";    }
            case FEATURE3_FUNCTION_END -> {             return "recall";    }
            case FEATURE3_RETURN -> {                   return "recall";    }
            case FEATURE3_UNREACHABLE_FUNCTION -> {     return "recall";    }
            case FEATURE3_FUNCTION_CALLS -> {           return "F1-score";    }
            case FEATURE3_VARIADIC_FUNCTION -> {        return "F1-score";    }
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
