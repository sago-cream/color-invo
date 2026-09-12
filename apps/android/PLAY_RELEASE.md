# Google Play release guide

The app and listing can be built and checked without a Play account. Uploading still requires verified Play Console access, a first manual bundle upload, and completed app-content forms. New personal accounts may also need a closed test before production access.

## What is ready without an account

- Native Android app, localized offline privacy policy and setup help, and home-screen widget.
- English and Traditional Chinese listing text, 512 × 512 icons, 1024 × 500 feature graphics, and three 1080 × 1920 screenshots each.
- Release minification, debug/release lint, JVM tests, barcode rendering/decoding tests, editor tests, and a real widget-host test.
- CI on `main`, `feat/android-native`, and Android pull requests. It tests API 26 and 36 and saves an unsigned AAB, installable debug APK, R8 mapping, checksums, and test reports.
- Manual `candidate`, `bundle`, `upload`, and `promote` workflow modes. The release workflow runs the same checks before signing; promotion reuses an exact existing version.
- Offline guards for listing limits, signing credentials, bundle identity/version, unexpected permissions, signatures, and 16 KB ELF/APK alignment. Upload also verifies that the public privacy pages cover Android.

Merge the Android workflows into the default branch to expose the manual workflow in GitHub's Actions UI. Branch pushes already run account-free checks. A GitHub artifact named `android-candidate-…` contains an **unsigned** AAB: it cannot be uploaded to Play. `android-signed-…` contains the signed AAB for Play. Signing does not require a service account.

## Build now

Install JDK 17, Android SDK 36/build-tools 36.0.0, Python 3, and Ruby 3.3. Use the committed Gradle wrapper and `Gemfile.lock`.

```sh
bun run android:candidate
bun run android:test:device
cd apps/android
bundle install
bundle exec ruby fastlane/release_config_test.rb
```

The candidate is at `app/build/outputs/bundle/release/app-release.aab`. Its adjacent `release-manifest.json` records package, version, target API, native libraries, SHA-256, Git commit, and whether the working tree was dirty. The AAB embeds R8 mapping; CI also saves `mapping.txt` separately. Keep the exact signed artifact used for an upload.

## Prepare the upload key (no Play account needed)

Create a dedicated upload key using JDK `keytool`. This command prompts for passwords; it does not print or put them in shell history:

```sh
keytool -genkeypair -v -storetype JKS \
  -keystore /private/path/colorinvo-upload.jks \
  -alias colorinvo-upload -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=ColorInvo Upload"
```

Back up the key and passwords in private storage. This is the upload key, distinct from the app-signing key managed by Play App Signing. Do not use a debug key or the temporary fixture keys from tests.

For local signing, copy `keystore.properties.example` to `keystore.properties` and fill in all four values. `storeFile` is absolute or relative to `apps/android`. Java properties escaping applies. Alternatively set the four `ANDROID_UPLOAD_*` variables listed below; environment variables override properties, including empty values. Run:

```sh
ANDROID_VERSION_NAME=0.1.0 ANDROID_VERSION_CODE=1 bun run android:bundle
```

This checks the real private key before building and verifies every bundle content entry's signature afterward. No Google credentials are needed.

## Fill in GitHub secrets

Create the `google-play` environment in repository Settings → Environments. Add these secrets:

| Secret | Value |
| --- | --- |
| `ANDROID_UPLOAD_KEYSTORE_BASE64` | Base64 contents of the private upload keystore; wrapped or single-line encoding both work |
| `ANDROID_UPLOAD_STORE_PASSWORD` | Keystore password |
| `ANDROID_UPLOAD_KEY_ALIAS` | `colorinvo-upload`, or the alias actually used |
| `ANDROID_UPLOAD_KEY_PASSWORD` | Private-key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | Complete Google service-account JSON; needed only for upload/promotion |

The previous combined `ANDROID_KEYSTORE_PROPERTIES` CI secret is replaced by individual fields so passwords and paths cannot be misparsed by shell-generated properties files. The local `keystore.properties` path remains supported. Credentials are deleted after CI runs and never included in artifacts.

Once signing secrets exist, dispatch **Android Play release** with `operation=bundle`, version name and a fresh version code. Download the `android-signed-…` artifact even if API access has not been configured yet.

## First Play Console setup

1. Create and verify the developer account and create **ColorInvo / 條色盤**, package `dev.hsichen.colorinvo`. Keep that package ID; it becomes fixed after the first upload.
2. Enroll in Play App Signing. Manually upload the signed AAB from `operation=bundle` to the internal track. [Fastlane requires an initial manual upload](https://docs.fastlane.tools/actions/upload_to_play_store/#quick-start).
3. Complete the forms using [PLAY_DECLARATIONS.md](PLAY_DECLARATIONS.md), add listing assets from `play/`, choose countries/pricing and the intended audience, and add internal testers. The console determines which declarations apply.
4. Use `https://colorinvo.hsichen.dev/en/privacy` for English and `https://colorinvo.hsichen.dev/privacy` for Traditional Chinese. Confirm the deployed policy includes Android. `bun run android:policy:check` checks both URLs.
5. Enable the Google Play Android Developer API in a Google Cloud project. Create a service account, download its JSON key, and invite its `client_email` in Play Console with access to ColorInvo. Grant testing-release and store-presence permissions; grant production-release permission when production promotion is needed. [Google's API setup guide](https://developers.google.com/android-publisher/getting_started) describes the account connection.
6. Add the JSON to the GitHub environment, or set `PLAY_SERVICE_ACCOUNT_JSON=/absolute/path/to/key.json` in the gitignored `.env.local` for local use.

The first manual upload consumes its version code. Choose a higher code for the next upload. Never rebuild and upload different content under an already-used code; promotion uses the existing code.

## Subsequent upload and promotion

| Operation | What it does | Required credentials |
| --- | --- | --- |
| `candidate` | Runs checks and exports an unsigned candidate | None |
| `bundle` | Checks, signs, verifies, and exports an AAB | Upload key and three signing fields |
| `upload` | Builds a signed AAB and submits it with the committed listing | Signing + Play JSON |
| `promote` | Moves the selected existing version to another track, without rebuilding or replacing listing assets | Play JSON |

`upload` and `promote` default to **validation only** and **draft**. Validation calls Google but does not commit the edit. After the result is clean, rerun with `validate_only=false`. Use `release_status=completed` to make a release available on its track after any required Google review; use `draft` while the initial app setup is incomplete. `alpha` and `beta` are the API names of the default closed/open tracks. Custom named tracks are not configured by this workflow.

Local equivalents (set version name/code and credentials in `.env.local`):

```sh
# Build and validate the internal draft with Google.
bun run android:release

# Commit the already-built bundle and the complete reviewed listing.
PLAY_VALIDATE_ONLY=false PLAY_RELEASE_STATUS=draft bun run android:upload

# Promote exactly version 2 after testing; no AAB rebuild or listing update.
ANDROID_VERSION_CODE=2 PLAY_SOURCE_TRACK=internal PLAY_TRACK=production \
  PLAY_RELEASE_STATUS=completed PLAY_VALIDATE_ONLY=false bun run android:promote
```

Use an incremented version code if Google considers a validation upload consumed. Keep release operations serialized (the workflow does this). If an upload fails, the signed artifact is retained; investigate Console/API errors before retrying. If a released build is bad, halt rollout in Console and fix forward with a higher version code.

## Regenerate listing screenshots

```sh
bun run android:screenshots
bun run android:store:check
```

Use an API 33+ emulator; real devices are rejected because fixtures replace debug-app data. Set `ANDROID_SERIAL` if more than one emulator is running. Capture temporarily uses 1080 × 1920, density 360, and normal text size, then restores the previous display settings. It installs the test APKs directly so screenshots survive until they are pulled. Synthetic `/ABC1234` and a generated wallpaper keep personal data out of the assets. Review regenerated assets before committing; upload synchronizes the committed listing by hash.

## Remaining release gates

These cannot be completed with missing account access or simulated credentials:

- Developer identity/device verification, first app upload, app-content declarations, countries/pricing, Play App Signing enrollment, service-account grants, and Google's review.
- For personal accounts created after November 13, 2023, [at least 12 testers continuously opted in for 14 days](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en) before applying for production access, if this requirement applies to the account. Internal testing alone does not satisfy it.
- Review the Play pre-launch report for the exact uploaded bundle and smoke-test the Play-installed build on a physical device. Confirm wallpaper selection, all styles, background/foreground transitions, reboot, widget resizing, accessibility text size, and a real checkout scanner. Emulator decoding tests are useful but do not prove optical scanner performance.

The current [target-API requirement](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en) is met by target SDK 36. Recheck Play requirements when actually launching; account verification and testing deadlines are not made faster by adding keys to CI.
