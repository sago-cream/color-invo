<div align="center">

  <img src="apps/ios/Resources/ColorInvo/Assets.xcassets/AppIcon.appiconset/AppIcon-10241024-1x.png" alt="ColorInvo app icon" width="96" />
<h1>ColorInvo</h1>
<p>Make your Taiwan mobile invoice carrier barcode match your wallpaper on iOS and Android.</p>
  <a href="./README.zh.md">繁體中文</a>
</div>

## Why

* Instant Barcode Access: Show your invoice barcode in a second with an iOS or Android widget.
* Wallpaper-Based Theming: Pick a wallpaper, and the app extracts representative colors and a small preview to generate matching themes.
* Guaranteed Scannability: Barcode colors are generated to meet commercial scanner reflectance and contrast requirements.

## Run on Android

Install Android Studio with the Android 16 / API 36 SDK, start an emulator or connect an unlocked device, then run:

```sh
bun run android
```

Run unit tests, lint, and compile both app and instrumented-test APKs:

```sh
bun run android:check
```

With a device or emulator connected, run the full UI/widget tests:

```sh
bun run android:test:device
```

## Google Play release

Run `bun run android:candidate` now: no Play account or signing secrets are needed. It checks the localized listing, release tooling, unit tests, debug/release lint, and the minified AAB (including 16 KB native compatibility). GitHub Actions also runs Android 8 and Android 16 emulator tests and saves the candidate and debug APK as artifacts.

When an upload key is configured, `bun run android:bundle` produces a verified signed AAB for the first manual Play Console upload. Once the app record and API access exist, `bun run android:release` builds and validates an internal draft with Google; set `PLAY_VALIDATE_ONLY=false` to commit the upload. `bun run android:promote` promotes a specific existing version without rebuilding it.

The **Android Play release** workflow has `candidate`, `bundle`, `upload`, and `promote` modes. Store copy, icons, feature graphics, and three screenshots per locale are committed under `apps/android/play`. Regenerate them on an API 33+ emulator with `bun run android:screenshots`; validate them with `bun run android:store:check`.

See the [release guide](apps/android/PLAY_RELEASE.md) for the exact secrets, first-upload steps, commands, and remaining account requirements, and the [prepared Console answers](apps/android/PLAY_DECLARATIONS.md) for the app-content forms.

## Run on iOS Simulator

Build ColorInvo, boot and open an available iPhone simulator, install the app, and launch it:

```sh
bun run simulator
```

## Codex iOS simulator screenshots

To send a visible iOS Simulator screenshot in Codex chat, capture the booted simulator and attach the PNG bytes to the chat result instead of linking to a local Mac path:

```sh
bun run ios:screenshot-chat
```

The script writes an absolute PNG path under `.codex-screenshots/` and prints a `node_repl` snippet. Run that snippet with the `node_repl` `js` tool so Codex chat receives the image bytes through `nodeRepl.emitImage(...)`; this makes the screenshot viewable from Codex mobile too.
