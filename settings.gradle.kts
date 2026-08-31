pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "jcode-ext-flutter"

// One module today: the New Flutter Project gallery. The Gradle root is the repository root rather
// than `native/`, so `native/` holds modules and nothing else -- no wrapper, no jars, no build state.
include(":newproject")
project(":newproject").projectDir = file("native/newproject")
