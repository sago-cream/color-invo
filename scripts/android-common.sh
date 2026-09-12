#!/usr/bin/env bash

ANDROID_ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_APP_DIR="${ANDROID_APP_DIR:-$ANDROID_ROOT_DIR/apps/android}"
ANDROID_PACKAGE_NAME="${ANDROID_PACKAGE_NAME:-dev.hsichen.colorinvo}"
ANDROID_DEBUG_PACKAGE_NAME="${ANDROID_DEBUG_PACKAGE_NAME:-$ANDROID_PACKAGE_NAME.debug}"

android_die() {
    echo "$*" >&2
    exit 1
}

android_load_env_file() {
    local env_path="$1"
    [[ -f "$env_path" ]] || return 0

    local line key value
    while IFS= read -r line || [[ -n "$line" ]]; do
        [[ -z "$line" || "$line" == \#* || "$line" != *=* ]] && continue
        key="${line%%=*}"
        value="${line#*=}"
        [[ "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue
        [[ -z "${!key+x}" ]] || continue
        printf -v "$key" '%s' "$value"
        export "$key"
    done <"$env_path"
}

android_load_env() {
    if [[ -n "${ANDROID_ENV_FILE:-}" ]]; then
        android_load_env_file "$ANDROID_ENV_FILE"
    else
        android_load_env_file "$ANDROID_ROOT_DIR/.env.local"
        android_load_env_file "$ANDROID_ROOT_DIR/.env"
    fi
}

android_find_sdk() {
    local candidate
    for candidate in \
        "${ANDROID_HOME:-}" \
        "${ANDROID_SDK_ROOT:-}" \
        "$HOME/Library/Android/sdk" \
        "/opt/homebrew/share/android-commandlinetools" \
        "/usr/local/share/android-commandlinetools"
    do
        [[ -n "$candidate" && -d "$candidate/platforms" ]] || continue
        ANDROID_HOME="$candidate"
        ANDROID_SDK_ROOT="$candidate"
        export ANDROID_HOME ANDROID_SDK_ROOT
        return
    done
    android_die "Android SDK not found. Install API 36 in Android Studio or set ANDROID_HOME."
}

android_gradle() {
    (cd "$ANDROID_APP_DIR" && ./gradlew "$@")
}

android_device_count() {
    "$ANDROID_HOME/platform-tools/adb" devices | awk 'NR > 1 && $2 == "device" { count++ } END { print count + 0 }'
}

android_require_device() {
    [[ "$(android_device_count)" -gt 0 ]] || android_die "No unlocked Android device or emulator is connected."
}

android_resolve_release_versions() {
    ANDROID_VERSION_NAME="${ANDROID_VERSION_NAME:-0.1.0}"
    ANDROID_VERSION_CODE="${ANDROID_VERSION_CODE:-1}"
    if [[ "${ANDROID_RELEASE_PROMPT:-0}" == "1" && -t 0 ]]; then
        local value
        read -r -p "Version name [$ANDROID_VERSION_NAME]: " value
        ANDROID_VERSION_NAME="${value:-$ANDROID_VERSION_NAME}"
        read -r -p "Version code [$ANDROID_VERSION_CODE]: " value
        ANDROID_VERSION_CODE="${value:-$ANDROID_VERSION_CODE}"
    fi
    [[ "$ANDROID_VERSION_CODE" =~ ^[1-9][0-9]{0,9}$ && "$ANDROID_VERSION_CODE" -le 2100000000 ]] || android_die "ANDROID_VERSION_CODE must be an integer from 1 to 2100000000."
    [[ "$ANDROID_VERSION_NAME" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-][A-Za-z0-9.-]+)?$ ]] || android_die "ANDROID_VERSION_NAME must look like 0.1.0 or 0.1.0-beta.1."
    export ANDROID_VERSION_NAME ANDROID_VERSION_CODE
}

android_require_release_signing() {
    java "$ANDROID_ROOT_DIR/scripts/AndroidSigningCheck.java" signing "$ANDROID_APP_DIR"
}

android_require_play_credentials() {
    python3 "$ANDROID_ROOT_DIR/scripts/android-play-credentials.py"
    command -v bundle >/dev/null || android_die "Ruby Bundler is required. Install Ruby 3.3 and run bundle install in apps/android."
    (cd "$ANDROID_APP_DIR" && bundle check >/dev/null) || android_die "Fastlane dependencies are missing. Run bundle install in apps/android."
}

android_load_env
android_find_sdk
