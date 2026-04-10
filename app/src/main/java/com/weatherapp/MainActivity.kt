package com.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weatherapp.components.AnimatedWeatherBackground
import com.weatherapp.components.GlassCard
import com.weatherapp.viewmodel.WeatherViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherApp()
                }
            }
        }
    }
}

@Composable
fun WeatherApp() {
    val viewModel: WeatherViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var currentCity by remember { mutableStateOf("London") }
    val focusManager = LocalFocusManager.current

    // Auto-update loop every 5 minutes
    LaunchedEffect(currentCity) {
        while (true) {
            viewModel.fetchWeather(currentCity)
            delay(300000L) 
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedWeatherBackground(
            weatherState = uiState.weatherState,
            modifier = Modifier.fillMaxSize()
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it 
                            viewModel.onSearchQueryChanged(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        placeholder = { Text("Search location...", color = Color.White.copy(alpha = 0.7f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    currentCity = searchQuery.trim()
                                    viewModel.clearSuggestions()
                                    focusManager.clearFocus()
                                    searchQuery = "" 
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    AnimatedVisibility(
                        visible = uiState.searchSuggestions.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF263238).copy(alpha = 0.95f))
                        ) {
                            items(uiState.searchSuggestions) { location ->
                                // Constructing proper query formatted string
                                val formattedName = buildString {
                                    append(location.name)
                                    if (location.state != null && location.state.isNotBlank()) append(", ${location.state}")
                                    if (location.country != null && location.country.isNotBlank()) append(", ${location.country}")
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            currentCity = formattedName
                                            viewModel.clearSuggestions()
                                            focusManager.clearFocus()
                                            searchQuery = ""
                                            viewModel.fetchWeather(formattedName)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = location.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    val subText = buildString {
                                        if (location.state != null) append(location.state)
                                        if (location.state != null && location.country != null) append(", ")
                                        if (location.country != null) append(location.country)
                                    }
                                    if (subText.isNotBlank()) {
                                        Text(
                                            text = subText,
                                            color = Color.LightGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiState
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
