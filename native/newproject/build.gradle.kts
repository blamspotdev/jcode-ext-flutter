plugins {
    id("com.android.application")
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
            kotlin.directories += "java"
            res.directories += "res"
            assets.directories += "assets"
        }
    }
}
