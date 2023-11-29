package nl.ou.debm.test;

import nl.ou.debm.common.feature1.ELoopCommands;
import nl.ou.debm.common.feature1.ELoopFinitude;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class LoopParametersTest {

    /**
     * Test the uniqueness of the feature prefixes
     */
    @org.junit.jupiter.api.Test
    void uniquePropertyNamesTest() {
        // create list of all properties
        var code = new ArrayList<String>();
        code.add(ELoopCommands.STRPROPERTYNAME);
        code.add(ELoopFinitude.STRPROPERTYNAME);

        // make sure all codes are unique
        for (int x=0;x<code.size()-1;++x){
            for (int y=x+1;y<code.size();++y) {
                assertNotEquals(code.get(x), code.get(y));
            }
        }
    }

}