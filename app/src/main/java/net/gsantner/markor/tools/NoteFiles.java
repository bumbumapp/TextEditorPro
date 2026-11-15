package net.gsantner.markor.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Creates notes in a folder with safe, unique file names (pure Java). */
public final class NoteFiles {
    private NoteFiles() {
    }

    public static String sanitize(final String name) {
        String s = name == null ? "" : name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", " ").replaceAll("\\s+", " ").trim();
        if (s.length() > 60) {
            s = s.substring(0, 60).trim();
        }
        return s.isEmpty() ? "Note" : s;
    }

    public static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    public static File uniqueFile(final File dir, final String baseName, final String extension) {
        final String base = sanitize(baseName);
        File f = new File(dir, base + "." + extension);
        int i = 2;
        while (f.exists()) {
            f = new File(dir, base + " " + i + "." + extension);
            i++;
        }
        return f;
    }

    /** Writes the note and returns the file, or null when writing failed. */
    public static File create(final File dir, final String baseName, final String extension, final String content) {
        try {
            if (!dir.isDirectory() && !dir.mkdirs()) {
                return null;
            }
            final File f = uniqueFile(dir, baseName, extension);
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(f)) {
                out.write(content.getBytes(StandardCharsets.UTF_8));
            }
            return f;
        } catch (final IOException | SecurityException e) {
            return null;
        }
    }

    /** Template bodies may contain the {date} placeholder. */
    public static String fill(final String body, final String date) {
        return body.replace("{date}", date);
    }

    /** First lines of a template body (title line and bare date line skipped, Markdown marks removed) for the card preview. */
    public static String preview(final String body, final String date) {
        final StringBuilder sb = new StringBuilder();
        int n = 0;
        for (final String line : body.split("\n")) {
            if (line.startsWith("# ") || line.trim().equals("{date}")) {
                continue;
            }
            final String s = line.replaceAll("^[#>\\-*\\s]+", "").replace("[ ]", "").replace("*", "").replace("{date}", date).trim();
            if (s.isEmpty()) {
                continue;
            }
            if (n++ > 0) {
                sb.append('\n');
            }
            sb.append(s);
            if (n == 3) {
                break;
            }
        }
        return sb.toString();
    }
}
