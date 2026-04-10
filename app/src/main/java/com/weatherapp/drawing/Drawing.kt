package com.weatherapp.drawing

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.weatherapp.state.WeatherState
import kotlin.random.Random

fun createCurvedPath(
    startX: Float, startY: Float,
    endX: Float, endY: Float,
    radiusX: Float, radiusY: Float
): Path {
    return Path().apply {
        moveTo(startX, startY)
        cubicTo(
            (startX + radiusX), (startY + radiusY),
            (endX + radiusX), (endY + radiusY),
            endX, endY
        )
    }
}

fun DrawScope.drawRain(size: Size, weatherState: WeatherState) {
    if (weatherState != WeatherState.Rain) return

    val dropColor = Color(200, 220, 255).copy(alpha = 0.5f)
    repeat(80) {
        val startX = Random.nextFloat() * size.width
        val startY = Random.nextFloat() * size.height
        val endY = startY + Random.nextFloat() * 60f + 40f
        val thickness = Random.nextFloat() * 3f + 1f

        drawLine(
            color = dropColor,
            start = androidx.compose.ui.geometry.Offset(startX, startY),
            end = androidx.compose.ui.geometry.Offset(startX, endY),
            strokeWidth = thickness,
            cap = StrokeCap.Round
        )
    }
}

fun DrawScope.drawSnow(size: Size, weatherState: WeatherState) {
    if (weatherState != WeatherState.Snow) return

    val snowColor = Color.White.copy(alpha = 0.8f)
    repeat(100) {
        val x = Random.nextFloat() * size.width
        val y = Random.nextFloat() * size.height
        val radius = Random.nextFloat() * 5f + 2f

        drawCircle(
            color = snowColor,
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
    }
}

fun DrawScope.drawLightning(size: Size, weatherState: WeatherState) {
    if (weatherState != WeatherState.Thunderstorm) return

    if (Random.nextFloat() > 0.95f) { // Random chance to show lightning
        val startX = Random.nextFloat() * size.width
        val endX = startX + (Random.nextFloat() - 0.5f) * 200f
        
        val lightningPath = Path().apply {
            moveTo(startX, 0f)
            lineTo(startX + (Random.nextFloat() - 0.5f) * 50f, size.height * 0.3f)
            lineTo(startX + (Random.nextFloat() - 0.5f) * 50f, size.height * 0.6f)
            lineTo(endX, size.height)
        }
        
        drawPath(
            path = lightningPath,
            color = Color.White.copy(alpha = 0.9f),
            style = Stroke(
                width = Random.nextFloat() * 8f + 2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
