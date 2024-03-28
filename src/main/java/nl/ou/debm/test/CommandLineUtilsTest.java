package nl.ou.debm.test;

import nl.ou.debm.common.CommandLineUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommandLineUtilsTest {

    @Test
    void BasicTest(){
        String[] args = {"/i=hallo", "/c=bye", "-o=hallo2"};

        List<CommandLineUtils.ParameterDefinition> pmd = new ArrayList<>();
        pmd.add(new CommandLineUtils.ParameterDefinition("output",
                "This is a long description of the output. We try to make it such at there will " +
                        "be line breaks in the actual output. Would we succeed? " +
                        "I think we actually will. Which would be very nice\n " +
                        "This is a linefee fed line\n" +
                        "And this is another one\n" +
                        "Why, why don't you hear me? Where, where are the good times we had? Why, why do you love him? Why, why do you need hem so bad?",
                "-o=", '1'));
        pmd.add(new CommandLineUtils.ParameterDefinition("input", "some other description", new String[]{"-i=", "/i="}, '*' ));
        pmd.add(new CommandLineUtils.ParameterDefinition("count", "some other description (2)", new String[]{"-c=", "/c="}, '?', "default" ));

        var me = new CommandLineUtils("my program name", "(c) 2024 by us all", pmd);
        me.setGeneralHelp("This is a general description of the file. I don't know what to do anyway. Must write something. Romeo, oh Romeo, Oh! Where for are's't thou Romeo?");

        var q = me.parseCommandLineInput(args);

        assertNotNull(q);
        for (var item : q){
            System.out.println(item.toString());
        }
    }
}
