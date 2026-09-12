#!/usr/bin/env bash
set -euo pipefail
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/android-common.sh"

ANDROID_AAB_PATH="${ANDROID_AAB_PATH:-$ANDROID_APP_DIR/app/build/outputs/bundle/release/app-release.aab}"
PLAY_TRACK="${PLAY_TRACK:-internal}"
export ANDROID_AAB_PATH PLAY_TRACK

[[ -f "$ANDROID_AAB_PATH" ]] || android_die "Release bundle not found at $ANDROID_AAB_PATH. Run bun run android:bundle first."
android_resolve_release_versions
android_require_play_credentials
(cd "$ANDROID_APP_DIR" && bundle exec fastlane android upload)
