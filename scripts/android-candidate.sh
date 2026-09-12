#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/android-common.sh"
android_resolve_release_versions
python3 "$SCRIPT_DIR/android-store-check.py"
python3 -m unittest discover -s "$SCRIPT_DIR/tests"
android_gradle testDebugUnitTest lintDebug lintRelease assembleDebug assembleDebugAndroidTest bundleRelease \
    -PversionName="$ANDROID_VERSION_NAME" -PversionCode="$ANDROID_VERSION_CODE"
"$SCRIPT_DIR/android-verify-bundle.sh" allow-unsigned
printf '%s\n' "Candidate: $ANDROID_APP_DIR/app/build/outputs/bundle/release/app-release.aab" \
    "A candidate without upload signing cannot be uploaded to Play. Use android:bundle when the upload key is configured."
