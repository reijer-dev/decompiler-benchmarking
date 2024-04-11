package nl.ou.debm.common.feature2;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;

public class DataStructureCodeMarker extends BaseCodeMarker {
    public static final String characteristic = CodeMarker.STRCODEMARKERGUID + EFeaturePrefix.DATASTRUCTUREFEATURE;

    public DataStructureCodeMarker(ETestCategories testCategory, String expected_type, String variable) {
        super(EFeaturePrefix.DATASTRUCTUREFEATURE);
        setProperty("category", testCategory.toString());
        setProperty("expected", expected_type);
        setProperty("variable", variable);
    }

    public DataStructureCodeMarker(String representation) {
        super(EFeaturePrefix.DATASTRUCTUREFEATURE);
        var success = fromString(representation);
        if (!success) throw new RuntimeException("invalid code marker string: " + representation);

        var has_expected_properties =
            bPropertyPresent("category")
            && bPropertyPresent("expected")
            && bPropertyPresent("variable");
        if ( ! has_expected_properties) throw new RuntimeException("cannot parse DataStructureCodeMarker from string " + representation + " because one or more properties are missing.");
    }

    //I use this, instead of printf, because it is more reliably recognized by decompilers. Printf calls are often (by almost all decompilers) decompiled with a wrong number of parameters. The only decompiler that I tried where printf is at an advantage is RecStudio. Compare for example (the following is decompiled code by RecStudio):
    //    __rdi = "metadata";
    //    L0000000140001720("metadata", __rsi);
    //    DataStructureCodeMarker(__rax, __rdi, __rsi);
    // The call to DataStructureCodeMarker receives an unnecessary first parameter, and the metadata string is not provided as a literal, but through a variable reference. This makes parsing the codemarker more difficult than with printf (which RecStudio has called L0000000140001720 here).
    // There is, however, one decompiler that absolutely fails with printf, which is Hex-Rays. It doesn't find the second parameter (the address of the variable), which makes the codemarker useless.
    // For other decompilers, both the standard printf and DataStructureCodeMarker work fine. printf is slightly worse because it often receives extra useless parameters, but they appear after the expected ones, and so can be easily ignored. All in all, and especially because of Hex-Rays, the custom DataStructureCodeMarker function seems to work best.
    @Override
    public String strPrintf() {
        return "DataStructureCodeMarker(" + '\"' + this + '\"' + ", &" + strPropertyValue("variable") + ");";
    }

    public ETypeCategory getTypeCategory() {
        return ETypeCategory.valueOf(strPropertyValue("category"));
    }
}
