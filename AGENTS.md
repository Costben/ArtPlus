# ArtPlus Agent Notes

## Build Upload Rule

After every successful `:mobile:assembleDebug`, copy `mobile/build/outputs/apk/debug/mobile-debug.apk` to both NAS latest paths:

- `/Volumes/Download/ArtPlusOutput/artplus-mobile-latest.apk`
- `/Volumes/Download/ArtPlus/artplus-mobile-latest.apk`

Also keep timestamped copies under:

- `/Volumes/Download/ArtPlusOutput/builds/`
- `/Volumes/Download/ArtPlus/builds/`

If `/Volumes/Download` is not mounted, report that the NAS upload was not completed.

If `/Volumes/Download` is unavailable, fall back to the WebDAV mount on `192.168.31.179:5005` for the same server before giving up on upload.

## Workspace Layout

Keep research and experiment artifacts under `outputs/research/`. Do not scatter new lab outputs at the project root.

Use `outputs/tmp/` only for disposable temporary files; it can be deleted during cleanup.
