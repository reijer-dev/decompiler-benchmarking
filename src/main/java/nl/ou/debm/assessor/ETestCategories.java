package nl.ou.debm.assessor;

/**
 * Enum containing descriptions of all test performed <br>
 * <br>
 * Use _AGGREGATED for final scores on your own feature<br>
 * Use child categories for details.<br>
 * <br>
 * Do not forget to add details in strTestDescription and strTestUnit, as well as an ID in lngUniversalIdentifier.<br>
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

    FEATURE3_AGGREGATED,
        FEATURE3_FUNCTION_IDENTIFICATION,
        FEATURE3_FUNCTION_START,
        FEATURE3_FUNCTION_PROLOGUE_RATE,
        FEATURE3_FUNCTION_EPILOGUE_RATE,
        FEATURE3_FUNCTION_END,
        FEATURE3_RETURN,
        FEATURE3_UNREACHABLE_FUNCTION,
        FEATURE3_FUNCTION_CALLS,
        FEATURE3_VARIADIC_FUNCTION,

    FEATURE4_AGGREGATED,
        FEATURE4_PARSER_ERRORS;

    public String strTestDescription(){
        switch (this){
            case FEATURE1_AGGREGATED -> {                      return "Loops, aggregated score";                            }
            case FEATURE2_AGGREGATED -> {                      return "Datastructures, aggregated score";                   }
            case FEATURE3_AGGREGATED -> {                      return "Function analysis, aggregated score";                }
            case FEATURE4_AGGREGATED -> {                      return "Syntax correctness, aggregated score";               }

            case FEATURE1_NUMBER_OF_LOOPS_GENERAL -> {         return "Number of loops found as loop - all loops";          }
            case FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED -> {    return "Number of loops found as loop - only normal loops";  }
            case FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP -> {return "Number of loops found as loop - only unrolled loops";}
            case FEATURE1_LOOP_BEAUTY_SCORE_OVERALL -> {       return "Loop quality score - all loops";                     }
            case FEATURE1_LOOP_BEAUTY_SCORE_NORMAL  -> {       return "Loop quality score - normal loops";                  }
            case FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED -> {      return "Loop quality score - unrolled loops";                }
            case FEATURE1_TOTAL_NUMBER_OF_GOTOS -> {           return "Total number of goto's found";                       }
            case FEATURE1_NUMBER_OF_UNWANTED_GOTOS -> {        return "Total number of unwanted goto's found";              }

            case FEATURE3_FUNCTION_IDENTIFICATION -> {  return "Found functions";     }
            case FEATURE3_FUNCTION_START -> {           return "Number of found function starts";     }
            case FEATURE3_FUNCTION_PROLOGUE_RATE -> {   return "Number of prologue statements";     }
            case FEATURE3_FUNCTION_EPILOGUE_RATE -> {   return "Number of epilogue statements";     }
            case FEATURE3_FUNCTION_END -> {             return "Number of function ends";     }
            case FEATURE3_RETURN -> {                   return "Number of found return statements";     }
            case FEATURE3_UNREACHABLE_FUNCTION -> {     return "Number of unreachable functions found";     }
            case FEATURE3_FUNCTION_CALLS -> {           return "Number of correctly identified calls";     }
            case FEATURE3_VARIADIC_FUNCTION -> {        return "Variadic functions";     }

            case FEATURE4_PARSER_ERRORS -> {            return "Parser errors"; }
        }
        return "";
    }

    public String strTestUnit(){
        switch (this){

            case FEATURE1_AGGREGATED -> {                       return "unit1";                             }
            case FEATURE2_AGGREGATED -> {                       return "unit2";                             }
            case FEATURE3_AGGREGATED -> {                       return "unit3";                             }
            case FEATURE4_AGGREGATED -> {                       return "unit4";                             }
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

            case FEATURE4_PARSER_ERRORS -> {            return "errors per total number lines"; }
        }
        return "";
    }

    /**
     * Is test result the aggregated feature score? (helps to comprise and lay out tables)
     * @return true if test result is a feature-aggregated one.
     */
    public boolean bIsAggregatedScore(){
        switch (this){
            case FEATURE1_AGGREGATED, FEATURE2_AGGREGATED, FEATURE3_AGGREGATED, FEATURE4_AGGREGATED -> { return true;}
        }
        return false;
    }

    /**
     * Get a unique ID for the metric.
     * @return ID
     */
    public long lngUniversalIdentifier(){
        // IMPORTANT!
        // The idea of the ID is that the metric can be analysed by other computer code easily, as it comes up
        // in the XML produced. DO NOT CHANGE METRIC ID"S PREVIOUSLY SET as it will hamper backward compatibility.
        // That is also why we don't just return the ordinal number of the element in the ENUM.

        long out = 0;
        switch (this) {

            case FEATURE1_AGGREGATED ->                         out = 100;
            case FEATURE1_NUMBER_OF_LOOPS_GENERAL ->            out = 101;
            case FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED ->       out = 102;
            case FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP ->   out = 103;
            case FEATURE1_LOOP_BEAUTY_SCORE_OVERALL ->          out = 104;
            case FEATURE1_LOOP_BEAUTY_SCORE_NORMAL ->           out = 105;
            case FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED ->         out = 106;
            case FEATURE1_TOTAL_NUMBER_OF_GOTOS ->              out = 107;
            case FEATURE1_NUMBER_OF_UNWANTED_GOTOS ->           out = 108;

            case FEATURE2_AGGREGATED ->                         out = 200;

            case FEATURE3_AGGREGATED ->                         out = 300;
            case FEATURE3_FUNCTION_IDENTIFICATION ->            out = 301;
            case FEATURE3_FUNCTION_START ->                     out = 302;
            case FEATURE3_FUNCTION_PROLOGUE_RATE ->             out = 303;
            case FEATURE3_FUNCTION_EPILOGUE_RATE ->             out = 304;
            case FEATURE3_FUNCTION_END ->                       out = 305;
            case FEATURE3_RETURN ->                             out = 306;
            case FEATURE3_UNREACHABLE_FUNCTION ->               out = 307;
            case FEATURE3_FUNCTION_CALLS ->                     out = 308;
            case FEATURE3_VARIADIC_FUNCTION ->                  out = 309;

            case FEATURE4_AGGREGATED ->                         out = 400;
            case FEATURE4_PARSER_ERRORS ->                      out = 401;
        }
        return out;
    }
}
