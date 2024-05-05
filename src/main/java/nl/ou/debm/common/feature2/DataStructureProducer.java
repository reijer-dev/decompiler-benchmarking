package nl.ou.debm.common.feature2;

import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.producer.*;

import java.util.*;

import static nl.ou.debm.common.ProjectSettings.*;

/*
Variables of various kinds are created. To test whether a decompiler can detect the datatype, the variables are also used. My initial idea was to generate algorithms, so that each test has its own usage pattern, but there was not enough time for that, and it's probably not really necessary. What matters most is that the usage patterns give enough information.

To save time I also only generate structs with members of builtin/primitve type. It would be interesting to test structs containing arrays, pointers, other structs etc. but that would make everything much more complicated.
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
    // how many tests are generated of various kinds

    // initTestcasesToAdd keeps generating test parameters until all kinds of tests are added to testcasesToAdd. All those testcases must be added to the CGenerator before this class is satisfied. More testcases may be added if the CGenerator keeps asking for more, up until a limit set in ProjectSettings.
    List<TestParameters> testcasesToAdd = new ArrayList<>();

    public DataStructureProducer(CGenerator generator){
        this.generator = generator;
    }

    private void initTestcasesToAdd()
    {
        int global_builtin = 0;
        int global_struct = 0;
        int global_builtin_array = 0;
        int global_struct_array = 0;
        int local_builtin = 0;
        int local_struct = 0;
        int local_builtin_array = 0;
        int local_struct_array = 0;
        int global_builtin_ptr = 0;
        int global_struct_ptr = 0;
        int global_builtin_array_ptr = 0;
        int global_struct_array_ptr = 0;
        int local_builtin_ptr = 0;
        int local_struct_ptr = 0;
        int local_builtin_array_ptr = 0;
        int local_struct_array_ptr = 0;

        while( ! (
            global_builtin >= 1
            && global_struct >= 1
            && global_builtin_array >= 1
            && global_struct_array >= 1
            && local_builtin >= 1
            && local_struct >= 1
            && local_builtin_array >= 1
            && local_struct_array >= 1
            && global_builtin_ptr >= 1
            && global_struct_ptr >= 1
            && global_builtin_array_ptr >= 1
            && global_struct_array_ptr >= 1
            && local_builtin_ptr >= 1
            && local_struct_ptr >= 1
            && local_builtin_array_ptr >= 1
            && local_struct_array_ptr >= 1
        )) {
            var param = generateParameters();

            if (param.scope == EScope.global) {
                if (param.baseType.bIsPrimitive()) {
                    if (param.array) {
                        if (param.ptr) {
                            if(global_builtin_array_ptr++ == 0) { testcasesToAdd.add(param); }
                        }
                        else {
                            if(global_builtin_array++ == 0) { testcasesToAdd.add(param); }
                        }
                    }
                    else {
                        if (param.ptr) {
                            if(global_builtin_ptr++ == 0) { testcasesToAdd.add(param); }
                        }
                        else {
                            if(global_builtin++ == 0) { testcasesToAdd.add(param); }
                        }
                    }
                }
                else {
                    if (param.array) {
                        if (param.ptr) {
                            if(global_struct_array_ptr++ == 0) { testcasesToAdd.add(param); }
                        }
                        else {
                            if(global_struct_array++ == 0) { testcasesToAdd.add(param); }
                        }
                    }
                    else {
                        if (param.ptr) {
                            if(global_struct_ptr++ == 0) { testcasesToAdd.add(param); }
                        }
                        else {
                            if(global_struct++ == 0) { testcasesToAdd.add(param); }
                        }
                    }
                }
            }
            else {
                if (param.baseType.bIsPrimitive()) {
                    if (param.array) {
                        if (param.ptr) {
                            if(local_builtin_array_ptr++ == 0) { testcasesToAdd.add(param); }
                        }
                        else {
                            if(local_builtin_array++ == 0) { testcasesToAdd.add(param); }
                        }
                    }
                    else {
                        if (param.ptr) {
                            if(local_builtin_ptr++ == 0) { testcasesToAdd.add(param); }
                        }
                        else {
                            if(local_builtin++ == 0) { testcasesToAdd.add(param); }
                        }
                    }
                }
                else {
                    if (param.array) {
                        if (param.ptr) {
                            if(local_struct_array_ptr++ == 0) { testcasesToAdd.add(param); }
                        }
                        else {
                            if(local_struct_array++ == 0) { testcasesToAdd.add(param); }
                        }
                    }
                    else {
                        if (param.ptr) {
                            if(local_struct_ptr++ == 0) { testcasesToAdd.add(param); }
                        }
                        else {
                            if(local_struct++ == 0) { testcasesToAdd.add(param); }
                        }
                    }
                }
            }
        }
        
        assert testcasesToAdd.size() == 16;
    }

    public static int randomInt(int min, int max) {
        var r = new Random();
        return r.nextInt(max - min + 1) + min;
    }

    // Chooses a random int inside the interval, where the probability for larger ints is lower.
    // The idea behind this is that an array size of 600 is not significantly different from 601, but 3 and 4 are more interesting. For example, a decompiler may judge a size 3 array to be a struct, and a size 4 array to be an array, based on heuristics. (I haven't seen this in practice.)
    public static int randomIntBiased(int min, int max) {
        var bias = 10.0; //the higher this is, the more bias towards higher outcomes

        var rmax = Math.atan(DS_MAX_ARRAY_SIZE / bias);
        var rmin = Math.atan(DS_MIN_ARRAY_SIZE / bias);
        var rrange = rmax - rmin;
        var r = Math.random();
        r *= rrange;
        r += rmin;
        return (int)(Math.tan(r) * bias);
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


    String makeVar(String typename, String varname) {
        return
            "typename varname; __CM_use_memory(&varname, sizeof(varname));"
            .replaceAll("typename", typename)
            .replaceAll("varname", varname)
            ;
    }




    // The useAs functions generates code that helps to make the datatype known to a decompiler.
    // todo add more usage patterns

    // valueExpr is an expression for a value, usually a variable name, but it can also be something like *p to get the value a pointer p points to
    String useAsIntegral(DataType T, String valueExpr) {
        assert T.bIsPrimitive();
        assert ! (
            T.getNameForUse().equals("float")
            || T.getNameForUse().equals("double"));

        boolean unsigned = T.getNameForUse().contains("unsigned");

        if (unsigned && Math.random() < 0.5) {
            return """
                { // use %valueExpr% as %T% (collatz)
                    %make_count%
                    while( %valueExpr% != 1 ) {
                        if (%valueExpr% % 2 == 0) %valueExpr% = %valueExpr% / 2;
                        else                      %valueExpr% = %valueExpr% * 3 + 1;
                        count++;
                    }
                    __CM_use_memory(&count, sizeof(count));
                }
                """ .replaceAll("%make_count%", makeVar(T.getNameForUse(), "count"))
                    .replaceAll("%valueExpr%", valueExpr)
                    .replaceAll("%T%", T.getNameForUse())
                    ;
        }
        else {
            int fixed_modulus = randomInt(2, 50);
            return """
            {
                %make_b%         
                while(b) {
                    %make_runtime_modulus%
                    %make_factor%
                    %make_term%
                    %make_divisor%
                    
                    __CM_use_memory(&b, sizeof(b));
                    if(b)   %valueExpr% %= %fixed_modulus%;
                    
                    __CM_use_memory(&b, sizeof(b));
                    if(b)   %valueExpr% %= runtime_modulus;
                    
                    __CM_use_memory(&b, sizeof(b));
                    if(b)   %valueExpr% *= factor;
                    
                    __CM_use_memory(&b, sizeof(b));
                    if(b)   %valueExpr% += term;
                    
                    __CM_use_memory(&b, sizeof(b));
                    if(b)   %valueExpr% /= divisor;
                    
                    __CM_use_memory(&b, sizeof(b));
                }
            }
            """ .replaceAll("%make_b%", makeVar("bool", "b"))
                .replaceAll("%make_runtime_modulus%", makeVar(T.getNameForUse(), "runtime_modulus"))
                .replaceAll("%make_factor%", makeVar(T.getNameForUse(), "factor"))
                .replaceAll("%make_term%", makeVar(T.getNameForUse(), "term"))
                .replaceAll("%make_divisor%", makeVar(T.getNameForUse(), "divisor"))
                .replaceAll("%fixed_modulus%", ""+fixed_modulus)
                .replaceAll("%valueExpr%", valueExpr)
                ;
        }
    }

    String useAsFloatingPoint(DataType T, String valueExpr) {
        assert T.bIsPrimitive();
        assert T.getNameForUse().equals("float")
            || T.getNameForUse().equals("double");

        // This calculates the Mandelbrot formula (what that is is irrelevant for decompiler testing). It really requires 2 (floating point) inputs: the real and imaginary parts of c. I use the one input (valueExpr) as the real part of c, and use __CM_use_memory to initialize the imaginary part (ci).
        return """
            { //use %valueExpr% as %T% (mandelbrot)
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
                    // todo this doesn't work if the member has a pointer type. We don't currently use pointers in structs.
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
            else if (param.array) {
                String typename = generator.addTypedef(param.baseType, "", "[" + param.arraySize + "]");
                var dataType = new DataType(typename, false, "{}");
                varname = generator.getGlobal(dataType).getName();
            }
            else  {
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
            ret.initialization = "__CM_use_memory(&" + varname + ", sizeof(void*));";
        }
        else if (param.array) {
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
            if (Math.random() < 0.5) {
                // loop over all elements
                arrayUsagePattern = """
                    for (int i=0; i<%size%; i++) {
                        %element_usage%
                    }
                    """ .replaceAll("%size%", ""+param.arraySize)
                        ;
            }
            else {
                // loop an unknown number of times, each time choosing an index to access
                arrayUsagePattern = """
                    %make_b%
                    while(b) {
                        %make_i%
                        %element_usage%
                        __CM_use_memory(&b, sizeof(b));
                    }
                    """ .replaceAll("%make_b%", makeVar("bool", "b"))
                        .replaceAll("%make_i%", makeVar("int", "i"))
                    ;
            }

            String valueExpr = varname + "[i]";
            String elementUsage = useAs(param.baseType, valueExpr);

            usage = arrayUsagePattern
                .replaceAll("%element_usage%", elementUsage.replaceAll("\n", "\n\t"))
                ;
        }
        else if (param.ptr) {
            usage = useAs(param.baseType, "*" + varname);
        }
        else {
            usage = useAs(param.baseType, varname);
        }

        ret.testStatement = testTemplate
            .replaceAll("%usage%", usage.replaceAll("\n", "\n\t"))
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

        if (firstStatement) {
            if (f.getName().equals("main")) {
                initializeGlobalsFunction.addStatement("// initialization of globals");
                generator.addFunction(initializeGlobalsFunction, this);
                ret.add(initializeGlobalsFunction.getName() + "();");
                firstStatement = false;
                initTestcasesToAdd();
            }
            else {
                return ret;
            }
        }

        if (testcaseCount >= DS_MAX_TESTCASES) {
            return ret;
        }

        // create and add a testcase
        TestParameters param;
        if (testcasesToAdd.size() > 0) {
            int lastIdx = testcasesToAdd.size() - 1;
            param = testcasesToAdd.get(lastIdx);
            testcasesToAdd.remove(lastIdx);
        }
        else {
            param = generateParameters();
        }
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

        if (param.scope == EScope.global) {
            initializeGlobalsFunction.addStatement( testCode.initialization );
        }

        return ret;
    }

    @Override
    public boolean isSatisfied() {
        return testcasesToAdd.isEmpty();
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
