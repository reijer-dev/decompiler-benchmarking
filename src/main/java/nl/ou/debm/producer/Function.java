package nl.ou.debm.producer;

import nl.ou.debm.common.IOElements;

import java.util.ArrayList;
import java.util.List;

import static nl.ou.debm.common.Misc.strTrimRight;

public class Function {

    private static long lngFunctionCounter = 0;     // keep track of the number of created functions for autoname

    /** function name*/                                 private String name;
    /** return data type */                             private DataType type;
    /** function parameter list */                      private final List<FunctionParameter> parameters = new ArrayList<>();
    /** function statements */                          private final List<String> statements = new ArrayList<>();
    /** does it have varargs in its parameters*/        private boolean hasVarArgs = false;
    /** is the function callable*/                      private boolean isCallable = true;
    /** use earlier cached function code */             public boolean use_cached_code = true;
    /** earlier cached code */                          private String cached_code = "";
    /** external file name (file_name_only.c) */        private String m_strExternalFileName = "";
    /** don't add automatic code markers */             private boolean m_bBlockAutoStartAndEndMarkers = false;

    /**
     * Construct a function object
     * @param type  function name
     * @param name  function return data type (may be void, may not be empty)
     */
    public Function(DataType type, String name){
        setType(type);
        setName(name);
    }
    public Function(DataType type){
        setType(type);
        setName("function_" + (lngFunctionCounter++));
    }

    // This is very similar to what appendCode does in the beginning.
    // The difference is that the declaration ends with a semicolor instead of a block
    // (the function body) and the parameters are not named.
    public void appendDeclaration(StringBuilder sb) {
        sb.append('\n');                                    // new line
        sb.append(type.getNameForUse());                    // return type
        sb.append(' ');
        sb.append(name);                                    // function name
        sb.append('(');                                     // list parameters

        if(parameters.size() == 0)
            sb.append("void");

        for(var i = 0; i < parameters.size(); i++){
            if(i > 0)
                sb.append(", ");
            sb.append(parameters.get(i).getType().getNameForUse());
        }

        if(hasVarArgs) {
            if(parameters.size() > 0)
                sb.append(", ");
            sb.append("...");
        }

        sb.append(");\n");
    }

    /**
     * Emit the code to a StringBuilder-object that accumulates all the generated c-code parts
     * @param sb_extern    the StringBuilder to which this function's written code will be added.
     */
    public void appendCode(CGenerator generator, StringBuilder sb_extern){
        //emit the same function definition when this method is called a second time. Multiple calls result in slightly different definitions because the codemarkers are re-generated, causing them to have different IDs. If a true re-generation is required, set use_cached_code to false.
        if (use_cached_code && ! cached_code.equals("")) {
            sb_extern.append(cached_code);
            return;
        }

        var sb = new StringBuilder();
        sb.append('\n');                                    // new line
        sb.append(type.getNameForUse());                    // return type
        sb.append(' ');
        sb.append(name);                                    // function name
        sb.append('(');                                     // list parameters

        if(parameters.size() == 0)
            sb.append("void");

        for(var i = 0; i < parameters.size(); i++){
            if(i > 0)
                sb.append(", ");
            parameters.get(i).appendCode(sb);
        }

        if(hasVarArgs) {
            if(parameters.size() > 0)
                sb.append(", ");
            sb.append("...");
        }

        sb.append("){\n");

        if (!m_bBlockAutoStartAndEndMarkers) {
            for (var injector : generator.functionBodyInjectors) {
                var startMarker = injector.appendCodeMarkerAtStart(this);
                startMarker.setAutoGeneratedFlag(true);
                sb.append('\t').append(startMarker.strPrintf()).append('\n');
            }
        }

        var endStatementsPrinted = false;
        if (statements.size() > 0) {
            var lastStatement = statements.get(statements.size() - 1);
            for(var statement : statements){                    // list all statements
                if(statement.equals(lastStatement) && statement.trim().startsWith("return ")) {
                    appendEndStatements(generator,sb);
                    endStatementsPrinted = true;
                }
                sb.append('\t');
                sb.append(statement.replaceAll("\n", "\n\t"));
                sb.append('\n');
            }
        }
        if(!endStatementsPrinted)
            appendEndStatements(generator,sb);

        sb.append("}\n");                                 // close function
        if (use_cached_code) {
            cached_code = sb.toString();
        }
        sb_extern.append(sb);
    }

    private void appendEndStatements(CGenerator generator, StringBuilder sb){
        if (!m_bBlockAutoStartAndEndMarkers) {
            for (var injector : generator.functionBodyInjectors) {
                var endMarker = injector.appendCodeMarkerAtEnd(this);
                endMarker.setAutoGeneratedFlag(true);
                sb.append('\t').append(endMarker.strPrintf()).append('\n');
            }
        }
    }

    // getters & setters
    // =================
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public void setHasVarArgs(boolean hasVarArgs){ this.hasVarArgs = hasVarArgs; }
    public boolean hasVarArgs(){ return this.hasVarArgs; }

    public List<FunctionParameter> getParameters(){
        return parameters;
    }

    // Add new information to function
    public void addParameter(FunctionParameter parameter){
        parameters.add(parameter);
    }

    /**
     * Add single statement to a function.
     * The caller is responsible for the correct syntax of the statement
     * added. That means that the caller must terminate the statement with ; when
     * required by the C standard. It is not forbidden to add multiple statements
     * in one single string, but it is recommended to use a list as parameter.
     * @param statement     String representing the statement to be added.
     */
    public void addStatement(String statement){
        statements.add(strTrimRight(statement));
    }

    /**
     * see: {@link Function#addStatements(List)}
     * @param newStatements String list containing new statements
     */
    public void addStatements(List<String> newStatements) {
        for (var statement : newStatements) {
            statements.add(strTrimRight(statement));
        }
    }

    public boolean isCallable() {
        return isCallable;
    }

    public void setCallable(boolean callable) {
        isCallable = callable;
    }

    /**
     * Mark this function as belonging to an external c-file, rather than to the main.c-file
     * @param strExternalFileName Name of the external c-file. Do not add a path to the name (it will
     *                            be removed). You may add .c to the file name, but if you don't
     *                            this function will do it for you. Null or empty means this
     *                            function will belong to main.c
     */
    public void setExternalFileName(String strExternalFileName){
        if (strExternalFileName==null){
            m_strExternalFileName="";
        }
        m_strExternalFileName = IOElements.strGetFilenameWithDefaultExtension(strExternalFileName, ".c");
    }

    /**
     * Get this function's external filename value. Empty if it belongs to main.c.
     * @return empty or valid file name ending with .c
     */
    public String strGetExternalFileName(){
        return m_strExternalFileName;
    }

    public boolean bGetBlockAutoStartAndEndMarkers(){
        return m_bBlockAutoStartAndEndMarkers;
    }

    public void setBlockAutoStartAndEnMarkers(boolean bBlock){
        m_bBlockAutoStartAndEndMarkers = bBlock;
    }
}
