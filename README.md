# Daily Journal (Android)

A simple offline-first daily journal for Markdown notes stored in a user-selected folder.

## Setup / Build / Run

- Open the project in Android Studio (Giraffe+). Sync Gradle.
- Connect a device or start an emulator.
- Run the `app` configuration, or from CLI:

```bash
./gradlew assembleDebug
```

## Storage scheme

After choosing a root folder via the Android system picker, the app creates:

- Monthly folders: `YYYY-MM/`
- Daily entries: `YYYY-MM/YYYY-MM-DD.md`
- Attachments: `YYYY-MM/attachments/`

Attachments are copied into the monthly `attachments` folder with collision-safe names.

## Features

- Offline-first Markdown editor with live preview.
- Auto-save (debounced) and save on background.
- Attach files or photos and append Markdown links/images.
- Calendar with markers for days that have entries.
- Settings screen to change the storage folder.

## Known limitations

- Markdown preview resolves `attachments/...` links to SAF content URIs; links opened from the preview rely on system handlers.
- Calendar markers are computed by scanning the current month folder.
