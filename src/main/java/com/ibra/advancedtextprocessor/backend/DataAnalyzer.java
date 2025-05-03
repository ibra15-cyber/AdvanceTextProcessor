package com.ibra.advancedtextprocessor.backend;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class for analyzing text data using Stream API and regex
 */
public class DataAnalyzer {

    /**
     * Performs word frequency analysis on the given text
     *
     * @param text The text to analyze
     * @param minWordLength Minimum word length to include
     * @return Map of words to their frequencies, sorted by frequency (descending)
     */
    public static Map<String, Long> wordFrequencyAnalysis(String text, int minWordLength) {
        if (text == null || text.isEmpty()) {
            return new LinkedHashMap<>();
        }

        // Convert to lowercase and split into words
        String[] words = text.toLowerCase().split("\\s+");

        // Use streams to process and count words
        Map<String, Long> frequencyMap = Arrays.stream(words)
                // Remove non-alphabetic characters
                .map(word -> word.replaceAll("[^a-zA-Z]", ""))
                // Filter out empty strings and words shorter than minWordLength
                .filter(word -> !word.isEmpty() && word.length() >= minWordLength)
                // Group-by-word and count occurrences
                .collect(Collectors.groupingBy(
                        word -> word,
                        Collectors.counting()
                ));

        // Sort by frequency (descending)
        return frequencyMap.entrySet().stream()
                .sorted(Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


    public static String summarizeText(String text, int maxSentences) {
        if (text == null || text.isEmpty() || maxSentences < 1) {
            return "";
        }

        // Split text into sentences
        String[] sentences = text.split("[.!?]+\\s*");

        if (sentences.length <= maxSentences) {
            return text; // Already short enough
        }

        // Calculate word frequencies across the entire text
        Map<String, Long> wordFrequencies = wordFrequencyAnalysis(text, 4);

        // Score sentences based on word importance
        Map<String, Double> sentenceScores = new HashMap<>();

        for (String sentence : sentences) {
            if (sentence.trim().split("\\s+").length < 3) {
                continue; // Skip very short sentences
            }

            double score = 0;
            String[] words = sentence.toLowerCase().split("\\s+");

            for (String word : words) {
                String cleanWord = word.replaceAll("[^a-zA-Z]", "");
                if (!cleanWord.isEmpty() && cleanWord.length() >= 4) {
                    score += wordFrequencies.getOrDefault(cleanWord, 0L);
                }
            }

            // Normalize by sentence length to avoid bias towards longer sentences
            score = words.length > 0 ? score / words.length : 0;
            sentenceScores.put(sentence, score);
        }

        // Select top scoring sentences
        List<String> topSentences = sentenceScores.entrySet().stream()
                .sorted(Entry.<String, Double>comparingByValue().reversed())
                .limit(maxSentences)
                .map(Entry::getKey)
                .collect(Collectors.toList());

        // Find original indices to maintain original order
        Map<String, Integer> sentenceToIndex = new HashMap<>();
        for (int i = 0; i < sentences.length; i++) {
            sentenceToIndex.put(sentences[i], i);
        }

        // Sort by original position in text
        topSentences.sort(Comparator.comparing(s -> sentenceToIndex.getOrDefault(s, 0)));

        // Join sentences back together
        return String.join(". ", topSentences) + ".";
    }

    /**
     * Extracts emails from text using regex and streams
     *
     * @param text The text to process
     * @return List of extracted email addresses
     */
    public static List<String> extractEmails(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        // Regex pattern for email addresses
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
        java.util.regex.Matcher matcher = emailPattern.matcher(text);

        List<String> emails = new ArrayList<>();
        while (matcher.find()) {
            emails.add(matcher.group());
        }

        return emails.stream()
                .distinct() // Remove duplicates
                .collect(Collectors.toList());
    }

    /**
     * Extracts URLs from text using regex and streams
     *
     * @param text The text to process
     * @return List of extracted URLs
     */
    public static List<String> extractUrls(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        // Regex pattern for URLs
        Pattern urlPattern = Pattern.compile(
                "https?://[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]"
        );
        java.util.regex.Matcher matcher = urlPattern.matcher(text);

        List<String> urls = new ArrayList<>();
        while (matcher.find()) {
            urls.add(matcher.group());
        }

        return urls.stream()
                .distinct() // Remove duplicates
                .collect(Collectors.toList());
    }
}
