package nl.ou.debm.common;

import nl.ou.debm.common.antlr.*;
import nl.ou.debm.producer.IFeature;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;


/**
 * The CodeMarker class essentially serves as a wrapper for a hashmap containing a set
 * of name/value-combinations. The name is the key, the value is the value.
 * The wrapper's added value lies in mapping the map to a String that can be used in the code.
 * The toString-method from the hash-class cannot be used, as it doesn't escape characters it uses
 * to separate fields and values. This wrapper does! It also assures that no double quotes
 * show up in the resulting String, so it can be used as output for a C-function call such as
 * printf("...wrapper_string...");<br>
 * Some decompilers do not print the entire string in their decompiled c files, for example
 * BinaryNinja (online version) prints this: 'printf("c5852db2-7acb-cba3-7f81-e7ef3cd1…");' As we have no
 * general way of predicting /where/ the cut is made, we cannot simply be sure that we have a complete
 * code marker object. We therefor put a test sum at the end of the marker.<br>
 * The wrapper also makes sure whenever a value is queried, the return is always a valid String-object,
 * though of course, it may be empty.<br>
 * <br>
 * Using JSON was considered. However: JSON used double quotes, which would have to be escaped manually
 * in order to be able to use the resulting string in C-code. Furthermore, JSON output may be
 * lay-outed (indents and LF's). These would also have to be undone.<br>
 * The outputted string will always start with a publicly available GUID, followed by a feature code. This
 * makes it very easy to distinguish between any string and a code marker string, and it also helps to
 * distinguish code makers created by the several features.<br>
 * <br>
 * CodeMarker is abstract, so direct property access is shielded. It is recommended to write a child class
 * specific to the feature using the CodeMarker, so that child class can set and get properties, hiding
 * the property names in constants in the child class.<br>
 */

public abstract class CodeMarker {

    // constants
    // ---------
    /** separates properties */                                                 private static final char PROPERTYSEPARATOR =',';
    /** separates property name from property value */                          private static final char VALUESEPARATOR =':';
    /** escape necessary to use the toSting-result as a string in C-code */     private static final char DOUBLEQUOTES ='\"';
    /** escapes special separator chars */                                      private static final char ESCAPECHAR ='\\';
    /** escape characters in proper working order */                            private static final char[] ESCAPESEQUENCE = {ESCAPECHAR, PROPERTYSEPARATOR, VALUESEPARATOR, DOUBLEQUOTES};
    /** special field to be used with printf */                                 private static final String STRDECIMALFIELD = "__DECIMAL_FIELD__";
    /** special field to be used with printf */                                 private static final String STRFLOATFIELD = "__FLOAT_FIELD__";
    /** special field to be used with printf */                                 private static final String STRPOINTERFIELD = "__POINTER_FIELD__";
    /** field to determine whether the code marker was autogenerated */         private static final String STRAUTOGEN = "AUTOGENERATED";
    /** checksum for integrity purposes */                                      private static final String STRCHECKSUM = "CHECKSUM";
    /** makes sure this is not an ordinary string, but a code marker string */  public static final String STRCODEMARKERGUID = "c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8";
    /** end marker for header */                                                private static final String STRHEADEREND = ">>";
    /** printf external function name */                                        public static final String STREXTERNALPRINTF = "__CM_printf";
    /** printf external function name + int */                                  public static final String STREXTERNALPRINTF_INT = "__CM_printf_int";
    /** printf external function name + float */                                public static final String STREXTERNALPRINTF_FLOAT = "__CM_printf_float";
    /** printf external function name + float */                                public static final String STREXTERNALPRINTF_PTR = "__CM_printf_ptr";
    /** printf external functions file name */                                  public static final String STREXTERNALFILE = "codemarkers.c";

    /** the actual map, containing all the data */                              private final HashMap<String, String> propMap = new HashMap<>();
    /** Regex pattern to find code markers from C-code */                       private final static HashMap<EFeaturePrefix, Pattern> _C_patterns = new HashMap<>();
    /** Regex pattern to find code markers from string literals */              private final static HashMap<EFeaturePrefix, Pattern> _StringLit_patterns = new HashMap<>();
    /** Regex pattern to find code markers from LLVM-code */                    private final static HashMap<EFeaturePrefix, Pattern> _LLVM_patterns = new HashMap<>();

    // ID-fields
    // ---------
    /** keep track of the ID's */
    private static long lngNextCodeMarkerID=1;
    /** property name for ID field */
    private static final String STRIDFIELD="ID";
    /** feature that created this CodeMarker */
    private String strFeatureCode = "";

    /**
     * This class serves as a struct. It contains data about code markers found in an LLVM-IR file
     */
    public static class CodeMarkerLLVMInfo{
        /**
         * Only constructor.
         * @param cm CodeMarker object that represents the code marker found
         */
        public CodeMarkerLLVMInfo(CodeMarker cm){
            codeMarker = cm;
        }
        /** the code marker object */
        public final CodeMarker codeMarker;
        /** the number of times this CM occurs in code */
        public long iNOccurrencesInLLVM = 0;
        /** // non-duplicate array of function names in which it occurs */
        public List<String> strLLVMFunctionNames = new ArrayList<>();
        public String strLLVMID = "";

        @Override
        public String toString(){
            return "N=" + Misc.strGetNumberWithPrefixZeros(iNOccurrencesInLLVM, 2) + ", " + strLLVMID + ", " + codeMarker.strPrintf() + ", func(s): " + strLLVMFunctionNames;
        }
    }

    /**
     * This is a private class to have a walk through the LLVM-file, using ANTLR. As it is
     * specific to the CodeMarker code, it is implemented here
     */
    private static class CodeMarkerLLVMListener extends LLVMIRBaseListener {
        static private final Object lockObj = new Object();


        /**
         * Perform the search on a given tree, representing the LLVM file.
         * @param parser  parser to be searched
         */
        public static void DoTheSearch(LLVMIRParser parser, Map<Long, CodeMarkerLLVMInfo> out, Map<String, Long> IDMap_out){
            assert out!=null : "Forgot to initialize return map! (1)";
            assert IDMap_out!=null : "Forgot to initialize return map! (2)";

            synchronized (lockObj) {

                out.clear();
                IDMap_out.clear();

                // redirect stderr -- we are not interested in LLVM parser errors, because they do not
                // interfere with our search, and they are not the result of the decompiler
                var defaultStdErr = System.err;
                PrintStream myStdErr = null;
                File stdErrFilename = null;
                try {
                    stdErrFilename = Files.createTempFile("ReroutedStdErr", ".txt").toFile();
                    myStdErr = new PrintStream(new FileOutputStream(stdErrFilename.getPath()));
                    System.setErr(myStdErr);
                } catch (Exception ignore) {
                }

                var tree = parser.compilationUnit();
                var walker = new ParseTreeWalker();
                var listener = new CodeMarkerLLVMListener(out);
                // the listener needs to do multiple passes, because LLVM allows global string definitions
                // and function definitions to be mixed
                while (listener.bSearchAgain()) {
                    parser.reset();
                    walker.walk(listener, tree);
                }

                // close redirected file
                if (myStdErr != null) {
                    myStdErr.flush();
                    myStdErr.close();
                }

                // undo redirection
                System.setErr(defaultStdErr);

                // remove temp file
                if (stdErrFilename != null) {
                    IOElements.deleteFile(stdErrFilename.getPath());
                }

                // make sure that all the function names have no doubles
                for (var item : listener.m_InfoMap.entrySet()) {
                    item.getValue().strLLVMFunctionNames = new ArrayList<>(new LinkedHashSet<>(item.getValue().strLLVMFunctionNames));
                }
                // and return the lot
                out.putAll(listener.m_InfoMap);
                IDMap_out.putAll(listener.m_L2CMIdentifierMap);
            }
        }

        /** to ensure that a call within a call would not be a problem */   private int m_iCallInstructionNestingLevel = 0;
        /** pass control, ignore global identifiers */                      private boolean m_bLeaveGlobalIdentifiers = true;
        /** pass control, ignore function calls */                          private boolean m_bLeaveFunctionCalls = true;
        /** pass control, search state */                                   private int m_iCurrentSearchState = 0;
        /** output, mapped by code marker ID */                             private final Map<Long, CodeMarkerLLVMInfo> m_InfoMap;
        /** map LLVM identifiers to code marker identifiers */              private final Map<String, Long> m_L2CMIdentifierMap = new HashMap<>();
        /** keep track of function currently worked in */                   private String m_strCurrentFunctionName;
        /** keep track of globals, used in locals */                        private final Map<String, List<String>> m_locVarMap = new HashMap<>();

        /**
         * only constructor, sets map to be used for output
         * @param map  output map
         */
        private CodeMarkerLLVMListener(Map<Long, CodeMarkerLLVMInfo> map){
            m_InfoMap = map;
            map.clear();
        }

        /**
         * to be used in search loop; see static function using it for example
         * @return true if another walk is needed, false if otherwise
         */
        private boolean bSearchAgain(){
            if (m_iCurrentSearchState>=2){
                return false;
            }
            m_iCurrentSearchState++;
            m_bLeaveGlobalIdentifiers = (m_iCurrentSearchState != 1);
            m_bLeaveFunctionCalls = (m_iCurrentSearchState != 2);
            return true;
        }


        /**
         * callback for entering phi-instructions, makes sure to map local variables to global variables
         * @param ctx the parse tree
         */
        @Override
        public void enterPhiInst(LLVMIRParser.PhiInstContext ctx) {
            super.enterPhiInst(ctx);

            // only search in call instructions in correct phase
            if (m_bLeaveFunctionCalls){
                return;
            }

            // get local var name
            String strLocalVarName = ctx.parent.parent.getChild(0).getText();
            List<String> refTab = null;

            // extract globals
            String strTheLot =  ctx.getText();
            int p=-1;
            while (true) {
                // look for next global
                p = strTheLot.indexOf('@', p + 1);
                if (p == -1) {
                    break;
                }
                int p2 = p + 1;
                while (p2 < strTheLot.length()) {
                    char c = strTheLot.charAt(p2);
                    if (!((Character.isLetterOrDigit(c)) ||
                            (c == '-') ||
                            (c == '$') ||
                            (c == '.') ||
                            (c == '_'))) {
                        break;
                    }
                    ++p2;
                }
                // make ref tab when needed
                if (refTab == null) {
                    refTab = new ArrayList<>();
                    m_locVarMap.put(strLocalVarName, refTab);
                }
                // add global to map
                refTab.add(strTheLot.substring(p, p2));
            }
        }

        /**
         * mark a call instruction entered. The mark is used elsewhere.
         * @param ctx the parse tree
         */
        @Override
        public void enterCallInst(LLVMIRParser.CallInstContext ctx) {
            super.enterCallInst(ctx);

            // only search in call instructions in correct phase
            if (m_bLeaveFunctionCalls){
                return;
            }

            // internal mark: we are in a call instruction
            m_iCallInstructionNestingLevel++;
        }

        /**
         * un-mark a call instruction entered; mark is used elsewhere
         * @param ctx the parse tree
         */
        @Override
        public void exitCallInst(LLVMIRParser.CallInstContext ctx) {
            super.exitCallInst(ctx);

            // only search in call instructions in correct phase
            if (m_bLeaveFunctionCalls){
                return;
            }
            // internal mark: we are no longer in the last call instruction
            m_iCallInstructionNestingLevel--;
        }

        /**
         * keep track of current definition function name
         * @param ctx the parse tree
         */
        @Override
        public void enterFuncDef(LLVMIRParser.FuncDefContext ctx) {
            super.enterFuncDef(ctx);

            // only do when necessary
            if (m_bLeaveFunctionCalls){
                return;
            }

            m_strCurrentFunctionName = ctx.funcHeader().GlobalIdent().getText();
            m_locVarMap.clear();    // clear local variable table
        }

        /**
         * Try to find global identifiers being used in a function call
         * @param ctx the parse tree
         */
        @Override
        public void enterEveryRule(ParserRuleContext ctx) {
            super.enterEveryRule(ctx);

            // only 1+ when marked by enterCallInst, so no
            // need for phase testing, as enterCallInst does that already
            if (m_iCallInstructionNestingLevel <1){
                return;
            }

            // use information (global identifiers)
            var x = ctx.getTokens(LLVMIRLexer.GlobalIdent);
            for (var item: x){
                processIdentifier(item.getText());
            }

            // local identifiers
            if (ctx.getText().startsWith("%")){
                var refList = m_locVarMap.get(ctx.getText());
                if (refList != null){
                    for (var item : refList){
                        processIdentifier(item);
                    }
                }
            }
        }

        private void processIdentifier(String LLVM_ID) {
            Long CM_ID = m_L2CMIdentifierMap.get(LLVM_ID);
            if (CM_ID != null) {
                // the LLVM_ID is in our map, so we process the wanted data
                var ci = m_InfoMap.get(CM_ID);
                ci.iNOccurrencesInLLVM++;
                ci.strLLVMFunctionNames.add(m_strCurrentFunctionName);
            }
        }

        /**
         * Make a map of all global definitions that have code markers
         * @param ctx the parse tree
         */
        @Override
        public void enterGlobalDef(LLVMIRParser.GlobalDefContext ctx) {
            super.enterGlobalDef(ctx);

            // only search in globals in correct phase
            if (m_bLeaveGlobalIdentifiers){
                return;
            }

            // check if global definition contains a code marker /any/ code marker
            var gcm = CodeMarker.findInGlobalDef(ctx.getText());
            if (gcm==null){
                return;
            }

            // remember global identifier and code marker ID
            m_L2CMIdentifierMap.put(ctx.GlobalIdent().toString(), gcm.lngGetID());
            // set up the corresponding code marker object
            var cmi = new CodeMarkerLLVMInfo(gcm);
            cmi.strLLVMID = ctx.GlobalIdent().toString();
            m_InfoMap.put(gcm.lngGetID(), cmi);
        }
    }

    // constructors

    /**
     * Constructor, setting up code marker and including the producing feature's ID-code
     * @param feature   The producer feature class that creates this code marker
     */
    public CodeMarker(IFeature feature){
        // set ID
        setID();
        // set feature code
        setFeatureCode(feature);
    }

    /**
     * Constructor, setting up code marker and including a specified feature prefix
     * @param prefix   Prefix to be used
     */
    public CodeMarker(EFeaturePrefix prefix){
        // set ID
        setID();
        // set feature code
        setFeatureCode(prefix);
    }

    /**
     * Construct a new class and import values directly from the string.
     * When provided, the marker's ID is copied from string. If not, a new
     * one is provided.
     * @param strCodedProperties    see: {@link #fromString(String)}
     */
    public CodeMarker(String strCodedProperties){
        // construct directly from string
        fromString(strCodedProperties);
    }

    /**
     * Construct a new class and import values directly from another code marker.
     * The new copy will have a unique ID.
     * @param codeMarker            property source
     */
    public CodeMarker(final CodeMarker codeMarker){
        // copy constructor
        fromCodeMarker(codeMarker);
        setID();
        this.strFeatureCode = codeMarker.strFeatureCode;
    }

    // hashmap access

    /**
     * Clear property table
     */
    protected void clear(){
        long lngID = lngGetID();
        propMap.clear();
        setID(lngID);
    }

    /**
     * Set a new value for a property (add property if not present yet)
     * Updating ID-field is not possible. Updating feature code is only
     * possible by using SetFeatureCode
     * @param strPropertyName   name of the property
     * @param strPropertyValue  value of the property
     */
    protected void setProperty(String strPropertyName, String strPropertyValue){
        if (!strPropertyName.equals(STRIDFIELD) &&
            !strPropertyName.equals(STRCHECKSUM)) {
            propMap.put(strPropertyName, strPropertyValue);
        }
    }

    /**
     * check whether or not property is present in the map
     * @param strPropertyName property name to be checked
     * @return true is present, otherwise false
     */
    protected boolean bPropertyPresent(String strPropertyName){
        return propMap.containsKey(strPropertyName);
    }

    /**
     * Sets a code that identifies the feature that created the marker
     * @param feature   the feature whose prefix is to be used
     */
    public void setFeatureCode(IFeature feature){
        this.strFeatureCode = feature.getPrefix().toString();
    }

    /**
     * Sets a code that identifies the feature that created the marker
     * @param prefix   the prefix to be used
     */
    public void setFeatureCode(EFeaturePrefix prefix){
        this.strFeatureCode = prefix.toString();
    }

    private void setID(){
        long id = lngNextCodeMarkerID++;
        setID(id);
    }
    private void setID(long lngID){
        propMap.put(STRIDFIELD, Long.toHexString(lngID));
    }

    public void setAutoGeneratedFlag(boolean bAutomaticallyGenerated){
        if (bAutomaticallyGenerated){
            propMap.put(STRAUTOGEN,"T");
        }
        else {
            propMap.remove(STRAUTOGEN);
        }
    }

    public boolean bGetAutoGeneratedFlag(){
        return propMap.containsKey(STRAUTOGEN);
    }

    /**
     * Make sure a property is added with "%d" as value. When it comes
     * to making a marker, this part of the string can be used by printf
     * to print a decimal variable
     */
    public void AddIntegerField(){
        setProperty(STRDECIMALFIELD, "%d");
    }

    /**
     * Make sure a property is added with "%d" as value. When it comes
     * to making a marker, this part of the string can be used by printf
     * to print a pointer value
     */
    public void AddPointerField(){
        setProperty(STRPOINTERFIELD, "%p");
    }

    /**
     * Make sure a property is added with "%f" as value. When it comes
     * to making a marker, this part of the string can be used by printf
     * to print a floating point variable
     */
    public void AddFloatField(){
        setProperty(STRFLOATFIELD, "%f");
    }

    /**
     * Get value for a property.
     * @param strPropertyName   name of the property
     * @return                  value of the property. If not set, it returns an empty string ("")
     */
    public String strPropertyValue(String strPropertyName){
        String out = propMap.get(strPropertyName);
        if (out == null){
            return "";
        }
        return out;
    }

    public Long lngGetID(){
        return Misc.lngRobustHexStringToLong(propMap.get(STRIDFIELD));
    }

    /**
     * Remove a property from the list (if it was on the list, otherwise nothing happens)
     * @param strPropertyName   name of the property
     */
    protected void removeProperty(String strPropertyName){
        if (!strPropertyName.equals(STRIDFIELD)){
            propMap.remove(strPropertyName);
        }
    }

    /**
     * Get number of properties
     * @return  number of properties
     */
    public int iNProperties(){
        return propMap.size();
    }

    /**
     * Map the properties to one single string, make sure that the proper characters are escaped.
     * @return      string containing property information. Use as input for {@link #fromString(String)}
     */
    public String toString(){
        var sb = new StringBuilder();
        // add header
        sb.append(STRCODEMARKERGUID);
        sb.append(strFeatureCode);
        sb.append(STRHEADEREND);
        // make temp map, so we can add properties in a specific order
        // we first add the code marker ID, then other ID-fields, then we add the other fields
        Map<String, String> pm = new HashMap<>(propMap);
        // add ID
        addPropToStringBuilder(STRIDFIELD, pm.get(STRIDFIELD), sb);
        pm.remove(STRIDFIELD);
        // add all properties containing "ID" in capitals
        final List<String> rml = new ArrayList<>();
        for (var s : pm.entrySet()){
            if (s.getKey().contains("ID")) {
                addPropToStringBuilder(s.getKey(), s.getValue(), sb);   // add to string
                rml.add(s.getKey());                                    // remember property name to remove from temporary map
            }
        }
        // remove all 'ID'-fields from temporary map
        for (var rmk : rml){
            pm.remove(rmk);
        }
        // add remaining properties
        for (var s : pm.entrySet()){
            sb.append(strEscapeString(s.getKey()));
            sb.append(VALUESEPARATOR);
            sb.append(strEscapeString(s.getValue()));
            sb.append(PROPERTYSEPARATOR);
        }
        // add checksum
        int iChecksum = Misc.iCalcCRC16(sb.substring(0, sb.length()-1));
        sb.append(strEscapeString(STRCHECKSUM));
        sb.append(VALUESEPARATOR);
        sb.append(strEscapeString(Misc.strGetHexNumberWithPrefixZeros(iChecksum,4)));
        // return result
        return sb.toString();
    }

    /**
     * helper function; add a property name and value, the necessary separators and escape the lot
     * @param strProp property name
     * @param strVal property value
     * @param sb stringbuilder to add them to
     */
    private void addPropToStringBuilder(String strProp, String strVal, StringBuilder sb){
        sb.append(strEscapeString(strProp));
        sb.append(VALUESEPARATOR);
        sb.append(strEscapeString(strVal));
        sb.append(PROPERTYSEPARATOR);
    }

    /**
     * Initialize table from a given string. Thus, a marker that was created and exported
     * using toString (@see toString) can be converted back to a property list, making it
     * easier to query the properties and their values.
     * The current list of properties is thrown out.
     * This function ignores any string that does not start with the code marker GUID.
     * @param strCodedProperties    String containing the property information. Use the output of
     *                              {@link #toString()}
     */
    public boolean fromString(String strCodedProperties){
        return fromString(strCodedProperties, true);
    }

    /**
     * Initialize table from a given string. Thus, a marker that was created and exported
     * using toString (@see toString) can be converted back to a property list, making it
     * easier to query the properties and their values.
     * The current list of properties is only thrown out if flag to clear table is set. If
     * the flag is not set, the imported information is simply applied to the existing data,
     * adding or updating properties.
     * This function ignores any string that does not start with the code marker GUID.
     * @param strCodedProperties    see {@link #fromString(String)}
     * @param bClearTable           true means table is cleared before processing
     */
    public boolean fromString(String strCodedProperties, boolean bClearTable){
        // as strings may be concatenated in C, the string presented could contain "", indicating a concatenation
        // these can be safely removed, as our class always escapes "-chars, so they can't be ours anyway
        strCodedProperties = strCodedProperties.replaceAll("\"\"", "");

        // check validity of string
        if (!strCodedProperties.startsWith(STRCODEMARKERGUID)){
            // ignore any non-code-marker
            return false;
        }

        // check if checksum is present
        String strTotalChecksum = strCodedProperties.substring(strCodedProperties.length()-STRCHECKSUM.length()-5);
        if (!strTotalChecksum.startsWith(STRCHECKSUM)){
            // checksum not present
            return false;
        }

        // check checksum value
        int iCalculatedChecksum = Misc.iCalcCRC16(strCodedProperties.substring(0, strCodedProperties.length()-strTotalChecksum.length()-1));
        int iWantedChecksum = (int)Misc.lngRobustHexStringToLong(strTotalChecksum.substring(strTotalChecksum.length()-4));
        if (iCalculatedChecksum != iWantedChecksum){
            // checksum not correct
            return false;
        }

        // find code marker header end
        int iHeaderEndMarkerPos = strCodedProperties.indexOf(STRHEADEREND);
        if (iHeaderEndMarkerPos<0){
            // ignore any non-code-marker
            return false;
        }

        // extract code marker creating feature code
        strFeatureCode = strCodedProperties.substring(STRCODEMARKERGUID.length(), iHeaderEndMarkerPos);
        if (strFeatureCode.isEmpty()){
            // ignore any non-code marker
            return false;
        }

        // clear map
        if (bClearTable) {
            clear();
        }

        // split at properties level
        var p = strCodedProperties.substring(iHeaderEndMarkerPos + STRHEADEREND.length()).split("" + PROPERTYSEPARATOR );
        for (var prop : p){
            // split name/value
            var v = prop.split("" + VALUESEPARATOR);
            // only do something if there are exactly two entries
            if (v.length == 2){
                // and do not add checksum
                if (!v[0].equals(STRCHECKSUM)) {
                    propMap.put(strDeEscapeString(v[0]), strDeEscapeString(v[1]));
                }
            }
        }

        // check ID
        if (!bPropertyPresent(STRIDFIELD)){
            // no ID in string. Strange, but possible --> simply set new ID
            setID();
        }
        else{
            // there was an ID in the string. Make sure no conflicts can occur by auto-numbering
            long lID = lngGetID();
            if (lngNextCodeMarkerID<=lID) {
                lngNextCodeMarkerID=lID + 1;
            }
        }
        return true;
    }

    /**
     * Construct a new class and import values directly from a C-statement
     * Returns null when no code marker with this prefix is found
     * @param prefix            prefix to determine the type of code marker
     * @param cStatement        cStatement that possibly contains a code marker
     */
    public static CodeMarker findInStatement(EFeaturePrefix prefix, String cStatement){
        if(!cStatement.contains(prefix.toString() + ">>"))
            return null;

        var matcher = _C_patterns.get(prefix).matcher(cStatement);
        if (matcher.find())
            return EFeaturePrefix.createNewFeaturedCodeMarker(prefix, matcher.group(1));
        return null;
    }

    /**
     * Construct a new object and import values from a post-fix expression
     * Returns null when no code marker with this prefix is found (including: no code marker at all)
     * @param ctx       context to search
     * @param prefix    prefix to determine the type of the code marker
     * @return          the appropriate code marker object, may be null
     */
    public static CodeMarker findInPostFixExpression(CParser.PostfixExpressionContext ctx, EFeaturePrefix prefix) {
        // do a quick scan, try to find the prefix
        String strToFind = prefix.toString();
        String strContext = ctx.getText();
        if (!strContext.contains(strToFind)) {
            // prefix is 2 chars, try to find them with a "" in the middle (this can happen if
            // there is a cutoff just in between te prefix)
            // and better be safe than sorry: longer prefixes are checked for every position ;-)
            boolean bFound = false;
            for (int i = 1; i < strToFind.length(); ++i) {
                if (strContext.contains(strToFind.substring(0, i) + "\"\"" + strToFind.substring(i))) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                return null;
            }
        }

        // get a list of terminal nodes
        return findInListOfTerminalNodes(Misc.getAllTerminalNodes(ctx, true), prefix);
    }

    /**
     * Construct an appropriate code marker object from a list of terminal nodes. It looks for a pattern
     * [identifier] ( [string literal] [...] )
     * @param nodes nodelist
     * @param prefix prefix for the searched code marker
     * @return the wanted object, or null input is no code marker
     */
    public static CodeMarker findInListOfTerminalNodes(List<Misc.ANTLRParsedElement> nodes, EFeaturePrefix prefix){
        // we need at least 4 nodes: identifier ( stringLit )
        if (nodes.size()<4){
            return null;
        }

        // the first node must be an identifier
        if (nodes.get(0).iTokenID != CLexer.Identifier){
            return null;
        }

        // the second node must be a (
        if (nodes.get(1).iTokenID != CLexer.LeftParen){
            return null;
        }

        // the last node must be a )
        if (nodes.get(nodes.size()-1).iTokenID != CLexer.RightParen){
            return null;
        }

        // if the third node is a string literal: check it
        if (nodes.get(2).iTokenID == CLexer.StringLiteral){
            return MatchCodeMarkerStringLiteral(nodes.get(2).strText, prefix);
        }

        return null;
    }

    /**
     * Construct a new class and import values directly from a LLVM-declaration
     * Returns null when no code marker with this prefix is found
     * @param prefix            prefix to determine the type of code marker
     * @param strGlobalDefinition    definition that possibly contains a code marker
     */
    public static CodeMarker findInGlobalDef(EFeaturePrefix prefix, String strGlobalDefinition){
        var matcher = _LLVM_patterns.get(prefix).matcher(strGlobalDefinition);
        return matcher.find() ? EFeaturePrefix.createNewFeaturedCodeMarker(prefix, strStripFrays(matcher.group())) : null;
    }
    /**
     * Construct a new class and import values directly from a LLVM-declaration
     * Returns null when no code marker is found. All different code markers are tried.
     * @param strGlobalDefinition    definition that possibly contains a code marker
     */
    public static CodeMarker findInGlobalDef(String strGlobalDefinition){
        for (var prefix : EFeaturePrefix.values()) {
            var matcher = _LLVM_patterns.get(prefix).matcher(strGlobalDefinition);
            if (matcher.find()) {
                return EFeaturePrefix.createNewFeaturedCodeMarker(prefix, strStripFrays(matcher.group()));
            }
        }
        return null;
    }

    /**
     * Return a code marker object on the basis of a quoted code marker string
     * @param strCandidate string literal that may be a code marker
     * @param prefix the type of code marker we are looking for
     * @return the appropriate code marker object, or null if no code marker was passed
     */
    public static CodeMarker MatchCodeMarkerStringLiteral(String strCandidate, EFeaturePrefix prefix){
        var matcher = _StringLit_patterns.get(prefix).matcher(strCandidate);
        if (matcher.find()) {
            return EFeaturePrefix.createNewFeaturedCodeMarker(prefix, matcher.group(1));
        }
        return null;
    }

    /**
     * Aux routine to strip of some characters not wanted after a search
     */
    private static String strStripFrays(String strIn){
        return strIn.substring(1, strIn.length()-4);
    }

    public static boolean isInStatement(EFeaturePrefix prefix, String cStatement){
        return _C_patterns.get(prefix).matcher(cStatement).find();
    }

    //Precompile regex patterns for all features
    static {
        for(var prefix : EFeaturePrefix.values()) {
            _C_patterns.put(prefix, Pattern.compile(".+\\([^\"]*\"(" + STRCODEMARKERGUID + prefix + ">>.+" + STRCHECKSUM + ".+)\"", Pattern.CASE_INSENSITIVE));
            _StringLit_patterns.put(prefix, Pattern.compile("\"(" + STRCODEMARKERGUID + prefix + ">>.+" + STRCHECKSUM + ".+)\"", Pattern.CASE_INSENSITIVE));
            _LLVM_patterns.put(prefix, Pattern.compile( "\"" + STRCODEMARKERGUID + prefix + ">>.+"  + STRCHECKSUM + ".+\\Q\\\\E00\"", java.util.regex.Pattern.CASE_INSENSITIVE));
        }
    }

    /**
     * Get information on code markers in a parsed LLVM file.
     * @param lparser the parser representing the data
     * @param llvmInfo_out output map; is cleared before adding output
     */
    public static void getCodeMarkerInfoFromLLVM(LLVMIRParser lparser, Map<Long, CodeMarkerLLVMInfo> llvmInfo_out,
                                                                       Map<String, Long> LLVMtoCMID_out){
        CodeMarkerLLVMListener.DoTheSearch(lparser, llvmInfo_out, LLVMtoCMID_out);
    }

    /**
     * Copy all the date from another CodeMarker object
     * @param originalCodeMarker    data source
     */
    public void fromCodeMarker(final CodeMarker originalCodeMarker){
        fromString(originalCodeMarker.toString());
    }

    /**
     * Make sure escape characters are put in place
     * @param strIn     raw string input
     * @return          escaped string
     */
    private String strEscapeString(String strIn){
        for (int p=0; p<ESCAPESEQUENCE.length ; ++p){
            strIn = strIn.replace("" + ESCAPESEQUENCE[p], "" + ESCAPECHAR + p);
        }
        return strIn;
    }

    /**
     * Make sure escape characters are replaced by their originals
     * @param strIn     escaped string input
     * @return          raw string output
     */
    private String strDeEscapeString(String strIn){
        for (int p=ESCAPESEQUENCE.length-1; p>=0 ; --p){
            strIn = strIn.replace( "" + ESCAPECHAR + p, "" + ESCAPESEQUENCE[p]);
        }
        return strIn;
    }

    /**
     * Return a complete code marker statement, which comes down to: printf([codeMarker_in_double_quotes]);
     * @return  the appropriate printf-statement
     */
    public String strPrintf(){
        return STREXTERNALPRINTF + "(\"" + this + "\");";
    }

    /**
     * Return a complete code marker statement, printing the code marker and an integer variable,
     * which comes down to: printf([codeMarker_in_double_quotes], int_var_name);
     * @param strVariableName name of the variable to be printed
     * @return  the appropriate printf-statement
     */
    public String strPrintfInteger(String strVariableName){
        // make sure a decimal field is added
        AddIntegerField();
        return STREXTERNALPRINTF_INT + "(\"" + this + "\", " + strVariableName + ");";
    }

    /**
     * Return a complete code marker statement, printing the code marker and a float variable,
     * which comes down to: printf([codeMarker_in_double_quotes], int_var_name);
     * @param strVariableName name of the variable to be printed
     * @return  the appropriate printf-statement
     */
    public String strPrintfFloat(String strVariableName){
        // make sure a decimal field is added
        AddFloatField();
        return STREXTERNALPRINTF_FLOAT + "(\"" + this + "\", " + strVariableName + ");";
    }

    /**
     * Return a complete code marker statement, printing the code marker and a float variable,
     * which comes down to: printf([codeMarker_in_double_quotes], int_var_name);
     * @param ptrExpr memory address expression such as a variable name, &varname etc.
     * @return  the appropriate printf-statement
     */
    public String strPrintfPtr(String ptrExpr){
        // make sure a decimal field is added
        AddPointerField();
        return STREXTERNALPRINTF_PTR + "(\"" + this + "\", " + ptrExpr + ");";
    }
}
