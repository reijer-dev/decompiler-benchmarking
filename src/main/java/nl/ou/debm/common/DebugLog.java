package nl.ou.debm.common;

import java.io.FileOutputStream;
import java.io.PrintStream;

import static nl.ou.debm.common.Misc.printArrangedText;

public class DebugLog {
    private static final PrintStream s_stream;
    private static final int s_iPaperWidth = 120;
    private static final int s_iLeftIndent = 10;

    static {
        try {
            s_stream = new PrintStream(new FileOutputStream(Environment.logfile));
        }
        catch (Exception e){
            throw new RuntimeException("Could not create log file");
        }
    }

    public static void pr(Object x){
        printArrangedText(s_stream, x.toString(), s_iPaperWidth, s_iLeftIndent, 0);
        System.out.println(x);
    }

    public static void pr(long l){
        pr((Long) l);
    }
}