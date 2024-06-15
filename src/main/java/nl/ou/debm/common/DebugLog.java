package nl.ou.debm.common;

import java.io.FileOutputStream;
import java.io.PrintStream;

public class DebugLog {
    static final PrintStream s_stream;

    static {
        try {
            s_stream = new PrintStream(new FileOutputStream(Environment.logfile));
        }
        catch (Exception e){
            throw new RuntimeException("Could not create log file");
        }
    }

    public static void pr(Object x){
        s_stream.println(x.toString());
        System.out.println(x);
    }

    public static void pr(long l){
        pr((Long) l);
    }
}