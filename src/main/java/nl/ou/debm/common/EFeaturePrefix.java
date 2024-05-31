package nl.ou.debm.common;

import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.feature1.LoopAssessor;
import nl.ou.debm.common.feature1.LoopCodeMarker;
import nl.ou.debm.common.feature1.LoopProducer;
import nl.ou.debm.common.feature2.DataStructureAssessor;
import nl.ou.debm.common.feature2.DataStructureProducer;
import nl.ou.debm.common.feature3.FunctionAssessor;
import nl.ou.debm.common.feature3.FunctionCodeMarker;
import nl.ou.debm.common.feature3.FunctionProducer;
import nl.ou.debm.common.feature4.GeneralDecompilerPropertiesAssessor;
import nl.ou.debm.common.feature5.IndirectionsAssessor;
import nl.ou.debm.common.feature5.IndirectionsProducer;
import nl.ou.debm.producer.CGenerator;
import nl.ou.debm.producer.IFeature;

/**
 *     enumerate feature prefixes
 *     this enables testing for duplicates and reversing a code to a constant
 */
public enum EFeaturePrefix {
    CONTROLFLOWFEATURE, DATASTRUCTUREFEATURE, FUNCTIONFEATURE, METADATA, INDIRECTIONSFEATURE, GENERALDECOMPILERPROPERTIES;

    /**
     * This code is used for the code markers
     * @return a two-digit code marker code
     */
    @Override
    public String toString() {
        switch (this){
            case FUNCTIONFEATURE ->             { return "FF"; }
            case DATASTRUCTUREFEATURE ->        { return "DS"; }
            case CONTROLFLOWFEATURE ->          { return "CF"; }
            case INDIRECTIONSFEATURE ->         { return "IF"; }
            case METADATA ->                    { return "MD"; }
            case GENERALDECOMPILERPROPERTIES -> { return "GD"; }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    /**
     * This char is used for CLI-options on the assessor
     * @return a single char designating a feature
     */
    public char charFeatureLetterForCLIOptions () {
        switch (this){
            case METADATA ->                    { return '0'; }
            case CONTROLFLOWFEATURE ->          { return '1'; }
            case DATASTRUCTUREFEATURE ->        { return '2'; }
            case FUNCTIONFEATURE ->             { return '3'; }
            case GENERALDECOMPILERPROPERTIES -> { return '4'; }
            case INDIRECTIONSFEATURE ->         { return '5'; }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    /**
     * Return a new instance of correct IAssessor implementation
     * @return the new instance
     */
    public IAssessor getAppropriateIAssessorClass(){
        switch (this){
            case CONTROLFLOWFEATURE ->          { return new LoopAssessor(); }
            case DATASTRUCTUREFEATURE ->        { return new DataStructureAssessor(); }
            case FUNCTIONFEATURE ->             { return new FunctionAssessor(); }
            case GENERALDECOMPILERPROPERTIES -> { return new GeneralDecompilerPropertiesAssessor(); }
            case INDIRECTIONSFEATURE ->         { return new IndirectionsAssessor(); }
            case METADATA ->                    { return null; }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public IFeature getAppropriateIProducerClass(CGenerator cgenerator){
        switch (this){
            case CONTROLFLOWFEATURE ->          { return new LoopProducer(cgenerator); }
            case DATASTRUCTUREFEATURE ->        { return new DataStructureProducer(cgenerator); }
            case FUNCTIONFEATURE ->             { return new FunctionProducer(cgenerator); }
            case INDIRECTIONSFEATURE ->         { return new IndirectionsProducer(cgenerator); }
            case GENERALDECOMPILERPROPERTIES, METADATA
                    ->                          { return null; }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    /**
     * Return a short description for use in the CLI help text
     * @return short description
     */
    public String strGetCLIDescription(){
        switch (this){
            case FUNCTIONFEATURE ->             { return "function metrics"; }
            case DATASTRUCTUREFEATURE ->        { return "data structure metrics"; }
            case CONTROLFLOWFEATURE ->          { return "control flow metrics"; }
            case GENERALDECOMPILERPROPERTIES -> { return "general decompiler metrics"; }
            case INDIRECTIONSFEATURE ->         { return "indirection metrics"; }
            case METADATA ->                    { return null; }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    /**
     * Convert string to constant
     * @param strIn   String containing prefix or its full name, Matching prefix is case-insensitive.
     * @return        constant matching the string, or null if not found
     */
    public static EFeaturePrefix fromString(String strIn){
        try{
            // the easy way, used for all full names
            return valueOf(strIn);
        }
        catch (Exception e) {
            // the hard way: search all the short codes manually
            for (var fp : EFeaturePrefix.values()) {
                if (fp.toString().equalsIgnoreCase(strIn)) {
                    return fp;
                }
            }
        }
        return null;
    }

    /**
     * Create a CodeMarker object, either a BaseCodeMarker or a specific child,
     * indicated by the feature using the code marker
     * @param prefix     prefix indicating the feature using the code marker
     * @return           an object of CodeMarker type or one of its children
     */
    public static CodeMarker createNewFeaturedCodeMarker(EFeaturePrefix prefix, String strCodedProperties) {
        if (prefix == EFeaturePrefix.CONTROLFLOWFEATURE) {
            return new LoopCodeMarker(strCodedProperties);
        }
        if (prefix == EFeaturePrefix.FUNCTIONFEATURE) {
            return new FunctionCodeMarker(strCodedProperties);
        }
        return new BaseCodeMarker(strCodedProperties);
    }
}
