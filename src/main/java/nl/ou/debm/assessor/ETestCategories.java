package nl.ou.debm.assessor;

/**
 * Enum containing descriptions of all test performed <br>
 * <br>
 * Use _AGGREGATED for final scores on your own feature<br>
 * Use child categories for details.<br>
 * <br>
 * Do not forget to add details in strTestDescription and strTestUnit, as well as an ID in lngUniversalIdentifier.<br>
 * <br>
 * As sorting of lists of test results is done using the enumeration as the primary key,
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
        FEATURE1_NUMBER_OF_GOTOS,
        FEATURE1_NUMBER_OF_UNWANTED_GOTOS,

    FEATURE2_AGGREGATED,
        FEATURE2_ASSESSABLE, //a testcases is assessable if its codemarker is well-formed
        FEATURE2_GLOBALS_FOUND,
        FEATURE2_LOCALS_FOUND,
        FEATURE2_INTEGERS_FOUND,
        FEATURE2_INTEGERS_SCORE,
        FEATURE2_FLOATS_FOUND,
        FEATURE2_FLOATS_SCORE,
        FEATURE2_POINTERS_FOUND,
        FEATURE2_POINTERS_SCORE,
        FEATURE2_ARRAYS_FOUND,
        FEATURE2_ARRAYS_SCORE,
        FEATURE2_STRUCTS_FOUND,
        FEATURE2_STRUCTS_SCORE,


    FEATURE3_AGGREGATED,
        FEATURE3_FUNCTION_IDENTIFICATION,
        FEATURE3_FUNCTION_START,
        FEATURE3_FUNCTION_PROLOGUE_RATE,
/*        FEATURE3_FUNCTION_EPILOGUE_RATE,
        FEATURE3_FUNCTION_END,*/
        FEATURE3_RETURN,
        FEATURE3_UNREACHABLE_FUNCTION,
        FEATURE3_FUNCTION_CALLS,
        FEATURE3_VARIADIC_FUNCTION,

    FEATURE4_AGGREGATED,
        FEATURE4_DECOMPILED_FILES_PRODUCED,
        FEATURE4_ANTLR_CRASHES,
        FEATURE4_PARSER_ERRORS;

    public String strTestDescription(){
        switch (this){
            case FEATURE1_AGGREGATED -> {                      return "Loops, aggregated score";                            }
            case FEATURE2_AGGREGATED -> {                      return "Datastructures, aggregated score";                   }
            case FEATURE3_AGGREGATED -> {                      return "Function analysis, aggregated score";                }
            case FEATURE4_AGGREGATED -> {                      return "Syntax correctness, aggregated score";               }

            case FEATURE1_NUMBER_OF_LOOPS_GENERAL -> {         return "Number of loops found as loop - all loops";          }
            case FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED -> {    return "Number of loops found as loop - only preserved loops";}
            case FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP -> {return "Number of loops found as loop - only unrolled loops";}
            case FEATURE1_LOOP_BEAUTY_SCORE_OVERALL -> {       return "Loop quality score - all loops";                     }
            case FEATURE1_LOOP_BEAUTY_SCORE_NORMAL  -> {       return "Loop quality score - preserved loops";               }
            case FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED -> {      return "Loop quality score - unrolled loops";                }
            case FEATURE1_NUMBER_OF_GOTOS ->          {        return "Total number of goto's found";                       }
            case FEATURE1_NUMBER_OF_UNWANTED_GOTOS -> {        return "Total number of inexcusable goto's found";           }

            case FEATURE2_ASSESSABLE -> {               return "Recovered testcases"; }
            case FEATURE2_GLOBALS_FOUND -> {            return "Globals detected"; }
            case FEATURE2_LOCALS_FOUND -> {             return "Locals detected"; }
            case FEATURE2_INTEGERS_FOUND -> {           return "Integers detected"; }
            case FEATURE2_INTEGERS_SCORE -> {           return "Integers type accuracy"; }
            case FEATURE2_FLOATS_FOUND -> {             return "Floating point numbers detected"; }
            case FEATURE2_FLOATS_SCORE -> {             return "Floating point numbers type accuracy"; }
            case FEATURE2_POINTERS_FOUND -> {           return "Pointers detected"; }
            case FEATURE2_POINTERS_SCORE -> {           return "Pointers type accuracy"; }
            case FEATURE2_ARRAYS_FOUND -> {             return "Arrays detected"; }
            case FEATURE2_ARRAYS_SCORE -> {             return "Arrays type accuracy"; }
            case FEATURE2_STRUCTS_FOUND -> {            return "Structs detected"; }
            case FEATURE2_STRUCTS_SCORE -> {            return "Structs type accuracy"; }

            case FEATURE3_FUNCTION_IDENTIFICATION -> {  return "Reachable functions";     }
            case FEATURE3_FUNCTION_START -> {           return "Function start addresses";     }
            case FEATURE3_FUNCTION_PROLOGUE_RATE -> {   return "Unwanted prologue statements";     }
/*            case FEATURE3_FUNCTION_EPILOGUE_RATE -> {   return "Unwanted epilogue statements";     }
            case FEATURE3_FUNCTION_END -> {             return "Function end addresses";     }*/
            case FEATURE3_RETURN -> {                   return "Return statements";     }
            case FEATURE3_UNREACHABLE_FUNCTION -> {     return "Unreachable functions";     }
            case FEATURE3_FUNCTION_CALLS -> {           return "Function calls";     }
            case FEATURE3_VARIADIC_FUNCTION -> {        return "Variadic functions";     }

            case FEATURE4_PARSER_ERRORS -> {            return "Parser errors"; }
            case FEATURE4_DECOMPILED_FILES_PRODUCED -> {return "Number of decompiled files produced"; }
            case FEATURE4_ANTLR_CRASHES ->             {return "Number of parser crashes"; }
        }
        return "";
    }

    public String strTestUnit(){
        switch (this){

            case FEATURE1_AGGREGATED -> {                       return "unit1";                             }
            case FEATURE2_AGGREGATED -> {                       return "unit2";                             }
            case FEATURE3_AGGREGATED -> {                       return "unit3";                             }
            case FEATURE4_AGGREGATED -> {                       return "unit4";                             }
            case FEATURE1_NUMBER_OF_LOOPS_GENERAL, FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP,
                 FEATURE1_NUMBER_OF_UNWANTED_GOTOS, FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED,
                    FEATURE4_DECOMPILED_FILES_PRODUCED, FEATURE4_ANTLR_CRASHES -> {
                                                                return "#";                                 }
            case FEATURE1_LOOP_BEAUTY_SCORE_OVERALL, FEATURE1_LOOP_BEAUTY_SCORE_NORMAL, FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED ->
                                               {                return "school mark";        }
            case FEATURE3_FUNCTION_IDENTIFICATION -> {  return "recall";    }
            case FEATURE3_FUNCTION_START -> {           return "recall";    }
            case FEATURE3_FUNCTION_PROLOGUE_RATE -> {   return "%";    }
/*            case FEATURE3_FUNCTION_EPILOGUE_RATE -> {   return "%";    }
            case FEATURE3_FUNCTION_END -> {             return "recall";    }*/
            case FEATURE3_RETURN -> {                   return "recall";    }
            case FEATURE3_UNREACHABLE_FUNCTION -> {     return "recall";    }
            case FEATURE3_FUNCTION_CALLS -> {           return "F1-score";    }
            case FEATURE3_VARIADIC_FUNCTION -> {        return "F1-score";    }

            case FEATURE4_PARSER_ERRORS -> {            return "errors per total number of lines"; }
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
        // The idea of the ID is that the metric can be analyzed by other computer code easily, as it comes up
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
            case FEATURE1_NUMBER_OF_UNWANTED_GOTOS ->           out = 107;

            case FEATURE2_AGGREGATED ->                         out = 200;
            case FEATURE2_ASSESSABLE ->                         out = 201;
            case FEATURE2_GLOBALS_FOUND ->                      out = 202;
            case FEATURE2_LOCALS_FOUND ->                       out = 203;
            case FEATURE2_INTEGERS_FOUND ->                     out = 204;
            case FEATURE2_INTEGERS_SCORE ->                     out = 205;
            case FEATURE2_FLOATS_FOUND ->                       out = 206;
            case FEATURE2_FLOATS_SCORE ->                       out = 207;
            case FEATURE2_POINTERS_FOUND ->                     out = 208;
            case FEATURE2_POINTERS_SCORE ->                     out = 209;
            case FEATURE2_ARRAYS_FOUND ->                       out = 210;
            case FEATURE2_ARRAYS_SCORE ->                       out = 211;
            case FEATURE2_STRUCTS_FOUND ->                      out = 212;
            case FEATURE2_STRUCTS_SCORE ->                      out = 213;

            case FEATURE3_AGGREGATED ->                         out = 300;
            case FEATURE3_FUNCTION_IDENTIFICATION ->            out = 301;
            case FEATURE3_FUNCTION_START ->                     out = 302;
            case FEATURE3_FUNCTION_PROLOGUE_RATE ->             out = 303;
/*            case FEATURE3_FUNCTION_EPILOGUE_RATE ->             out = 304;
            case FEATURE3_FUNCTION_END ->                       out = 305;*/
            case FEATURE3_RETURN ->                             out = 306;
            case FEATURE3_UNREACHABLE_FUNCTION ->               out = 307;
            case FEATURE3_FUNCTION_CALLS ->                     out = 308;
            case FEATURE3_VARIADIC_FUNCTION ->                  out = 309;

            case FEATURE4_AGGREGATED ->                         out = 400;
            case FEATURE4_DECOMPILED_FILES_PRODUCED ->          out = 401;
            case FEATURE4_ANTLR_CRASHES ->                      out = 402;
            case FEATURE4_PARSER_ERRORS ->                      out = 403;
        }
        return out;
    }

    /**
     * Get number of test categories
     * @return number of test categories
     */
    public static long size(){
        return ETestCategories.values().length;
    }

    public boolean bIsFeature4Test(){
        return this== FEATURE4_AGGREGATED ||
                this ==FEATURE4_ANTLR_CRASHES ||
                this == FEATURE4_PARSER_ERRORS ||
                this == FEATURE4_DECOMPILED_FILES_PRODUCED;
    }
}
