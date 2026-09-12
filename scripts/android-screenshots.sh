#!/usr/bin/env bash
set -euo pipefail
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/android-common.sh"

# Screenshot fixtures replace debug-app data. Restrict this command to an emulator.
ADB="$ANDROID_HOME/platform-tools/adb"
SERIAL="${ANDROID_SERIAL:-$("$ADB" -e get-serialno)}"
[[ "$SERIAL" == emulator-* ]] || android_die "Use an emulator for listing fixtures (set ANDROID_SERIAL if several are running)."
adb_device() { "$ADB" -s "$SERIAL" "$@"; }
[[ "$(adb_device shell getprop ro.build.version.sdk | tr -d '\r')" -ge 33 ]] || android_die "Listing capture requires an API 33+ emulator for app locales."

original_size="$(adb_device shell wm size | sed -n 's/Override size: //p' | tr -d '\r')"
original_density="$(adb_device shell wm density | sed -n 's/Override density: //p' | tr -d '\r')"
original_font="$(adb_device shell settings get system font_scale | tr -d '\r')"
restore_display() {
    adb_device shell wm size "${original_size:-reset}" >/dev/null
    adb_device shell wm density "${original_density:-reset}" >/dev/null
    adb_device shell settings put system font_scale "$original_font" >/dev/null
}
trap restore_display EXIT
# Native 9:16 capture: Play permits a longest/shortest dimension ratio of at most 2.
adb_device shell wm size 1080x1920
adb_device shell wm density 360
adb_device shell settings put system font_scale 1.0
android_gradle assembleDebug assembleDebugAndroidTest
adb_device install -r "$ANDROID_APP_DIR/app/build/outputs/apk/debug/app-debug.apk"
adb_device install -r "$ANDROID_APP_DIR/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"

for locale in en-US zh-TW; do
    log_path="$(mktemp)"
    # Gradle connectedAndroidTest uninstalls the app (and its exported files). Run the
    # already-built instrumentation directly, and check its result, before pulling.
    adb_device shell am instrument -w -e class dev.hsichen.colorinvo.PlayStoreScreenshotTest \
        -e locale "$locale" "$ANDROID_DEBUG_PACKAGE_NAME.test/androidx.test.runner.AndroidJUnitRunner" | tee "$log_path"
    if ! grep -Eq '^OK \([1-9][0-9]* tests?\)' "$log_path"; then
        rm -f "$log_path"
        android_die "Listing capture failed for $locale. No assets were copied for this locale."
    fi
    rm -f "$log_path"
    output_dir="$ANDROID_APP_DIR/play/$locale/images"
    mkdir -p "$output_dir"
    adb_device pull "/sdcard/Android/data/$ANDROID_DEBUG_PACKAGE_NAME/files/play-store/$locale/." "$output_dir/"
done
python3 "$ANDROID_ROOT_DIR/scripts/android-store-check.py"
