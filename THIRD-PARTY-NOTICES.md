# Third-Party Notices

ArtPlus as a whole is licensed under **GPL-3.0-or-later** (see `LICENSE`).
The following components are adapted from upstream projects and remain
under their original licenses, as noted in each file header.

## GPL-3.0-or-later

- **KernelSU Manager** — https://github.com/tiann/KernelSU
  - Files: `mobile/.../glass/FloatingBottomBar.kt`,
    `mobile/.../glass/DampedDragAnimation.kt`,
    `mobile/.../glass/InteractiveHighlight.kt`,
    `mobile/.../glass/DragGestureInspector.kt`
  - Upstream license: GPL-3.0-or-later (all parts except the `kernel/` directory).
  - Changes: ported to this project's Compose setup, package renamed to
    `dev.artplus.mobile.glass`, behavior kept equivalent.

## Apache-2.0

- **Kyant0/AndroidLiquidGlass** — https://github.com/Kyant0/AndroidLiquidGlass
  - Files: `mobile/.../glass/CombinedBackdrop.kt`,
    `mobile/.../glass/Lens.kt`, `mobile/.../glass/Vibrancy.kt`,
    `mobile/.../glass/InnerShadow.kt`
  - Changes: adapted to Miuix blur APIs used by this project.
- **compose-miuix-ui examples** (e.g. `IosLiquidGlassNavigationBar`)
  - Referenced by `FloatingBottomBar.kt` and the Kyant-based files above.
- **Miuix KMP libraries** (`top.yukonga.miuix.kmp`, v0.9.3, Gradle dependency)
  - https://github.com/YuKongA/miuix — Apache-2.0.

## Other Gradle dependencies (linking only)

- AndroidX / Jetpack Compose — Apache-2.0
- `com.microsoft.onnxruntime:onnxruntime-android` — MIT
- `com.caverock:androidsvg-aar` — Apache-2.0
- `com.composables:icons-lucide-android` (Lucide icons) — ISC
- `io.github.kyant0:shapes` — Apache-2.0
