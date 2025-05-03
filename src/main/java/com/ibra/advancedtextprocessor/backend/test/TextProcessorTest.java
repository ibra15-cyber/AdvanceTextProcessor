package com.ibra.advancedtextprocessor.backend.test;


import com.ibra.advancedtextprocessor.backend.TextProcessor;
import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.*;

public class TextProcessorTest {

    @Test
    public void testFindMatches() {
        String text = "The quick brown fox jumps over the lazy dog";

        // Basic matching
        List<String> matches = TextProcessor.findMatches(text, "\\b\\w{4}\\b", 0);
        assertEquals(3, matches.size());
        assertTrue(matches.contains("quick"));
        assertTrue(matches.contains("jumps"));
        assertTrue(matches.contains("lazy"));

        // Case insensitive
        matches = TextProcessor.findMatches(text, "\\bthe\\b", Pattern.CASE_INSENSITIVE);
        assertEquals(2, matches.size());

        // Empty cases
        assertTrue(TextProcessor.findMatches("", "pattern", 0).isEmpty());
        assertTrue(TextProcessor.findMatches(text, "", 0).isEmpty());
        assertTrue(TextProcessor.findMatches(null, "pattern", 0).isEmpty());
    }

    @Test(expected = PatternSyntaxException.class)
    public void testFindMatchesInvalidPattern() {
        TextProcessor.findMatches("text", "[invalid", 0);
    }

    @Test
    public void testHighlightMatches() {
        String text = "Hello world! Welcome to the world of programming.";

        // Basic highlighting
        String highlighted = TextProcessor.highlightMatches(text, "world", "**", "**", 0);
        assertEquals("Hello **world**! Welcome to the **world** of programming.", highlighted);

        // Case insensitive
        highlighted = TextProcessor.highlightMatches(text, "WORLD", "[[", "]]", Pattern.CASE_INSENSITIVE);
        assertTrue(highlighted.contains("[[world]]"));

        // Empty cases
        assertEquals("text", TextProcessor.highlightMatches("text", "", "<", ">", 0));
        assertEquals(null, TextProcessor.highlightMatches(null, "pattern", "<", ">", 0));
    }

    @Test(expected = PatternSyntaxException.class)
    public void testHighlightMatchesInvalidPattern() {
        TextProcessor.highlightMatches("text", "[invalid", "<", ">", 0);
    }

    @Test
    public void testReplaceAll() {
        String text = "The quick brown fox jumps over the lazy dog";

        // Basic replacement
        String replaced = TextProcessor.replaceAll(text, "\\b\\w{4}\\b", "****", 0);
        assertEquals("The **** brown fox **** over the **** dog", replaced);

        // Group replacement
        replaced = TextProcessor.replaceAll("John Doe", "(\\w+) (\\w+)", "$2, $1", 0);
        assertEquals("Doe, John", replaced);

        // Empty cases
        assertEquals("text", TextProcessor.replaceAll("text", "", "replacement", 0));
        assertEquals(null, TextProcessor.replaceAll(null, "pattern", "replacement", 0));
        assertEquals("text", TextProcessor.replaceAll("text", "pattern", null, 0));
    }

    @Test(expected = PatternSyntaxException.class)
    public void testReplaceAllInvalidPattern() {
        TextProcessor.replaceAll("text", "[invalid", "replacement", 0);
    }

    @Test
    public void testIsValidPattern() {
        assertTrue(TextProcessor.isValidPattern("\\d+"));
        assertTrue(TextProcessor.isValidPattern("[A-Za-z]"));
        assertFalse(TextProcessor.isValidPattern("[invalid"));
        assertFalse(TextProcessor.isValidPattern(""));
        assertFalse(TextProcessor.isValidPattern(null));
    }

    @Test
    public void testGetDetailedMatches() {
        String text = "John: 123-456-7890, Jane: 987-654-3210";

        // Test with capturing groups
        List<TextProcessor.MatchInfo> matches = TextProcessor.getDetailedMatches(
                text, "(\\w+): (\\d{3})-(\\d{3})-(\\d{4})", 0);

        assertEquals(2, matches.size());

        // Verify first match
        TextProcessor.MatchInfo first = matches.get(0);
        assertEquals("John: 123-456-7890", first.getMatchText());
        assertEquals(0, first.getStartPosition());
        assertEquals(17, first.getEndPosition());
        assertEquals("John", first.getGroup(1));
        assertEquals("123", first.getGroup(2));
        assertEquals("456", first.getGroup(3));
        assertEquals("7890", first.getGroup(4));

        // Test empty cases
        assertTrue(TextProcessor.getDetailedMatches("", "pattern", 0).isEmpty());
        assertTrue(TextProcessor.getDetailedMatches(text, "", 0).isEmpty());
        assertTrue(TextProcessor.getDetailedMatches(null, "pattern", 0).isEmpty());
    }

    @Test(expected = PatternSyntaxException.class)
    public void testGetDetailedMatchesInvalidPattern() {
        TextProcessor.getDetailedMatches("text", "[invalid", 0);
    }

    @Test
    public void testMatchInfo() {
        TextProcessor.MatchInfo info = new TextProcessor.MatchInfo(
                "test", 10, 14, new String[]{"test", "est"});

        assertEquals("test", info.getMatchText());
        assertEquals(10, info.getStartPosition());
        assertEquals(14, info.getEndPosition());
        assertEquals("test", info.getGroup(0));
        assertEquals("est", info.getGroup(1));
        assertNull(info.getGroup(2));
        assertTrue(info.toString().contains("pos: 10-14"));
    }
}
