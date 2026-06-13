# Liquid Glass Icon Method Notes

Date: 2026-06-13

This note records the current experimental direction before continuing the liquid-glass icon work later.

## Current Method

- Treat the already-composed adaptive icon as the analysis source. Do not use only the adaptive background layer as the background source, because that loses foreground/base relationships for component-style icons.
- Do not blur the app background. Background blur caused dark corners, lost stripe/detail patterns, and made some icons look unlike the original.
- Preserve complex bases that are part of the icon subject, such as the metronome base. Only remove plate/background when the selected separation rule explicitly classifies it as background.
- Keep glass treatment subtle. Avoid high white-point highlights, fixed circular glare, strong corner vignettes, and visible rounded-rectangle strokes.
- Prefer edge and subject-detail treatment over global recoloring. The effect should read as a light glass material over the existing icon structure, not as a white glass card on top of the icon.

## Known Bad Results To Avoid

- A fixed circle or glare appearing in the same position on every icon.
- Four dark corners in iOS/light-glass outputs.
- A white rounded-rectangle line around dark outputs.
- Losing original background texture, such as the two dark stripes in the "Are you good?" sample.
- Removing component bases that should remain part of the subject.

## Next Validation Step

Before moving this into the APK, generate a Mac-side comparison sheet from the same raw icon ZIPs/APKs used by the Android app. The sheet should show original, current local output, and liquid-glass output so the effect can be approved visually before compiling.
