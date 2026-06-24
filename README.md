# Pinote

Pinote is a tiny Android app that turns short notes into notifications.

## Behavior

- Opening the app shows a text box and opens the keyboard.
- Tapping Done posts the note as a notification.
- Each notification has a Done action that removes it.
- Swiping a notification recreates it until Done is tapped.
- Notes survive app restarts and phone reboots.
- Dark UI with the Done button kept above the keyboard.

## Build

```bash
./gradlew assembleDebug
```

The checked build artifact is `dist/pinote-v1.2-debug.apk`.
