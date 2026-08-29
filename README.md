# JCode — Flutter Dev Pack

Flutter development inside J Code, on the phone: **Dart** language support, a
**`flutter-app`** project template, and the everyday Flutter commands. `flutter run`
drives J Code's own virtual device, **hot reload included**.

Flutter on a phone is not the same shape as Flutter on a laptop, and this pack exists
for the differences.

## Flutter here is a git clone, not the tarball

Flutter builds arm64 engine artifacts, but publishes an **x86-64-only Linux archive**.
On this device the archive simply does not run. The `flutter` toolchain entry in
**Toolchains → Languages** installs the SDK by cloning the stable channel into
`/opt/flutter` and letting it bootstrap its own Dart SDK, which is the install that
works. That entry is required by this pack, so installing the pack offers it.

It is a large download — the shallow clone, the Dart SDK and the Android engine come to
well over a gigabyte — so it precaches the Android engine during the install rather than
leaving the first build to stall on a download with nothing but a Gradle task name on
screen.

## The template is load-bearing, not a convenience

**A project straight out of `flutter create` cannot be built on this device.** The
Android NDK ships no `linux-arm64` prebuilts — only `toolchains/llvm/prebuilt/linux-x86_64/`
— so AGP's `stripDebugSymbols` task has no binary to run and the build fails on it every
time, with an error that says nothing about architectures.

The fix is one block, and the `flutter-app` template writes it into every project it
scaffolds:

```kotlin
android {
    packaging {
        jniLibs {
            keepDebugSymbols.add("**/*.so")
        }
    }
}
```

The `.so` files ship unstripped and nothing else changes. For a project you **cloned** rather
than scaffolded, **Check Flutter can build here** (below) reports whether the block is present
and prints it if not.

### What it costs, and what does not fix it

A debug Flutter engine `.so` is ~400MB with its symbols kept, and a debug APK carries four ABIs:
measured, **a 1.4GB debug APK**. It builds, installs and hot reloads at that size — but it is a
real cost, and the two obvious cures were tried and do not work:

- `flutter build apk --debug --target-platform android-arm64` is **ignored for debug builds** —
  byte-for-byte the same APK.
- An `abiFilters` on the debug variant made it **larger** (1.8GB), not smaller.

The real fix is to give the NDK a stripper it can run. The distro already ships `llvm-strip-18`
and `aarch64-linux-gnu-strip`, and the Android SDK toolchain already substitutes an ARM-native
`aapt2` by exactly this pattern — so the same substitution belongs in the `android-sdk` toolchain
entry, where it would fix stripping for every Android project on the device rather than for
Flutter alone. Not done yet.

## Running on the virtual device

The virtual device belongs to the **Android Dev Pack**, which this pack requires: it binds
an adb daemon in J Code's own storage, and the distro's native aarch64 `adb` client
connects to it. Flutter sees that as an ordinary device:

```
flutter run -d localfilesystem:/run/jcode-vdevice-adb.sock
```

which is what the scaffolded project's **Run on the virtual device** task does. `r` hot
reloads, `R` hot restarts, `q` quits — it is an interactive session, which is why it is a
terminal task rather than a build task.

Open the Device panel (or its tab) once so the device is up before running it. A device
that is not up is not a device Flutter can find.

## What you get

- **Dart** — coloring, completions and snippet helpers, weighted towards writing widgets
  (`st`, `stf`, `build`, `setState`, `initState`, `ListView.builder`, …). Formatting is
  `dart format`, which ships with the SDK.
- **Dart Analysis Server** — the real language server, from `dart language-server`. It is
  part of the Flutter SDK, so there is nothing extra to install.
- **`flutter-app` template** — `flutter create` with an organisation and platform choice,
  the debug-symbol patch above, and a `.jcode/run.yaml` with **Run on the virtual device**
  and **Build APK**.
- **Build helpers** — offered on any project with a `pubspec.yaml`:
  - **Check Flutter can build here** — `flutter doctor -v`, plus the stripper check.
  - **Get packages** — `flutter pub get`.
  - **Analyze** — `flutter analyze`.
  - **Run tests** — `flutter test`.
  - **Build debug APK** — `flutter build apk --debug`.
  - **Clean** — `flutter clean`.

## Requirements

| | |
|---|---|
| J Code | 1.7.0 or newer |
| Extensions | Android Dev Pack (`jcode.pack.android`) — the virtual device and its adb |
| Toolchains | `flutter` (required), `android-sdk` (needed to build an APK) |

## Licence

MIT — see [LICENSE](LICENSE).
