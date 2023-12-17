package nl.ou.debm.assessor;

import java.security.InvalidParameterException;

import static nl.ou.debm.common.Misc.strGetContainersBaseFolder;

public class Main {

    public static void main(String[] args) throws Exception {
        if(args.length != 1)
            throw new InvalidParameterException("Program can only be run with exactly one argument!");

        var ass = new Assessor();
        ass.RunTheTests(strGetContainersBaseFolder(),
                args[0], false);
    }
}
