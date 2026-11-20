buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // AGP 9 compiles Kotlin itself, and depends on an older Kotlin Gradle plugin than this one. A
        // newer one goes on the build classpath, which is how AGP documents it: nothing applies
        // `org.jetbrains.kotlin.android` any more, because AGP 9 refuses to have it applied.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    id("com.android.application") version "9.5.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}

/**
 * The Flutter Dev Pack's native half: one archive, loaded into JCode's own process on demand.
 *
 * `build-libs/` holds JCode's jars -- named apart from `lib/`, which holds the payloads that ship,
 * because one letter between "the jars we compile against" and "the archives we publish" is a
 * mistake waiting to happen. They are copies of the Android Dev Pack's, which is where the shape of
 * this file comes from too; the two packs are separate repositories and the gallery each one draws
 * is its own, so the alternative to a second copy is a shared library neither repository can host.
 *
 * **The dependency rules are the ABI.** Anything JCode already ships is `compileOnly`: the module
 * must resolve those classes from JCode at runtime, because the composition it returns is spliced
 * into JCode's own and two Compose runtimes in one process do not interoperate. Anything JCode does
 * NOT ship may be bundled -- and must be, since nothing else will provide it.
 *
 * The module keeps its Kotlin under `java/` and declares that directory on the `kotlin` source set,
 * not the `java` one: a Kotlin directory on `java` is one AGP 9's built-in Kotlin does not support.
 *
 * `targetSdk` is deliberately absent: this archive is never installed as an app, so the `targetSdk`
 * that governs the process is **JCode's**. Setting one here would look like a guarantee it cannot
 * make.
 */
subprojects {
    // Everything below hangs off the plugin being applied: a `dependencies` block evaluated before
    // that has no `compileOnly` configuration to add to, which is a "Configuration with name
    // 'compileOnly' not found" at configuration time rather than anything about this pack.
    plugins.withId("com.android.application") {
        extensions.configure<com.android.build.api.dsl.ApplicationExtension>("android") {
            compileSdk = 36

            defaultConfig {
                versionCode = 1
                versionName = "0.1.0"
            }

            buildTypes {
                release {
                    // JCode does not minify either, and an obfuscated entry class cannot be found by name.
                    isMinifyEnabled = false
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }

            buildFeatures.compose = true
        }

        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension>("kotlin") {
            jvmToolchain(21)
            compilerOptions {
                // The standard library this archive runs against is JCode's, not this build's: it
                // loads into JCode's process, which resolves `kotlin.*` from its own copy first. So the
                // compiler may only call what JCode's Kotlin has -- the rule the compileOnly pins below
                // follow, applied to the standard library. Raise it when JCode's Kotlin moves.
                apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2
            }
        }

        // Pinned to what JCode actually RESOLVES, not to the BOM it declares. Those differ, and a
        // pack that trusted the BOM alone compiled against an older API than the one it runs on --
        // which surfaces as a NoSuchMethodError at the first draw, not at build time.
        //
        // Re-check these against JCode's `:app:dependencies` whenever its own versions move.
        dependencies {
            add("compileOnly", rootProject.files("build-libs/jcode-ext-api-abi3.jar"))
            add("compileOnly", rootProject.files("build-libs/jcode-core-design.jar"))

            add("compileOnly", "androidx.compose.ui:ui:1.9.0")
            add("compileOnly", "androidx.compose.foundation:foundation:1.9.0")
            add("compileOnly", "androidx.compose.runtime:runtime:1.9.0")
            add("compileOnly", "androidx.compose.material3:material3:1.3.1")
            add("compileOnly", "androidx.core:core-ktx:1.15.0")
            add("compileOnly", "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
        }
    }
}
