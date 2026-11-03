package net.gsantner.markor.util;

import android.content.Context;

import net.gsantner.markor.R;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/** Seeds the notebook with sample documents on first launch so every feature is visible at once. */
public class SampleDocs {
    private static final String PREF_SEEDED = "sample_docs_seeded_v1";

    public static final String WELCOME_MD = "# Welcome to Text Editor Pro\n\n"
            + "Everything you see here works **without any permission**. Your notes live in the app's own notebook folder.\n\n"
            + "## What you can do\n\n"
            + "- Write plain text and **Markdown** with live preview (tap the eye icon)\n"
            + "- Keep a **To-Do** list in the todo.txt format\n"
            + "- Jot ideas down instantly in **QuickNote**\n"
            + "- **Search** across all your notes\n"
            + "- Share text into the app from any other app\n\n"
            + "## Markdown cheat sheet\n\n"
            + "1. `# Heading` makes a title\n"
            + "2. `**bold**` and `*italic*` format text\n"
            + "3. `- item` starts a bullet list\n"
            + "4. `- [ ] task` adds a checkbox\n\n"
            + "- [x] Open Welcome.md\n"
            + "- [ ] Tap the pencil to edit this note\n"
            + "- [ ] Create your first note from Home\n\n"
            + "> Optional: in Settings you can pick a shared folder (needs All files access). It is never required.\n";

    public static final String TODO_TXT = "(A) Try the To-Do tab +welcome @home\n"
            + "(B) Add a due date to a task due:2030-01-31 +welcome\n"
            + "(B) Write my first note +notes @home\n"
            + "(C) Search for a word across all notes +welcome\n"
            + "x 2026-01-01 Install Text Editor Pro +welcome\n";

    public static final String QUICKNOTE_MD = "# QuickNote\n\n"
            + "A scratchpad that is always one tap away. Type anything here, it saves automatically.\n\n"
            + "- Shopping: milk, eggs, coffee\n"
            + "- Idea: weekly review every Sunday\n";

    public static void seedIfNeeded(final Context context) {
        try {
            final AppSettings as = AppSettings.get();
            if (as.getBool(PREF_SEEDED, false) || !as.isNotebookInAppStorage()) {
                return;
            }
            final File dir = as.getNotebookDirectory();
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            write(new File(dir, "Welcome.md"), WELCOME_MD);
            write(new File(dir, context.getString(R.string.todo_default_filename)), TODO_TXT);
            write(new File(dir, context.getString(R.string.quicknote_default_filename)), QUICKNOTE_MD);
            as.setBool(PREF_SEEDED, true);
        } catch (Exception ignored) {
        }
    }

    public static void restoreWelcome(final File f) {
        try {
            //noinspection ResultOfMethodCallIgnored
            f.getParentFile().mkdirs();
            write(f, WELCOME_MD);
        } catch (Exception ignored) {
        }
    }

    private static void write(final File f, final String text) throws Exception {
        if (f.exists() && f.length() > 0) {
            return;
        }
        try (FileOutputStream os = new FileOutputStream(f)) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }
}
