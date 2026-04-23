package com.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// --- 1. DATA MODELS ---
data class WeatherResponse(
    @SerializedName("name") val name: String,
    @SerializedName("main") val main: MainData,
    @SerializedName("weather") val weather: List<WeatherData>,
    @SerializedName("wind") val wind: WindData,
    @SerializedName("dt") val dt: Long,
    @SerializedName("sys") val sys: SysData?
) {
    data class MainData(@SerializedName("temp") val temp: Double, @SerializedName("humidity") val humidity: Int)
    data class WeatherData(@SerializedName("main") val main: String, @SerializedName("description") val description: String)
    data class WindData(@SerializedName("speed") val speed: Double)
    data class SysData(@SerializedName("sunrise") val sunrise: Long, @SerializedName("sunset") val sunset: Long)
}

data class ForecastResponse(@SerializedName("list") val list: List<ForecastItem>) {
    data class ForecastItem(
        @SerializedName("dt_txt") val dtTxt: String,
        @SerializedName("main") val main: WeatherResponse.MainData,
        @SerializedName("weather") val weather: List<WeatherResponse.WeatherData>,
        @SerializedName("wind") val wind: WeatherResponse.WindData
    )
}

data class LocationResult(
    @SerializedName("name") val name: String,
    @SerializedName("state") val state: String?,
    @SerializedName("country") val country: String?
)

sealed class WeatherState {
    data object Sunny : WeatherState(); data object Cloudy : WeatherState(); data object Rain : WeatherState()
    data object Snow : WeatherState(); data object Thunderstorm : WeatherState(); data object Fog : WeatherState()
    data object Night : WeatherState()
}

data class HourlyForecast(
    val time: String, val temperature: String, val description: String,
    val mainCondition: String, val humidity: String, val wind: String, val weatherState: WeatherState
)

data class WeatherUiState(
    val weatherState: WeatherState = WeatherState.Sunny, val temperature: String = "--°C",
    val humidity: String = "--%", val wind: String = "-- km/h", val cityName: String = "Loading...",
    val description: String = "Loading...", val isLoading: Boolean = true, val errorMessage: String? = null,
    val searchSuggestions: List<LocationResult> = emptyList(), val hourlyForecast: List<HourlyForecast> = emptyList(),
    val selectedForecast: HourlyForecast? = null
)

// --- 2. API SERVICE ---
interface WeatherApiService {
    @GET("weather")
    fun getCurrentWeather(@Query("q") city: String, @Query("appid") key: String, @Query("units") units: String): Call<WeatherResponse>

    @GET("forecast")
    fun getForecast(@Query("q") city: String, @Query("appid") key: String, @Query("units") units: String): Call<ForecastResponse>

    @GET("http://api.openweathermap.org/geo/1.0/direct")
    fun getLocations(@Query("q") query: String, @Query("limit") limit: Int, @Query("appid") key: String): Call<List<LocationResult>>
}

// --- 3. VIEWMODEL ---
class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()
    
    private val API_KEY = "8f5cb364ff723780b0d683c35aee4c1f"
    private val api = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(WeatherApiService::class.java)

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) { _uiState.update { it.copy(searchSuggestions = emptyList()) }; return }
        searchJob = viewModelScope.launch {
            delay(500)
            api.getLocations(query, 5, API_KEY).enqueue(object : Callback<List<LocationResult>> {
                override fun onResponse(call: Call<List<LocationResult>>, response: Response<List<LocationResult>>) {
                    if (response.isSuccessful) _uiState.update { it.copy(searchSuggestions = response.body() ?: emptyList()) }
                }
                override fun onFailure(call: Call<List<LocationResult>>, t: Throwable) {}
            })
        }
    }

    fun fetchWeather(city: String) {
        _uiState.update { it.copy(isLoading = true, cityName = city, selectedForecast = null) }
        api.getCurrentWeather(city, API_KEY, "metric").enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                response.body()?.let { data ->
                    val isNight = data.dt < (data.sys?.sunrise ?: 0) || data.dt > (data.sys?.sunset ?: Long.MAX_VALUE)
                    _uiState.update { it.copy(
                        weatherState = mapCondition(data.weather.firstOrNull()?.main ?: "Clear", isNight),
                        temperature = "${Math.round(data.main.temp)}°C",
                        humidity = "${data.main.humidity}%",
                        wind = "${Math.round(data.wind.speed * 3.6)} km/h",
                        cityName = data.name,
                        description = data.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                        isLoading = false
                    )}
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) { _uiState.update { it.copy(isLoading = false, errorMessage = t.message) } }
        })

        api.getForecast(city, API_KEY, "metric").enqueue(object : Callback<ForecastResponse> {
            override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                response.body()?.list?.take(8)?.let { list ->
                    val forecasts = list.map { item ->
                        val hour = try { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.dtTxt)?.let { d ->
                            Calendar.getInstance().apply { time = d }.get(Calendar.HOUR_OF_DAY)
                        } ?: 12 } catch (e: Exception) { 12 }
                        HourlyForecast(
                            time = formatTime(item.dtTxt),
                            temperature = "${Math.round(item.main.temp)}°C",
                            description = item.weather.firstOrNull()?.description ?: "",
                            mainCondition = item.weather.firstOrNull()?.main ?: "Clear",
                            humidity = "${item.main.humidity}%",
                            wind = "${Math.round(item.wind.speed * 3.6)} km/h",
                            weatherState = mapCondition(item.weather.firstOrNull()?.main ?: "Clear", hour < 6 || hour > 19)
                        )
                    }
                    _uiState.update { it.copy(hourlyForecast = forecasts) }
                }
            }
            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {}
        })
    }

    private fun formatTime(t: String) = try {
        SimpleDateFormat("h a", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(t)!!)
    } catch (e: Exception) { t }

    private fun mapCondition(c: String, night: Boolean) = when (c.lowercase()) {
        "clear" -> if (night) WeatherState.Night else WeatherState.Sunny
        "clouds" -> WeatherState.Cloudy; "rain", "drizzle" -> WeatherState.Rain
        "snow" -> WeatherState.Snow; "thunderstorm" -> WeatherState.Thunderstorm
        "mist", "fog" -> WeatherState.Fog; else -> if (night) WeatherState.Night else WeatherState.Sunny
    }

    fun selectForecast(item: HourlyForecast?) { _uiState.update { it.copy(selectedForecast = item) } }
    fun clearSuggestions() { _uiState.update { it.copy(searchSuggestions = emptyList()) } }
}

// --- 4. UI COMPONENTS ---
@Composable
fun WeatherApp() {
    val viewModel: WeatherViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("London") }
    val focus = LocalFocusManager.current

    LaunchedEffect(city) { while(true) { viewModel.fetchWeather(city); delay(300000) } }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(uiState.weatherState)
        Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp, start = 16.dp, end = 16.dp)) {
            OutlinedTextField(
                value = query, onValueChange = { query = it; viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth().background(Color.White.copy(0.2f), RoundedCornerShape(12.dp)),
                placeholder = { Text("Search location...", color = Color.White.copy(0.7f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
                singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { if(query.isNotBlank()) { city = query; viewModel.clearSuggestions(); focus.clearFocus(); query = "" } }),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            )
            // Suggestions List
            if (uiState.searchSuggestions.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(0.8f))) {
                    items(uiState.searchSuggestions) { loc ->
                        Text("${loc.name}, ${loc.country}", color = Color.White, modifier = Modifier.fillMaxWidth().clickable { city = loc.name; viewModel.clearSuggestions(); focus.clearFocus(); query = "" }.padding(16.dp))
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            GlassCard(uiState) { viewModel.selectForecast(it) }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun GlassCard(state: WeatherUiState, onClick: (HourlyForecast?) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.verticalGradient(listOf(Color.White.copy(0.15f), Color.White.copy(0.05f)))).border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(24.dp)).blur(16.dp)) {
        if (state.isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center).padding(48.dp), color = Color.White)
        else Column(Modifier.padding(24.dp)) {
            val display = state.selectedForecast ?: HourlyForecast("Now", state.temperature, state.description, "", state.humidity, state.wind, state.weatherState)
            Text(state.cityName, fontSize = 18.sp, color = Color.White.copy(0.9f))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(display.description, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(display.temperature, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Text("Humidity: ${display.humidity} | Wind: ${display.wind}", color = Color.White.copy(0.8f))
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { ForecastItem("Now", state.selectedForecast == null) { onClick(null) } }
                items(state.hourlyForecast) { item -> ForecastItem(item.time, state.selectedForecast == item) { onClick(item) } }
            }
        }
    }
}

@Composable
fun ForecastItem(time: String, sel: Boolean, onClick: () -> Unit) {
    Box(Modifier.size(60.dp, 80.dp).clip(RoundedCornerShape(16.dp)).background(if(sel) Color.White.copy(0.3f) else Color.White.copy(0.1f)).border(if(sel) 2.dp else 1.dp, Color.White, RoundedCornerShape(16.dp)).clickable { onClick() }, Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(time, fontSize = 12.sp, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.size(20.dp).background(Color.White, CircleShape))
        }
    }
}

@Composable
fun AnimatedBackground(state: WeatherState) {
    val brush = when(state) {
        WeatherState.Sunny -> Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2)))
        WeatherState.Night -> Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF1A237E)))
        else -> Brush.verticalGradient(listOf(Color(0xFF37474F), Color(0xFF546E7A)))
    }
    Box(Modifier.fillMaxSize().background(brush)) {
        Canvas(Modifier.fillMaxSize()) {
            when(state) {
                WeatherState.Rain -> drawRaindrops(size)
                WeatherState.Snow -> drawSnowflakes(size)
                WeatherState.Thunderstorm -> { drawRaindrops(size); if(Random.nextFloat() > 0.9f) drawRect(Color.White.copy(0.3f), size = size) }
                else -> {}
            }
        }
    }
}

fun DrawScope.drawRaindrops(size: Size) {
    repeat(50) { drawLine(Color.White.copy(0.4f), Offset(Random.nextFloat()*size.width, Random.nextFloat()*size.height), Offset(Random.nextFloat()*size.width, Random.nextFloat()*size.height + 20f), 2f) }
}
fun DrawScope.drawSnowflakes(size: Size) {
    repeat(50) { drawCircle(Color.White.copy(0.7f), 3f, Offset(Random.nextFloat()*size.width, Random.nextFloat()*size.height)) }
}

// --- 5. MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme { Surface(color = MaterialTheme.colorScheme.background) { WeatherApp() } } }
    }
}
