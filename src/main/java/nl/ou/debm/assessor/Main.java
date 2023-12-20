package nl.ou.debm.assessor;

import nl.ou.debm.common.Environment;

import java.security.InvalidParameterException;

public class Main {

    public static void main(String[] args) throws Exception {
        if(args.length != 1)
            throw new InvalidParameterException("Program can only be run with exactly one argument!");

        var ass = new Assessor();
        ass.RunTheTests(Environment.containerBasePath, args[0], false);
    }
}
