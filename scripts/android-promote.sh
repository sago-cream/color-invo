#!/usr/bin/env bash
set -euo pipefail
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/android-common.sh"
android_resolve_release_versions
android_require_play_credentials
(cd "$ANDROID_APP_DIR" && bundle exec fastlane android promote)
