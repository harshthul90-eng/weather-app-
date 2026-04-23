package com.weatherapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherapp.state.HourlyForecast
import com.weatherapp.state.WeatherUiState

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    uiState: WeatherUiState,
    onForecastItemClick: (HourlyForecast?) -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .blur(16.dp)
        )
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            WeatherInfo(
                uiState = uiState,
                onForecastItemClick = onForecastItemClick
            )
        }
    }
}

@Composable
fun WeatherInfo(
    uiState: WeatherUiState,
    onForecastItemClick: (HourlyForecast?) -> Unit
) {
    val displayData = uiState.selectedForecast ?: HourlyForecast(
        time = "Now",
        temperature = uiState.temperature,
        description = uiState.description,
        mainCondition = "",
        humidity = uiState.humidity,
        wind = uiState.wind,
        weatherState = uiState.weatherState
    )

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // City Name
        Text(
            text = uiState.cityName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayData.description,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = displayData.temperature,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Hum: ${displayData.humidity}",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "Wind: ${displayData.wind}",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ForecastRow(
            modifier = Modifier.fillMaxWidth(),
            forecastItems = uiState.hourlyForecast,
            selectedItem = uiState.selectedForecast,
            onItemClick = onForecastItemClick
        )
    }
}

@Composable
fun ForecastRow(
    modifier: Modifier = Modifier,
    forecastItems: List<HourlyForecast>,
    selectedItem: HourlyForecast?,
    onItemClick: (HourlyForecast?) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ForecastItem(
                time = "Now",
                isSelected = selectedItem == null,
                onClick = { onItemClick(null) }
            )
        }
        items(forecastItems) { item ->
            ForecastItem(
                time = item.time,
                isSelected = selectedItem == item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
fun ForecastItem(
    modifier: Modifier = Modifier,
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .width(60.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier.background(Color.White.copy(alpha = 0.3f))
                } else {
                    Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
                }
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = time,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier
                .size(24.dp)
                .background(
                    if (isSelected) Color.White else Color.White.copy(alpha = 0.5f), 
                    RoundedCornerShape(12.dp)
                )
            )
        }
    }
}
