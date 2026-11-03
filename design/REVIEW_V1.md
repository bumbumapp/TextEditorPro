# Review V1 (self-review, dark variants checked in headless Chrome; render is clipped on the right because headless Chrome enforces a min window width, not a design defect)
- home_dark: 8/10. Clear hierarchy (greeting, big title, hero tile, 2x2 tiles, one recent card, floating bottom bar). Defects: icon button top-right clipped in render; "View all" link not needed; hero decoration weak.
- files_dark: 8/10. Files inside one rounded card, clear "New" action.
- editor_dark: 8/10. Chips format bar, readable text area.
Decision: implement home as designed; app chrome is Bento dark (polished #12141C / #1B1E29 lifted cards, indigo accent) because the Markor chrome hard-codes light-on-dark text in dozens of layouts.
