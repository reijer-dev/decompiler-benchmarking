package nl.ou.debm.producer;

import nl.ou.debm.common.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static nl.ou.debm.common.ProjectSettings.*;

// TODO: implement proper procedures (which in C are functions returning void)


public class CGenerator {
    // constants that define the behavior publicly available and grouped, so they can be
    // easily identified and modified

    private String main_filename;
    private final List<IFeature> features = new ArrayList<>();  // keep track of all feature classes
    public final List<IFunctionBodyInjector> functionBodyInjectors = new ArrayList<>();
    private int featureIndex = 0;                               // make sure that all feature classes are used throughout code building
    public HashMap<DataType, List<Function>> callableFunctionsByReturnType = new HashMap<>();   // Store functions sorted by return type
    public HashMap<DataType, List<Function>> callableFunctionsByReturnTypeWithParameters = new HashMap<>();   // Store functions sorted by return type
    public HashMap<DataType, List<Function>> callableFunctionsByReturnTypeWithoutParameters = new HashMap<>();   // Store functions sorted by return type
    public List<Function> functions = new ArrayList<>();        // Store functions in list, to maintain their creation order
    public HashMap<DataType, List<Variable>> globalsByType = new HashMap<>();   // store global variables
    public List<Struct> structs = new ArrayList<>();            // store structs
    public HashMap<String, Struct> structsByName = new HashMap<>(); // store structs by name
    public final DataType[] rawDataTypes;                       // table of basic data types
    private List<String> typedefs = new ArrayList<>();
    private long lngNextTypedef = 0;
    private Function mainFunction;                              // main function
    private long lngNextGlobalLabel = 0;                        // index for next requested global label name
    private final HashMap<IFeature, Long> neededIterationsForSatisfaction = new HashMap<>();
    private final List<String> includes = new ArrayList<>();
    /** This is to mark certain functions and globals to be defined in a different file.
     By default, the code generator uses the file that contains the main function.
     Every function or global that needs this behavior overridden must be inserted in this map.
     The String value is the name of the file the definition should be in.
     All other files will contain only a declaration. Related function: isOwnedByFile */
    private final Map<Object, String> ownedByFile = new HashMap<>();
    private boolean useOwnedByFile = true;


    public CGenerator() {
        // constructor
        // -----------
        main_filename = "main.c"; //todo keep a list of reserved names?

        // fill the array of feature-objects
        for (var prefix : EFeaturePrefix.values()){
            var new_feature = prefix.getAppropriateIProducerClass(this);
            if (new_feature!=null){
                features.add(new_feature);
            }
        }

        // fill functionBodyInjectors array
        for (var feature : features){
            if (feature instanceof IFunctionBodyInjector f){
                functionBodyInjectors.add(f);    // safe, we just checked
            }
        }

        // fill map for statistics
        for(var feature : features)
            neededIterationsForSatisfaction.put(feature, 0L);

        // fill array of raw data types
        //todo refer to constants from the DataType class (if they prove useful)
        rawDataTypes = new DataType[]{
            DataType.make_primitive("char", "0"),
            DataType.make_primitive("short", "0"),
            DataType.make_primitive("long", "0"),
            DataType.make_primitive("long long", "0"),
            DataType.make_primitive("unsigned char", "0"),
            DataType.make_primitive("unsigned short", "0"),
            DataType.make_primitive("unsigned long", "0"),
            DataType.make_primitive("unsigned long long", "0"),
            DataType.make_primitive("float", "0.0"),
            DataType.make_primitive("double", "0.0"),
        };
    }

    /**
     * Generate C-source code
     * @return  Map of filenames to contents
     */
    public Map<String, String> generateSourceFiles() {
        // note: the uniqueness of the EFeaturePrefixes used to be tested here,
        // but is moved to a test class. The exception formally thrown here is removed.

        // create external code marker functions
        createCodeMarkerFunctions();

        // set the wheels in motion. After this, everything that defines the code is created.
        createMainFunction();

        // generate code

        // first determine which files there are. There is always a main file (of which the name is stored in the class member main_filename).
        // In addition, the map ownedByFile may mention other files that some entities need to be defined in.
        var filenames = new HashSet<String>();
        filenames.add(main_filename);
        for (var entity : ownedByFile.keySet()) {
            var filename = ownedByFile.get(entity);
            filenames.add(filename);
        }
        //check: the amalgamation filename is reserved so it should not be used:
        for (var filename : filenames) {
            assert ! filename.equals(IOElements.cAmalgamationFilename);
        }
        filenames.add(IOElements.cAmalgamationFilename);

        // Includes and struct declarations are the same in every file, so they can already be generated before looping over the files.
        var includes_and_declarations = new StringBuilder();
        writeIncludes(includes_and_declarations);
        writeStructs(includes_and_declarations);
        writeTypedefs(includes_and_declarations);
        writeFunctionDeclarations(includes_and_declarations);
        writeGlobalVariableDeclarations(includes_and_declarations);

        var ret = new HashMap<String, String>();

        //Create code for every file and add it to the return value.
        for (var filename : filenames)
        {
            if (filename.equals(IOElements.cAmalgamationFilename)) {
                //for this file, enable emitting all definitions
                useOwnedByFile = false;
            }

            var sb = new StringBuilder();
            sb.append(includes_and_declarations);
            //Prevent warnings of unused values for compiler
            sb.append("\n#pragma clang diagnostic ignored \"-Wunused-value\"").append(System.lineSeparator());
            writeGlobalVariables(sb, filename);
            writeFunctions(sb, filename);

            ret.put(filename, sb.toString());
            useOwnedByFile = true;
        }

        return ret;
    }

    // add function to the correct lists
    private void addFunction(Function f) {
        // add to callable functions
        if (f.isCallable()) {
            addFunctionToCallableFunctionsByReturnType(f);
        }
        // add to function table in general
        functions.add(f);
        // add to map of external functions
        if (!f.strGetExternalFileName().isEmpty()){
            ownedByFile.put(f, f.strGetExternalFileName());
        }
    }

    /**
     * add a function to the code, external entry point<br>
     * the feature prefix is added to the function name
     * @param f function to be added
     * @param feature feature that adds the function (needed for feature prefix)
     */
    public void addFunction(Function f, IFeature feature){
        // add prefix to function name
        f.setName(feature.getPrefix() + "_" + f.getName());
        // process function
        addFunction(f);
    }

    // returns the created name
    // Example:
    //      addTypeDef(DataType.void_t, "*", "[10]")
    // creates the typedef
    //      typedef void *name[10];
    // where name is the chosen name. The user is responsible that this results in a valid typedef.
    public String addTypedef(DataType baseType, String before_name, String after_name) {
        String name = "typedef_" + lngNextTypedef++;
        typedefs.add("typedef " + baseType.getNameForUse() + " " + before_name + name + after_name + ";");
        return name;
    }

    private void addStruct(Struct s) {
        // add struct to array of structs
        structs.add(s);
        // add struct to map of structs
        structsByName.put(s.name, s);
    }

    private boolean isOwnedByFile(Object entity, String filename) {
        if ( ! useOwnedByFile) {
            return true;
        }
        if ( ! ownedByFile.containsKey(entity)) {
            return filename.equals(main_filename);
        }
        return filename.equals(ownedByFile.get(entity));
    }

    /**
     * Export all struct declarations to the string builder
     */
    private void writeStructs(StringBuilder sb) {
        for (var struct : structs) {
            struct.appendCode(sb);
        }
    }

    private void writeTypedefs(StringBuilder sb) {
        sb.append("// typedefs \n");
        for (var typedef : typedefs) {
            sb.append(typedef).append("\n");
        }
    }

    private void writeIncludes(StringBuilder sb) {
        for (var feature : features) {
            var includes = feature.getIncludes();
            if (includes != null)
                this.includes.addAll(includes);
        }
        for(var include : includes.stream().distinct().toList())
            sb.append("#include ").append(include).append(System.lineSeparator());
    }

    private void writeGlobalVariableDeclarations(StringBuilder sb) {
        sb.append("// global variable declarations \n");
        for (var globalType : globalsByType.keySet()) {
            for (var global : globalsByType.get(globalType)) {
                sb.append("extern ");
                sb.append(global.getType().getNameForUse());
                sb.append(' ');
                sb.append(global.getName());
                sb.append(';');
                sb.append('\n');
            }
        }
    }


    /**
     * Export global variables to the string builder
     */
    private void writeGlobalVariables(StringBuilder sb, String filename) {
        sb.append("\n// global variables\n");
        for (var globalType : globalsByType.keySet()) {
            for (var global : globalsByType.get(globalType)) {
                if ( ! isOwnedByFile(global, filename)) {
                    continue;
                }
                sb.append(global.getType().getNameForUse());
                sb.append(' ');
                sb.append(global.getName());
                if (global.hasValue()) {
                    sb.append(" = ");
                    sb.append(global.getValue());
                }
                sb.append(';');
                sb.append('\n');
            }
        }
    }

    private void writeFunctionDeclarations(StringBuilder sb) {
        sb.append("// function declarations\n");
        for (var function : functions) {
            function.appendDeclaration(sb);
        }
    }

    /**
     * Export all functions to the string builder.
     */
    private void writeFunctions(StringBuilder sb, String filename) {
        // write all the created function, except main
        sb.append("\n// functions\n");
        for (var function : functions) {
            if ( ! isOwnedByFile(function, filename)) {
                continue;
            }
            function.appendCode(this, sb);
        }

        // write main
        if (isOwnedByFile(mainFunction, filename)) {
            mainFunction.appendCode(this, sb);
        }
    }


    /**
     * Create main function for the code. This function will add statements to
     * the main function as long as it takes to satisfy all feature requirements.
     */
    private void createMainFunction() {
        // make new function object, int main()
        mainFunction = new Function(DataType.make_primitive("int", "0"), "main");
        var versionMarker = new BaseCodeMarker(EFeaturePrefix.METADATA);
        versionMarker.setProperty("version", MetaData.Version);
        mainFunction.addStatement(versionMarker.strPrintf());

        // Because of the recursive nature of getNewStatement,
        // the main function may, in the end, turn out to be very short:
        // it may even have only one line!
        while (!allFeaturesSatisfied()) {
            mainFunction.addStatements(getNewStatements(1, mainFunction));
        }
        for(var entry : neededIterationsForSatisfaction.entrySet())
            System.out.println(entry.getKey() + " needed " + entry.getValue() + " iterations");

        // Use standard exit code as a last statement
        mainFunction.addStatement("return 0;");

        // Add the function to the collection of functions, sorted by return type
        addFunctionToCallableFunctionsByReturnType(mainFunction);
    }

    /**
     * create the external functions our code markers use
     */
    private void createCodeMarkerFunctions() {
        includes.add("<stdio.h>");

        // we need to create three functions:
        // 1: function(char*)
        // 2: function(char*, int)
        // 3: function(char*, float)

        final String STRTEXTPAR="cText";
        final String STRINTPAR="iIntVal";
        final String STRFLOATPAR="fltFloatVal";

        var char_function = new Function(DataType.make_primitive("void", "0"), CodeMarker.STREXTERNALPRINTF);
        char_function.addParameter(new FunctionParameter(STRTEXTPAR, DataType.make_primitive("char*", "\"\"")));
        char_function.addStatement("printf(" + STRTEXTPAR + ");");
        char_function.setExternalFileName(CodeMarker.STREXTERNALFILE);
        char_function.setBlockAutoStartAndEnMarkers(true);
        addFunction(char_function);
        
        var char_int_function = new Function(DataType.make_primitive("void", "0"), CodeMarker.STREXTERNALPRINTF_INT);
        char_int_function.addParameter(new FunctionParameter(STRTEXTPAR, DataType.make_primitive("char*", "\"\"")));
        char_int_function.addParameter(new FunctionParameter(STRINTPAR, DataType.make_primitive("int", "0")));
        char_int_function.addStatement("printf(" + STRTEXTPAR + ", " + STRINTPAR + ");");
        char_int_function.setExternalFileName(CodeMarker.STREXTERNALFILE);
        char_int_function.setBlockAutoStartAndEnMarkers(true);
        addFunction(char_int_function);

        var char_float_function = new Function(DataType.make_primitive("void", "0"), CodeMarker.STREXTERNALPRINTF_FLOAT);
        char_float_function.addParameter(new FunctionParameter(STRTEXTPAR, DataType.make_primitive("char*", "\"\"")));
        char_float_function.addParameter(new FunctionParameter(STRFLOATPAR, DataType.make_primitive("float", "0")));
        char_float_function.addStatement("printf(" + STRTEXTPAR + ", " + STRFLOATPAR + ");");
        char_float_function.setExternalFileName(CodeMarker.STREXTERNALFILE);
        char_float_function.setBlockAutoStartAndEnMarkers(true);
        addFunction(char_float_function);

        var char_ptr_function = new Function(DataType.make_primitive("void", "0"), CodeMarker.STREXTERNALPRINTF_PTR);
        char_ptr_function.addParameter(new FunctionParameter(STRTEXTPAR, DataType.make_primitive("char*", "\"\"")));
        char_ptr_function.addParameter(new FunctionParameter("ptr", DataType.ptrTypeOf(DataType.void_t)));
        char_ptr_function.addStatement("printf(" + STRTEXTPAR + ", ptr);");
        char_ptr_function.setExternalFileName(CodeMarker.STREXTERNALFILE);
        char_ptr_function.setBlockAutoStartAndEnMarkers(true);
        addFunction(char_ptr_function);

        // This is not really a codemarker but a utility. It can be used to control which information a compiler has that it can use for optimization.
        var use_memory_function = new Function(DataType.make_primitive("void", "0"), "__CM_use_memory");
        use_memory_function.addParameter(new FunctionParameter("ptr", DataType.ptrTypeOf(DataType.void_t)));
        use_memory_function.addParameter(new FunctionParameter("size", DataType.make_primitive("unsigned", "0")));
        use_memory_function.addStatement("fwrite(ptr, size, 1, stdout);");
        use_memory_function.addStatement("fread(ptr, size, 1, stdin);");
        use_memory_function.setExternalFileName(CodeMarker.STREXTERNALFILE);
        use_memory_function.setBlockAutoStartAndEnMarkers(true);
        addFunction(use_memory_function);
    }

    /**
     * This adds a function to the return-type-sorted list of functions.
     * @param function  The function to be added.
     */
    private void addFunctionToCallableFunctionsByReturnType(Function function){
        // make sure that the hashmap has a key/value-pair
        if (!callableFunctionsByReturnType.containsKey(function.getType()))
            callableFunctionsByReturnType.put(function.getType(), new ArrayList<>());
        // add the function to the list of values (=list of functions) for the key (=list of return data types)
        callableFunctionsByReturnType.get(function.getType()).add(function);

        if(function.getParameters().size() > 0) {
            if (!callableFunctionsByReturnTypeWithParameters.containsKey(function.getType()))
                callableFunctionsByReturnTypeWithParameters.put(function.getType(), new ArrayList<>());
            callableFunctionsByReturnTypeWithParameters.get(function.getType()).add(function);
        }else{
            if (!callableFunctionsByReturnTypeWithoutParameters.containsKey(function.getType()))
                callableFunctionsByReturnTypeWithoutParameters.put(function.getType(), new ArrayList<>());
            callableFunctionsByReturnTypeWithoutParameters.get(function.getType()).add(function);
        }
    }

    /**
     * Test whether all features are satisfied in their output.
     * @return  true if all the selected are satisfied, false if one or more aren't
     */
    private boolean allFeaturesSatisfied() {
        var result = true;
        for (var feature : features) {
            if (!feature.isSatisfied()) {
                neededIterationsForSatisfaction.merge(feature, 1L, Long::sum);
                result = false;
            }
        }
        return result;
    }

    /**
     * Get a new expression. It may terminate at the start when instructed as such,
     * otherwise it will allow recursion.
     * @param type  Type of expression requested.
     * @param terminating  Force direct recursion termination when true, otherwise
     *                     allow subexpressions being part of the expression
     * @return  An expression in text format.
     */
    public String getNewExpression(DataType type, boolean terminating) {
        return getNewExpression(terminating ? Integer.MAX_VALUE : 1, type);
    }

    /**
     * Get a new expression. This may use recursion, but to prevent endless
     * recursion, it asks for the current depth of the recursion.
     * if recursion is too deep, the feature class will be instructed not to
     * use recursion anymore.
     * @param currentDepth  Current depth of recursion.
     * @param type  Type of expression requested.
     * @return  An expression in text format.
     */
    public String getNewExpression(int currentDepth, DataType type) {
        IFeature currentFeature;
        // idea indicates the for loop will not loop if all the features implement IExpressionGenerator,
        // which is true. But is is no problem, as the current feature always switches at the beginning
        // of the loop, so there is still a variety of features called.
        for (int count = 0; count < features.size(); count ++) {   // only loop all the features once
            currentFeature = features.get(iNextFeatureIndex());
            if (currentFeature instanceof IExpressionGenerator expressionGenerator) {
                return expressionGenerator.getNewExpression(currentDepth, type,
                            Math.random() < CHANCE_OF_TERMINATION_EXPRESSION_RECURSION || currentDepth > MAX_EXPRESSION_DEPTH);
            }
        }
        return null;
    }

    /**
     * Return index to the current feature to be used and then switch to the next feature
     * @return  feature-index, ranging 0... features.size()
     */
    private int iNextFeatureIndex() {
        int q = featureIndex;
        featureIndex++;
        featureIndex%=features.size();
        return q;
    }

    /**
     * Get one or more new statements. As any expression is a statement as well, it may return
     * an expression (see: getNewExpression).
     * @return   list of Strings containing one or more statements
     */
    public List<String> getNewStatements(int currentDepth, Function f) {
        return getNewStatements(currentDepth, f, null);
    }

    /**
     * Get one or more statements, depending on the preferences given. If no preferences are given,
     * all are supposed to be "don't care'.
     * @param prefs     preferences for the statement(s) requested. May be null, meaning any
     *                  statement (or expression) will do.
     * @return          a list of one or more statements, fulfilling the preferences
     *                  if preferences cannot be fulfilled, the list is empty
     */
    public List<String> getNewStatements(int currentDepth, Function f, StatementPrefs prefs) {
        // are there any preferences?
        if (prefs==null) {
            // there are no preferences, so make an object with only don't-cares
            prefs = new StatementPrefs(null);
        }

        // determine number of statements
        ENumberOfStatementsPref numberOfStatementsPref = prefs.numberOfStatements;
        if (numberOfStatementsPref == ENumberOfStatementsPref.DON_T_CARE){
            if (Math.random() < CHANCE_OF_MULTIPLE_STATEMENTS){
                numberOfStatementsPref = ENumberOfStatementsPref.MULTIPLE;
            }
            else {
                numberOfStatementsPref = ENumberOfStatementsPref.SINGLE;
            }
        }
        int iNumberOfStatements = 1;
        if (numberOfStatementsPref == ENumberOfStatementsPref.MULTIPLE){
            // ensure that multiple means at least two
            iNumberOfStatements = ((int)Math.floor(Math.random() * MAX_MUTLIPLE_STATEMENTS))+2;
        }

        // get statement(s)
        var list = new ArrayList<String>();
        while (list.size()<iNumberOfStatements){
            // return an expression as statement?
            // do so: 1. if chance will have it
            //        2. if none of the features returns a statement
            //        3. when requested
            //
            // but: do not return an expression if explicitly not wanted
            boolean bReturnStatement = !(Math.random() < ProjectSettings.CHANCE_OF_EXPRESSION_AS_STATEMENT);
            if (prefs.expression == EStatementPref.NOT_WANTED){
                bReturnStatement = true;
            }
            if (prefs.expression == EStatementPref.REQUIRED){
                bReturnStatement = false;
            }

            // return non-expression statement?
            boolean bStatementsAdded = false;
            if (bReturnStatement) {
                IFeature currentFeature;
                for (int count = 0; count < features.size(); count++) {   // only loop all the features once
                    currentFeature = features.get(iNextFeatureIndex());
                    if (currentFeature instanceof IStatementGenerator statementGenerator) {
                        // adapt preferences to account for the quantity of statements
                        // if multiple statements were requested, the list.size() loop will make sure that there
                        // are at least two statements. Therefor, in this case, if a feature returns only
                        // one statement, it is ok
                        var p2=new StatementPrefs(prefs);
                        if (iNumberOfStatements>1){
                            p2.numberOfStatements=ENumberOfStatementsPref.DON_T_CARE;
                        }
                        else{
                            p2.numberOfStatements=ENumberOfStatementsPref.SINGLE;
                        }

                        // get statement(s) from feature class
                        List<String> temp_list = statementGenerator.getNewStatements(currentDepth, f, p2);

                        // the result may be empty (or null), as the feature class may not be able to comply to
                        // the preferences set, in which case the search must continue
                        if (temp_list!=null) {
                            if (!temp_list.isEmpty()) {
                                list.addAll(temp_list);
                                bStatementsAdded = true;
                                break;
                            }
                        }
                    }
                    // if the loop is complete, all the feature classes have been examined and none of
                    // them has returned a statement or statements.
                    // This shouldn't happen of course, but it might happen during testing and building
                    // and this avoids infinite loops
                }
            }

            // handle expression return (if needed)
            if (!bStatementsAdded){
                // there is no non-expression statement to return (either because of
                // chance, or by lack of features that produce statements)
                // so, return an expression (if allowed)
                if (!(prefs.expression == EStatementPref.NOT_WANTED)){
                    list.add(getNewExpression(currentDepth + 1, getDataType()) + ";\n");
                }
                else{
                    // prevent infinite loops
                    break;
                }
            }

            // as the list will grow, this loop will stop sooner or later
        }
        return list;
    }


    /**
     * Get a function object, returning a function that returns data of a
     * random type.
     * @return  function object
     */
    public Function getFunction(int currentDepth, DataType type) { return getFunction(currentDepth, type, EWithParameters.UNDEFINED); }

    /**
     * Get a function object, making sure the function returns data of the type
     * passed to this method.
     * @param type  describes the data type that the function should return
     * @return  function object; returns null on error.
     */
    public Function getFunction(int currentDepth, DataType type, EWithParameters withParameters) { return getFunction(currentDepth, type, withParameters, null); }
    public Function getFunction(int currentDepth, DataType type, EWithParameters withParameters, IFeature preferredProducer) {
        var mCallableFunctionsByReturnType = callableFunctionsByReturnType;
        if(withParameters != EWithParameters.UNDEFINED){
            mCallableFunctionsByReturnType = withParameters == EWithParameters.YES ? callableFunctionsByReturnTypeWithParameters : callableFunctionsByReturnTypeWithoutParameters;
        }
        // determine whether to create a new function
        //
        // default: only a new function needed when there are no functions at all
        var createNew = mCallableFunctionsByReturnType.isEmpty();
        // when a specific return type is specified, check the existence of such function
        if (type != null && !mCallableFunctionsByReturnType.containsKey(type))
            createNew = true;
        // make sure in a certain percentage of calls a new function is created anyway
        var newFunctionChance = CHANCE_OF_CREATION_OF_A_NEW_FUNCTION * (FUNCTION_TARGET_MAX_AMOUNT - functions.size()) / FUNCTION_TARGET_MAX_AMOUNT;
        if(CHANCE_OF_CREATION_OF_A_NEW_FUNCTION == 1)
            createNew = true;
        if (currentDepth < MAX_EXPRESSION_DEPTH && Math.random() < newFunctionChance)
            createNew = true;

        // if a new function is wanted, make it
        if (createNew) {
            // new function wanted
            var currentFeature = preferredProducer == null ? features.get(iNextFeatureIndex()) : preferredProducer;
            if(preferredProducer != null)
                featureIndex = features.indexOf(preferredProducer);
            for (int count = 0; count < features.size(); count ++) {   // only loop all the features once
                if (currentFeature instanceof IFunctionGenerator functionGenerator) {
                    Function newFunction;

                    //To prevent an infinite loop, we allow features to deliver no more than 1000 unreachable functions in a row.
                    var loopLimit = 100;

                    do {
                        // feature has a function generator, so use it
                        newFunction = functionGenerator.getNewFunction(currentDepth, type, withParameters);

                        // attach prefix to function, so function names will be unique
                        newFunction.setName(currentFeature.getPrefix() + "_" + newFunction.getName());

                        // store function in the two sets
                        addFunction(newFunction);
                    } while(!newFunction.isCallable() && loopLimit-- > 0);

                    if(!newFunction.isCallable())
                        throw new RuntimeException(currentFeature.getClass().getName() + " does not produce a callable function after " + loopLimit + " tries");
                    assert !(newFunction.getName().startsWith("function_")) : "Error in function filename (1)";
                    return newFunction;
                }
                currentFeature = features.get(iNextFeatureIndex());
            }
            // no new function could be constructed
            return null;
        } else {
            // existing function wanted.
            //
            // make sure the parameter object is not affected
            var wantedType = type;
            // if no particular return type is wanted, pick a s random one
            if (wantedType == null) {
                var keys = mCallableFunctionsByReturnType.keySet().stream().toList();
                wantedType = keys.get(ThreadLocalRandom.current().nextInt(0, keys.size()));
            }
            // pick a random function on the basis of the return type
            var r =  mCallableFunctionsByReturnType.get(wantedType).get(ThreadLocalRandom.current().nextInt(0, mCallableFunctionsByReturnType.get(wantedType).size()));
            assert !(r.getName().startsWith("function_")) : "Error in function filename (2)";
            return r;
        }
    }

    /**
     * Get a struct.
     * @return struct object, null in case of error.
     */
    public Struct getStruct() {
        // TODO: Implement getStruct where caller can specify a datatype that must be
        //       included in the struct that is returned

        // slightly refactored to prepare for the TODO

        // default: only create new on empty struct
        boolean createNew=structs.isEmpty();
        if (Math.random() < CHANCE_OF_CREATION_OF_A_NEW_STRUCT)
            createNew=true;
        // TODO: check list for struct with certain datatype

        // if new struct is wanted, create it
        if (createNew) {
            // new struct wanted
            IFeature currentFeature;
            for (int count = 0; count < features.size(); count ++) {   // only loop all the features once
                currentFeature = features.get(iNextFeatureIndex());
                if (currentFeature instanceof IStructGenerator structGenerator) {
                    // feature has struct generator, so use it
                    var newStruct = structGenerator.getNewStruct();
                    // attach prefix to struct name
                    newStruct.prefixName(currentFeature.getPrefix().toString());
                    // add the struct
                    addStruct(newStruct);
                    // and return the result
                    return newStruct;
                }
            }
            // no struct could be created, so return null
            return null;
        } else {
            // no new struct wanted, so return a random one
            return structs.get(ThreadLocalRandom.current().nextInt(0, structs.size()));
        }
    }

    public DataType getRawDataType() {
        return rawDataTypes[ThreadLocalRandom.current().nextInt(0, rawDataTypes.length)];
    }

    public DataType getDataType() {
        if (Math.random() < CHANCE_OF_STRUCT_WHEN_ASKING_FOR_RANDOM_DATATYPE)
            return getStruct();
        return getRawDataType();
    }

    public Variable getGlobal(DataType type) {
        var createNew = false;
        if (globalsByType.isEmpty())
            createNew = true;
        if (Math.random() < ProjectSettings.CHANCE_OF_CREATION_OF_A_NEW_GLOBAL)
            createNew = true;
        if (type != null && !globalsByType.containsKey(type))
            createNew = true;
        if (createNew) {
            IFeature currentFeature;
            for (int count = 0; count < features.size(); count ++) {   // only loop all the features once
                currentFeature = features.get(iNextFeatureIndex());
                if (currentFeature instanceof IGlobalVariableGenerator globalVariableGenerator) {
                    var newGlobalVariable = globalVariableGenerator.getNewGlobalVariable(type);
                    newGlobalVariable.setName(currentFeature.getPrefix() + "_" + newGlobalVariable.getName());
                    if (!globalsByType.containsKey(newGlobalVariable.getType()))
                        globalsByType.put(newGlobalVariable.getType(), new ArrayList<>());
                    globalsByType.get(newGlobalVariable.getType()).add(newGlobalVariable);
                    return newGlobalVariable;
                }
            }
            return null;
        } else {
            if (type == null) {
                var keys = globalsByType.keySet().stream().toList();
                type = keys.get(ThreadLocalRandom.current().nextInt(0, keys.size()));
            }
            return globalsByType.get(type).get(ThreadLocalRandom.current().nextInt(0, globalsByType.get(type).size()));
        }
    }

    /**
     * Get a label that is globally unique.
     * @return  "_LAB*:", where * is a unique sequential number (long type)
     */
    public String getLabel(){
        /*
         Labels in C have a function scope, but as it is not easy to determine
         in which function code is added, we use globally unique labels.
         */
        return "_LAB" + lngNextGlobalLabel++ + ":";
    }
}
