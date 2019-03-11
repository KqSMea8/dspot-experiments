package com.baeldung.file;


import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;


public class FilenameFilterManualTest {
    private static File directory;

    @Test
    public void whenFilteringFilesEndingWithJson_thenEqualExpectedFiles() {
        FilenameFilter filter = ( dir, name) -> name.endsWith(".json");
        String[] expectedFiles = new String[]{ "people.json", "students.json" };
        String[] actualFiles = FilenameFilterManualTest.directory.list(filter);
        Assert.assertArrayEquals(expectedFiles, actualFiles);
    }

    @Test
    public void whenFilteringFilesEndingWithXml_thenEqualExpectedFiles() {
        Predicate<String> predicate = ( name) -> name.endsWith(".xml");
        String[] expectedFiles = new String[]{ "teachers.xml", "workers.xml" };
        List<String> files = Arrays.stream(FilenameFilterManualTest.directory.list()).filter(predicate).collect(Collectors.toList());
        String[] actualFiles = files.toArray(new String[files.size()]);
        Assert.assertArrayEquals(expectedFiles, actualFiles);
    }
}
