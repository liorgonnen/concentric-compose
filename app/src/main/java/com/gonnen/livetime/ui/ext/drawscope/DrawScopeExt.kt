package com.gonnen.livetime.ui.ext.drawscope

import android.content.Context
import android.graphics.Paint.Align.CENTER
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.gonnen.livetime.R

private fun createNativeTextPaint(context: Context, textColor: Color = White) = Paint().asFrameworkPaint().apply {
    color = textColor.toArgb()
    textAlign = CENTER
    isFakeBoldText = true
    typeface = context.resources.getFont(R.font.google_sand_regular)
}

@Composable
fun rememberNativeTextPaint(textColor: Color): android.graphics.Paint {
    val context = LocalContext.current
    return remember { createNativeTextPaint(context, textColor) }
}

val NativePaint.textHeight get() = textSize - fontMetrics.bottom

fun DrawScope.drawText(text: String, offset: Offset, textPaint: android.graphics.Paint) {
    drawContext.canvas.nativeCanvas.drawText(text, offset.x, offset.y + textPaint.textHeight / 2f, textPaint)
}