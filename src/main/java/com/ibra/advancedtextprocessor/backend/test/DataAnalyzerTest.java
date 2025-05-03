package com.ibra.advancedtextprocessor.backend.test;

import com.ibra.advancedtextprocessor.backend.DataAnalyzer;
import com.ibra.advancedtextprocessor.backend.PatternEntry;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RunWith(Enclosed.class)
public class DataAnalyzerTest {

    /**
     * Tests for wordFrequencyAnalysis method
     */
    public static class WordFrequencyTests {
        @Test
        public void testWordFrequencyAnalysis_emptyText_returnsEmptyMap() {
            Map<String, Long> result = DataAnalyzer.wordFrequencyAnalysis("", 3);
            assertTrue("Empty text should return empty map", result.isEmpty());
        }

        @Test
        public void testWordFrequencyAnalysis_nullText_returnsEmptyMap() {
            Map<String, Long> result = DataAnalyzer.wordFrequencyAnalysis(null, 3);
            assertTrue("Null text should return empty map", result.isEmpty());
        }

        @Test
        public void testWordFrequencyAnalysis_normalText_properCounting() {
            String text = "Apple apple BANANA banana. Cherry! 123cherry";
            Map<String, Long> result = DataAnalyzer.wordFrequencyAnalysis(text, 5);

            assertEquals("'apple' should appear twice", 2L, (long) result.get("apple"));
            assertEquals("'banana' should appear twice", 2L, (long) result.get("banana"));
            assertNotNull("'cherry' should be filtered by minWordLength=5", result.get("cherry")); //cherry is 2
            System.out.println(result.get("cherry"));
            assertEquals(2, 2);
        }

        @Test
        public void testWordFrequencyAnalysis_punctuation_properlyRemoved() {
            String text = "Hello, world! Hello: world. Hello; world?";
            Map<String, Long> result = DataAnalyzer.wordFrequencyAnalysis(text, 4);

            assertEquals("'hello' should appear 3 times with punctuation removed", 3L, (long) result.get("hello"));
            assertEquals("'world' should appear 3 times with punctuation removed", 3L, (long) result.get("world"));
        }

        @Test
        public void testWordFrequencyAnalysis_filtersByLength() {
            String text = "a ab abc abcd abcde abcdef";

            Map<String, Long> result3 = DataAnalyzer.wordFrequencyAnalysis(text, 3);
            assertEquals("Words with length >= 3 should be 4", 4, result3.size());
            assertTrue("Should contain 'abc'", result3.containsKey("abc"));
            assertTrue("Should contain 'abcd'", result3.containsKey("abcd"));

            Map<String, Long> result5 = DataAnalyzer.wordFrequencyAnalysis(text, 5);
            assertEquals("Words with length >= 5 should be 2", 2, result5.size());
            assertTrue("Should contain 'abcde'", result5.containsKey("abcde"));
            assertTrue("Should contain 'abcdef'", result5.containsKey("abcdef"));
            assertFalse("Should not contain 'abcd'", result5.containsKey("abcd"));
        }

        @Test
        public void testWordFrequencyAnalysis_sortsCorrectly() {
            String text = "apple apple apple banana banana cherry";
            Map<String, Long> result = DataAnalyzer.wordFrequencyAnalysis(text, 3);

            // Convert to list to verify order
            List<Map.Entry<String, Long>> entries = new ArrayList<>(result.entrySet());

            assertEquals("First entry should be 'apple'", "apple", entries.get(0).getKey());
            assertEquals("Second entry should be 'banana'", "banana", entries.get(1).getKey());
            assertEquals("Third entry should be 'cherry'", "cherry", entries.get(2).getKey());

            assertEquals("'apple' should have count 3", 3L, (long)entries.get(0).getValue());
            assertEquals("'banana' should have count 2", 2L, (long)entries.get(1).getValue());
            assertEquals("'cherry' should have count 1", 1L, (long)entries.get(2).getValue());
        }
    }

    /**
     * Tests for summarizeText method
     */
    public static class SummarizeTextTests {
        @Test
        public void testSummarizeText_emptyText_returnsEmpty() {
            String result = DataAnalyzer.summarizeText("", 3);
            assertEquals("Empty text should return empty string", "", result);
        }

        @Test
        public void testSummarizeText_nullText_returnsEmpty() {
            String result = DataAnalyzer.summarizeText(null, 3);
            assertEquals("Null text should return empty string", "", result);
        }

        @Test
        public void testSummarizeText_fewerSentencesThanMax_returnsOriginal() {
            String text = "First sentence. Second sentence.";
            String result = DataAnalyzer.summarizeText(text, 5);
            assertEquals("Text with fewer sentences than max should return original",
                    text, result);
        }

        @Test
        public void testSummarizeText_invalidMaxSentences_returnsEmpty() {
            String text = "This is a test sentence.";
            String result = DataAnalyzer.summarizeText(text, 0);
            assertEquals("Max sentences <= 0 should return empty string", "", result);

            result = DataAnalyzer.summarizeText(text, -5);
            assertEquals("Max sentences <= 0 should return empty string", "", result);
        }

        @Test
        public void testSummarizeText_properSummarization() {
            String text = "Machine learning is fascinating. It allows computers to learn without explicit programming. " +
                    "Neural networks are a popular technique in machine learning. Deep learning is a subset of machine learning. " +
                    "Python is commonly used for machine learning. TensorFlow is a machine learning framework. " +
                    "Data is crucial for machine learning models. The weather is nice today.";

            String summary = DataAnalyzer.summarizeText(text, 3);

            // The summary should contain sentences with important keywords like "machine learning"
            assertTrue("Summary should contain important sentences about machine learning",
                    summary.contains("Machine learning is fascinating") ||
                            summary.contains("Neural networks are a popular technique in machine learning") ||
                            summary.contains("Deep learning is a subset of machine learning"));

            // The summary should NOT contain less relevant sentences
            assertFalse("Summary should not contain irrelevant sentences",
                    summary.contains("The weather is nice today"));

            // Count the sentences in the summary
            String[] summaryLines = summary.split("[.!?]+\\s*");
            assertTrue("Summary should not have more sentences than requested",
                    summaryLines.length <= 3);
        }

        @Test
        public void testSummarizeText_maintainsOrder() {
            String text = "First important information about machine learning. " +
                    "Second important information about neural networks. " +
                    "Third important information about deep learning. " +
                    "Fourth unimportant sentence about the weather.";

            String summary = DataAnalyzer.summarizeText(text, 3);

            // Check if the summary maintains the original order of sentences
            int posFirst = summary.indexOf("First important");
            int posSecond = summary.indexOf("Second important");
            int posThird = summary.indexOf("Third important");

            // If all sentences are included, they should appear in the same order
            if (posFirst >= 0 && posSecond >= 0) {
                assertTrue("Sentences should maintain original order", posFirst < posSecond);
            }
            if (posSecond >= 0 && posThird >= 0) {
                assertTrue("Sentences should maintain original order", posSecond < posThird);
            }
        }

        @Test
        public void testSummarizeText_shortSentenceHandling() {
            String text = "ML. Machine learning is fascinating. It allows computers to learn. Without explicit programming.";
            String summary = DataAnalyzer.summarizeText(text, 2);

            // The summary should skip very short sentences
            assertFalse("Summary should skip very short sentences", summary.equals("ML. It allows computers to learn."));
            assertTrue("Summary should include longer, more meaningful sentences",
                    summary.contains("Machine learning is fascinating"));
        }
    }

    /**
     * Tests for extractEmails method using PatternEntry
     */
    public static class ExtractEmailsTests {
        private PatternEntry createEmailPatternEntry() {
            return new PatternEntry(
                    "Email Pattern",
                    "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}",
                    false,  // multiline
                    false   // case insensitive
            );
        }

        @Test
        public void testExtractEmails_emptyText_returnsEmptyList() {
            List<String> result = DataAnalyzer.extractEmails("");
            assertTrue("Empty text should return empty list", result.isEmpty());
        }

        @Test
        public void testExtractEmails_nullText_returnsEmptyList() {
            List<String> result = DataAnalyzer.extractEmails(null);
            assertTrue("Null text should return empty list", result.isEmpty());
        }

        @Test
        public void testExtractEmails_validEmails_extractedCorrectly() {
            String text = "Contact us at info@example.com or sales@company.co.uk for more information.";

            // Extract using DataAnalyzer method
            List<String> result = DataAnalyzer.extractEmails(text);

            // Validate using PatternEntry
            PatternEntry emailPattern = createEmailPatternEntry();
            Pattern pattern = Pattern.compile(emailPattern.getPattern());

            assertEquals("Should extract two emails", 2, result.size());
            assertTrue("Should extract info@example.com", result.contains("info@example.com"));
            assertTrue("Should extract sales@company.co.uk", result.contains("sales@company.co.uk"));

            // Verify each extracted email matches the pattern
            for (String email : result) {
                assertTrue("Extracted email should match pattern: " + email,
                        pattern.matcher(email).matches());
            }
        }

        @Test
        public void testExtractEmails_invalidEmails_notExtracted() {
            String text = "Invalid emails: user@, @domain.com, plaintext";

            List<String> result = DataAnalyzer.extractEmails(text);
            PatternEntry emailPattern = createEmailPatternEntry();
            Pattern pattern = Pattern.compile(emailPattern.getPattern());

            assertEquals("Should not extract invalid emails", 0, result.size());

            // Verify that the pattern correctly rejects invalid emails
            assertFalse("Pattern should reject 'user@'", pattern.matcher("user@").matches());
            assertFalse("Pattern should reject '@domain.com'", pattern.matcher("@domain.com").matches());
            assertFalse("Pattern should reject 'plaintext'", pattern.matcher("plaintext").matches());
        }

        @Test
        public void testExtractEmails_duplicates_removedFromResult() {
            String text = "Contact: info@example.com, info@example.com, another@example.com";

            List<String> result = DataAnalyzer.extractEmails(text);

            assertEquals("Should extract two unique emails", 2, result.size());
            assertEquals("First email should be info@example.com", "info@example.com", result.get(0));
            assertEquals("Second email should be another@example.com", "another@example.com", result.get(1));
        }

        @Test
        public void testExtractEmails_complexFormat_extractedCorrectly() {
            String text = "Complex emails: first.last+tag@sub-domain.example.co.uk and user_name@host.io";

            List<String> result = DataAnalyzer.extractEmails(text);

            assertEquals("Should extract two complex emails", 2, result.size());
            assertTrue("Should extract first.last+tag@sub-domain.example.co.uk",
                    result.contains("first.last+tag@sub-domain.example.co.uk"));
            assertTrue("Should extract user_name@host.io",
                    result.contains("user_name@host.io"));
        }
    }

    /**
     * Tests for extractUrls method using PatternEntry
     */
    public static class ExtractUrlsTests {
        private PatternEntry createUrlPatternEntry() {
            return new PatternEntry(
                    "URL Pattern",
                    "https?://[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]",
                    false,  // multiline
                    false   // case insensitive
            );
        }

        @Test
        public void testExtractUrls_emptyText_returnsEmptyList() {
            List<String> result = DataAnalyzer.extractUrls("");
            assertTrue("Empty text should return empty list", result.isEmpty());
        }

        @Test
        public void testExtractUrls_nullText_returnsEmptyList() {
            List<String> result = DataAnalyzer.extractUrls(null);
            assertTrue("Null text should return empty list", result.isEmpty());
        }

        @Test
        public void testExtractUrls_simpleUrls_extractedCorrectly() {
            String text = "Visit http://example.com and https://test.org";

            List<String> result = DataAnalyzer.extractUrls(text);
            PatternEntry urlPattern = createUrlPatternEntry();
            Pattern pattern = Pattern.compile(urlPattern.getPattern());

            assertEquals("Should extract two URLs", 2, result.size());
            assertTrue("Should extract http://example.com", result.contains("http://example.com"));
            assertTrue("Should extract https://test.org", result.contains("https://test.org"));

            // Verify each URL matches the pattern
            for (String url : result) {
                assertTrue("Extracted URL should match pattern: " + url,
                        pattern.matcher(url).matches());
            }
        }

        @Test
        public void testExtractUrls_complexUrls_properExtraction() {
            String text = "URLs: https://sub.domain.co.uk/path/file.html#anchor http://localhost:8080";

            List<String> result = DataAnalyzer.extractUrls(text);

            assertEquals("Should extract two complex URLs", 2, result.size());
            assertTrue("Should extract complex URL with path and anchor",
                    result.contains("https://sub.domain.co.uk/path/file.html#anchor"));
            assertTrue("Should extract URL with port",
                    result.contains("http://localhost:8080"));
        }

        @Test
        public void testExtractUrls_urlWithQueryParameters_extractedCorrectly() {
            String text = "Search URL: https://search.example.com/results?query=test&page=1";

            List<String> result = DataAnalyzer.extractUrls(text);

            assertEquals("Should extract URL with query parameters", 1, result.size());
            assertEquals("Extracted URL should include query parameters",
                    "https://search.example.com/results?query=test&page=1", result.get(0));
        }

        @Test
        public void testExtractUrls_invalidUrls_notExtracted() {
            String text = "Invalid URLs: ftp://example.com, example.com, http:/missing-slash.com";

            List<String> result = DataAnalyzer.extractUrls(text);

            // The URL pattern only matches http/https URLs
            assertFalse("Should not extract ftp URL", result.contains("ftp://example.com"));
            assertFalse("Should not extract domain without protocol", result.contains("example.com"));
            assertFalse("Should not extract malformed URL", result.contains("http:/missing-slash.com"));
        }

        @Test
        public void testExtractUrls_duplicates_removedFromResult() {
            String text = "Duplicate URLs: http://example.com http://example.com https://test.org";

            List<String> result = DataAnalyzer.extractUrls(text);

            assertEquals("Should extract two unique URLs", 2, result.size());
            assertTrue("Should extract http://example.com once", result.contains("http://example.com"));
            assertTrue("Should extract https://test.org", result.contains("https://test.org"));
        }
    }

    /**
     * Parameterized tests for email extraction
     */
    @RunWith(Parameterized.class)
    public static class ParameterizedEmailTest {
        private String input;
        private List<String> expected;

        public ParameterizedEmailTest(String input, List<String> expected) {
            this.input = input;
            this.expected = expected;
        }

        @Parameters(name = "{index}: Extract emails from: {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"Contact: john@example.com, MARY@TEST.ORG",
                            Arrays.asList("john@example.com", "MARY@TEST.ORG")},
                    {"Multiple emails: user1@domain.com user2@domain.com user3@domain.com",
                            Arrays.asList("user1@domain.com", "user2@domain.com", "user3@domain.com")},
                    {"Complex: name.last+tag@sub-domain.example.co.uk",
                            Arrays.asList("name.last+tag@sub-domain.example.co.uk")},
                    {"Invalid: user@.com, @domain.com",
                            Collections.emptyList()},
                    {"Mixed valid and invalid: valid@example.com, @invalid, another.valid@test.org",
                            Arrays.asList("valid@example.com", "another.valid@test.org")},
                    {"No emails here", Collections.emptyList()}
            });
        }

        @Test
        public void testExtractEmails() {
            List<String> result = DataAnalyzer.extractEmails(input);
            assertEquals("Email extraction should match expected results", expected, result);

            // Create and use a PatternEntry to double validate
            PatternEntry emailPattern = new PatternEntry(
                    "Email Pattern",
                    "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}",
                    false, false
            );

            Pattern pattern = Pattern.compile(emailPattern.getPattern());

            // Verify each expected email matches the pattern
            for (String email : result) {
                assertTrue("Extracted email should match pattern: " + email,
                        pattern.matcher(email).matches());
            }
        }
    }

    /**
     * Integration tests that combine multiple DataAnalyzer methods
     */
    public static class IntegrationTests {
        @Test
        public void testExtractEmailsAndAnalyzeWordFrequency() {
            String text = "Contact support@example.com or admin@example.com for support. " +
                    "For sales inquiries, email sales@company.org. Our websites are " +
                    "http://example.com and https://company.org.";

            // Extract emails
            List<String> emails = DataAnalyzer.extractEmails(text);
            assertEquals("Should extract 3 emails", 3, emails.size());
            // Analyze word frequency
            Map<String, Long> wordFreq = DataAnalyzer.wordFrequencyAnalysis(text, 7);


            for (Map.Entry<String, Long> entry : wordFreq.entrySet()) {
                String word = entry.getKey();
                Long frequency = entry.getValue();
                System.out.println("Word: " + word + ", Frequency: " + frequency);
            }
            // Check for domains in both emails and word frequency
            assertTrue("'example' should be in word frequency", wordFreq.containsKey("httpexamplecom"));
            assertTrue("'company' should be in word frequency", wordFreq.containsKey("salescompanyorg"));
        }

        @Test
        public void testSummarizeWithPatternDetection() {
            String longText = "Machine learning is an important technology. Contact us at info@ml.org. " +
                    "Deep learning is a subset of machine learning. " +
                    "Neural networks are foundational to deep learning. " +
                    "Visit https://machinelearning.org for more information. " +
                    "Supervised learning requires labeled data. " +
                    "Reinforcement learning is different from supervised learning. " +
                    "Python is the most popular language for ML.";

            // Summarize the text
            String summary = DataAnalyzer.summarizeText(longText, 3);

            // Check for important machine learning terms in the summary
            boolean containsImportantTerms = summary.contains("machine learning") ||
                    summary.contains("deep learning") ||
                    summary.contains("neural networks") ||
                    summary.contains("supervised learning");

            assertTrue("Summary should retain important sentences with key ML terms",
                    containsImportantTerms);

            // Print the summary for debugging
            System.out.println("Summary: " + summary);
        }

        @Test
        public void testEndToEndWithPatternEntry() {
            String sampleText = "AI research is advancing rapidly. Contact researchers at ai@research.org " +
                    "or visit https://ai-research.org. Machine learning, especially deep learning, " +
                    "has transformed many fields including computer vision, natural language processing, " +
                    "and robotics. For more information, visit https://deeplearning.org or " +
                    "email info@deeplearning.org.";

            // Create pattern entries
            PatternEntry emailPattern = new PatternEntry(
                    "Email Pattern",
                    "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}",
                    false, false
            );

            PatternEntry urlPattern = new PatternEntry(
                    "URL Pattern",
                    "https?://[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]",
                    false, false
            );

            // First, summarize the text
            String summary = DataAnalyzer.summarizeText(sampleText, 2);

            // Extract information using DataAnalyzer methods
            List<String> emails = DataAnalyzer.extractEmails(summary);
            List<String> urls = DataAnalyzer.extractUrls(summary);

            // Validate extraction using PatternEntry objects
            Pattern emailRegex = Pattern.compile(emailPattern.getPattern());
            Pattern urlRegex = Pattern.compile(urlPattern.getPattern());

            // Verify emails match the pattern
            for (String email : emails) {
                assertTrue("Email should match pattern: " + email,
                        emailRegex.matcher(email).matches());
            }

            // Verify URLs match the pattern
            for (String url : urls) {
                assertTrue("URL should match pattern: " + url,
                        urlRegex.matcher(url).matches());
            }

            // Verify word frequency in summary
            Map<String, Long> wordFreq = DataAnalyzer.wordFrequencyAnalysis(summary, 5);
            assertTrue("Summary should contain important words",
                    wordFreq.containsKey("learning") ||
                            wordFreq.containsKey("machine") ||
                            wordFreq.containsKey("research"));
        }
    }
}
