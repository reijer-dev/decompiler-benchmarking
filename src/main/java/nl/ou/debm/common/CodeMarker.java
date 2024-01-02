package nl.ou.debm.common;

import nl.ou.debm.producer.IFeature;

import java.util.HashMap;
import java.util.regex.Pattern;


/**
 * The CodeMarker class essentially serves as a wrapper for a hashmap containing a set
 * of name/value-combinations. The name is the key, the value is the value.
 * The wrapper's added value lies in mapping the map to a String that can be used in the code.
 * The toString-method from the hash-class cannot be used, as it doesn't escape characters it uses
 * to separate fields and values. This wrapper does! It also assures that no double quotes
 * show up in the resulting String, so it can be used as output for a C-function call such as
 * printf("...wrapper_string...");<br>
 * The wrapper also makes sure whenever a value is queried, the return is always a valid String-object,
 * though of course, it may be empty.<br>
 * <br>
 * Using JSON was considered. However: JSON used double quotes, which would have to be escaped manually
 * in order to be able to use the resulting string in C-code. Furthermore, JSON output may be
 * lay-outed (indents and LF's). These would also have to be undone.<br>
 * The outputted string will always start with a publicly available GUID, followed by a feature code. This
 * makes it very easy to distinguish between any string and a code marker string and it also helps to
 * distinguish code makers created by the several features.
 */

public class CodeMarker {

    // constants
    private static final char PROPERTYSEPARATOR =',';       // separates properties
    private static final char VALUESEPARATOR =':';          // separates property name from property value
    private static final char DOUBLEQUOTES ='\"';           // escape necessary to use the toSting-result as a string in C-code
    private static final char ESCAPECHAR ='\\';             // escapes special separator chars
    private static final char[] ESCAPESEQUENCE = {ESCAPECHAR, PROPERTYSEPARATOR, VALUESEPARATOR, DOUBLEQUOTES};
    private static final String STRDECIMALFIELD = "__DECIMAL_FIELD__";  // special field to be used with printf
    private static final String STRFLOATFIELD = "__FLOAT_FIELD__";  // special field to be used with printf
    public static final String STRCODEMARKERGUID = "c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8";  // makes sure this is not an ordinary string, but a code marker string
    private static final String STRHEADEREND = ">>";                                        // end marker for header

    // the actual map, containing all the data
    private final HashMap<String, String> propMap = new HashMap<>();
    //Regex pattern to find code markers from C-code
    private final static HashMap<EFeaturePrefix, Pattern> _patterns = new HashMap<>();

    // ID-fields
    private static long lngNextCodeMarkerID=1;  // keep track of the ID's
    private static final String STRIDFIELD="ID";      // property name for ID field
    private String strFeatureCode = "";                 // feature that created this CodeMarker

    // constructors

    /**
     * Constructor, setting up code marker and including the producing feature's ID-code
     * @param feature   The producer feature class that creates this codemarker
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
    public void clear(){
        String strID=getID();
        propMap.clear();
        propMap.put(STRIDFIELD, strID);
    }

    /**
     * Set a new value for a property (add property if not present yet)
     * Updating ID-field is not possible. Updating feature code is only
     * possible by using SetFeatureCode
     * @param strPropertyName   name of the property
     * @param strPropertyValue  value of the property
     */
    public void setProperty(String strPropertyName, String strPropertyValue){
        if (!strPropertyName.equals(STRIDFIELD)) {
            propMap.put(strPropertyName, strPropertyValue);
        }
    }

    /**
     * Sets a code that identifies the feature that created the marker
     * @param feature   the feature whose prefix is to be used
     */
    public void setFeatureCode(IFeature feature){
        this.strFeatureCode = feature.getPrefix();
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
        propMap.put(STRIDFIELD, Long.toHexString(id));
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
     * Make sure a property is added with "%f" as value. When it comes
     * to making a marker, this part of the string can be used by printf
     * to print a decimal variable
     */
    public void AddFloatField(){
        setProperty(STRFLOATFIELD, "%f");
    }

    /**
     * Get value for a property
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

    public String getID(){
        return propMap.get(STRIDFIELD);
    }

    /**
     * Remove a property from the list (if it was on the list, otherwise nothing happens)
     * @param strPropertyName   name of the property
     */
    public void removeProperty(String strPropertyName){
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
        sb.append(STRCODEMARKERGUID);
        sb.append(strFeatureCode);
        sb.append(STRHEADEREND);
        for (var s : propMap.entrySet()){
            sb.append(strEscapeString(s.getKey()));
            sb.append(VALUESEPARATOR);
            sb.append(strEscapeString(s.getValue()));
            sb.append(PROPERTYSEPARATOR);
        }
        return sb.substring(0, sb.length() - 1);
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
    public void fromString(String strCodedProperties){
        fromString(strCodedProperties, true);
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
    public void fromString(String strCodedProperties, boolean bClearTable){
        // check validity of string
        if (!strCodedProperties.startsWith(STRCODEMARKERGUID)){
            // ignore any non-code-marker
            return;
        }

        // find code marker header end
        int iHeaderEndMarkerPos = strCodedProperties.indexOf(STRHEADEREND);
        if (iHeaderEndMarkerPos<0){
            // ignore any non-code-marker
            return;
        }

        // extract code marker creating feature code
        strFeatureCode = strCodedProperties.substring(STRCODEMARKERGUID.length(), iHeaderEndMarkerPos);
        if (strFeatureCode.isEmpty()){
            // ignore any non-code marker
            return;
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
                propMap.put(strDeEscapeString(v[0]), strDeEscapeString(v[1]));
            }
        }

        // check ID
        String strID = getID();
        if (strID == null){
            // no ID in string. Strange, but possible --> simply set new ID
            setID();
        }
        else{
            // there was an ID in the string. Make sure no conflicts can occur by auto-numbering
            long lID = Long.parseLong(strID, 16);
            if (lngNextCodeMarkerID<=lID) {
                lngNextCodeMarkerID=lID + 1;
            }
        }
    }



    /**
     * Construct a new class and import values directly from a C-statement
     * Returns null when no code marker with this prefix is found
     * @param prefix            prefix to determine the type of codemarker
     * @param cStatement        cStatement that possibly contains a codemarker
     */
    public static CodeMarker findInStatement(EFeaturePrefix prefix, String cStatement){
        var matcher = _patterns.get(prefix).matcher(cStatement);
        return matcher.find() ? EFeaturePrefix.createNewFeaturedCodeMarker(prefix, matcher.group(1)) : null;
    }

    public static boolean isInStatement(EFeaturePrefix prefix, String cStatement){
        return _patterns.get(prefix).matcher(cStatement).find();
    }

    //Precompile regex patterns for all features
    static {
        for(var prefix : EFeaturePrefix.values())
            _patterns.put(prefix, Pattern.compile(".+\\(\"(" + STRCODEMARKERGUID + prefix + ">>.+)\"", Pattern.CASE_INSENSITIVE));
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
     * Return a complete codemarker statement, which comes down to: printf([codeMarker_in_double_quotes]);
     * @return  the appropriate printf-statement
     */
    public String strPrintf(){
        return "printf(\"" + this + "\");";
    }

    public String strPrintfInteger(String strVariableName){
        // make sure a decimal field is added
        AddIntegerField();
        return "printf(\"" + this + "\", " + strVariableName + ");";
    }

    public String strPrintfFloat(String strVariableName){
        // make sure a decimal field is added
        AddFloatField();
        return "printf(\"" + this + "\", " + strVariableName + ");";
    }
}
