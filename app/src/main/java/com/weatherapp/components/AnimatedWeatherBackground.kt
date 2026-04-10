package com.weatherapp.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.weatherapp.drawing.drawLightning
import com.weatherapp.drawing.drawRain
import com.weatherapp.drawing.drawSnow
import com.weatherapp.state.WeatherState

@Composable
fun AnimatedWeatherBackground(
    weatherState: WeatherState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(getBackgroundBrush(weatherState))
    ) {
        Crossfade(
            targetState = weatherState,
            animationSpec = tween(durationMillis = 1000),
            label = "Weather Animation Transition"
        ) { state ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    WeatherState.Sunny -> SunAnimation()
                    WeatherState.Cloudy -> CloudAnimation()
                    WeatherState.Rain -> CanvasAnimation { drawRain(size, state) }
                    WeatherState.Snow -> CanvasAnimation { drawSnow(size, state) }
                    WeatherState.Thunderstorm -> ThunderstormAnimation(state)
                    WeatherState.Fog -> FogAnimation()
                    WeatherState.Night -> NightAnimation()
                }
            }
        }
    }
}

@Composable
fun SunAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Yellow.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
fun CloudAnimation() {
    // Add multiple subtle cloud shapes or gradient blobs
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxSize(0.6f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
fun CanvasAnimation(onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    Canvas(modifier = Modifier.fillMaxSize(), onDraw = onDraw)
}

@Composable
fun ThunderstormAnimation(state: WeatherState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRain(size, WeatherState.Rain)
        drawLightning(size, state)
    }
}

@Composable
fun FogAnimation() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Gray.copy(alpha = 0.6f), Color.LightGray.copy(alpha = 0.8f))
                )
            )
    )
}

@Composable
fun NightAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.3f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
        )
    }
}

fun getBackgroundBrush(weatherState: WeatherState): Brush {
    return when (weatherState) {
        WeatherState.Sunny -> Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2)))
        WeatherState.Cloudy -> Brush.verticalGradient(listOf(Color(0xFF37474F), Color(0xFF546E7A)))
        WeatherState.Rain -> Brush.verticalGradient(listOf(Color(0xFF1C313A), Color(0xFF455A64)))
        WeatherState.Snow -> Brush.verticalGradient(listOf(Color(0xFF455A64), Color(0xFF78909C)))
        WeatherState.Thunderstorm -> Brush.verticalGradient(listOf(Color(0xFF101416), Color(0xFF263238)))
        WeatherState.Fog -> Brush.verticalGradient(listOf(Color(0xFF37474F), Color(0xFF607D8B)))
        WeatherState.Night -> Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF1A237E)))
    }
}
