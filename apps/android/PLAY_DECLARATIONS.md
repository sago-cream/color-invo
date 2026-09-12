# Prepared Play Console information

Prepared September 12, 2026 from the Android app and merged release manifest. These are answers to review while completing the account's Console forms, not declarations already submitted to Google.

## Listing and contact

| Field | Prepared value |
| --- | --- |
| Package | `dev.hsichen.colorinvo` |
| English / Traditional Chinese titles | ColorInvo / 條色盤; exact titles and descriptions in `play/` |
| Suggested category | Personalization: wallpaper colors and home-screen barcode widgets |
| Support email | `its.hsichen@gmail.com` |
| Website | `https://colorinvo.hsichen.dev` |
| Privacy policy | `https://colorinvo.hsichen.dev/en/privacy` and `https://colorinvo.hsichen.dev/privacy` |
| App access | All functionality is available without a login, subscription, invitation, or special access |
| Reviewer setup | Enter the synthetic carrier `/ABC1234`; choose a wallpaper with the system picker; add ColorInvo from the launcher widget picker |
| Ads | No ads or advertising SDK |
| In-app purchases | None |

Choose pricing, distribution countries, intended age groups, and account contact details in Console. Those are owner/account choices. The Android app does not create a Taiwan mobile invoice carrier account or check invoice balances; users bring an existing carrier.

## Data safety draft

The current Android app does not transmit user data to the developer or third parties. For the form's collection/sharing questions, the prepared answer is **no data collected or shared by the app**, subject to reviewing the current Console definitions and the final build. [Google excludes processing solely on-device from collection](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en).

| Data / behavior | Current implementation |
| --- | --- |
| Mobile invoice carrier and selected colors | Private SharedPreferences on the device, shared with the widget; no server request |
| Selected wallpaper | Read only after a system-picker selection; analyzed locally; stores only a small local preview and derived colors |
| Photos/storage permissions | No broad photo-library or external-storage permission |
| Network transmission | No `INTERNET` permission; no backend client, analytics, advertising ID, crash-reporting SDK, or account SDK |
| Widget background work | Glance/WorkManager uses wake-lock, network-state, boot-completed, and a private receiver permission; no foreground service or network transfer |
| Backup / device transfer | OS-managed Android backup includes carrier preferences and wallpaper preview according to user settings; disclosed in the policy |
| User support | Email address and message content are handled only when the user separately contacts support; no message is sent automatically by the app |
| Deletion | Android Settings → Apps → ColorInvo → Storage → Clear storage, or uninstall; system backups are managed separately and can restore data |
| Account deletion | No in-app accounts or account creation |

The release bundle guard fails if new permissions appear, including Internet, broad photo access, or foreground services. A dependency or feature that changes data handling requires revisiting this page and the policy. Website request logs are disclosed separately and are not app analytics.

## App-content forms

- **Content rating:** complete IARC as a utility. There is no violence, sexual content, gambling, user chat, online user-generated content, or in-app purchasing in the app. Google generates the rating from submitted answers; no rating is claimed here.
- **Target audience:** choose the intended shopper audience. The app has no child-directed marketing or Families configuration; do not opt into Families without a separate review.
- **News, health, government:** the app supplies none of these services and is not affiliated with a government. It displays a user-provided carrier barcode.
- **Financial features:** no payments, money transfer, lending, investments, crypto, or financial advice. The barcode is for Taiwan e-invoice collection, not a payment instrument; review any additional financial-function options in the actual form.
- **Sensitive permissions / foreground services:** no broad media permission, foreground service, exact alarm, VPN, accessibility service, or all-files access is declared.

Complete any additional account-specific forms shown by Console, then use its pre-launch report and the release checklist for final validation.
