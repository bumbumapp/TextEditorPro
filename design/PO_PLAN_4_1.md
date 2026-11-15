# Product plan for Huawei rule 4.1 (2026-11-16)

## What exists (1.2.0)
Markor fork: plain text / Markdown / todo.txt editor with live preview, file browser (app-private Notebook, works with
no permission), QuickNote, To-Do, full-text search, home screen with 4 shortcut tiles + recent files.
Every tile on Home opens "an editor" -> reviewer perceives one simple feature (text editing).

## Category scan (Markor, Notepad++ style apps, "Text Editor"/"Notepad" top Android apps)
Users of text-editor apps ask for: text utilities (case/sort/dedupe/counts), JSON/code formatting, templates for quick
notes, comparing two texts, reading statistics. All of these work offline, with no permission and no data collection.

## New modules (each is its own screen, all offline, no new permissions)
1. Text Tools: paste/type text, 12 operations (UPPER/lower/Title/Sentence case, sort A-Z/Z-A/by length, reverse lines,
   remove duplicate lines, remove empty lines, trim spaces, add line numbers, join lines), live stats
   (words, characters, lines, sentences, reading time), word-frequency top list. Copy / share / save as note.
2. JSON Studio: format (2/4 spaces/tab), minify, strict validate with line:column error, key/size summary, copy/share/save.
3. Templates: 8 ready note templates (To-do list, Meeting notes, Daily journal, Shopping list, Weekly plan,
   Project brief, Book notes, Recipe) with live preview, one tap creates a note and opens it in the editor.
4. Text Compare: two text boxes -> line diff with added/removed/changed highlighting and counts, swap, copy.

## Home
Hero "New note" stays; new "Tools" Bento grid (2 columns, 4 colored tiles) directly under hero showing the four modules,
then existing shortcuts row (To-Do, QuickNote, Markdown, Search) in compact tiles, then recent files.

## Quality
Pure-Java logic classes (TextToolsEngine, JsonStudio, TextDiff, NoteTemplates) with JVM unit tests. Release R8 build tested.
Strings translated into ru, de, ar, es, id, pt, tr; English fallback for the rest.
