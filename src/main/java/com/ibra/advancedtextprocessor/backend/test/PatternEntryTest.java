package com.ibra.advancedtextprocessor.backend.test;


import com.ibra.advancedtextprocessor.backend.PatternEntry;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

public class PatternEntryTest {
    @Test
    public void testConstructorAndGetters() {
        PatternEntry entry = new PatternEntry("Test Pattern", "\\d+", true, false);
        assertEquals("Test Pattern", entry.getName());
        assertEquals("\\d+", entry.getPattern());
        assertTrue(entry.isMultiline());
        assertFalse(entry.isCaseInsensitive());
    }

    @Test
    public void testSetters() {
        PatternEntry entry = new PatternEntry("", "", false, false);
        entry.setName("Email Pattern");
        entry.setPattern("[a-z]+@[a-z]+\\.[a-z]+");
        entry.setMultiline(true);
        entry.setCaseInsensitive(true);
        assertEquals("Email Pattern", entry.getName());
        assertEquals("[a-z]+@[a-z]+\\.[a-z]+", entry.getPattern());
        assertTrue(entry.isMultiline());
        assertTrue(entry.isCaseInsensitive());
    }

    @Test
    public void testToString() {
        PatternEntry entry = new PatternEntry("URL Pattern", "https?://.*", false, true);
        assertEquals("URL Pattern", entry.toString());
    }

    @Test
    public void testEquals() {
        PatternEntry entry1 = new PatternEntry("Pattern1", "\\w+", true, false);
        PatternEntry entry2 = new PatternEntry("Pattern1", "\\w+", true, false);
        PatternEntry entry3 = new PatternEntry("Pattern2", "\\d+", false, true);
        // Reflexive
        assertTrue(entry1.equals(entry1));
        // Symmetric
        assertTrue(entry1.equals(entry2));
        assertTrue(entry2.equals(entry1));
        // Different values
        assertFalse(entry1.equals(entry3));
        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals("Not a PatternEntry"));
    }

    @Test
    public void testHashCode() {
        PatternEntry entry1 = new PatternEntry("Test", "pattern", true, false);
        PatternEntry entry2 = new PatternEntry("Test", "pattern", true, false);
        PatternEntry entry3 = new PatternEntry("Different", "pattern", true, false);
        // Equal objects must have equal hash codes
        assertEquals(entry1.hashCode(), entry2.hashCode());
        // Ideally, unequal objects should have different hash codes
        assertNotEquals(entry1.hashCode(), entry3.hashCode());
        // Consistent with Objects.hash()
        int expectedHash = Objects.hash("Test", "pattern", true, false);
        assertEquals(expectedHash, entry1.hashCode());
    }

    @Test
    public void testEqualsWithNullName() {
        PatternEntry entry1 = new PatternEntry(null, "pattern", true, false);
        PatternEntry entry2 = new PatternEntry(null, "pattern", true, false);
        PatternEntry entry3 = new PatternEntry("Name", "pattern", true, false);
        assertTrue(entry1.equals(entry2));
        assertFalse(entry1.equals(entry3));
    }

    @Test
    public void testEqualsWithNullPattern() {
        PatternEntry entry1 = new PatternEntry("Name", null, true, false);
        PatternEntry entry2 = new PatternEntry("Name", null, true, false);
        PatternEntry entry3 = new PatternEntry("Name", "pattern", true, false);
        assertTrue(entry1.equals(entry2));
        assertFalse(entry1.equals(entry3));
    }

    // New practical validation tests

    @Test
    public void testEmailPatternValidation() {
        // Common email regex pattern
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        PatternEntry emailEntry = new PatternEntry("Email Pattern", emailPattern, false, false);

        // Pattern compiled based on PatternEntry properties
        Pattern pattern = createPatternFromEntry(emailEntry);

        // Valid emails should match
        assertTrue("Valid email should match", pattern.matcher("user@example.com").matches());
        assertTrue("Valid email with numbers should match", pattern.matcher("user123@example.com").matches());
        assertTrue("Valid email with dots should match", pattern.matcher("first.last@example.com").matches());
        assertTrue("Valid email with plus should match", pattern.matcher("user+tag@example.com").matches());
        assertTrue("Valid email with subdomain should match", pattern.matcher("user@sub.example.com").matches());

        // Invalid emails should not match
        assertFalse("Email without @ should not match", pattern.matcher("userexample.com").matches());
        assertFalse("Email without domain should not match", pattern.matcher("user@").matches());
        assertFalse("Email without username should not match", pattern.matcher("@example.com").matches());
        assertFalse("Email with invalid characters should not match", pattern.matcher("user*@example.com").matches());
        assertFalse("Email with incomplete domain should not match", pattern.matcher("user@example").matches());
    }

    @Test
    public void testEmailPatternValidationCaseInsensitive() {
        // Same pattern but with case insensitivity
        String emailPattern = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";
        PatternEntry emailEntry = new PatternEntry("Email Pattern", emailPattern, false, true);

        Pattern pattern = createPatternFromEntry(emailEntry);

        // Should match both lowercase and uppercase with case insensitive flag
        assertTrue("Lowercase email should match", pattern.matcher("user@example.com").matches());
        assertTrue("Uppercase email should match with case insensitivity",
                pattern.matcher("USER@EXAMPLE.COM").matches());
        assertTrue("Mixed case email should match with case insensitivity",
                pattern.matcher("User@Example.Com").matches());
    }

    @Test
    public void testMultilineEmailList() {
        // Pattern to match email addresses in multiple lines
        String multilineEmailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        PatternEntry multilineEntry = new PatternEntry("Multiline Emails", multilineEmailPattern, true, false);

        Pattern pattern = createPatternFromEntry(multilineEntry);

        String multilineInput = "john.doe@example.com\n" +
                "invalid-email\n" +
                "jane.smith@company.org";

        // With multiline flag, we need to test each line separately or use find() method
        String[] lines = multilineInput.split("\n");
        assertTrue("First line should match", pattern.matcher(lines[0]).matches());
        assertFalse("Second line should not match", pattern.matcher(lines[1]).matches());
        assertTrue("Third line should match", pattern.matcher(lines[2]).matches());
    }

    @Test
    public void testURLPatternValidation() {
        String urlPattern = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$";
        PatternEntry urlEntry = new PatternEntry("URL Pattern", urlPattern, false, true);

        Pattern pattern = createPatternFromEntry(urlEntry);

        // Valid URLs
        assertTrue("Simple URL should match", pattern.matcher("http://example.com").matches());
        assertTrue("HTTPS URL should match", pattern.matcher("https://example.com").matches());
        assertTrue("URL with path should match", pattern.matcher("https://example.com/path/to/resource").matches());
        assertTrue("URL without protocol should match", pattern.matcher("example.com").matches());

        // Invalid URLs
        assertFalse("Malformed URL should not match", pattern.matcher("http:/example.com").matches());
        assertFalse("Invalid protocol should not match", pattern.matcher("ftp://example.com").matches());
    }

    @Test
    public void testPhoneNumberValidation() {
        // US phone number pattern
        String phonePattern = "^\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$";
        PatternEntry phoneEntry = new PatternEntry("Phone Number", phonePattern, false, false);

        Pattern pattern = createPatternFromEntry(phoneEntry);

        // Valid phone numbers
        assertTrue("Standard format should match", pattern.matcher("123-456-7890").matches());
        assertTrue("Parentheses format should match", pattern.matcher("(123) 456-7890").matches());
        assertTrue("Period separator should match", pattern.matcher("123.456.7890").matches());
        assertTrue("No separator should match", pattern.matcher("1234567890").matches());

        // Invalid phone numbers
        assertFalse("Too few digits should not match", pattern.matcher("123-456-789").matches());
        assertFalse("Too many digits should not match", pattern.matcher("123-456-78901").matches());
        assertFalse("Letters should not match", pattern.matcher("123-ABC-7890").matches());
    }

    @Test
    public void testDatePatternValidation() {
        // Date pattern (YYYY-MM-DD)
        String datePattern = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$";
        PatternEntry dateEntry = new PatternEntry("ISO Date", datePattern, false, false);

        Pattern pattern = createPatternFromEntry(dateEntry);

        // Valid dates
        assertTrue("Standard date should match", pattern.matcher("2023-01-15").matches());
        assertTrue("End of month should match", pattern.matcher("2023-12-31").matches());
        assertTrue("First of month should match", pattern.matcher("2023-02-01").matches());

        // Invalid dates (note: this pattern doesn't validate actual calendar rules)
        assertFalse("Invalid month should not match", pattern.matcher("2023-13-01").matches());
        assertFalse("Invalid day should not match", pattern.matcher("2023-01-32").matches());
        assertFalse("Wrong format should not match", pattern.matcher("01/15/2023").matches());
    }

    // Helper method to create Pattern from PatternEntry
    private Pattern createPatternFromEntry(PatternEntry entry) {
        int flags = 0;
        if (entry.isMultiline()) {
            flags |= Pattern.MULTILINE;
        }
        if (entry.isCaseInsensitive()) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        return Pattern.compile(entry.getPattern(), flags);
    }
}
