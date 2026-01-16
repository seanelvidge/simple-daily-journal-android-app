# Decisions

- Markdown rendering uses Markwon with Coil inside an AndroidView for stability; preview text swaps `attachments/...` links with SAF content URIs so images render without copying to cache.
- Attachments are named with a timestamp prefix `yyyy-MM-dd_HHmmss` plus the original base name; if a collision exists, an 8-char UUID suffix is added.
- Calendar is a custom Compose month grid so note markers (dots) can be shown per day.
- Entries are created on first save; blank days are not written unless the user types.
