package nl.ou.debm.common;

/*
    This class stores a global indicator of the environment. The assumption is that this indicator is set at program startup and remains the same throughout the program execution. The indicator is used here and elsewhere in the code for environment dependent behavior.
*/

public class Environment {
    public static EEnv actual;
    public static String containerBasePath;
    public static String decompilerPath; //where are the decompilers located

    public enum EEnv {
        KESAVA,
        JAAP,
        REIJER,
        DEFAULT
    }

    static {
        actual = EEnv.DEFAULT;

        if (IOElements.bFolderExists("C:\\OU\\IB9902, IB9906 - Afstudeerproject\\")) { actual = EEnv.KESAVA; }
        if (IOElements.bFolderExists("C:\\Users\\reije\\")) { actual = EEnv.REIJER; }
        if (IOElements.bFolderExists("/home/jaap/VAF")) { actual = EEnv.JAAP; }

        //set containerBaseFolder
        containerBasePath = switch (actual) {
            case KESAVA -> "C:\\OU\\IB9902, IB9906 - Afstudeerproject\\_repo\\_containers\\";
            case JAAP -> "/home/jaap/VAF/containers/";
            case REIJER -> "C:\\Users\\reije\\OneDrive\\Documenten\\Development\\c-program\\containers\\";
            case DEFAULT -> "containers";
        };

        decompilerPath = switch (actual) {
            case KESAVA -> "C:\\OU\\IB9902, IB9906 - Afstudeerproject\\_repo\\_decompilers\\";
            case JAAP -> "scripts";
            case REIJER -> "C:\\Users\\reije\\OneDrive\\Documenten\\Development\\c-program\\decompilers\\";
            case DEFAULT -> "scripts";
        };

        System.out.println("using environment " + actual.toString());
        System.out.println("using containerBasePath " + containerBasePath);
    }

    public static String strGetPerfectDecompilerScript(){
        if (actual == EEnv.JAAP){
            return "theperfectdecompiler.sh";
        }
        else {
            return "theperfectdecompiler.bat";
        }
    }
}
