package dev.jcode.ext.flutter.newproject

import dev.blamspot.jcode.ext.api.NativeHost

/**
 * Turning a chosen template into a project on disk.
 *
 * The recipes are the pack's own shell scripts, run exactly the way JCode runs them when the same
 * template is picked from its New Project dialog — same environment contract, same scripts, one set
 * of scaffolding. A second implementation here would be a second thing to keep correct, and the two
 * would drift the first time one of them was fixed.
 */
internal object Scaffold {

    /** Where a new project goes. The workspace root, as the runtime sees it. */
    const val WORKSPACE = "/workspace"

    /** POSIX single-quoting, so a project name can never be read as shell. */
    private fun sh(v: String): String = "'" + v.replace("'", "'\\''") + "'"

    /**
     * What a command *printed*, and nothing else.
     *
     * stdout alone, deliberately. The runtime writes its own warnings to stderr — proot complains
     * there about any binding it cannot resolve, which happens for reasons that have nothing to do
     * with the command being run — and a value with a warning stuck on the end of it is no longer a
     * path. Folding the two together is for logs; this is for answers.
     */
    private suspend fun out(host: NativeHost, command: String, timeoutMs: Long = 30_000): String =
        host.exec(command, timeoutMs = timeoutMs).stdout.trimEnd()

    /**
     * This pack's own directory, as a path inside the runtime.
     *
     * Found rather than assumed: JCode binds the extension directory into the runtime at the same
     * absolute path it has on the host, and that path carries the application id — which differs
     * between a release install, a debug one and the fresh-install variant.
     */
    suspend fun packDir(host: NativeHost): String? = out(
        host,
        "ls -d /data/data/*/files/extensions/jcode.pack.flutter 2>/dev/null | head -1",
        10_000,
    ).lineSequence().map { it.trim() }.firstOrNull { it.startsWith("/") }

    suspend fun exists(host: NativeHost, path: String): Boolean =
        out(host, "test -e ${sh(path)} && echo yes || echo no", 8_000).contains("yes")

    /** What a scaffold run reports back: the finished project, or why there isn't one. */
    sealed interface Result {
        data class Created(val projectDir: String) : Result
        data class Failed(val message: String, val log: String = "") : Result
    }

    /** One script in a recipe. A [required] one missing is a failure, not something to skip past. */
    private data class Step(val label: String, val path: String, val required: Boolean = false)

    /**
     * Runs [template]'s recipe into a new directory under the workspace.
     *
     * The inputs reach the scripts as `JCODE_*` environment variables, which is the contract the
     * pack's scripts were already written against — `JCODE_PROJECT_DIR`, `JCODE_PROJECT_NAME` and
     * one `JCODE_INPUT_<ID>` per configured value.
     *
     * [onStep] is called with each step's label as it starts, so the page can say what is happening
     * during a scaffold that takes a while.
     */
    suspend fun run(
        host: NativeHost,
        template: Template,
        config: Config,
        onStep: (String) -> Unit,
    ): Result {
        val pack = packDir(host)
            ?: return Result.Failed("Could not find the Flutter Dev Pack's files in the runtime.")

        val projectDir = "$WORKSPACE/${config.folder}"
        if (exists(host, projectDir)) {
            return Result.Failed("A folder named \"${config.folder}\" already exists in the workspace.")
        }

        val env = mapOf(
            "JCODE_PROJECT_DIR" to projectDir,
            "JCODE_PROJECT_NAME" to config.folder,
            "JCODE_INPUT_ORG" to config.org,
            "JCODE_INPUT_PLATFORMS" to config.platforms,
            // `flutter create`'s own two switches, which is the whole of what separates these five
            // entries from each other.
            "JCODE_INPUT_TEMPLATE" to template.template,
            "JCODE_INPUT_EMPTY" to if (template.empty) "1" else "",
        )

        onStep("Creating the project folder")
        host.exec("mkdir -p ${sh(projectDir)}", timeoutMs = 15_000)

        val tpl = "$pack/templates/flutter-app"
        val steps = buildList {
            add(Step("Creating the Flutter project", "$tpl/create-flutter-project.sh", required = true))
            // A package and a plugin have no Android app in them, and the two steps that make one
            // buildable on this device would have nothing to work on. Running them anyway fails a
            // project that is perfectly correct.
            if (template.buildsApk) {
                add(Step("Making the project buildable on this device", "$tpl/keep-native-debug-symbols.sh"))
                add(Step("Configuring Build & Run", "$tpl/configure-build-run.sh"))
            }
        }

        for ((label, path, required) in steps) {
            if (!exists(host, path)) {
                if (!required) continue
                host.exec("rm -rf ${sh(projectDir)}", timeoutMs = 30_000)
                return Result.Failed("The pack's scaffold for \"${template.name}\" is missing: $path")
            }
            onStep(label)
            val r = host.exec(
                "sh ${sh(path)}",
                workdir = projectDir,
                timeoutMs = 15 * 60_000,
                env = env,
            )
            if (r.exitCode != 0 || r.error != null) {
                val log = (r.stdout + r.stderr).trimEnd()
                // The folder is removed on failure: a half-scaffolded project is worse than none,
                // because the next attempt with the same name is refused by the check above.
                host.exec("rm -rf ${sh(projectDir)}", timeoutMs = 30_000)
                return Result.Failed(
                    r.error ?: log.lineSequence().filter { it.isNotBlank() }.lastOrNull() ?: "$label failed.",
                    log,
                )
            }
        }
        return Result.Created(projectDir)
    }
}
