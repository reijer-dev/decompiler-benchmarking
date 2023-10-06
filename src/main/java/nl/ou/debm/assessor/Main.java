package nl.ou.debm.assessor;

import static nl.ou.debm.common.IOElements.strAdaptPathToMatchFileSystemAndAddSeparator;
import static nl.ou.debm.common.Misc.strGetContainersBaseFolder;

public class Main {

    public static void main(String[] args) throws Exception {
        var ass = new Assessor();
        ass.RunTheTests(strGetContainersBaseFolder(),
                strAdaptPathToMatchFileSystemAndAddSeparator(strGetContainersBaseFolder()) + "decompile.sh");
    }
}
