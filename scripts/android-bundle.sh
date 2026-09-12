#!/usr/bin/env bash
set -euo pipefail
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/android-common.sh"
android_resolve_release_versions
android_require_release_signing
python3 "$ANDROID_ROOT_DIR/scripts/android-store-check.py"
android_gradle lintRelease bundleRelease \
    -PversionName="$ANDROID_VERSION_NAME" \
    -PversionCode="$ANDROID_VERSION_CODE"
"$ANDROID_ROOT_DIR/scripts/android-verify-bundle.sh"
echo "Signed bundle: $ANDROID_APP_DIR/app/build/outputs/bundle/release/app-release.aab"
