package nl.ou.debm.test;

import nl.ou.debm.common.CodeMarker;
import org.junit.jupiter.api.Test;

public class CodeMarkerTest {
    @Test
    void BasicTests(){
        var cm = new CodeMarker();

        cm.setProperty("Naam", "Jaap");
        cm.setProperty("Adres", "Laan van Nergens 311");
        cm.setProperty("Woonplaats", "Ergenschhuizen ZH");

        System.out.println(cm);
    }
}
