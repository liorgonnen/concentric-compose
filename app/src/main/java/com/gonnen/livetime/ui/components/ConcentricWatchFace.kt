package com.gonnen.livetime.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gonnen.livetime.ext.minutesInMillis
import com.gonnen.livetime.ext.secondsInMillis
import com.gonnen.livetime.ui.ext.animation.rememberAnimationTimeMillis
import com.gonnen.livetime.ui.ext.drawscope.drawText
import com.gonnen.livetime.ui.ext.drawscope.rememberNativeTextPaint
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

typealias Angle = Float

private const val Pi = Math.PI.toFloat()
private const val OneMinuteMillis = 60 * 1000L
private const val OneHourMillis = OneMinuteMillis * 60
private const val DegreesPerTick = 360f / 60f
private const val OuterRadiusFraction = 1f
private const val InnerRadiusFraction = 0.95f
private const val InnerRadiusTickFraction = 0.90f
private const val TextSizeFraction = 0.08f
private const val HoursTextSizeFraction = 0.22f
private const val CurrentMinutesTextSizeFraction = 0.08f
private const val TextRadiusFraction = InnerRadiusTickFraction - TextSizeFraction - 0.03f
private const val MinutesFraction = TextRadiusFraction - TextSizeFraction - 0.05f
private const val TickStrokeWidth = 5f
private const val TimeWindowStrokeWidth = 7f
private const val TimeWindowHeightFraction = 0.18f
private const val TimeWindowHorizontalOffsetFraction = 0.15f
private const val TimeWindowMinutesOffsetFraction = TimeWindowHorizontalOffsetFraction / 2f + ((MinutesFraction / 2f) * InnerRadiusTickFraction) / 2f
private const val BigTickStep = 5

private fun Angle.toOffset() = (this * Pi / 180f).let { angle -> Offset(cos(angle), sin(angle)) }
private fun Iterable<Int>.toPaddedStringArray() = map { it.toString().padStart(2, '0') }.toTypedArray()

private val TickText = (0 until 60 step BigTickStep).toPaddedStringArray()
private val TimeText = (0 until 60).toPaddedStringArray()

data class ConcentricWatchFaceColors(
    val tickColor: Color = Color(0xFF777777),
    val minutesTickDigitsColor: Color = Color(0xFFAAAAAA),
    val secondsTickDigitsColor: Color = Color(0xFFB6F0E2),
    val currentHoursColor: Color = Color.White,
    val currentMinutesColor: Color = currentHoursColor,
    val timeWindowColor: Color = secondsTickDigitsColor,
)

@Composable
fun ConcentricWatchFace(
    modifier: Modifier = Modifier,
    timeProvider: () -> LocalTime = { LocalTime.now() },
    colors: ConcentricWatchFaceColors = ConcentricWatchFaceColors()
) {
    var time by remember { mutableStateOf(timeProvider()) }

    LaunchedEffect(true) {
        while (true) {
            time = timeProvider()
            delay(1000)
        }
    }

    ConcentricWatchFaceDisplay(modifier, time, colors)
}

@Composable
fun ConcentricWatchFaceDisplay(
    modifier: Modifier = Modifier,
    time: LocalTime,
    colors: ConcentricWatchFaceColors = ConcentricWatchFaceColors()
) {
    val secondsTextPaint = rememberNativeTextPaint(colors.secondsTickDigitsColor)
    val minutesTextPaint = rememberNativeTextPaint(colors.minutesTickDigitsColor)
    val currentMinutesTextPaint = rememberNativeTextPaint(textColor = colors.currentMinutesColor)
    val hoursTextPaint = rememberNativeTextPaint(textColor = colors.currentHoursColor)
    val animationTimeMillis by rememberAnimationTimeMillis()

    val secondsAngle by remember { derivedStateOf { 360f * (time.secondsInMillis + animationTimeMillis) / OneMinuteMillis } }
    val minutesAngle by remember { derivedStateOf { 360f * (time.minutesInMillis + animationTimeMillis) / OneHourMillis } }

    Canvas(
        modifier
            .aspectRatio(1f)
            .clipToBounds()
            .onSizeChanged { size ->
                val width = size.width.toFloat()
                secondsTextPaint.textSize = width * TextSizeFraction
                currentMinutesTextPaint.textSize = width * CurrentMinutesTextSizeFraction
                minutesTextPaint.textSize = width * TextSizeFraction
                hoursTextPaint.textSize = width * HoursTextSizeFraction
            }
    ) {
        drawTicks(angle = secondsAngle, colors.tickColor)
        drawTickDigits(angle = secondsAngle, textPaint = secondsTextPaint)

        scale(scale = MinutesFraction) {
            drawTicks(minutesAngle, colors.tickColor)
        }

        drawTimeWindow(color = colors.timeWindowColor) {
            scale(scale = MinutesFraction) {
                drawTickDigits(minutesAngle, minutesTextPaint)
            }
        }

        drawCurrentMinutes(time.minute, currentMinutesTextPaint)
        drawCurrentHours(time.hour, hoursTextPaint)
    }
}

private fun DrawScope.drawTimeWindow(color: Color, drawContentClipped: DrawScope.() -> Unit) {
    val height = TimeWindowHeightFraction * size.height
    val halfHeight = height / 2f
    val center = size.center + Offset(x = TimeWindowHorizontalOffsetFraction * size.width, y = -halfHeight)

    drawRoundRect(
        color = color,
        topLeft = center,
        size = Size(size.width, height),
        style = Stroke(width = TimeWindowStrokeWidth),
        cornerRadius = CornerRadius(halfHeight))

    clipRect(
        left = center.x,
        top = center.y - TimeWindowStrokeWidth / 2f,
        right = size.width,
        bottom = center.y + height + TimeWindowStrokeWidth / 2f,
        clipOp = ClipOp.Difference) {
        drawContentClipped()
    }
}

private fun DrawScope.drawCurrentHours(hours: Int, textPaint: Paint) {
    drawText(TimeText[hours], size.center, textPaint)
}

private fun DrawScope.drawCurrentMinutes(minutes: Int, textPaint: Paint) {
    val offset = center + Offset(x = size.width * TimeWindowMinutesOffsetFraction, y = 0f)
    drawText(TimeText[minutes], offset, textPaint)
}

private fun DrawScope.drawTicks(angle: Angle, color: Color) {
    val outerRadius = size.minDimension / 2f * OuterRadiusFraction

    for (tick in 0 until 60) {
        val isBigTick = tick.mod(BigTickStep) == 0
        val innerRadius = (if (isBigTick) InnerRadiusTickFraction else InnerRadiusFraction) * outerRadius
        val offset = (angle - tick * DegreesPerTick).toOffset()

        drawLine(
            color = color,
            start = size.center + offset * innerRadius,
            end = size.center + offset * outerRadius,
            strokeWidth = TickStrokeWidth,
            cap = Round
        )
    }
}

private fun DrawScope.drawTickDigits(angle: Angle, textPaint: Paint) {
    val outerRadius = size.minDimension / 2f * OuterRadiusFraction

    for (tick in 0 until 60 step BigTickStep) {
        val offset = (angle - tick * DegreesPerTick).toOffset()

        val textOffset = center + offset * TextRadiusFraction * outerRadius
        drawText(text = TickText[tick / BigTickStep], textOffset, textPaint)
    }
}

@Preview
@Composable
fun Preview() {
    ConcentricWatchFace(
        modifier = Modifier
            .size(200.dp, 200.dp)
            .padding(16.dp),
        timeProvider = { LocalTime.of(9, 44)})
}