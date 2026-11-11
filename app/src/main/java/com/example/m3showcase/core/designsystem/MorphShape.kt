package com.example.m3showcase.core.designsystem

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

/** Draws a [Morph] between two MaterialShapes at [progress] (0f..1f, springs may overshoot). */
class MorphShape(
    private val morph: Morph,
    private val progress: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        // Fit the morph's own bounds into the composable size, so it works for any polygon space.
        val b = morph.calculateBounds()
        val matrix = Matrix().apply {
            scale(size.width / (b[2] - b[0]), size.height / (b[3] - b[1]))
            translate(-b[0], -b[1])
        }
        val path = morph.toPath(progress).asComposePath()
        path.transform(matrix)
        return Outline.Generic(path)
    }
}
