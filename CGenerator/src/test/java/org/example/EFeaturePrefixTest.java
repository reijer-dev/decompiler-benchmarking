package org.example;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EFeaturePrefixTest {

    @org.junit.jupiter.api.Test
    void fromString() {
    }

    /**
     * Test the uniqueness of the feature prefixes
      */
    @org.junit.jupiter.api.Test
    void uniqueShortCodes() {
        // create list of all values
        var code = new ArrayList<String>();
        for (var fp : EFeaturePrefix.values()) {
            code.add(fp.toString());
        }

        // make sure all codes are unique
        for (int x=0;x<code.size()-1;++x){
            for (int y=x+1;y<code.size();++y) {
                assertNotEquals(code.get(x), code.get(y));
            }
        }
    }

    @org.junit.jupiter.api.Test
    void valueOf() {
    }
}