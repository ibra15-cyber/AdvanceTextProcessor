package com.ibra.advancedtextprocessor.backend;


import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Class for handling file operations with advanced text processing capabilities
 */
public class FileProcessor {

    /**
     * Reads the entire content of a file as a string
     *
     * @param file The file to read
     * @return The file content as a string
     * @throws IOException If an I/O error occurs
     */
    public static String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * Writes string content to a file
     *
     * @param file The file to write to
     * @param content The content to write
     * @throws IOException If an I/O error occurs
     */
    public static void writeFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }

    /**
     * Processes a file line by line, applying a regex filter
     *
     * @param file The file to process
     * @param patternStr The regex pattern to filter lines
     * @param flags Regex flags
     * @return A string containing only the lines that match the pattern
     * @throws IOException If an I/O error occurs
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public static String filterFileByLinePattern(File file, String patternStr, int flags)
            throws IOException, PatternSyntaxException {
        Pattern pattern = Pattern.compile(patternStr, flags);
        StringBuilder filteredContent = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (pattern.matcher(line).find()) {
                    filteredContent.append(line).append("\n");
                }
            }
        }

        return filteredContent.toString();
    }

    /**
     * Processes a file by replacing text that matches a pattern
     *
     * @param file The file to process
     * @param patternStr The regex pattern to match
     * @param replacement The replacement string
     * @param flags Regex flags
     * @return The content with replacements
     * @throws IOException If an I/O error occurs
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public static String replaceInFile(File file, String patternStr, String replacement, int flags)
            throws IOException, PatternSyntaxException {
        String content = readFile(file);
        return TextProcessor.replaceAll(content, patternStr, replacement, flags);
    }

    /**
     * Processes multiple files with the same pattern and writes results to the output directory
     *
     * @param inputFiles List of input files
     * @param outputDir Output directory
     * @param patternStr The regex pattern to match
     * @param replacement The replacement string
     * @param flags Regex flags
     * @return Number of files processed
     * @throws IOException If an I/O error occurs
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public static int batchReplaceInFiles(List<File> inputFiles, File outputDir,
                                          String patternStr, String replacement, int flags)
            throws IOException, PatternSyntaxException {

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        if (!outputDir.isDirectory()) {
            throw new IOException("Output path is not a directory");
        }

        AtomicInteger processedCount = new AtomicInteger(0);

        // Process files in parallel
        inputFiles.parallelStream().forEach(inputFile -> {
            try {
                String processedContent = replaceInFile(inputFile, patternStr, replacement, flags);
                File outputFile = new File(outputDir, inputFile.getName());
                writeFile(outputFile, processedContent);
                processedCount.incrementAndGet();
            } catch (IOException | PatternSyntaxException e) {
                // Log error but continue processing other files
                System.err.println("Error processing file " + inputFile.getName() + ": " + e.getMessage());
            }
        });

        return processedCount.get();
    }

    /**
     * Counts occurrences of a pattern in a file
     *
     * @param file The file to process
     * @param patternStr The regex pattern to count
     * @param flags Regex flags
     * @return The number of occurrences
     * @throws IOException If an I/O error occurs
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public static int countOccurrencesInFile(File file, String patternStr, int flags)
            throws IOException, PatternSyntaxException {
        String content = readFile(file);
        Pattern pattern = Pattern.compile(patternStr, flags);
        java.util.regex.Matcher matcher = pattern.matcher(content);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    /**
     * Extracts all lines containing a pattern from multiple files and combines the results
     *
     * @param inputFiles List of input files
     * @param patternStr The regex pattern to match
     * @param flags Regex flags
     * @return String containing all matching lines from all files
     * @throws IOException If an I/O error occurs
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public static String grepFiles(List<File> inputFiles, String patternStr, int flags)
            throws IOException, PatternSyntaxException {
        Pattern pattern = Pattern.compile(patternStr, flags);
        StringBuilder result = new StringBuilder();

        for (File file : inputFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (pattern.matcher(line).find()) {
                        result.append(file.getName())
                                .append(":").append(lineNumber)
                                .append(": ").append(line)
                                .append("\n");
                    }
                }
            }
        }

        return result.toString();
    }

    /**
     * Finds files in a directory that contain text matching a pattern
     *
     * @param directory The directory to search in
     * @param patternStr The regex pattern to match
     * @param flags Regex flags
     * @param recursive Whether to search recursively in subdirectories
     * @return List of files containing matches
     * @throws IOException If an I/O error occurs
     * @throws PatternSyntaxException If the pattern is invalid
     */
    public static List<File> findFilesContainingPattern(File directory, String patternStr,
                                                        int flags, boolean recursive)
            throws IOException, PatternSyntaxException {

        if (!directory.isDirectory()) {
            throw new IOException("Not a directory: " + directory.getPath());
        }

        Pattern pattern = Pattern.compile(patternStr, flags);
        List<File> matchingFiles = new ArrayList<>();

        File[] files = directory.listFiles();
        if (files == null) {
            return matchingFiles;
        }

        for (File file : files) {
            if (file.isFile()) {
                try {
                    // Only check text files
                    if (isTextFile(file)) {
                        String content = readFile(file);
                        if (pattern.matcher(content).find()) {
                            matchingFiles.add(file);
                        }
                    }
                } catch (IOException e) {
                    // Skip files that can't be read
                    System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
                }
            } else if (recursive && file.isDirectory()) {
                matchingFiles.addAll(findFilesContainingPattern(file, patternStr, flags, true));
            }
        }

        return matchingFiles;
    }

    /**
     * Basic check if a file appears to be a text file
     *
     * @param file The file to check
     * @return true if the file is likely a text file
     */
    public static boolean isTextFile(File file) {
        String name = file.getName().toLowerCase();
        String[] textExtensions = {".txt", ".csv", ".log", ".xml", ".json", ".html", ".md", ".java", ".py", ".js", ".css"};

        for (String ext : textExtensions) {
            if (name.endsWith(ext)) {
                return true;
            }
        }

        // Additional check by sampling the beginning of the file for binary data
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return !containsBinaryData(bytes, 1000);  // Check first 1000 bytes
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Check if byte array likely contains binary data
     *
     * @param bytes The bytes to check
     * @param maxBytesToCheck Maximum number of bytes to check
     * @return true if the data appears to be binary
     */
    private static boolean containsBinaryData(byte[] bytes, int maxBytesToCheck) {
        int bytesToCheck = Math.min(bytes.length, maxBytesToCheck);
        int binaryByteCount = 0;

        for (int i = 0; i < bytesToCheck; i++) {
            byte b = bytes[i];
            // Check for null bytes or control characters other than CR, LF, and tab
            if (b == 0 || (b < 0x09 || (b > 0x0D && b < 0x20))) {
                binaryByteCount++;
            }
        }

        // If more than 10% of bytes are binary, consider it a binary file
        return binaryByteCount > (bytesToCheck * 0.1);
    }
}