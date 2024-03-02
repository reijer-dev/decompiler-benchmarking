package nl.ou.debm.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

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
    public static String binaryFilename(EArchitecture architecture, ECompiler compiler, EOptimize optimize) {
        return strCombineName(binaryPrefix, architecture, compiler, optimize, binaryPostfix);
    }
    private static final String llvmPrefix = "llvm_";
    private static final String llvmPostfix = ".ll"; //todo aangepast van .llvm naar ll. Werkt alles nog goed?
    public static String llvmFilename(EArchitecture architecture, ECompiler compiler, EOptimize optimize) {
        return strCombineName(llvmPrefix, architecture, compiler, optimize, llvmPostfix);
    }
    private static final String cMainFilename = "main.c";
    private static final String cExternalFunctionsFilename = "external_functions.c";
    private static final String containerFolderPrefix = "container_";
    private static final String testFolderPrefix = "test_";
    private static final int numberOfDigits = 3;


    /**
     * Convert the path separators in a string to the OS-specific separators.
     * So: /hi/all/of/you will come out the same on Linux, but as \hi\all\of\you in Win
     * It will also add a proper path separator if the path doesn't include one in the end
     * @param strPath   Path to be adapted
     * @return          Adapted path. Empty input will result in empty output, otherwise it
     *                  always ends with a path separator.
     */
    public static String strAdaptPathToMatchFileSystemAndAddSeparator(String strPath){
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
        return strBinaryFullFileName(Environment.containerBasePath, iContainer, iTest, architecture, compiler, optimize);
    }
    public static String strBinaryFullFileName(String strBasePath, int iContainer, int iTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize){
        return strTestFullPath(strBasePath, iContainer, iTest) +
                strCombineName(binaryPrefix, architecture, compiler, optimize, binaryPostfix);
    }
    public static String strLLVMFullFileName(int iContainer, int iTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize){
        return strLLVMFullFileName(Environment.containerBasePath, iContainer, iTest, architecture, compiler, optimize);
    }
    public static String strLLVMFullFileName(String strBasePath, int iContainer, int iTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize){
        return strTestFullPath(strBasePath, iContainer, iTest) +
                strCombineName(llvmPrefix, architecture, compiler, optimize, llvmPostfix);
    }
    public static String strCSourceFullFilename(int iContainer, int iTest){
        return strCSourceFullFilename(Environment.containerBasePath, iContainer, iTest);
    }
    public static String strCSourceFullFilename(String strBasePath, int iContainer, int iTest){
        return strTestFullPath(strBasePath, iContainer, iTest) + cMainFilename;
    }
    public static String strCExternalFunctionsFullFilename(int iContainer, int iTest){
        return strCExternalFunctionsFullFilename(Environment.containerBasePath, iContainer, iTest);
    }
    public static String strCExternalFunctionsFullFilename(String strBasePath, int iContainer, int iTest){
        return strTestFullPath(strBasePath, iContainer, iTest) + cExternalFunctionsFilename;
    }

    private static String strCombineName(String strPrefix, EArchitecture architecture, ECompiler compiler, EOptimize optimize, String strPostfix){
        return strPrefix + architecture.strFileCode() + "_" +
                compiler.strFileCode() + "_" +
                optimize.strFileCode() +
                strPostfix;
    }
    public static String strContainerFullPath(int iContainer){
        return strContainerFullPath(Environment.containerBasePath, iContainer);
    }
    public static String strContainerFullPath(String strBasePath, int iContainer){
        return strAdaptPathToMatchFileSystemAndAddSeparator(strBasePath) +
                containerFolderPrefix + strGetNumberWithPrefixZeros(iContainer, numberOfDigits) + File.separatorChar;
    }
    public static String strTestFullPath(int iContainer, int iTest){
        return strTestFullPath(Environment.containerBasePath, iContainer, iTest);
    }
    public static String strTestFullPath(String strBasePath, int iContainer, int iTest){
        return strContainerFullPath(strBasePath, iContainer) +
                testFolderPrefix + strGetNumberWithPrefixZeros(iTest, numberOfDigits) + File.separatorChar ;
    }

    /**
     * Check whether folder exists (and ascertain it really is a folder)
     * @param strPath full path; path-separators are set to match the OS
     * @return true if path exists
     */
    public static boolean bFolderExists(String strPath){
        Path path = Paths.get(strAdaptPathToMatchFileSystemAndAddSeparator(strPath));
        return Files.exists(path) && Files.isDirectory(path);
    }

    /**
     * Check whether file exists (and ascertain it really is a file)
     * @param strPath full path; path-separators are set to match the OS
     * @return true if path exists
     */
    public static boolean bFileExists(String strPath){
        Path path = Paths.get(strAdaptPathToMatchFileSystemAndAddSeparator(strPath));
        return Files.exists(path) && !Files.isDirectory(path);
    }

    public static void deleteFile(String strFile){
        try{
            Files.deleteIfExists(Paths.get(strFile));
        }
        catch (IOException ignored){
        }
    }

    public static void writeToFile(String s, String path) throws IOException {
        var writer = new OutputStreamWriter(new FileOutputStream(path));
        writer.write(s);
        writer.flush();
        writer.close();
    }

    /**
     * Remove a folder and all its contents
     * @param path folder
     * @return true if folder no (longer) exists
     */
    public static boolean bFolderAndAllContentsDeletedOK(Path path){
        return bFolderAndAllContentsDeletedOK(path.toString());
    }
    /**
     * Remove a folder and all its contents
     * @param strPath folder
     * @return true if folder no (longer) exists
     */
    public static boolean bFolderAndAllContentsDeletedOK(String strPath){
        // based on: https://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java

        // get folder accessor
        Path folder = Paths.get(strPath);

        // only do something if folder exists in the first place...
        if (Files.exists(folder)){
            try {
                Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes baf) {
                        // delete any file encountered
                        try {
                            // try deleting the file
                            Files.delete(file);
                        } catch (IOException e) {
                            // and abort the lot if it is unsuccessful
                            return FileVisitResult.TERMINATE;
                        }
                        // but if all goes well, continue
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult postVisitDirectory(Path folder, IOException ioException) {
                        // make sure nothing went wrong before
                        if (ioException == null){
                            // delete folder when all its contents have been deleted
                            try {
                                // try deleting the file
                                Files.delete(folder);
                            } catch (IOException e) {
                                // and abort the lot if it is unsuccessful
                                return FileVisitResult.TERMINATE;
                            }
                            // but if all goes well, continue
                            return FileVisitResult.CONTINUE;
                        }
                        return FileVisitResult.TERMINATE;
                    }
                });
            }
            catch (IOException e){
                // ignore IO-error
            }
        }

        // if all went well, the directory is no more...
        return !Files.exists(folder);
    }
}
