package nl.ou.debm.common.feature2;

import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.producer.*;

import java.util.*;

import static nl.ou.debm.common.ProjectSettings.*;

/*
Some notes about the approach here
To save time I only generate structs with members of builtin/primitve type. It would be interesting to test structs containing arrays, pointers, other structs etc. but that would make everything much more complicated.
*/

public class DataStructureProducer implements IFeature, IStatementGenerator, IStructGenerator, IGlobalVariableGenerator {
    public static String external_filename = "DS_functions.c";

    final CGenerator generator;
    private int structCount = 0;
    private int variableCount = 0;
    private int testcaseCount = 0;

    // This function is added to the CGenerator, but later more statements are added as more globals are created. This is a bit of a hack, but it works, because java has reference types.
    Function initializeGlobalsFunction = new Function(DataType.void_t);
    boolean firstStatement = true;


    public DataStructureProducer(CGenerator generator){
        this.generator = generator;
    }

    public static int randomInt(int min, int max) {
        var r = new Random();
        return r.nextInt(max - min + 1) + min;
    }

    // Chooses a random int inside the interval, where the probability for larger ints is lower.
    // The idea behind this is that an array size of 600 is not significantly different from 601, but 3 and 4 are more interesting. For example, a decompiler may judge a size 3 array to be a struct, and a size 4 array to be an array, based on heuristics. (I haven't seen this in practice.)
    public static int randomIntBiased(int min, int max) {
        // This function maps the interval (0, 1), which is the interval of Math.random, to (min, max), using f(x) = 1/x as base. Normally f would map (0,1) to (1, infinity), but by shifting f to the left and upwards a little bit, you can get f(0) = min and f(1) = max, as desired.
        var x = Math.random();
        var fx = 1/(x + 1/(max-min+1)) + (min-1);
        return (int)Math.round(fx + 0.5); // + 0.5 to round to nearest int
    }

    String marker(String varname) {
        return new DataStructureCodeMarker(varname).toCode();
    }

    Function makeFunction(String body) {
        var f = new Function(DataType.void_t);
        var statements = List.of(body.split("\n"));
        f.addStatements(statements);
        f.setExternalFileName(external_filename);
        return f;
    }

    DataType builtin(String specifier) {
        return DataType.make_primitive(specifier, "0");
    }


    // This only supports named types. To create something like an array, there must be a typedef for that kind of array.
    String makeVar(String typename, String varname) {
        return
            "typename varname; __CM_use_memory(&varname, sizeof(varname));"
            .replaceAll("typename", typename)
            .replaceAll("varname", varname)
            ;
    }




    //
    //  Giveaway code todo explain: helps to make the datatype known to a decompiler
    //
    // valueExpr is an expression for a value, usually a variable name, but it can also be something like *p to get the value a pointer p points to
    String useAsIntegral(DataType T, String valueExpr) {
        assert T.bIsPrimitive();
        assert ! (
            T.getNameForUse().equals("float")
            || T.getNameForUse().equals("double"));

        // todo distinguish unsigned from signed
        // todo always collatz
        return """
            { // use %valueExpr% as %T% (collatz)
                %make_count%
                while( %valueExpr% != 1 ) {
                    if (%valueExpr% %2 == 0) %valueExpr% = %valueExpr% / 2;
                    else                     %valueExpr% = %valueExpr% * 3 + 1;
                    count++;
                }
                __CM_use_memory(&count, sizeof(count));
            }
            """ .replaceAll("%make_count%", makeVar(T.getNameForUse(), "count"))
                .replaceAll("%valueExpr%", valueExpr)
                .replaceAll("%T%", T.getNameForUse())
                ;
    }

    String useAsFloatingPoint(DataType T, String valueExpr) {
        assert T.bIsPrimitive();
        assert T.getNameForUse().equals("float")
            || T.getNameForUse().equals("double");

        //todo always mandelbrot
        // This calculates the Mandelbrot formula (what that is is irrelevant for decompiler testing). It really requires 2 (floating point) inputs: the real and imaginary parts of c. I use the one input (valueExpr) as the real part of c, and use __CM_use_memory to initialize the imaginary part (ci).
        return """
            { //use %valueExpr% as %T% (mandelbrot; %valueExpr% used instead of cr)
                %make_ci%
                %T% zr, zi, zrsqr, zisqr;
                zi = zr = zrsqr = zisqr = 0.0;
                
                %make_maxIters%
                int iterationCount = 0;
                while (zrsqr + zisqr <= 4.0 && iterationCount < maxIters) {
                    zi = zr * zi;
                    zi += zi;
                    zi += ci;
                    zr = zrsqr - zisqr + %valueExpr%;
                    zrsqr = zr * zr;
                    zisqr = zi * zi;
                    iterationCount++;
                }
                
                __CM_use_memory(&iterationCount, sizeof(iterationCount));
            }
            """ .replaceAll("%valueExpr%", valueExpr)
                .replaceAll("%T%", T.getNameForUse())
                .replaceAll("%make_maxIters%", makeVar("int", "maxIters"))
                .replaceAll("%make_ci%", makeVar(T.getNameForUse(), "ci"))
                ;
    }

    String useAs(DataType T, String valueExpr)
    {
        if (T instanceof Struct S) {
            var sb = new StringBuilder();
            for (var member : S.getProperties())
            {
                var memberType = member.getType();
                // All members are primitive because of how structs are currently generated.
                if (memberType.bIsPrimitive()) {
                    // todo dot doesn't work if the member has a pointer type. We don't currently use pointers in structs.
                    var usageStatement = useAs(memberType, "(" + valueExpr + ")." + member.getName());
                    sb.append(usageStatement);
                }
            }
            return sb.toString();
        }
        if (T.getNameForUse().equals("float")
            || T.getNameForUse().equals("double"))
        {
            return useAsFloatingPoint(T, valueExpr);
        }
        return useAsIntegral(T, valueExpr);
    }


    //
    //  Test generators
    //

    enum EScope {
        global, local
    }

    static class TestParameters {
        boolean newFunction;
        boolean array;
        int arraySize; //only valid if array is true
        boolean ptr;
        EScope scope;
        DataType baseType;
    }

    TestParameters generateParameters() {
        var ret = new TestParameters();
        ret.newFunction = Math.random() < DS_CHANCE_TEST_NEW_FUNCTION;
        ret.array = Math.random() < DS_CHANCE_ARRAY;
        ret.ptr = Math.random() < DS_CHANCE_PTR;

        if (Math.random() < DS_CHANCE_SCOPE_LOCAL)
            ret.scope = EScope.local;
        else
            ret.scope = EScope.global;

        if (ret.array) ret.arraySize = randomIntBiased(DS_MIN_ARRAY_SIZE, DS_MAX_ARRAY_SIZE);
        else           ret.arraySize = -1;

        ret.baseType = generator.getDataType();
        return ret;
    }

    // The separation into 3 parts is because depending on parameters, the code must be placed in a different location. For globals, initialization is done in a separate function, but for locals, the initialization is done right after the declaration.
    static class TestCode {
        //declaration and initialization of the test subject (a variable)
        String declaration;
        String initialization;
        // a block statement that uses the variable in such a way that a decompiler should be able to determine its data type
        String testStatement;
    }

    TestCode makeTestCode(TestParameters param)
    {
        var ret = new TestCode();

        // For globals, let the CGenerator choose a name. The CGenerator will also generate the declaration, so for globals ret.declaration is useless.
        String varname;
        if (param.scope == EScope.global) {
            if (param.ptr) {
                varname = generator.getGlobal(DataType.ptrTypeOf(param.baseType)).getName();
            }
            else {
                varname = generator.getGlobal(param.baseType).getName();
            }
        }
        else {
            varname = "testSubject";
        }

        // declaration
        if (param.ptr) {
            // This is also the declaration if param.array is also true.
            ret.declaration = param.baseType.getNameForUse() + " *" + varname + ";";
        }
        else if (param.array) {
            // because param.ptr is false, this will be a fixed size array. I don't use variable length arrays, so all dynamically sized arrays are used through pointers.
            ret.declaration = param.baseType.getNameForUse() + ' ' + varname + "[" + param.arraySize + "];";
        }
        else {
            ret.declaration = param.baseType.getNameForUse() + " " + varname + ";";
        }

        // initialization
        if (param.ptr) {
            // using sizeof(void*) as the size of all pointers is the same anyway
            ret.initialization = "__CM_use_memory(&" + varname + ", sizeof(void*));";
        }
        else if (param.array) {
            // Note that there is no & before varname, because an array variable can be automatically converted to a pointer.
            ret.initialization = "__CM_use_memory(" + varname + ", sizeof(" + varname + "));";
        }
        else {
            ret.initialization = "__CM_use_memory(&" + varname + ", sizeof(" + varname + "));";
        }


        // testStatement
        String testTemplate;
        if (param.scope == EScope.local) {
            testTemplate = """
                {
                    %declaration%
                    %initialization%
                    %usage%
                    %marker%
                }
                """ .replaceAll("%declaration%", ret.declaration)
                    .replaceAll("%initialization%", ret.initialization)
                    ;
        }
        else {
            assert param.scope == EScope.global;
            testTemplate = """
                {
                    %usage%
                    %marker%
                }
                """;
        }

        String usage;
        if (param.array)
        {
            String arrayUsagePattern;
            if (Math.random() < 0.8) {
                // loop over all elements
                arrayUsagePattern = """
                    for (int i=0; i<%size%; i++) {
                        %element_usage%
                    }
                    """;
            }
            else {
                // choose an index to access (which index is determined by __CM_use_memory in makeVar)
                arrayUsagePattern = """
                    {
                        %make_i%
                        %element_usage%
                    }
                    """.replaceAll("%make_i%", makeVar("int", "i"));
            }

            String valueExpr = varname + "[i]";
            String elementUsage = useAs(param.baseType, valueExpr).replaceAll("\n", "\n\t\t");

            usage = arrayUsagePattern
                .replaceAll("%size%", ""+param.arraySize)
                .replaceAll("%element_usage%", elementUsage)
                ;
        }
        else if (param.ptr) {
            usage = useAs(param.baseType, "*" + varname);
        }
        else {
            usage = useAs(param.baseType, varname);
        }

        ret.testStatement = testTemplate
            .replaceAll("%usage%", usage)
            .replaceAll("%marker%", marker(varname))
            ;
        return ret;
    }





    //
    //  Overrides
    //

    @Override
    public List<String> getNewStatements(int currentDepth, Function f) {
        return getNewStatements(currentDepth, f, null);
    }

    // This is where creation of a new testcase is started.
    // A testcase is a block statement. This function creates a testcase and then either
    //     - directly returns the testcase, or
    //     - creates a new function containing the testcase, and returns a call to that function.
    @Override
    public List<String> getNewStatements(int currentDepth, Function f, StatementPrefs prefs) {
        if (prefs==null){
            prefs = new StatementPrefs(null);
        }
        var ret = new ArrayList<String>();

        boolean makeNewFunction = Math.random() < DS_CHANCE_TEST_NEW_FUNCTION;

        // check if the preferences can be fulfilled
        if ((makeNewFunction || firstStatement) && prefs.expression == EStatementPref.NOT_WANTED) {
            return ret;
        }
        if ( ! makeNewFunction && prefs.compoundStatement == EStatementPref.NOT_WANTED) {
            return ret;
        }
        if (prefs.numberOfStatements == ENumberOfStatementsPref.MULTIPLE) {
            return ret;
        }
        if (prefs.loop == EStatementPref.REQUIRED || prefs.assignment == EStatementPref.REQUIRED) {
            return ret;
        }

        // todo what if f is not callable? does that mean the tests cannot be found?
        if ( ! f.isCallable()) {
            System.out.println("warning: test added in uncallable function " + f.getName());
        }

        // the first generated statement must be a call to the function that initializes globals, and that call must be in the main function
        if (firstStatement) {
            if (f.getName().equals("main")) {
                generator.addFunction(initializeGlobalsFunction, this);
                ret.add(initializeGlobalsFunction.getName() + "();");
            }
            firstStatement = false;
        }
        else {
            return ret;
        }

        // create and add a testcase
        var param = generateParameters();
        var testCode = makeTestCode(param);
        if (Math.random() < DS_CHANCE_TEST_NEW_FUNCTION) {
            var newFunction = makeFunction(testCode.testStatement);
            generator.addFunction(newFunction, this); //changes the name of the function
            ret.add(newFunction.getName() + "();");
        }
        else {
            ret.add(testCode.testStatement);
        }
        testcaseCount++;
        System.out.println("testcaseCount is nu: " + testcaseCount);

        if (param.scope == EScope.global) {
            initializeGlobalsFunction.addStatement( testCode.initialization );
        }

        return ret;
    }

    @Override
    public boolean isSatisfied() {
        System.out.println("controle satisfied. testcaseCount is nu: " + testcaseCount);
        return testcaseCount > 1; //todo constante ergens neerzetten? hoe bepalen jaap en reijer wanneer te stoppen?
    }

    @Override
    public String getPrefix(){ return EFeaturePrefix.DATASTRUCTUREFEATURE.toString(); }

    @Override
    public List<String> getIncludes() {
        return new ArrayList<>();
    }

    @Override
    public Struct getNewStruct() {
        var result = new Struct("struct_" + structCount++);
        int memberCount = randomInt(DS_MIN_STRUCT_MEMBERS, DS_MAX_STRUCT_MEMBERS);
        for (int i=0; i<memberCount; i++) {
            var name = "v"+i;
            // todo: for now I use only raw/builtin/primitive types as struct members.
            var T = generator.getRawDataType();
            result.addProperty(new Variable(name, T));
        }
        return result;
    }

    @Override
    public Variable getNewGlobalVariable(DataType type) {
        if(type == null)
            type = generator.getDataType();

        return new Variable("var_" + variableCount++, type);
    }
}
