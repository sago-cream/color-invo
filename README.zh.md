<div align="center">
  <img src="apps/ios/Resources/ColorInvo/Assets.xcassets/AppIcon.appiconset/AppIcon-10241024-1x.png" alt="條色盤 app icon" width="96" />
  <h1>條色盤</h1>
  <p>讓你的載具條碼在 iOS 與 Android 都能和桌布顏色完美搭配。</p>
  <a href="./README.md">English</a>
</div>

## 功能介紹

- **一秒出示載具：** 利用 iOS 或 Android 小工具輕鬆展示載具條碼。
- **桌布主題配色：** 選一張桌布圖片，自動抓代表色與小張預覽並產生配色方案。
- **保證能掃的到：** 確保條碼配色符合商業掃描器反射率對比規則。

## 官方版本

官方條色盤 App 與網站由 Hsi 發佈於
[colorinvo.hsichen.dev](https://colorinvo.hsichen.dev)。Fork 版本請使用自己的 App
名稱、bundle identifier、圖示、截圖、支援連結、隱私權政策與部署端點。

## 安裝

iPhone 使用者可於
[App Store](https://apps.apple.com/tw/app/%E6%A2%9D%E8%89%B2%E7%9B%A4/id6786967206)
下載條色盤。

## 在 Android 執行

先用 Android Studio 安裝 Android 16 / API 36 SDK，啟動模擬器或連接已解鎖的裝置，接著執行：

```sh
bun run android
```

執行單元測試、Lint，並編譯 App 與裝置測試 APK：

```sh
bun run android:check
```

連接裝置或模擬器後，可執行完整介面與小工具測試：

```sh
bun run android:test:device
```

## Google Play 發佈

現在就能執行 `bun run android:candidate`，不需要 Play 帳號或簽署金鑰。這會檢查雙語商店素材、發佈工具、單元測試、debug/release lint，以及壓縮後的 AAB（含 16 KB 原生程式庫相容性）。GitHub Actions 也會測試 Android 8 與 Android 16，並保留候選 AAB、debug APK 與測試報告。

設定上傳金鑰後，`bun run android:bundle` 可產生已驗證簽章的 AAB，供首次手動上傳 Play Console。完成帳號與 API 設定後，`bun run android:release` 會建置並向 Google 驗證內部測試草稿；設定 `PLAY_VALIDATE_ONLY=false` 才會實際提交。`bun run android:promote` 可直接晉升指定的既有版本，不必重新建置。

**Android Play release** 工作流程包含 `candidate`、`bundle`、`upload` 與 `promote` 四種模式。雙語文案、圖示、主打圖片與各三張截圖已放在 `apps/android/play`。可在 API 33 以上的模擬器執行 `bun run android:screenshots` 重新產生，並以 `bun run android:store:check` 檢查。

金鑰名稱、首次上傳與後續操作請參考 [發佈指南](apps/android/PLAY_RELEASE.md)，帳號中的應用程式內容表單則可參考 [預備聲明資料](apps/android/PLAY_DECLARATIONS.md)。

## 在 iOS 模擬器執行

建置條色盤、啟動並開啟可用的 iPhone 模擬器，然後安裝與執行 App：

```sh
bun run simulator
```

## 授權

原始碼與文件使用 MIT License。「條色盤」、ColorInvo、App 圖示、原創美術、截圖、
App Store listing、網域與其他品牌資產不授權重用。詳見
[BRAND.md](./BRAND.md)。
