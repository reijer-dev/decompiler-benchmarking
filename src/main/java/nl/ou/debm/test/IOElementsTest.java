package nl.ou.debm.test;

import nl.ou.debm.common.IOElements;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IOElementsTest {
    @Test
    void FileIndices(){
        assertEquals("", IOElements.strAddFileIndex(null, 0, 0));
        assertEquals("", IOElements.strAddFileIndex("", 0, 0));
        assertEquals("/foo/bar", IOElements.strAddFileIndex("/foo/bar", 0, 1));
        assertEquals("/foo/bar10", IOElements.strAddFileIndex("/foo/bar", 10, 1));
        assertEquals("/foo/bar10.exe", IOElements.strAddFileIndex("/foo/bar.exe", 10, 1));
        assertEquals("/foo/bar.hoi10.exe", IOElements.strAddFileIndex("/foo/bar.hoi.exe", 10, 1));
    }

    @Test
    void ExtractAndUnifyFileNames(){
        assertEquals("foo.bar", IOElements.strGetFilenameWithDefaultExtension("/a/b/c/d/e/foo.BAR", ".bar"));
        assertEquals("foo.BAR", IOElements.strGetFilenameWithDefaultExtension("/a/b/c/d/e/foo.BAR", ""));
        assertEquals("foo.bar", IOElements.strGetFilenameWithDefaultExtension("/a/b/c/d/e/foo", ".bar"));
        assertEquals("foo.bar", IOElements.strGetFilenameWithDefaultExtension("/a/b/c/d/e.g/foo", ".bar"));
        assertEquals("foo.myown", IOElements.strGetFilenameWithDefaultExtension("/a/b/c/d/e.g/foo.myown", ".bar"));
    }
}
