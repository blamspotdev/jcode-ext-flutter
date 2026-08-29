#!/bin/sh
# Make this project buildable here
set -e

# The one thing that makes a Flutter project buildable here.
#
# The Android NDK ships no linux-arm64 prebuilts — only `toolchains/llvm/prebuilt/linux-x86_64/` —
# so AGP's `stripDebugSymbols` task has no binary to run at all on this device and the build fails
# on it every time. Keeping the symbols skips the task: the bundled `.so` files are larger and
# nothing else changes, which is the right trade against a stripper that cannot run under any
# circumstances.
#
# Measured: without this, `flutter build apk --debug` fails on stripDebugSymbols; with it, the same
# project builds and `flutter run` reaches hot reload.
#
# This is why the template exists rather than a note in a README — a project straight out of
# `flutter create` cannot be built here, and finding out why costs an afternoon.

APP="$JCODE_PROJECT_DIR/android/app"

if [ -f "$APP/build.gradle.kts" ]; then
  if grep -q keepDebugSymbols "$APP/build.gradle.kts"; then
    echo "android/app/build.gradle.kts already keeps its debug symbols."
  else
    cat >> "$APP/build.gradle.kts" <<'EOF'

// This device's NDK has no linux-arm64 prebuilts, so AGP's stripDebugSymbols task cannot run here
// at all. Keeping the symbols skips it, and the bundled .so files ship unstripped instead.
//
// It is expensive: a debug Flutter engine .so is ~400MB once its symbols are kept, and a debug APK
// carries four ABIs — measured, 1.4GB. Two obvious cures were tried and neither works.
// `flutter build apk --target-platform android-arm64` is ignored for debug builds (byte-for-byte
// the same APK), and an `abiFilters` on the debug variant made it *larger* (1.8GB). The real fix is
// to give the NDK a stripper it can run, which belongs in the Android SDK toolchain rather than in
// one project's build file.
android {
    packaging {
        jniLibs {
            keepDebugSymbols.add("**/*.so")
        }
    }
}
EOF
    echo "Patched android/app/build.gradle.kts."
  fi
elif [ -f "$APP/build.gradle" ]; then
  if grep -q keepDebugSymbols "$APP/build.gradle"; then
    echo "android/app/build.gradle already keeps its debug symbols."
  else
    cat >> "$APP/build.gradle" <<'EOF'

// This device's NDK has no linux-arm64 prebuilts, so AGP's stripDebugSymbols task cannot run here
// at all. Keeping the symbols skips it; the bundled .so files ship unstripped instead. See the .kts
// branch of the script that wrote this for what that costs and what does not fix it.
android {
    packaging {
        jniLibs {
            keepDebugSymbols += '**/*.so'
        }
    }
}
EOF
    echo "Patched android/app/build.gradle."
  fi
else
  # Said rather than passed over: a project with no Android module is a fine thing to have scaffolded
  # (Linux desktop only), and a silent skip here would leave the next person wondering whether the
  # patch had been applied.
  echo "No android/app build script — nothing to patch (this project has no Android module)."
fi
