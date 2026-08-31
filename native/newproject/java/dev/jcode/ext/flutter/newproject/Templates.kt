package dev.jcode.ext.flutter.newproject

/**
 * What a Flutter template asks for before it can be scaffolded.
 *
 * `flutter create`'s own arguments, and only those: a name, an organisation and — for the kinds that
 * produce an app — which platforms to generate. Nothing here is invented, because every field is
 * handed straight to a command that will reject what it does not like, and a question whose answer
 * the tool ignores is a question not worth asking.
 */
internal data class Config(
    val name: String = "",
    val org: String = "com.example",
    val platforms: String = "android",
) {
    /** A directory name, and JCode's rule for one: it lower-cases what it registers. */
    val folder: String get() = name.trim().lowercase().replace(Regex("[^a-z0-9._-]+"), "-").trim('-')

    /**
     * The Dart package name.
     *
     * Not the directory name: `flutter create` refuses anything that is not a valid Dart identifier,
     * and dashes, capitals and a leading digit are all ordinary in a project name and all rejected
     * there. Folded here rather than passed through and left to fail at the end of a long scaffold.
     */
    val packageName: String
        get() {
            val folded = name.trim().lowercase().map { if (it.isLetterOrDigit()) it else '_' }.joinToString("")
            val named = folded.ifBlank { "flutter_app" }
            return if (named.first().isDigit()) "app_$named" else named
        }

    val isValid: Boolean get() = folder.isNotEmpty() && org.matches(ORG)

    companion object {
        /** Two or more dot-separated segments, each starting with a letter — what `--org` accepts. */
        val ORG = Regex("""[a-zA-Z][A-Za-z0-9_]*(\.[a-zA-Z][A-Za-z0-9_]*)+""")

        /**
         * Android alone by default.
         *
         * Linux desktop is a real target here — the distro is arm64 Linux and Flutter builds for it
         * — but it needs GTK and clang, which the Flutter toolchain entry does not install. Android
         * is the one the virtual device runs.
         */
        val PLATFORMS = listOf("android", "android,linux")
    }
}

/** The rail across the top of the gallery. What the project *is*, which is how Flutter divides them. */
internal enum class Category(val label: String) {
    Apps("Apps"),
    Libraries("Libraries"),
    AddToApp("Add-to-app"),
    ;
}

/**
 * A gallery entry.
 *
 * [template] is `flutter create --template=`, and [empty] is its `--empty` flag: between them they
 * are the whole of what distinguishes these, which is why this pack draws a gallery rather than
 * shipping five scaffold scripts. [art] names a drawing rather than a file — see [TemplatePreview].
 *
 * [buildsApk] says whether the result has an Android app in it. A package and a plugin do not, and
 * the two steps that make a Flutter app buildable *on this device* have nothing to work on there —
 * running them anyway would fail on a project that is perfectly correct.
 */
internal data class Template(
    val id: String,
    val category: Category,
    val name: String,
    val description: String,
    val art: Art,
    val template: String,
    val empty: Boolean = false,
    val buildsApk: Boolean = true,
)

/** The shape a preview draws. */
internal enum class Art { Counter, EmptyApp, Package, Plugin, Module }

internal object Templates {

    val all: List<Template> = listOf(
        Template(
            id = "app",
            category = Category.Apps,
            name = "Flutter App",
            description = "The counter app: one screen, a floating action button and a widget test — " +
                "what `flutter create` gives you, patched so it builds on this device.",
            art = Art.Counter,
            template = "app",
        ),
        Template(
            id = "empty-app",
            category = Category.Apps,
            name = "Empty App",
            description = "The same app with the counter demo taken out, for starting from a blank screen.",
            art = Art.EmptyApp,
            template = "app",
            empty = true,
        ),
        Template(
            id = "package",
            category = Category.Libraries,
            name = "Dart Package",
            description = "Shared Dart code with no platform half — a library for other projects to depend on.",
            art = Art.Package,
            template = "package",
            buildsApk = false,
        ),
        Template(
            id = "plugin",
            category = Category.Libraries,
            name = "Plugin",
            description = "A package with an Android implementation behind a Dart API, and an example app.",
            art = Art.Plugin,
            template = "plugin",
            buildsApk = false,
        ),
        Template(
            id = "module",
            category = Category.AddToApp,
            name = "Flutter Module",
            description = "Flutter to be embedded in an existing Android app rather than run on its own.",
            art = Art.Module,
            template = "module",
        ),
    )

    fun inCategory(category: Category): List<Template> = all.filter { it.category == category }

    val categories: List<Category> = Category.entries
}
