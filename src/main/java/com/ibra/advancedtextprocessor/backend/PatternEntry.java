package com.ibra.advancedtextprocessor.backend;

import java.util.Objects;

/**
 * Represents a saved regex pattern with a name and additional metadata
 */
public class PatternEntry {
    private String name;
    private String pattern;
    private boolean multiline;
    private boolean caseInsensitive;

    public PatternEntry(String name, String pattern, boolean multiline, boolean caseInsensitive) {
        this.name = name;
        this.pattern = pattern;
        this.multiline = multiline;
        this.caseInsensitive = caseInsensitive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternEntry that = (PatternEntry) o;
        return multiline == that.multiline &&
                caseInsensitive == that.caseInsensitive &&
                Objects.equals(name, that.name) &&
                Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pattern, multiline, caseInsensitive);
    }
}