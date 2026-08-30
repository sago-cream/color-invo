<div align="center">

  <img src="apps/ios/Resources/ColorInvo/Assets.xcassets/AppIcon.appiconset/AppIcon-10241024-1x.png" alt="ColorInvo app icon" width="96" />
<h1>ColorInvo</h1>
  <p>Make your Taiwan mobile invoice carrier barcode match your wallpaper.</p>
  <a href="./README.zh.md">繁體中文</a>
</div>

## Why

* Instant Barcode Access: Show your invoice barcode in a second with an iOS widget.
* Wallpaper-Based Theming: Pick a wallpaper, and the app extracts representative colors and a small preview to generate matching themes.
* Guaranteed Scannability: Barcode colors are generated to meet commercial scanner reflectance and contrast requirements.

## Official version

The official ColorInvo app and website are published by Hsi at
[colorinvo.hsichen.dev](https://colorinvo.hsichen.dev). Forks should use their
own app name, bundle identifier, icons, screenshots, support links, privacy
policy, and deployment endpoints.

## Install

Download ColorInvo for iPhone from the
[App Store](https://apps.apple.com/tw/app/%E6%A2%9D%E8%89%B2%E7%9B%A4/id6786967206).

## Web deployment

The root `vercel.json` is the single deployment definition. Vercel installs the
`apps/web` workspace, runs the root `web:build` script, and publishes the static
export from `apps/web/dist`. Do not add a second config under `apps/web`.

## Run on iOS Simulator

Build ColorInvo, boot and open an available iPhone simulator, install the app, and launch it:

```sh
bun run simulator
```

## Codex simulator screenshots

To send a visible iOS Simulator screenshot in Codex chat, capture the booted simulator and attach the PNG bytes to the chat result instead of linking to a local Mac path:

```sh
bun run ios:screenshot-chat
```

The script writes an absolute PNG path under `.codex-screenshots/` and prints a `node_repl` snippet. Run that snippet with the `node_repl` `js` tool so Codex chat receives the image bytes through `nodeRepl.emitImage(...)`; this makes the screenshot viewable from Codex mobile too.
