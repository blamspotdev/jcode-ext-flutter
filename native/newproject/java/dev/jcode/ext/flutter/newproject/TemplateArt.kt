package dev.jcode.ext.flutter.newproject

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.blamspot.jcode.design.Space

/**
 * The preview on a gallery card.
 *
 * Drawn rather than shipped, for the same reasons the Android pack draws its own: a preview
 * generated from the same enum the recipe hangs off cannot advertise something the recipe does not
 * produce, it costs nothing in the archive, and it follows the theme — a PNG of a light-themed phone
 * in a dark drawer is exactly the sort of thing that makes an extension look bolted on.
 *
 * What each one shows is what `flutter create` actually leaves behind: a counter screen with a
 * floating action button, a blank one, a package with no platform half, a plugin bridging to one,
 * a module embedded in somebody else's app.
 */
@Composable
internal fun TemplatePreview(art: Art, modifier: Modifier = Modifier) {
    val frame = MaterialTheme.colorScheme.outlineVariant
    val fill = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val accent = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

    Box(modifier = modifier.fillMaxWidth().height(112.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxWidth().height(112.dp).padding(Space.sm)) {
            when (art) {
                Art.Package -> drawPackage(frame, fill, accent, muted)
                Art.Plugin -> drawPlugin(frame, fill, accent, muted)
                Art.Module -> drawModule(frame, fill, accent, muted)
                else -> drawPhone(art, frame, fill, accent, muted)
            }
        }
    }
}

private fun DrawScope.phoneRect(): Pair<Offset, Size> {
    val h = size.height
    val w = h * 0.52f
    return Offset((size.width - w) / 2f, 0f) to Size(w, h)
}

private fun DrawScope.phoneFrame(at: Offset, box: Size, frame: Color, fill: Color): CornerRadius {
    val r = CornerRadius(box.width * 0.12f)
    drawRoundRect(color = fill, topLeft = at, size = box, cornerRadius = r)
    drawRoundRect(color = frame, topLeft = at, size = box, cornerRadius = r, style = Stroke(width = 2f))
    return r
}

private fun DrawScope.drawPhone(art: Art, frame: Color, fill: Color, accent: Color, muted: Color) {
    val (at, box) = phoneRect()
    phoneFrame(at, box, frame, fill)

    val barH = box.height * 0.12f
    drawRect(color = accent.copy(alpha = 0.75f), topLeft = at.copy(y = at.y + 2f), size = Size(box.width, barH))

    if (art == Art.Counter) {
        // The counter: a number in the middle and the button that increments it.
        val lineW = box.width * 0.30f
        drawRoundRect(
            color = muted,
            topLeft = Offset(at.x + (box.width - lineW) / 2f, at.y + box.height * 0.44f),
            size = Size(lineW, box.height * 0.045f),
            cornerRadius = CornerRadius(4f),
        )
        drawCircle(
            color = accent.copy(alpha = 0.8f),
            radius = box.width * 0.10f,
            center = Offset(at.x + box.width * 0.78f, at.y + box.height * 0.84f),
        )
    }
}

private fun DrawScope.drawPackage(frame: Color, fill: Color, accent: Color, muted: Color) {
    // No phone at all: a package has no platform half, and drawing one would say it does.
    val w = size.width * 0.52f
    val h = size.height * 0.62f
    val at = Offset((size.width - w) / 2f, (size.height - h) / 2f)
    val r = CornerRadius(6f)
    drawRoundRect(color = fill, topLeft = at, size = Size(w, h), cornerRadius = r)
    drawRoundRect(color = frame, topLeft = at, size = Size(w, h), cornerRadius = r, style = Stroke(width = 2f))
    // A lid seam, and the lines of a source file under it.
    drawLine(
        color = accent.copy(alpha = 0.7f),
        start = Offset(at.x, at.y + h * 0.26f),
        end = Offset(at.x + w, at.y + h * 0.26f),
        strokeWidth = 3f,
    )
    repeat(3) { i ->
        drawRoundRect(
            color = muted,
            topLeft = Offset(at.x + w * 0.16f, at.y + h * (0.44f + i * 0.16f)),
            size = Size(w * (0.56f - i * 0.12f), h * 0.06f),
            cornerRadius = CornerRadius(3f),
        )
    }
}

private fun DrawScope.drawPlugin(frame: Color, fill: Color, accent: Color, muted: Color) {
    // Two halves and the bridge between them: Dart on one side, the platform on the other.
    val w = size.width * 0.30f
    val h = size.height * 0.46f
    val y = (size.height - h) / 2f
    val gap = size.width * 0.10f
    val left = Offset((size.width - (w * 2 + gap)) / 2f, y)
    val right = Offset(left.x + w + gap, y)
    val r = CornerRadius(5f)

    drawRoundRect(color = fill, topLeft = left, size = Size(w, h), cornerRadius = r)
    drawRoundRect(color = frame, topLeft = left, size = Size(w, h), cornerRadius = r, style = Stroke(width = 2f))
    drawRoundRect(color = accent.copy(alpha = 0.25f), topLeft = right, size = Size(w, h), cornerRadius = r)
    drawRoundRect(color = frame, topLeft = right, size = Size(w, h), cornerRadius = r, style = Stroke(width = 2f))

    drawLine(
        color = muted,
        start = Offset(left.x + w, y + h / 2f),
        end = Offset(right.x, y + h / 2f),
        strokeWidth = 4f,
    )
}

private fun DrawScope.drawModule(frame: Color, fill: Color, accent: Color, muted: Color) {
    // A phone that is somebody else's, with Flutter occupying part of it.
    val (at, box) = phoneRect()
    phoneFrame(at, box, frame, fill)
    val barH = box.height * 0.12f
    drawRect(color = muted.copy(alpha = 0.5f), topLeft = at.copy(y = at.y + 2f), size = Size(box.width, barH))
    drawRoundRect(
        color = accent.copy(alpha = 0.45f),
        topLeft = Offset(at.x + box.width * 0.10f, at.y + barH + box.height * 0.12f),
        size = Size(box.width * 0.80f, box.height * 0.46f),
        cornerRadius = CornerRadius(4f),
    )
}
