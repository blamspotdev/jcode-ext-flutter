package dev.jcode.ext.flutter.newproject

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.blamspot.jcode.ext.api.JCodeNativeExtension
import dev.blamspot.jcode.ext.api.NativeHost

/**
 * New Flutter Project, native.
 *
 * Reached from JCode's own New Project dialog: the `flutter-app` template names this view in its
 * `gallery:`, the dialog collects the project's name and then steps aside. One surface, one view —
 * everything this module does is the wizard.
 */
class NewProjectExtension : JCodeNativeExtension {

    @Composable
    override fun Content(host: NativeHost, params: Map<String, String>) {
        // `newFlutterProject:MyApp` — the dialog passes the name on the view, so the gallery does
        // not ask for it a second time.
        val view = params[JCodeNativeExtension.Params.VIEW].orEmpty()
        GalleryPage(host, view.substringAfter(':', ""), Modifier)
    }
}
