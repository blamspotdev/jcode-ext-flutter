#!/bin/sh
# Configure Build & Run
set -e

cat > "$JCODE_PROJECT_DIR/.jcode/run.yaml" <<EOF
version: 1
name: Flutter app
terminals:
  - label: Run on the virtual device
    command: |
      clear
      set -e
      # JCode's terminals run with no login profile, so none of this is inherited.
      if command -v javac >/dev/null 2>&1; then
        JAVA_HOME="\$(dirname "\$(dirname "\$(readlink -f "\$(command -v javac)")")")"
        export JAVA_HOME
        export PATH="\$JAVA_HOME/bin:\$PATH"
      fi
      # The SDK Manager installs as the runtime user, so the SDK may live in a different home.
      ANDROID_HOME="\$HOME/android-sdk"
      [ -d "\$ANDROID_HOME" ] || ANDROID_HOME="\$(ls -d /home/*/android-sdk /root/android-sdk 2>/dev/null | head -1)"
      export ANDROID_HOME ANDROID_SDK_ROOT="\$ANDROID_HOME"
      cd "$JCODE_PROJECT_DIR"
      # JCode's virtual device, as adb sees it. The Android Dev Pack binds an adb daemon in JCode's
      # own storage and the distro's adb client connects to that socket, so this serial IS the
      # device — no emulator and no USB target is involved.
      DEVICE=localfilesystem:/run/jcode-vdevice-adb.sock
      echo '== J Code: flutter run (hot reload) =='
      if ! flutter devices --machine 2>/dev/null | grep -q "\$DEVICE"; then
        echo 'The virtual device is not connected.'
        echo 'Open the Device panel (or its tab) once so the device is up, then run this again.'
        echo
      fi
      # Interactive on purpose: r hot-reloads, R hot-restarts, q quits. That is the whole reason
      # this is a terminal rather than a build task.
      exec flutter run -d "\$DEVICE"

  - label: Build APK
    command: |
      clear
      set -e
      if command -v javac >/dev/null 2>&1; then
        JAVA_HOME="\$(dirname "\$(dirname "\$(readlink -f "\$(command -v javac)")")")"
        export JAVA_HOME
        export PATH="\$JAVA_HOME/bin:\$PATH"
      fi
      ANDROID_HOME="\$HOME/android-sdk"
      [ -d "\$ANDROID_HOME" ] || ANDROID_HOME="\$(ls -d /home/*/android-sdk /root/android-sdk 2>/dev/null | head -1)"
      export ANDROID_HOME ANDROID_SDK_ROOT="\$ANDROID_HOME"
      cd "$JCODE_PROJECT_DIR"
      echo '== J Code: flutter build apk --debug =='
      flutter build apk --debug
      echo
      echo 'APK ready: build/app/outputs/flutter-apk/app-debug.apk'
EOF
