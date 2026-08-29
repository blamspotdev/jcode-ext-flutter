#!/bin/sh
# Build
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
echo "== flutter build apk --debug =="
flutter build apk --debug
echo
echo "APK ready: build/app/outputs/flutter-apk/app-debug.apk"
