package com.ibra.advancedtextprocessor.backend;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Manager class for handling collections of PatternEntry objects
 * Provides functionality for storing, retrieving, and managing regex patterns
 */
public class PatternManager {
    private List<PatternEntry> patterns;
    private static final String DEFAULT_PATTERNS_FILE = "saved_patterns.dat";

    /**
     * Creates a new PatternManager with an empty pattern list
     */
    public PatternManager() {
        this.patterns = new ArrayList<>();
    }

    /**
     * Creates a new PatternManager with the provided patterns
     *
     * @param patterns Initial list of patterns
     */
    public PatternManager(List<PatternEntry> patterns) {
        this.patterns = new ArrayList<>(patterns);
    }

    /**
     * Adds a new pattern entry
     *
     * @param entry The pattern entry to add
     * @return true if the pattern was added successfully
     */
    public boolean addPattern(PatternEntry entry) {
        if (entry == null || entry.getName() == null || entry.getName().isEmpty()
                || entry.getPattern() == null || entry.getPattern().isEmpty()) {
            return false;
        }

        // Check if entry with same name already exists
        if (patterns.stream().anyMatch(p -> p.getName().equals(entry.getName()))) {
            return false;
        }

        // Validate the pattern
        try {
            TextProcessor.isValidPattern(entry.getPattern());
        } catch (Exception e) {
            return false;
        }

        patterns.add(entry);
        return true;
    }

    /**
     * Removes a pattern entry by name
     *
     * @param name The name of the pattern to remove
     * @return true if the pattern was removed
     */
    public boolean removePattern(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        int initialSize = patterns.size();
        patterns = patterns.stream()
                .filter(p -> !p.getName().equals(name))
                .collect(Collectors.toList());

        return patterns.size() < initialSize;
    }

    /**
     * Updates an existing pattern
     *
     * @param name The name of the pattern to update
     * @param newEntry The new pattern entry data
     * @return true if the pattern was updated
     */
    public boolean updatePattern(String name, PatternEntry newEntry) {
        if (name == null || name.isEmpty() || newEntry == null
                || newEntry.getPattern() == null || newEntry.getPattern().isEmpty()) {
            return false;
        }

        // Validate the new pattern
        try {
            TextProcessor.isValidPattern(newEntry.getPattern());
        } catch (Exception e) {
            return false;
        }

        boolean found = false;
        for (int i = 0; i < patterns.size(); i++) {
            if (patterns.get(i).getName().equals(name)) {
                patterns.set(i, newEntry);
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Gets a pattern entry by name
     *
     * @param name The name of the pattern to get
     * @return The pattern entry, or null if not found
     */
    public PatternEntry getPattern(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        return patterns.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all patterns
     *
     * @return List of all pattern entries
     */
    public List<PatternEntry> getAllPatterns() {
        return new ArrayList<>(patterns);
    }

    /**
     * Gets patterns that match a filter
     *
     * @param nameFilter Substring to filter pattern names by
     * @return List of matching pattern entries
     */
    public List<PatternEntry> findPatterns(String nameFilter) {
        if (nameFilter == null) {
            return new ArrayList<>(patterns);
        }

        return patterns.stream()
                .filter(p -> p.getName().toLowerCase().contains(nameFilter.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Saves patterns to a file
     *
     * @param file The file to save to
     * @return true if saved successfully
     */
    public boolean saveToFile(File file) {
        if (file == null) {
            file = new File(DEFAULT_PATTERNS_FILE);
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(patterns);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving patterns: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads patterns from a file
     *
     * @param file The file to load from
     * @return true if loaded successfully
     */
    @SuppressWarnings("unchecked")
    public boolean loadFromFile(File file) {
        if (file == null) {
            file = new File(DEFAULT_PATTERNS_FILE);
        }

        if (!file.exists() || !file.isFile()) {
            return false;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            if (obj instanceof List<?>) {
                // Check if it's a list of PatternEntry objects
                List<?> list = (List<?>) obj;
                if (!list.isEmpty() && list.get(0) instanceof PatternEntry) {
                    patterns = (List<PatternEntry>) obj;
                    return true;
                }
            }
            return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading patterns: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exports patterns to a human-readable text file
     *
     * @param file The file to export to
     * @return true if exported successfully
     */
    public boolean exportToTextFile(File file) {
        if (file == null || patterns.isEmpty()) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (PatternEntry entry : patterns) {
                writer.write("Name: " + entry.getName());
                writer.newLine();
                writer.write("Pattern: " + entry.getPattern());
                writer.newLine();
                writer.write("Multiline: " + entry.isMultiline());
                writer.newLine();
                writer.write("Case Insensitive: " + entry.isCaseInsensitive());
                writer.newLine();
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting patterns: " + e.getMessage());
            return false;
        }
    }

    /**
     * Imports patterns from a specially formatted text file
     *
     * @param file The file to import from
     * @return Number of patterns successfully imported
     */
    public int importFromTextFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return 0;
        }

        int importCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentName = null;
            String currentPattern = null;
            boolean currentMultiline = false;
            boolean currentCaseInsensitive = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    // End of entry, add if we have valid data
                    if (currentName != null && currentPattern != null) {
                        PatternEntry entry = new PatternEntry(
                                currentName, currentPattern, currentMultiline, currentCaseInsensitive);

                        if (addPattern(entry)) {
                            importCount++;
                        }
                    }

                    // Reset for next entry
                    currentName = null;
                    currentPattern = null;
                    currentMultiline = false;
                    currentCaseInsensitive = false;
                    continue;
                }

                if (line.startsWith("Name:")) {
                    currentName = line.substring(5).trim();
                } else if (line.startsWith("Pattern:")) {
                    currentPattern = line.substring(8).trim();
                } else if (line.startsWith("Multiline:")) {
                    currentMultiline = Boolean.parseBoolean(line.substring(10).trim());
                } else if (line.startsWith("Case Insensitive:")) {
                    currentCaseInsensitive = Boolean.parseBoolean(line.substring(16).trim());
                }
            }

            // Don't forget to process the last entry if file doesn't end with empty line
            if (currentName != null && currentPattern != null) {
                PatternEntry entry = new PatternEntry(
                        currentName, currentPattern, currentMultiline, currentCaseInsensitive);

                if (addPattern(entry)) {
                    importCount++;
                }
            }

            return importCount;
        } catch (IOException e) {
            System.err.println("Error importing patterns: " + e.getMessage());
            return importCount;
        }
    }

    /**
     * Creates a common regex pattern collection with useful predefined patterns
     *
     * @return A PatternManager with common patterns
     */
    public static PatternManager createCommonPatternCollection() {
        List<PatternEntry> commonPatterns = new ArrayList<>();

        commonPatterns.add(new PatternEntry(
                "Email Address",
                "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
                false, true));

        commonPatterns.add(new PatternEntry(
                "URL",
                "https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_+.~#?&/=]*)",
                false, true));

        commonPatterns.add(new PatternEntry(
                "IP Address",
                "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b",
                false, false));

        commonPatterns.add(new PatternEntry(
                "US Phone Number",
                "\\(?\\d{3}\\)?[-. ]?\\d{3}[-. ]?\\d{4}",
                false, false));

        commonPatterns.add(new PatternEntry(
                "Date (MM/DD/YYYY)",
                "\\b(0?[1-9]|1[0-2])/(0?[1-9]|[12]\\d|3[01])/\\d{4}\\b",
                false, false));

        commonPatterns.add(new PatternEntry(
                "HTML Tag",
                "<([a-zA-Z][a-zA-Z0-9]*)[^>]*>.*?</\\1>",
                true, true));

        commonPatterns.add(new PatternEntry(
                "Credit Card Number",
                "\\b(?:\\d{4}[-. ]?){3}\\d{4}\\b",
                false, false));

        commonPatterns.add(new PatternEntry(
                "ZIP Code",
                "\\b\\d{5}(?:-\\d{4})?\\b",
                false, false));

        commonPatterns.add(new PatternEntry(
                "Social Security Number",
                "\\b\\d{3}[-. ]?\\d{2}[-. ]?\\d{4}\\b",
                false, false));

        commonPatterns.add(new PatternEntry(
                "Hex Color Code",
                "#[0-9a-fA-F]{6}",
                false, true));

        return new PatternManager(commonPatterns);
    }

    /**
     * Applies all patterns to a text and returns all matches organized by pattern
     *
     * @param text The text to process
     * @return Map of pattern names to their match lists
     */
    public Map<String, List<String>> processTextWithAllPatterns(String text) {
        if (text == null || text.isEmpty() || patterns.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<String>> results = new HashMap<>();

        for (PatternEntry entry : patterns) {
            try {
                int flags = 0;
                if (entry.isMultiline()) {
                    flags |= java.util.regex.Pattern.MULTILINE;
                }
                if (entry.isCaseInsensitive()) {
                    flags |= java.util.regex.Pattern.CASE_INSENSITIVE;
                }

                List<String> matches = TextProcessor.findMatches(text, entry.getPattern(), flags);
                results.put(entry.getName(), matches);
            } catch (PatternSyntaxException e) {
                // Skip invalid patterns but log the error
                System.err.println("Invalid pattern '" + entry.getName() + "': " + e.getMessage());
                results.put(entry.getName(), new ArrayList<>());
            }
        }

        return results;
    }
}
