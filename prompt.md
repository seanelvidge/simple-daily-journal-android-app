You are codex-cli operating as an autonomous senior Android engineer + designer. Build a simple daily journal Android app per the spec below. Make all design/engineering decisions yourself without asking follow-up questions. If something is ambiguous, choose the simplest sensible default and document it in a short DECISIONS.md.

GOALS (high level)
- Offline-first daily journal.
- On open: default to today’s date and show an editor ready for text entry.
- Notes are written in Markdown and rendered automatically (live preview or seamless read mode).
- User selects a storage folder via Android system file picker (Storage Access Framework). All notes + attachments live in that user-chosen folder, using the subfolder scheme below.

REQUIRED FEATURES
1) Storage location selection (Android Files interface)
- On first launch, prompt user to pick a folder using ACTION_OPEN_DOCUMENT_TREE.
- Persist access using takePersistableUriPermission.
- Provide a Settings screen to change/reselect the folder later.
- All file operations must use SAF/DocumentFile (no direct path assumptions).

2) File/folder scheme inside selected root folder
- Create one subfolder per month using format: "YYYY-MM" (e.g., "2026-01").
- Each day’s entry is a Markdown file in the month folder:
  - Filename: "YYYY-MM-DD.md" (e.g., "2026-01-16.md")
- Each month folder contains an attachments subfolder: "attachments"
  - Store copied attachment files there.
  - Attachment filenames should be collision-safe (e.g., "YYYY-MM-DD_HHMMSS_originalname.ext" or UUID-based).

3) Editing + Markdown rendering
- Main screen shows:
  - Date header (today by default).
  - Text editor for Markdown.
  - Rendered Markdown view (either live split view or toggle).
- Markdown rendering should support:
  - Headings, bold/italic, lists, code blocks, links, blockquotes.
  - Images referenced by local attachment files.
- Autosave behavior:
  - Save frequently (debounced, e.g., 500–1000ms after typing stops) and also on pause/background.
  - Never lose content.

4) Attachments
- Provide an “Attach” action for the current day.
- Allow picking:
  - Any file via SAF (ACTION_OPEN_DOCUMENT, allow multiple).
  - Photos via the Android photo picker (prefer modern Photo Picker; fall back to SAF if unavailable).
- Copy selected items into that month’s attachments folder.
- Append (or maintain) attachment references in the Markdown file so they persist:
  - For images: append Markdown image links at bottom:
    - ![filename](attachments/<copied_filename>)
  - For non-images: append a bullet list of links:
    - - [filename](attachments/<copied_filename>)
- Display attachments at bottom of the note in the UI (rendered preview should show images; non-images as links).

5) Calendar / date navigation with note presence
- Provide a calendar dropdown / date picker accessible from the top bar.
- It must clearly indicate which days have notes and which do not.
  - Use markers/dots/bold dates for existing entries.
- Selecting a day loads that day’s entry (create on first edit if it doesn’t exist).
- Provide quick navigation:
  - Previous day / next day buttons.

6) UI/UX
- Simple, modern, sleek Material 3 design.
- Support light/dark mode automatically.
- Minimal chrome: top app bar with date + calendar button + attach + settings.
- Smooth and responsive.

NON-FUNCTIONAL REQUIREMENTS
- Use Kotlin + Jetpack Compose + Material 3.
- Single-activity architecture is fine.
- Use a small ViewModel + repository structure.
- Use coroutines; file IO off main thread.
- No backend, no network required.
- Robust error handling: if folder permission revoked, guide user to reselect.
- Keep dependencies minimal and well-known.

DELIVERABLES
- A complete Android Studio project that builds and runs.
- README.md with:
  - Setup/build/run instructions.
  - Storage scheme explanation.
  - Known limitations.
- DECISIONS.md capturing key choices (rendering approach, attachment naming, etc.).
- Basic tests where feasible:
  - Unit tests for date formatting, month folder naming, attachment naming.
  - (Optional) lightweight instrumentation tests for repository logic using in-memory/fake DocumentFile abstractions.

IMPLEMENTATION GUIDANCE (you decide details, but follow these principles)
- Persist chosen folder URI in DataStore.
- Abstract SAF operations behind an interface (e.g., StorageRepository) for testability.
- For calendar marking, scan month folders / filenames to build a set of existing dates; cache results and refresh when returning to app or after save/attach.
- Markdown rendering: choose a reliable library that works well with Compose (e.g., Markwon with Android Views via AndroidView, or a Compose-native markdown renderer). Prefer stability over novelty.
- For image display, ensure local attachments resolve correctly under SAF; if needed, copy to app cache for display, but the source of truth remains in the selected folder. Document the approach.

AUTONOMOUS WORKFLOW
- Create the project, implement features incrementally in sensible commits.
- Do not ask the user questions.
- If a feature is tricky (e.g., SAF image rendering), implement the simplest robust solution and document it.
- Ensure the app works end-to-end:
  - pick folder → open today → type markdown → autosave → attach image → image appears in preview → pick past date → markers show which days exist.
- commit and push changes to the git repo regularly

OUTPUT FORMAT
- Print a short plan (bullet list).
- Then implement the repo.
- At the end, print:
  - How to build/run.
  - Any important decisions/limitations.

