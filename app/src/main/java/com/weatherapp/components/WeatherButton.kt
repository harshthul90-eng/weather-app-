package com.weatherapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherapp.state.WeatherState

@Composable
fun WeatherButton(
    modifier: Modifier = Modifier,
    state: WeatherState,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        val abbreviation = when (state) {
            WeatherState.Sunny -> "Sun"
            WeatherState.Cloudy -> "Cld"
            WeatherState.Rain -> "Rain"
            WeatherState.Snow -> "Snw"
            WeatherState.Thunderstorm -> "Thd"
            WeatherState.Fog -> "Fog"
            WeatherState.Night -> "Ngt"
        }
        Text(
            text = abbreviation,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(2.dp)
        )
    }
}
