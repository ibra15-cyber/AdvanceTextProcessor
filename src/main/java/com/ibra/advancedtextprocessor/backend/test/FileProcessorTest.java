package com.ibra.advancedtextprocessor.backend.test;



import com.ibra.advancedtextprocessor.backend.FileProcessor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

import org.junit.*;

@RunWith(Enclosed.class)
public class FileProcessorTest {
    private static final String TEST_CONTENT = "Line 1\nLine 2\nLine 3";
    private static Path tempDir;
    private static Path testFile;
    private static Path readOnlyFile;

    @BeforeClass
    public static void setUpClass() throws IOException {
        tempDir = Files.createTempDirectory("fileproc-test");
        testFile = tempDir.resolve("test.txt");
        Files.write(testFile, TEST_CONTENT.getBytes());

        readOnlyFile = tempDir.resolve("readonly.txt");
        Files.write(readOnlyFile, "content".getBytes());
        readOnlyFile.toFile().setReadOnly();
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        // Recursive delete with retry for locked files
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        if (Files.exists(path)) {
                            path.toFile().setWritable(true);
                            Files.delete(path);
                        }
                    } catch (IOException e) {
                        System.err.println("Warning: Could not delete " + path);
                    }
                });
    }

    @Test
    public void testReadWriteFile() throws IOException {
        Path tempFile = tempDir.resolve("temp_" + System.currentTimeMillis() + ".txt");
        String content = "Test content";

        FileProcessor.writeFile(tempFile.toFile(), content);
        String readContent = FileProcessor.readFile(tempFile.toFile());
        assertEquals(content, readContent.trim());
    }

    @Test(expected = IOException.class)
    public void testReadNonExistentFile() throws IOException {
        FileProcessor.readFile(tempDir.resolve("nonexistent.txt").toFile());
    }

    @Test(expected = IOException.class)
    public void testWriteReadOnlyFile() throws IOException {
        FileProcessor.writeFile(readOnlyFile.toFile(), "should fail");
    }

    @Test
    public void testFilterFileByLinePattern() throws Exception {
        String result = FileProcessor.filterFileByLinePattern(testFile.toFile(), "Line [13]", 0);
        assertEquals("Line 1\nLine 3\n", result);
    }

    @Test(expected = PatternSyntaxException.class)
    public void testInvalidPatternFilter() throws Exception {
        FileProcessor.filterFileByLinePattern(testFile.toFile(), "[invalid", 0);
    }

    @Test
    public void testReplaceInFile() throws Exception {
        Path tempFile = tempDir.resolve("replace_" + System.currentTimeMillis() + ".txt");
        Files.write(tempFile, "Hello World".getBytes());

        String result = FileProcessor.replaceInFile(tempFile.toFile(), "World", "Universe", 0);
        assertEquals("Hello Universe", result.trim());
    }

    @Test
    public void testCountOccurrences() throws Exception {
        int count = FileProcessor.countOccurrencesInFile(testFile.toFile(), "Line", 0);
        assertEquals(3, count);
    }

    @Test
    public void testGrepFiles() throws Exception {
        Path file1 = tempDir.resolve("grep1.txt");
        Path file2 = tempDir.resolve("grep2.txt");
        Files.write(file1, "match\nno match".getBytes());
        Files.write(file2, "first match\nsecond match".getBytes());

        String result = FileProcessor.grepFiles(
                Arrays.asList(file1.toFile(), file2.toFile()),
                "match",
                0
        );
        assertTrue(result.contains("grep1.txt:1: match"));
        assertTrue(result.contains("grep2.txt:1: first match"));
        assertTrue(result.contains("grep2.txt:2: second match"));
    }

    @Test
    public void testFindFilesContainingPattern() throws Exception {
        Path searchDir = tempDir.resolve("search");
        Files.createDirectories(searchDir);

        Path matchFile = searchDir.resolve("match.txt");
        Path noMatchFile = searchDir.resolve("nomatch.txt");
        Files.write(matchFile, "secret pattern".getBytes());
        Files.write(noMatchFile, "normal text".getBytes());

        List<File> results = FileProcessor.findFilesContainingPattern(
                searchDir.toFile(),
                "secret",
                0,
                false
        );
        assertEquals(1, results.size());
        assertEquals(matchFile.getFileName().toString(), results.get(0).getName());
    }

    @Test
    public void testIsTextFile() throws IOException {
        Path textFile = tempDir.resolve("text.txt");
        Files.write(textFile, "text content".getBytes());

        Path binaryFile = tempDir.resolve("binary.dat");
        Files.write(binaryFile, new byte[] {0, 1, 2, 3});

        assertTrue(FileProcessor.isTextFile(textFile.toFile()));
        assertFalse(FileProcessor.isTextFile(binaryFile.toFile()));
    }

    @RunWith(Parameterized.class)
    public static class BatchReplaceTest {
        @Parameterized.Parameters(name = "{index}: files={0}, expected={1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { Arrays.asList("test1.txt", "test2.txt"), 2 },
                    { Arrays.asList("nonexistent.txt"), 0 }
            });
        }

        private final List<File> inputFiles;
        private final int expectedCount;

        public BatchReplaceTest(List<String> filenames, int expected) {
            this.inputFiles = filenames.stream()
                    .map(name -> tempDir.resolve(name).toFile())
                    .collect(Collectors.toList());
            this.expectedCount = expected;

            // Setup test files (skip nonexistent)
            filenames.stream()
                    .filter(name -> !name.contains("nonexistent"))
                    .forEach(name -> {
                        try {
                            Files.write(tempDir.resolve(name), "content".getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to create test file", e);
                        }
                    });
        }

        @Test
        public void testBatchReplace() throws Exception {
            Path outputDir = tempDir.resolve("output_" + System.currentTimeMillis());
            Files.createDirectories(outputDir);

            int processed = FileProcessor.batchReplaceInFiles(
                    inputFiles,
                    outputDir.toFile(),
                    "content",
                    "replaced",
                    0
            );
            assertEquals(expectedCount, processed);
        }
    }

    @After
    public void verifyNoLeakedFiles() throws IOException {
        // Verify no unexpected files remain
        try (var files = Files.list(tempDir)) {
            long count = files
                    .filter(path -> !path.equals(testFile) && !path.equals(readOnlyFile))
                    .count();
            assertTrue("Test leaked files: " + count, count <= 2); // Allow for current test files
        }
    }
}