# Android / iOS parity

Reviewed on 2026-09-12. Android started at `80710d30` on `feat/android-native`.
The reference is the iOS runtime in `origin/main` at `6c328146`; its source files
are identical to the iOS runtime already present on the Android branch.

## Behavior and persistence

| Area | Before | After |
| --- | --- | --- |
| Autosave | Every draft, including invalid codes and unsafe colors, replaced the widget settings. | Only valid carriers with scanner-ready palettes sync, with the same 350 ms debounce as iOS. |
| Save ordering | Cancelled save jobs could race with newer edits. | A single writer completes saves in order and processes the latest draft, including a revert during a write. |
| Sync feedback | Empty and invalid drafts could say “Synced.” | The UI distinguishes synced, saving, unsynced, and failed saves. |
| Carrier entry | Overlong pasted values were silently truncated; invalid characters could crash the renderer. | Full input is retained for validation, normalization is locale independent, and invalid input shows an empty preview. |
| Wallpaper decoding | Full-resolution decode with no orientation handling or preview. | Orientation-aware decoding capped at 900 pixels, with a locally saved JPEG preview. Android 8 uses sampled decoding and AndroidX ExifInterface. |
| Color extraction | Different buckets, scoring, and source-color selection from iOS. | Matching quantization, saturation scoring, perceptual separation, and transparent-pixel filtering. |
| Generated palettes | Different colors and fewer than three choices for a single-color image. | The three iOS recipes, including hue fallbacks and scanner contrast margin. |
| Wallpaper selection | Selecting a palette did not retain its source or reset target. | The base palette and decoration color persist; manual edits retain the selected option and can be reset. |
| Import ordering | A late image could overwrite a newer import or manual color edit. | Cancelled/stale requests are ignored, and color edits made during analysis are retained. |
| Failed imports | Generic failure without a saved wallpaper preview. | The prior colors and preview remain available with localized error feedback. |
| Backup | Only carrier preferences were included. | The bounded wallpaper preview is included in backup and device transfer. |
| Legacy unsafe settings | Previously saved unsafe palettes could remain on the home screen. | The widget uses black and white for those palettes; the editor retains the original colors and explains the validation problem. |

## Artwork and interface

| Area | Before | After |
| --- | --- | --- |
| Barcode rendering | Separate Compose and widget implementations, floating bar edges, and layout padding in place of encoded quiet zones. | One renderer, rounded pixel boundaries, and ten narrow modules of quiet zone on each side. |
| Cat | A translucent circle-and-ears placeholder; changing the shared paint alpha also faded the widget bars. | The original iOS cat/detail assets, deterministic torn bars, and an intact scan band across the upper 39%. |
| Paint | A translucent circle, always using the first source color. | The iOS paint path using the selected source color. |
| Carrier text | Centered text below the barcode. | The iOS capsule placement: upper-right for paint, lower-right for cat/minimal. |
| Empty widget | A sample carrier barcode appeared before setup. | Localized setup guidance without a sample barcode. Sample values are limited to the editor preview. |
| Widget resizing | One fixed bitmap stretched to every widget size. | Rendering at each Glance host size, with responsive artwork proportions and rounded corners. |
| Widget accessibility | A fixed English description. | Localized carrier value or setup guidance; tapping still opens the editor. |
| Wallpaper preview | A standalone, taller card. | The 329:155 widget preview over a faded wallpaper background with the iOS spacing, outline, and shadow. |
| Editor layout | Large disconnected gaps, gray background, and default purple Material accents. | Grouped sections and shared spacing/typography tokens with the iOS white and blue palette, including dialog surfaces. |
| Style selection | Filter chips. | A full-width, accessible three-option control. |
| Text toggle | Only the switch was actionable. | The entire labeled row toggles the setting. |
| Palette options | Hex strings in chips; options absent before import. | Three split-color swatches, disabled placeholders, and persistent selection semantics. |
| Manual colors | Hex input fields only; no reset. | Color swatches open a visual RGB/hex dialog, with validation and a reset to the selected wallpaper palette. |
| Large text | Horizontal headers and controls could compete for space. | Stacked headers and color controls at larger text sizes, with scrollable content and dialog controls. |
| Localization | Some scanner guidance and accessibility labels were hardcoded in Chinese or English. | Localized guidance, statuses, controls, and accessibility labels in English and Traditional Chinese; Android 13+ per-app language choices are declared. |
| Branding | An unrelated barcode icon and English app name in Traditional Chinese. | The iOS icon artwork in an adaptive icon, a matching monochrome icon, and the localized name “條色盤.” |
| Widget picker | An empty rectangle preview. | A rendered cat widget preview. |
| Screenshot fixtures | Appended sample input could produce an invalid carrier and leave the keyboard visible. | Seeded carrier, palette, and wallpaper fixtures, a fresh view model, and a stable frame before capture. |
| Build output | Kotlin cache files appeared as untracked files. | The Android Kotlin cache is ignored. |

## Validation

- `bun run android:check`: 13 JVM tests, strict debug lint, debug APK, and instrumentation APK builds pass.
- The iOS palette recipe values were evaluated from the Swift source and added as Android golden values. A 4,096-color input grid also checks scanner guidance after color serialization.
- Android 15 / API 35 Pixel 7 emulator: the full 15-test suite passed before adding the legacy-palette case. The final focused run passed all five renderer tests, including that case, and the screenshot test. There are now 16 instrumentation tests in total.
- Four editor interaction tests and the screenshot test also passed at 150% font scale. English and Traditional Chinese screenshots, the larger-text layout, and the color dialog were visually reviewed.
- ZXing decodes the rendered scan band across 144 combinations of three widget sizes, three decorations, four palettes, text visibility, and two carrier strings. This also checks that the scan band contains only opaque foreground/background pixels.
- The release APK builds with R8 minification and resource shrinking. Release signing and Play upload remain separate steps in `PLAY_RELEASE.md`.

## Platform differences and remaining device checks

Android uses Compose controls, the system photo picker, local app storage, and
Glance widgets; iOS uses SwiftUI, PhotosPicker, App Groups, and WidgetKit. Android's
color dialog uses RGB sliders and hex entry in place of the iOS system color picker.
Image decoding and antialiasing can produce small pixel differences between the
platforms, while the palette recipes and decoration geometry now follow the same
reference.

Before a Play release, verify actual widget placement/resizing on the intended
OEM launchers and scanning on physical phones with store scanners. The emulator
checks do not establish physical scanner performance. The Android 8 image-decoding
fallback builds and passes lint, but was not exercised on an API 26 emulator in
this pass.
