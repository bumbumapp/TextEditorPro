package net.gsantner.markor.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Line based diff (LCS). Pure Java, unit tested. */
public final class TextDiff {
    private TextDiff() {
    }

    public enum Type {SAME, ADDED, REMOVED}

    public static final class Line {
        public final Type type;
        public final String text;

        Line(final Type type, final String text) {
            this.type = type;
            this.text = text;
        }
    }

    public static final class Result {
        public final List<Line> lines = new ArrayList<>();
        public int added, removed, changed;
        public boolean tooLarge;

        public boolean identical() {
            return !tooLarge && added == 0 && removed == 0 && changed == 0;
        }
    }

    public static final int MAX_LINES = 3000;

    public static Result diff(final String original, final String changed) {
        final List<String> a = split(original);
        final List<String> b = split(changed);
        final Result r = new Result();

        int start = 0;
        while (start < a.size() && start < b.size() && a.get(start).equals(b.get(start))) {
            start++;
        }
        int endA = a.size();
        int endB = b.size();
        while (endA > start && endB > start && a.get(endA - 1).equals(b.get(endB - 1))) {
            endA--;
            endB--;
        }
        final int n = endA - start;
        final int m = endB - start;
        if (n > MAX_LINES || m > MAX_LINES) {
            r.tooLarge = true;
            return r;
        }
        for (int i = 0; i < start; i++) {
            r.lines.add(new Line(Type.SAME, a.get(i)));
        }
        final short[][] lcs = new short[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                lcs[i][j] = a.get(start + i).equals(b.get(start + j))
                        ? (short) (lcs[i + 1][j + 1] + 1)
                        : (short) Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }
        int i = 0, j = 0;
        int runDel = 0, runAdd = 0;
        while (i < n || j < m) {
            if (i < n && j < m && a.get(start + i).equals(b.get(start + j))) {
                r.changed += Math.min(runDel, runAdd);
                r.removed += runDel - Math.min(runDel, runAdd);
                r.added += runAdd - Math.min(runDel, runAdd);
                runDel = runAdd = 0;
                r.lines.add(new Line(Type.SAME, a.get(start + i)));
                i++;
                j++;
            } else if (i < n && (j == m || lcs[i + 1][j] >= lcs[i][j + 1])) {
                r.lines.add(new Line(Type.REMOVED, a.get(start + i)));
                runDel++;
                i++;
            } else {
                r.lines.add(new Line(Type.ADDED, b.get(start + j)));
                runAdd++;
                j++;
            }
        }
        r.changed += Math.min(runDel, runAdd);
        r.removed += runDel - Math.min(runDel, runAdd);
        r.added += runAdd - Math.min(runDel, runAdd);
        for (int k = endA; k < a.size(); k++) {
            r.lines.add(new Line(Type.SAME, a.get(k)));
        }
        return r;
    }

    private static List<String> split(final String s) {
        if (s == null || s.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> l = new ArrayList<>(java.util.Arrays.asList(s.split("\r?\n", -1)));
        if (!l.isEmpty() && l.get(l.size() - 1).isEmpty()) {
            l.remove(l.size() - 1); // trailing newline is not a line
        }
        return l;
    }
}
