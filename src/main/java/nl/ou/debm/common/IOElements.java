package nl.ou.debm.common;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
    private static final String llvmPostfix = ".ll";
    private static final String asmPrefix = "assembly_";
    private static final String asmPostfix = ".s";
    public static final String cAmalgamationFilename = "amalgamation.c"; //There may be multiple source files. These are merged into this one c file.
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
        return strTestFullPath(strBasePath, iContainer, iTest) + strCombineName(binaryPrefix, architecture, compiler, optimize, binaryPostfix);
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
        return strTestFullPath(strBasePath, iContainer, iTest) + cAmalgamationFilename;
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

    // The binary and LLVM IR filenames are standardized by IOElements, to ensure the producer and assessor use the same filenames. The function strGeneralFilename can be used for other files that also need CompilerConfig information embedded in the filename. This is the case for LLVM IR bitcode files created by the producer. These are created for every CompilerConfig, so they need unique names for each CompilerConfig.
    public static String strGeneralFilename(String prefix, CompilerConfig config, String postfix) {
        return strCombineName(prefix, config.architecture, config.compiler, config.optimization, postfix);
    }
    public static String strBinaryFilename(CompilerConfig config) {
        return strGeneralFilename(binaryPrefix, config, binaryPostfix);
    }
    public static String strLLVMFilename(CompilerConfig config) {
        return strGeneralFilename(llvmPrefix, config, llvmPostfix);
    }
    public static String strASMFilename(CompilerConfig config) {
        return strGeneralFilename(asmPrefix, config, asmPostfix);
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
        catch (IOException ignored){ assert false; }
    }

    /**
     * Write string output file. Change IO Exception to RuntimeException if it occurs.
     * @param s what to write
     * @param path where to write to
     */
    public static void writeToFile(String s, String path)  {
        try {
            var writer = new OutputStreamWriter(new FileOutputStream(path));
            writer.write(s);
            writer.flush();
            writer.close();
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Write contents of StreamBuilder to output file. Change IO Exception to RuntimeException if it occurs.
     * @param sb what to write
     * @param strOutputFilename where to write to
     */
    public static void writeToFile(StringBuilder sb, String strOutputFilename) {
        // write to file
        writeToFile(sb.toString(), strOutputFilename);
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

    /**
     * Get sorted list of sub folders in a folder (no recursion, no separators)
     * @param strPath input path
     * @return sorted list of sub folders
     */
    public static List<String> getSubFolders(String strPath) {
        // https://www.baeldung.com/java-list-directory-files

        try (Stream<Path> stream = Files.list(Paths.get(strPath))) {
            List<String> out = new ArrayList<>(stream
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList());
            Collections.sort(out);
            return out;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wrapper function to count the number of lines in a file
     * @param strFilename file whose lines are to be counted
     * @return the number of lines, -1 for error
     */
    public static int iGetNumberOfLinesInFile(String strFilename) {
        // used for inspiration:
        // https://stackoverflow.com/questions/1277880/how-can-i-get-the-count-of-line-in-a-file-in-an-efficient-way
        LineNumberReader reader = null;
        int out = -1;
        try {
            reader = new LineNumberReader(new FileReader(strFilename));
            while (reader.readLine() != null);
            out = reader.getLineNumber();
        }
        catch (Exception ignore) {}
        finally {
            if (reader!=null){
                try {
                    reader.close();
                }
                catch (Exception ignore){}
            }
        }
        return out;
    }

    /**
     * add a file index when necessary, just before the extension or at the end, if no extension exists. So /foo/bar
     * will become /foo/bar#, /foo/bar.exe will become /foo/bar#.exe<br>
     * no index will be added when iIndex==0 and iMaxValue==1
     * @param strBaseFileName the full path to be edited
     * @param iIndex the index to be added
     * @param iMaxValue the number of files to be indexed in total
     * @return a new file name
     */
    public static String strAddFileIndex(String strBaseFileName, int iIndex, int iMaxValue){
        if (strBaseFileName==null){
            return "";
        }
        if (strBaseFileName.isEmpty()){
            return "";
        }
        if ((iIndex==0) && (iMaxValue==1)){
            return strBaseFileName;
        }

        int p;
        for (p=strBaseFileName.length()-1; p>=0; --p){
            if (strBaseFileName.charAt(p) == File.separatorChar){
                p=strBaseFileName.length();
                break;
            }
            if (strBaseFileName.charAt(p) == '.'){
                break;
            }
        }
        var out = new StringBuilder(strBaseFileName.length()+5);
        out.append(strBaseFileName, 0, p);
        out.append(iIndex);
        if (p<strBaseFileName.length()){
            out.append(strBaseFileName, p,strBaseFileName.length());
        }
        return out.toString();
    }
}
