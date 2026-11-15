# Review 4.1 V1 (self-review, dark variants rendered with headless Chrome; the right edge is cut in the PNGs because headless Chrome enforces a minimum window width, not a design defect)
- home_dark: 8/10. Hero + "Tools" 2x2 grid + shortcut chips + recent files tell the "many tools" story at once. Defects: hero too tall (reduce to ~88dp so Recent files shows earlier), "Make text work for you" caption redundant (dropped), "View all" not implemented (dropped).
- texttools_dark: 8/10. Clear input card, 4 stat tiles, grouped chips, top-words bars, sticky action bar. Defects: "Reading time" label long (allow 2 lines, 11sp), chips must wrap (not scroll) so every operation is visible.
- json_dark: 8/10. Status pill (green/red), Format/Minify segmented control, indent chips, mono output. Defects: line-number gutter is decorative (skipped in app), "Last valid result" caption dropped.
- templates_dark: 8/10. Selected card with accent border + check, sticky create button. Defects: preview lines are fake bars, app shows the real first lines of the template; template set replaced by the 8 real ones (To-do, Meeting, Journal, Shopping, Weekly, Project, Book, Recipe).
- compare_dark: 8/10. Summary counts coloured, +/- rows tinted, swap button between inputs. Defect: line-number gutter skipped in app.
Decision: implement as designed with the listed adjustments; app chrome stays Bento dark (the whole app is dark by design, see REVIEW_V1). Light variants exist in mockups41 but the app has no light theme.

## Implementation check (real release build, 1080x2400 emulator vs mockups)
- home: 8.5/10 - matches (hero, Tools 2x2 grid, 4 shortcut chips, recent files). Tile text 12-16sp, 16dp side insets both sides.
- texttools: 8.5/10 - matches; chips wrap (no hidden operations); action bar icon-only copy/share + primary save so Russian/German never clip.
- json: 8.5/10 - status pill, segmented format/minify, indent chips, mono result; RTL (Arabic) mirrors correctly.
- templates: 8.5/10 - real first lines as preview, accent border on selection.
- compare: 8.5/10 - swap button no longer clipped (fixed after first run), removed rows listed before added rows.
Defects found and fixed during QA: compare reported "identical" for replaced lines (unit test added), Russian action labels wrapped (replaced with icon-only buttons), keyboard opened on entry (focus trick), swap button clipped, JSON option chips wrapped on one row (split into two rows), template previews repeated the title (title line skipped).
