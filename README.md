# Simple Daily Journaling (Android)

A simple offline-first daily journal for Markdown notes stored in a user-selected folder.

Latest release: **v1.1.0**  
APK download: [https://github.com/seanelvidge/journalApp/releases/tag/v1.1.0](https://github.com/seanelvidge/simple-daily-journal-android-app/releases/tag/v1.1.0)

You can either download the APK from the release page or build it yourself.

## Setup / Build / Run

- Open the project in Android Studio (Giraffe+). Sync Gradle.
- Connect a device or start an emulator.
- Run the `app` configuration, or from CLI:

```bash
./gradlew assembleDebug
```

Release APK (signed if `keystore.properties` is configured):

```bash
./gradlew assembleRelease
```

Release output: `app/build/outputs/apk/release/journaling.apk`

## Storage scheme

After choosing a root folder via the Android system picker, the app creates:

- Monthly folders: `YYYY-MM/`
- Daily entries: `YYYY-MM/YYYY-MM-DD.md`
- Attachments: `YYYY-MM/attachments/`

Attachments are copied into the monthly `attachments` folder with collision-safe names.

## Features

- Offline-first Markdown editor with toggleable preview.
- Auto-save (debounced) and save on background; force save in Settings.
- Attach files or photos and append Markdown links/images.
- Calendar with markers for days that have entries, plus swipe left/right to change days.
- Template support for new entries.
- Theme selection (System/Light/Dark) and About dialog.

## Known limitations

- Markdown preview resolves `attachments/...` links to SAF content URIs; links opened from the preview rely on system handlers.
- Calendar markers are computed by scanning the current month folder.
