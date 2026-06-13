# ArtPlus Lifecycle and Security Verification Report

Date: 2026-06-13

## Scope

This pass covered the Android APK currently in `mobile/`:

- Build and CI release path.
- Manifest and APK security properties.
- Debug HTTP surface.
- Debug intent surface.
- GPT and RMBG network/storage handling.
- Root write path handling.
- GPT image generation path using a local mock endpoint.
- App launch and local icon generation on device `192.168.31.216:5555`.
- Static security scan for hardcoded secrets and risky Android surfaces.
- Manual-authorization surfaces such as SAF output directory selection and custom image import were code-reviewed; their picker grant flow was not automated.

The historical Python/WebUI source is not present as editable source in this repository snapshot; `cli/` and `tests/` only contain `__pycache__` artifacts, so this report treats the Android app as the current authoritative product surface.

## Code Changes Made

- GitHub Actions now builds `:mobile:assembleRelease` for release assets instead of publishing a debuggable APK.
- Workflow permissions are reduced to `contents: read` for build, with `contents: write` only in the release job.
- Release build is explicitly `isDebuggable = false` and `usesCleartextTraffic = false`.
- Debug build still allows cleartext for local development.
- Debug HTTP server now:
  - only starts in debuggable builds,
  - binds to loopback,
  - requires a random token for all routes,
  - requires the same token for debug intent generation.
- Package queries use `GET_META_DATA` instead of broad `MATCH_ALL`.
- RMBG ZIP extraction now canonicalizes output paths and limits entry count and unpacked bytes.
- RMBG downloads now reject non-HTTP(S), reject HTTP in release builds, and enforce a 2GB maximum download size.
- RMBG-2.0 ONNX default inference size was changed to 1024, with a one-time migration from the old 256 default because the current ModelScope RMBG-2.0 ONNX file rejects 128/256/512 input tensors.
- GPT API key is migrated from plaintext SharedPreferences to Android Keystore AES-GCM encrypted storage.
- GPT calls now reject HTTP URLs in release builds.
- Debug-only HTTP tuning can set GPT mock settings for lifecycle tests without exposing the API key in status responses.
- SAF child lookup was rewritten to make cursor closure explicit.
- README release documentation now matches the release APK workflow instead of saying debug APK.

## Verification Commands

Environment:

```bash
JAVA_HOME="$PWD/outputs/toolchain/jdk-17.0.19+10/Contents/Home"
ANDROID_HOME=/Users/rinshibuya/Library/Android/sdk
ANDROID_SDK_ROOT=/Users/rinshibuya/Library/Android/sdk
PATH="$PWD/outputs/toolchain/jdk-17.0.19+10/Contents/Home/bin:$PWD/outputs/toolchain/gradle-8.11.1/bin:$PATH"
GRADLE_OPTS="-Xmx5g -Dorg.gradle.workers.max=1 -Dkotlin.compiler.execution.strategy=in-process"
```

Passed:

```bash
outputs/toolchain/gradle-8.11.1/bin/gradle \
  :mobile:test \
  :mobile:lintDebug \
  :mobile:lintRelease \
  :mobile:assembleDebug \
  :mobile:assembleRelease \
  --no-daemon --stacktrace
```

Result: `BUILD SUCCESSFUL`.

Notes:

- Unit test tasks are `NO-SOURCE`; there are no current Java/Kotlin unit tests in the repo.
- Kotlin still emits non-fatal warnings for deprecated status/navigation bar setters and a few unnecessary safe calls.

## APK Inspection

Release APK:

- `mobile/build/outputs/apk/release/mobile-release.apk`
- Copied to `/Volumes/Download/ArtPlusOutput/artplus-mobile-1.0-security-release.apk`

`aapt dump xmltree` confirmed:

- `android:usesCleartextTraffic=false`
- `android:allowBackup=false`
- No `android:debuggable=true` on release application.
- `MainActivity` remains exported only for launcher entry.

`apksigner verify` confirmed:

- APK verifies with v2 signing.
- Certificate is the default Android debug certificate, as requested for this no-signing release pass.

## 216 Device Verification

Device:

- `192.168.31.216:5555`
- Model: `OnePlus8Pro`

Release install and launch:

- Installed `mobile-release.apk`.
- Launched `dev.artplus.mobile/.MainActivity`.
- `dumpsys package` flags show no `DEBUGGABLE`.
- UI loaded and reported: `共 535 个应用，其中 55 个有启动器入口。`
- Screenshot saved at `outputs/artplus_216_final_release.png`.
- Logcat showed no `FATAL EXCEPTION` or `AndroidRuntime` crash for ArtPlus.
- Port check showed no listener on `3964`.

Debug HTTP surface:

- Installed debug APK temporarily.
- Debug server listened only on `[::1]:3964`.
- Direct Mac-to-device probe `http://192.168.31.216:3964/debug/status` failed to connect.
- ADB-forwarded request without token returned `403 Forbidden`.
- ADB-forwarded request with token returned `200 OK`.
- Reinstalled release after debug testing.

GPT key storage:

- In debug build, stored fake GPT credentials.
- SharedPreferences contained `gpt_api_key_encrypted`.
- SharedPreferences did not contain the fake plaintext key.

Local generation:

- Triggered authenticated debug generation for `com.android.settings`.
- Polled until status reported completion.
- Output created under:
  `/sdcard/Android/data/dev.artplus.mobile/files/ArtPlus/com.android.settings`
- Verified ART+ PNGs exist, including:
  - `recbg.png`
  - `recfg.png`
  - `rec_night.png`
  - `monochrome.png`
  - `*_1x2.png`
  - `*_2x1.png`
  - `*_2x2.png`

Root write:

- Verified `su -c id` works on `192.168.31.216:5555`.
- Triggered authenticated debug generation with `install_root=true`.
- App reported: `调试生成完成并写入 Root，未刷新，请手动点刷新 ART+ 图标`.
- Verified output at `/data/oplus/uxicons/com.android.settings`.
- Files were written as root and include the expected ART+ PNG set.
- Added debug-only `root_write_mode` test control and verified all three UI write modes:
  - `all`: wrote normal, dark, monochrome, 1x2, 2x1, 2x2, and template PNG files.
  - `default`: wrote normal/dark/template PNG files and excluded `monochrome*.png`.
  - `monochrome`: wrote only `monochrome*.png`.

GPT image path:

- Started a local mock GPT image endpoint on the Mac and exposed it to the device with `adb reverse`.
- Set debug GPT mode to `images`, base URL to `http://127.0.0.1:38273/v1`, and a fake key through authenticated debug params.
- Triggered debug generation for `com.android.settings` with `use_gpt=true`.
- App completed generation and wrote the expected ART+ output files.
- Pulled `recbg.png` and `recfg.png` back from the device and verified their pixel colors match the mock images:
  - `recbg.png`: solid mock blue.
  - `recfg.png`: mock white/red foreground with transparency.
- This validates multipart request construction, authorization header use, image response parsing, GPT layer generation, and output writing without consuming a real GPT key.

RMBG:

- Triggered authenticated debug inspect for `com.android.settings` with `include_rmbg=true` and `rmbg_input_size=1024`.
- ONNX Runtime loaded, inference completed, and the app returned `ok: true`.
- Runtime on `192.168.31.216:5555` was about 123 seconds for `com.android.settings`.
- The generated RMBG candidate was rejected by the app's safety check because coverage was about 78% and touched the icon edge.
- Repeated with `org.telegram.messenger`; inference completed in about 125 seconds, but the RMBG candidate was also rejected because coverage was about 62.47% and still included the blue circular plate instead of isolating only the paper-plane glyph.
- Device-generated comparison image saved at `/Volumes/Download/ArtPlusOutput/artplus-rmbg-216-effect.png`.
- No app crash was observed during either RMBG run.

Static security scan:

- Searched source for common secret patterns, including OpenAI/GitHub/API-key forms. No hardcoded GPT/OpenAI/GitHub token was found.
- Reviewed risky surfaces found by search: `ServerSocket`, `LocalServerSocket`, `ProcessBuilder("su")`, `ZipInputStream`, `Authorization`, cleartext URLs, package enumeration permissions, SharedPreferences, and Android Keystore use.
- Confirmed release manifest/build config disables cleartext traffic, disables backup, and does not set `debuggable`.
- Confirmed debug HTTP is guarded by `FLAG_DEBUGGABLE`, loopback binding, and a random token.
- Confirmed RMBG ZIP extraction uses canonical path checks plus entry and unpacked-size limits.

Skipped or manual-only checks:

- Live external GPT upstream was not called. The app-side GPT code path was verified with a local mock endpoint to avoid consuming or exposing a real API key.
- SAF output directory export was not driven through the Android document picker because it requires an interactive persisted URI grant. The app's normal private output directory was verified, and the SAF export implementation was reviewed for cursor closure and file overwrite behavior.
- Custom foreground/background import through the system file picker was not automated for the same URI-grant reason. The import path was code-reviewed and ordinary generated candidate switching was exercised through debug generation.
- RMBG one-click model download was not re-downloaded during final verification because the component was already installed and the model is a large external dependency. The installed component was used for real on-device inference, and the downloader/unzipper was code-reviewed for URL, size, and Zip Slip handling.

## Residual Risks

- `QUERY_ALL_PACKAGES` remains intentional for installed app enumeration. This is still a privacy-sensitive permission.
- `PACKAGE_USAGE_STATS` remains declared. It should only be requested when a feature actually needs it.
- Release signing still uses the default Android debug certificate because no signing key was provided.
- `MainActivity` is exported for launcher use. Debug command extras are token-gated and debug-build-only, but the launcher activity remains callable by other apps.
- `UxIconConfigCli` uses private Android/OPlus APIs by reflection for ColorOS icon refresh behavior; lint flags this as unsupported and device-specific.
- RMBG model/component downloads still do not verify a pinned SHA-256 or signature. Size/path limits reduce DoS and Zip Slip risk, but they do not prove model authenticity.
- RMBG-2.0 now runs on 216 at 1024 input size, but it is slow on CPU and its candidates are not yet good enough for automatic adoption on tested launcher icons. It should remain an optional/manual candidate until model quality or post-processing improves.
- Third-party GitHub Actions are version-tag pinned, not commit-SHA pinned.
- No automated instrumentation or unit tests currently cover the large Compose/activity code path.
- The debug HTTP server remains a powerful local test surface in debug builds. It is loopback/token-gated, but should never be shipped as a debuggable release artifact.

## Current Status

The high-risk debug release exposure is fixed and verified. The release APK installed on `192.168.31.216:5555` is non-debuggable, has no debug HTTP listener, rejects release cleartext URL use at code/manifest level, and the local generation/root-write lifecycle was verified through debug-only authenticated tooling. RMBG is callable and completes inference at 1024, but the tested candidates are correctly rejected by the app because they preserve too much icon plate/background.
