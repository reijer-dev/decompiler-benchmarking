package nl.ou.debm.common;

/*
    This class stores a global indicator of the environment. The assumption is that this indicator is set at program startup and remains the same throughout the program execution. The indicator is used here and elsewhere in the code for environment dependent behavior.
*/

public class Environment {
    public static EnvEnum actual;
    public static String containerBasePath;

    enum EnvEnum {
        KESAVA,
        JAAP,
        REIJER,
        DEFAULT
    }

    static {
        actual = EnvEnum.DEFAULT;

        //Uncomment any of the following to enable the environment.
        //actual = env_code.JAAP;
        actual = EnvEnum.KESAVA;
        //actual = env_code.REIJER;

        //set containerBaseFolder
        containerBasePath = switch (actual) {
            case KESAVA -> "C:\\OU\\IB9902, IB9906 - Afstudeerproject\\_repo\\decompiler-benchmarking\\containers\\";
            case JAAP -> "/home/jaap/VAF/containers/";
            case REIJER -> "C:\\Users\\reije\\OneDrive\\Documenten\\Development\\c-program\\containers\\";
            case DEFAULT -> "containers";
        };
    }
}
