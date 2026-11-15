package net.gsantner.markor.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;
import java.util.Locale;

public class ToolsTest {
    static {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void stats() {
        final TextToolsEngine.Stats s = TextToolsEngine.stats("Hello world. This is a test!\nSecond line?");
        assertEquals(8, s.words);
        assertEquals(3, s.sentences);
        assertEquals(2, s.lines);
        assertEquals(41, s.characters);
        assertTrue(s.readingSeconds >= 1);
        final TextToolsEngine.Stats e = TextToolsEngine.stats("");
        assertEquals(0, e.words);
        assertEquals(0, e.readingSeconds);
        assertEquals(0, e.lines);
    }

    @Test
    public void readingTime() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 400; i++) sb.append("word ");
        assertEquals(120, TextToolsEngine.stats(sb.toString()).readingSeconds);
    }

    @Test
    public void topWords() {
        final List<TextToolsEngine.WordCount> l = TextToolsEngine.topWords("the cat and The dog and THE bird a", 2);
        assertEquals(2, l.size());
        assertEquals("the", l.get(0).word);
        assertEquals(3, l.get(0).count);
        assertEquals("and", l.get(1).word);
        assertEquals(2, l.get(1).count);
    }

    @Test
    public void cases() {
        assertEquals("HELLO", TextToolsEngine.upper("hello"));
        assertEquals("hello", TextToolsEngine.lower("HeLLo"));
        assertEquals("Hello World It's Ok", TextToolsEngine.title("hello wORLD it's ok"));
        assertEquals("Hello world. Next one! Ok", TextToolsEngine.sentence("hELLO world. next ONE! ok"));
    }

    @Test
    public void lines() {
        assertEquals("a\nB\nc", TextToolsEngine.sortAsc("c\nB\na"));
        assertEquals("c\nB\na", TextToolsEngine.sortDesc("a\nB\nc"));
        assertEquals("a\nbb\nccc", TextToolsEngine.sortByLength("ccc\na\nbb"));
        assertEquals("c\nb\na", TextToolsEngine.reverseLines("a\nb\nc"));
        assertEquals("a\nb\nc", TextToolsEngine.removeDuplicateLines("a\nb\na\nc\nb"));
        assertEquals("a\nb", TextToolsEngine.removeEmptyLines("a\n\n  \nb\n"));
        assertEquals("a b\nc", TextToolsEngine.trimSpaces("  a   b \n\tc  "));
        assertEquals("1. x\n2. y", TextToolsEngine.numberLines("x\ny"));
        assertEquals("a b c", TextToolsEngine.joinLines("a\n b\n\nc "));
    }

    @Test
    public void jsonFormatMinify() {
        final JsonStudio.Result r = JsonStudio.format("{\"a\":[1,2,{\"b\":null}],\"c\":\"x y\",\"d\":true}", 2);
        assertTrue(r.ok);
        assertEquals("{\n  \"a\": [\n    1,\n    2,\n    {\n      \"b\": null\n    }\n  ],\n  \"c\": \"x y\",\n  \"d\": true\n}", r.output);
        assertEquals(4, r.keys);
        assertEquals(4, r.depth);
        assertEquals("{\"a\":[1,2,{\"b\":null}],\"c\":\"x y\",\"d\":true}", JsonStudio.minify(r.output).output);
        assertEquals("{\n\t\"k\": 1\n}", JsonStudio.format("{\"k\":1}", -1).output);
        assertEquals("[]", JsonStudio.format(" [ ] ", 4).output);
    }

    @Test
    public void jsonErrors() {
        JsonStudio.Result r = JsonStudio.format("{\n  \"a\": 1\n  \"b\": 2\n}", 2);
        assertFalse(r.ok);
        assertEquals("expected_comma", r.error);
        assertEquals(3, r.line);
        assertEquals(3, r.column);
        assertEquals("trailing", JsonStudio.format("{} x", 2).error);
        assertEquals("empty", JsonStudio.format("  ", 2).error);
        assertFalse(JsonStudio.format("[01]", 2).ok);
        assertFalse(JsonStudio.format("[1,]", 2).ok);
        assertFalse(JsonStudio.format("{'a':1}", 2).ok);
        assertFalse(JsonStudio.format("{\"a\":1,}", 2).ok);
        assertFalse(JsonStudio.format("[1.]", 2).ok);
        assertFalse(JsonStudio.format("\"abc", 2).ok);
        assertFalse(JsonStudio.format("\"a\\xb\"", 2).ok);
        assertTrue(JsonStudio.format("-1.5e+10", 2).ok);
        assertTrue(JsonStudio.format("\"\\u00e9\"", 2).ok);
    }

    @Test
    public void jsonDeepNestingDoesNotCrash() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) sb.append('[');
        assertEquals("depth", JsonStudio.format(sb.toString(), 2).error);
    }

    @Test
    public void diff() {
        TextDiff.Result r = TextDiff.diff("a\nb\nc\nd", "a\nB\nc\nd\ne");
        assertEquals(1, r.changed);
        assertEquals(1, r.added);
        assertEquals(0, r.removed);
        assertFalse(r.identical());
        r = TextDiff.diff("same\n", "same");
        assertTrue(r.identical());
        r = TextDiff.diff("a\nb", "");
        assertEquals(2, r.removed);
        assertFalse("a replaced line is not identical", TextDiff.diff("a", "b").identical());
        r = TextDiff.diff("", "x");
        assertEquals(1, r.added);
        r = TextDiff.diff("1\n2\n3", "1\n3");
        assertEquals(1, r.removed);
        assertEquals(TextDiff.Type.REMOVED, r.lines.get(1).type);
    }

    @Test
    public void noteFiles() throws Exception {
        assertEquals("Note", NoteFiles.sanitize("  "));
        assertEquals("a b c", NoteFiles.sanitize("a/b:c"));
        assertEquals("To-do list 2026-11-16", NoteFiles.fill("To-do list {date}", "2026-11-16"));
        assertEquals("Today\nThis week\nx", NoteFiles.preview("# To-do list\n\n{date}\n\n## Today\n- [ ] \n\n## This week\n- [ ] x", "D"));
        assertEquals("Topic:\nWeek of D", NoteFiles.preview("# T\n**Topic:** \nWeek of {date}", "D"));
        final java.io.File dir = java.nio.file.Files.createTempDirectory("notes").toFile();
        final java.io.File f1 = NoteFiles.create(dir, "My note", "md", "hello");
        final java.io.File f2 = NoteFiles.create(dir, "My note", "md", "again");
        assertEquals("My note.md", f1.getName());
        assertEquals("My note 2.md", f2.getName());
        assertEquals(5, f1.length());
    }
}
