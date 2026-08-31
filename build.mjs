// Production build for the Flutter Dev Pack.
//
// What ships is one archive under `lib/` — the pack's native half, the New Flutter Project gallery,
// loaded on demand into JCode's process by `NativeExtensionLoader`. `jext pack` runs this
// (npm run build) before packaging, so packing the extension is enough to produce it — by hand or
// in CI.
//
// **An archive, not a loose dex.** A bare `.dex` is classes and nothing else: no resource table for
// `addAssetPath` to attach and nowhere for assets to live. The gallery draws its previews rather
// than shipping them, so it owns neither today — and can own both without a packaging change.
//
// The APK is unsigned and never installed as an app. JCode verifies the *extension*, not this.
//
// `lib/` is gitignored and the build tree is in `.jextignore`: the archive is rebuilt per release
// rather than committed, and the module that builds it stays out of the package. Without this
// script CI packs neither, and the pack fails to load with "native payload is missing".
import { spawnSync } from 'node:child_process';
import { copyFileSync, mkdirSync } from 'node:fs';
import { resolve } from 'node:path';

const win = process.platform === 'win32';
// Absolute, because a bare `gradlew.bat` is not found in the working directory the way `./gradlew`
// is on a POSIX shell, and the two platforms disagree about which relative spelling works.
const gradlew = resolve(win ? 'gradlew.bat' : 'gradlew');

// Keyed by the `entry.native[].id` it backs, so a module renamed here and not in extension.yaml is
// a mismatch somebody can see rather than a load failure at runtime.
const MODULES = {
  newproject: 'native/newproject/build/outputs/apk/release/newproject-release-unsigned.apk',
};

const build = spawnSync(gradlew, ['assembleRelease'], { stdio: 'inherit', shell: win });
if (build.status !== 0) process.exit(build.status || 1);

mkdirSync('lib', { recursive: true });
for (const [id, apk] of Object.entries(MODULES)) {
  copyFileSync(apk, `lib/${id}.apk`);
  console.log(`✓ built native/${id} → lib/${id}.apk`);
}
