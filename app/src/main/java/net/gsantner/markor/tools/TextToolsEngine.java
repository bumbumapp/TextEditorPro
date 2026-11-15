package net.gsantner.markor.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Pure-Java text utilities used by the Text Tools screen (no Android dependencies, unit tested). */
public final class TextToolsEngine {
    private TextToolsEngine() {
    }

    public static final class Stats {
        public int words, characters, charactersNoSpaces, lines, sentences, readingSeconds;
    }

    public static final class WordCount {
        public final String word;
        public final int count;

        public WordCount(final String word, final int count) {
            this.word = word;
            this.count = count;
        }
    }

    private static final int WORDS_PER_MINUTE = 200;

    public static Stats stats(final String text) {
        final Stats s = new Stats();
        if (text == null || text.isEmpty()) {
            return s;
        }
        s.characters = text.codePointCount(0, text.length());
        boolean inWord = false;
        int sentenceChars = 0;
        for (int i = 0; i < text.length(); ) {
            final int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            final boolean space = Character.isWhitespace(cp);
            if (!space) {
                s.charactersNoSpaces++;
            }
            if (space) {
                inWord = false;
            } else if (!inWord) {
                inWord = true;
                s.words++;
            }
            if (cp == '.' || cp == '!' || cp == '?' || cp == '。' || cp == '؟') {
                if (sentenceChars > 0) {
                    s.sentences++;
                    sentenceChars = 0;
                }
            } else if (!space) {
                sentenceChars++;
            }
        }
        if (sentenceChars > 0) {
            s.sentences++;
        }
        s.lines = text.split("\r?\n", -1).length;
        s.readingSeconds = s.words == 0 ? 0 : Math.max(1, Math.round(s.words * 60f / WORDS_PER_MINUTE));
        return s;
    }

    /** Most frequent words (case-insensitive, letters/digits only, at least 2 chars), ties alphabetical. */
    public static List<WordCount> topWords(final String text, final int limit) {
        final Map<String, Integer> counts = new HashMap<>();
        if (text != null) {
            final StringBuilder cur = new StringBuilder();
            for (int i = 0; i <= text.length(); ) {
                final int cp = i < text.length() ? text.codePointAt(i) : ' ';
                i += i < text.length() ? Character.charCount(cp) : 1;
                if (Character.isLetterOrDigit(cp) || (cp == '\'' && cur.length() > 0)) {
                    cur.appendCodePoint(cp);
                } else if (cur.length() > 0) {
                    String w = cur.toString().toLowerCase(Locale.ROOT);
                    while (w.endsWith("'")) {
                        w = w.substring(0, w.length() - 1);
                    }
                    if (w.codePointCount(0, w.length()) >= 2) {
                        final Integer c = counts.get(w);
                        counts.put(w, c == null ? 1 : c + 1);
                    }
                    cur.setLength(0);
                }
            }
        }
        final List<WordCount> out = new ArrayList<>();
        for (final Map.Entry<String, Integer> e : counts.entrySet()) {
            out.add(new WordCount(e.getKey(), e.getValue()));
        }
        Collections.sort(out, (a, b) -> a.count != b.count ? b.count - a.count : a.word.compareTo(b.word));
        return out.size() > limit ? new ArrayList<>(out.subList(0, limit)) : out;
    }

    // ---- case ----
    public static String upper(final String t) {
        return t.toUpperCase(Locale.getDefault());
    }

    public static String lower(final String t) {
        return t.toLowerCase(Locale.getDefault());
    }

    public static String title(final String t) {
        final StringBuilder sb = new StringBuilder(t.length());
        boolean start = true;
        for (int i = 0; i < t.length(); ) {
            final int cp = t.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.isLetter(cp)) {
                sb.appendCodePoint(start ? Character.toTitleCase(cp) : Character.toLowerCase(cp));
                start = false;
            } else {
                sb.appendCodePoint(cp);
                start = !(cp == '\'' || Character.isDigit(cp));
            }
        }
        return sb.toString();
    }

    public static String sentence(final String t) {
        final StringBuilder sb = new StringBuilder(t.length());
        boolean start = true;
        for (int i = 0; i < t.length(); ) {
            final int cp = t.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.isLetter(cp)) {
                sb.appendCodePoint(start ? Character.toUpperCase(cp) : Character.toLowerCase(cp));
                start = false;
            } else {
                sb.appendCodePoint(cp);
                if (cp == '.' || cp == '!' || cp == '?' || cp == '\n') {
                    start = true;
                } else if (Character.isDigit(cp)) {
                    start = false;
                }
            }
        }
        return sb.toString();
    }

    // ---- lines ----
    private static List<String> lines(final String t) {
        return new ArrayList<>(Arrays.asList(t.split("\r?\n", -1)));
    }

    private static String join(final List<String> l) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(l.get(i));
        }
        return sb.toString();
    }

    public static String sortAsc(final String t) {
        final List<String> l = lines(t);
        Collections.sort(l, String.CASE_INSENSITIVE_ORDER);
        return join(l);
    }

    public static String sortDesc(final String t) {
        final List<String> l = lines(t);
        Collections.sort(l, Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER));
        return join(l);
    }

    public static String sortByLength(final String t) {
        final List<String> l = lines(t);
        Collections.sort(l, (a, b) -> a.length() - b.length()); // stable
        return join(l);
    }

    public static String reverseLines(final String t) {
        final List<String> l = lines(t);
        Collections.reverse(l);
        return join(l);
    }

    public static String removeDuplicateLines(final String t) {
        return join(new ArrayList<>(new LinkedHashSet<>(lines(t))));
    }

    public static String removeEmptyLines(final String t) {
        final List<String> l = new ArrayList<>();
        for (final String s : lines(t)) {
            if (!s.trim().isEmpty()) {
                l.add(s);
            }
        }
        return join(l);
    }

    /** Trims each line and collapses runs of spaces/tabs into one space. */
    public static String trimSpaces(final String t) {
        final List<String> l = new ArrayList<>();
        for (final String s : lines(t)) {
            l.add(s.replaceAll("[ \\t\\u00A0]+", " ").trim());
        }
        return join(l);
    }

    public static String numberLines(final String t) {
        final List<String> src = lines(t);
        final List<String> l = new ArrayList<>();
        for (int i = 0; i < src.size(); i++) {
            l.add((i + 1) + ". " + src.get(i));
        }
        return join(l);
    }

    public static String joinLines(final String t) {
        final StringBuilder sb = new StringBuilder();
        for (final String s : lines(t)) {
            final String x = s.trim();
            if (x.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(x);
        }
        return sb.toString();
    }
}
