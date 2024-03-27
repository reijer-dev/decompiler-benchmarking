package nl.ou.debm.test;

import nl.ou.debm.common.CommandLineUtils;
import org.junit.jupiter.api.Test;

public class CommandLineUtilsTest {

    @Test
    void BasicTest(){
        String[] args = {"-o=hallo", "-i=bye"};

        var me = new CommandLineUtils("Mijn programmanaam", "mijn copyright");
        me.printProgramHeader();
    }
}
