package nl.ou.debm.test;

import nl.ou.debm.common.CommandLineUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class CommandLineUtilsTest {

    @Test
    void BasicTest(){
        String[] args = {"-o=hallo", "-i=bye"};

        List<CommandLineUtils.ParameterDefinition> pmd = new ArrayList<>();
        pmd.add(new CommandLineUtils.ParameterDefinition("output",
                "This is a long description of the output. We try to make it such at there will be line breaks in the actual output. Would we succeed?",
                "-o=", '1'));
        pmd.add(new CommandLineUtils.ParameterDefinition("input", "some other description", "-i=", '*' ));

        var me = new CommandLineUtils("my program name", "(c) 2024 by us all", pmd);

        var q = me.parseCommandLineInput(args);

        for (var item : q){
            System.out.println(item.toString());
        }
    }
}
