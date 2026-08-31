plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

/**
 * The New Flutter Project gallery: pick a kind of project by looking at it, configure it, scaffold
 * it with `flutter create`.
 */
android {
    namespace = "dev.jcode.ext.flutter.newproject"
    defaultConfig {
        minSdk = 26
        applicationId = "dev.jcode.ext.flutter.newproject"
    }
    sourceSets {
        getByName("main") {
            java.srcDirs("java")
            res.srcDirs("res")
            assets.srcDirs("assets")
        }
    }
}
