package nl.ou.debm.assessor;

import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.MyCListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

import static nl.ou.debm.common.IOElements.strAdaptPathToMatchFileSystemAndAddSeparator;
import static nl.ou.debm.common.Misc.strGetContainersBaseFolder;

public class Main {

    public static void main(String[] args) throws Exception {
        var ass = new Assessor();
        ass.RunTheTests(strGetContainersBaseFolder(),
                strAdaptPathToMatchFileSystemAndAddSeparator(strGetContainersBaseFolder()) + "decompile.sh");
    }
}
