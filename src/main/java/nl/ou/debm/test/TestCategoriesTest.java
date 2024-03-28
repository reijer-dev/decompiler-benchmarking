package nl.ou.debm.test;

import nl.ou.debm.assessor.ETestCategories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestCategoriesTest {

    @Test
    void UniversalIdentifierUniqueness(){
        for (var c1 : ETestCategories.values()){
            for (var c2 : ETestCategories.values()){
                if (c1!=c2){
                    assertNotEquals(c1.lngUniversalIdentifier(), c2.lngUniversalIdentifier());
                }
            }
        }
    }
}
