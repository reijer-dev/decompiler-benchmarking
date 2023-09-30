package nl.ou.debm.common;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static nl.ou.debm.common.Misc.strGetNumberWithPrefixZeros;

/**
 * This class contains IO-functions. It makes sure that both the producer and the
 * assessor use the same container structure and the same file names.
 */

public class IOElements {
    /*
        internal rule: any folder return ends with a path separator, OS-dependent
     */


    private static final String binaryPrefix = "binary_";
    private static final String binaryPostfix = ".exe";
    private static final String llvmPrefix = "llvm_";
    private static final String llvmPostfix = ".llvm";
    private static final String cSourceName = "source.c";
    private static final String containerFolderPrefix = "container_";
    private static final String testFolderPrefix = "test_";
    private static final int numberOfDigits = 3;


    private static String m_strBasePath="";

    public static void setBasePath(String strBasePath){
        m_strBasePath = strAdaptPathToMatchFileSystemAndAddSeparator(strBasePath);
    }
    public static String getBasePath(){
        return m_strBasePath;
    }

    private static String strAdaptPathToMatchFileSystemAndAddSeparator(String strPath){
        // empty input?
        if (strPath.isEmpty()){
            return strPath;
        }

        // determine what to look for and what to replace it with
        char oldChar, newChar;
        if (File.separatorChar == '/'){
            oldChar = '\\';
            newChar = '/';
        }
        else{
            oldChar = '/';
            newChar = '\\';
        }

        // make sure correct separator is used
        strPath = strPath.replace(oldChar, newChar);

        // do we need to add a separator?
        if (strPath.charAt(strPath.length()-1)!=File.separatorChar){
            strPath += File.separatorChar;
        }

        // return the result
        return strPath;
    }


    public static String strBinaryFullFileName(int iContainer, int iTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize){
        return strBinaryFullFileName(m_strBasePath, iContainer, iTest, architecture, compiler, optimize);
    }
    public static String strBinaryFullFileName(String strBasePath, int iContainer, int iTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize){
        return strTestFullPath(strBasePath, iContainer, iTest) +
                strCombineName(binaryPrefix, architecture, compiler, optimize, binaryPostfix);
    }
    public static String strLLVMFullFileName(int iContainer, int iTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize){
        return strLLVMFullFileName(m_strBasePath, iContainer, iTest, architecture, compiler, optimize);
    }
    public static String strLLVMFullFileName(String strBasePath, int iContainer, int iTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize){
        return strTestFullPath(strBasePath, iContainer, iTest) +
                strCombineName(llvmPrefix, architecture, compiler, optimize, llvmPostfix);
    }
    public static String strCSourceFullFilename(int iContainer, int iTest){
        return strCSourceFullFilename(m_strBasePath, iContainer, iTest);
    }
    public static String strCSourceFullFilename(String strBasePath, int iContainer, int iTest){
        return strTestFullPath(strBasePath, iContainer, iTest) + cSourceName;
    }

    private static String strCombineName(String strPrefix, EArchitecture architecture, ECompiler compiler, EOptimize optimize, String strPostfix){
        return strPrefix + architecture.strFileCode() + "_" +
                compiler.strFileCode() + "_" +
                optimize.strFileCode() +
                strPostfix;
    }
    public static String strContainerFullPath(int iContainer){
        return strContainerFullPath(m_strBasePath, iContainer);
    }
    public static String strContainerFullPath(String strBasePath, int iContainer){
        return strAdaptPathToMatchFileSystemAndAddSeparator(strBasePath) +
                containerFolderPrefix + strGetNumberWithPrefixZeros(iContainer, numberOfDigits) + File.separatorChar;
    }
    public static String strTestFullPath(int iContainer, int iTest){
        return strTestFullPath(m_strBasePath, iContainer, iTest);
    }
    public static String strTestFullPath(String strBasePath, int iContainer, int iTest){
        return strContainerFullPath(strBasePath, iContainer) +
                testFolderPrefix + strGetNumberWithPrefixZeros(iTest, numberOfDigits) + File.separatorChar ;
    }

    public static boolean bFolderExists(String strPath){
        return Files.exists(Paths.get(strAdaptPathToMatchFileSystemAndAddSeparator(strPath)));
    }
}
