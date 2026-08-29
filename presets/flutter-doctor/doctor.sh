#!/bin/sh
# Check
# JCode's terminals run `sh -c` with no login profile, so none of this is inherited.
if command -v javac >/dev/null 2>&1; then
  JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")"
  export JAVA_HOME
  export PATH="$JAVA_HOME/bin:$PATH"
fi
# The SDK Manager installs as the runtime user, so the SDK may live in a different home.
ANDROID_HOME="$HOME/android-sdk"
[ -d "$ANDROID_HOME" ] || ANDROID_HOME="$(ls -d /home/*/android-sdk /root/android-sdk 2>/dev/null | head -1)"
export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"
cd "$JCODE_PROJECT_DIR" || exit 1
echo "== flutter doctor =="
flutter doctor -v
echo
echo "== can this project build here? =="
APP=android/app
if [ ! -d "$APP" ]; then
  echo "- no Android module; nothing to check for the NDK stripper."
elif grep -qs keepDebugSymbols "$APP/build.gradle.kts" "$APP/build.gradle"; then
  echo "- keepDebugSymbols is set: stripDebugSymbols will be skipped. Good."
else
  echo "- keepDebugSymbols is NOT set."
  echo "  This device's NDK ships no linux-arm64 prebuilts, so AGP cannot run its stripper at"
  echo "  all and the build fails on :app:stripDebugSymbols. Add this to"
  echo "  android/app/build.gradle.kts:"
  echo
  echo "      android { packaging { jniLibs { keepDebugSymbols.add(\"**/*.so\") } } }"
fi
