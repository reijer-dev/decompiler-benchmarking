package nl.ou.debm.common;

/**
 * This class provides easy access to all setting, so they can be found and modified easily
 */

public class ProjectSettings {
    public static final double CHANCE_OF_EXPRESSION_AS_STATEMENT = 0.5;
    public static double CHANCE_OF_CREATION_OF_A_NEW_FUNCTION = 0.1;
    public static final double FUNCTION_TARGET_MAX_AMOUNT = 100;
    public static final double CHANCE_OF_CREATION_OF_A_NEW_STRUCT = 0.2;
    public static final double CHANCE_OF_CREATION_OF_A_NEW_GLOBAL = 0.5;
    public static final double CHANCE_OF_TERMINATION_EXPRESSION_RECURSION = 0.8;
    public static final double CHANCE_OF_STRUCT_WHEN_ASKING_FOR_RANDOM_DATATYPE = 0.5;
    public static final int MAX_EXPRESSION_DEPTH = 5;
    public static final double CHANCE_OF_MULTIPLE_STATEMENTS=.01;
    public static final int MAX_MUTLIPLE_STATEMENTS=6;

    public final static int IDEFAULTNUMBEROFCONTAINERS = 25;
    public final static int IDEFAULTTESTSPERCONTAINER = 75;


    //
    //  for testcase generation by DataStructureProducer
    //

    // whether the testcase gets its own function. Otherwise it is inserted in an existing function.
    public static final double DS_CHANCE_TEST_NEW_FUNCTION = 0.5;

    // If the scope is not local, it is automatically global.
    public static final double DS_CHANCE_SCOPE_LOCAL = 0.75;

    // Array and pointer chances are independent. If both are chosen, the result is a pointer to an array, which tests heap memory. If only pointer is chosen, but not array, that comes down to a single heap allocated instance, which is the same as an array of size 1. (Remark: the base type is chosen by the CGenerator; therefore there are no chances for that here.)
    public static final double DS_CHANCE_ARRAY = 0.4;
    public static final double DS_CHANCE_PTR = 0.4;

    public static final int DS_MIN_STRUCT_MEMBERS = 1;
    public static final int DS_MAX_STRUCT_MEMBERS = 5;
    public static final int DS_MIN_ARRAY_SIZE = 3;
    // Values near the maximum will almost never be chosen because there's a bias towards lower values.
    public static final int DS_MAX_ARRAY_SIZE = Integer.MAX_VALUE;
}
