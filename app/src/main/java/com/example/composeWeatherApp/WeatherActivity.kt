package com.example.composeWeatherApp


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.transition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.RippleIndication
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Devices
import androidx.ui.tooling.preview.Preview
import com.example.composeWeatherApp.ui.darkblue
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.util.*


class WeatherActivity : AppCompatActivity(){
    var BaseUrl = "http://api.openweathermap.org/"
    var AppId = "2e65127e909e178d0af311a81f39948c"
    var cityNameInput =  mutableStateOf("")
    var desc =  mutableStateOf("")
    var humidity =  mutableStateOf("")
    var pressure =  mutableStateOf("")
    var temperature =  mutableStateOf("")
    var windSpeed =  mutableStateOf("")
    var city =  mutableStateOf("")
    var image =  mutableStateOf(0)
    var dt = mutableStateOf(0.0)
    var isWeatherDataFetched = mutableStateOf(false)
    var bg_image = mutableStateOf(R.drawable.bg_red_final)
    val gradient = HorizontalGradient(
        listOf(Color(0xffa18cd1), Color(0xfffbc2eb)),
        startX = 0.0f,
        endX = 1000.0f,
        tileMode = TileMode.Repeated
    )
    var isExampleTry = false
    enum class Weather {
        SUNNY, CLOUDY, SNOW, RAINY, NONE
    }

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(contentColor = darkblue)
            {
                Stack() {
                    Image(asset = imageResource(id = bg_image.value), modifier = Modifier.fillMaxWidth().fillMaxHeight().
                    padding(0.dp))
                    Column {
                        Content()
                    }
                }
            }
        }
    }
    private val rotation = FloatPropKey()
    private val rotationTransitionDefinition = transitionDefinition<String> {
        state("A") { this[rotation] = 30f }
        state("B") { this[rotation] = 180f }
        transition(fromState = "A", toState = "B") {
            rotation using repeatable(
                animation = tween<Float>(
                    durationMillis = 2000,
                    easing = FastOutLinearInEasing
                ),
                iterations = AnimationConstants.Infinite
            )
        }
    }

    @ExperimentalAnimationApi
    @Composable
    private fun Content() {
        Column(modifier = Modifier.padding(20.dp)) {
            CityNameInput()
            WeatherIcon()
            WeatherData()
            LastUpdateTime()
        }
    }

    @ExperimentalAnimationApi
    @Composable
    private fun LastUpdateTime() {
        AnimatedVisibility(visible = isWeatherDataFetched.value) {
            val df: DateFormat = DateFormat.getDateTimeInstance()
            val updatedOn: String = df.format(Date(dt.value.toLong() * 1000))
            Row(modifier = Modifier.fillMaxWidth().padding(70.dp, 0.dp, 0.dp, 0.dp), horizontalArrangement = Arrangement.Center) {
                Text(city.value + " Last update: " + updatedOn,  modifier = Modifier.fillMaxWidth(), style = TextStyle(color = Color.Black), fontSize = 12.sp, fontFamily = FontFamily.Serif)
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    @ExperimentalAnimationApi
    @Composable
    private fun WeatherData() {
        AnimatedVisibility(visible = isWeatherDataFetched.value) {
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(10.dp).fillMaxWidth().height(200.dp),
                backgroundColor = Color(R.color.darkblue),
                contentColor = Color.White,
            ) {
                ConstraintLayout {
                    val (text1, text2, text3, text4, text5) = createRefs()
                    Text( desc.value
                        , Modifier.constrainAs(text1) {
                            top.linkTo(parent.top, margin = 16.dp)
                        }.padding(20.dp, 0.dp, 0.dp, 0.dp), style = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Serif)
                    )

                    Text("Pressure: " + pressure.value + " hPa", Modifier.constrainAs(text2) {
                        end.linkTo(parent.end, margin = 16.dp)
                    }.padding(0.dp, 15.dp, 8.dp, 0.dp),style = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Serif))

                    Text("WindSpeed: "+windSpeed.value + "km/hr", Modifier.constrainAs(text3) {
                        bottom.linkTo(parent.bottom, margin = 16.dp)
                    }.padding(20.dp, 15.dp, 10.dp, 0.dp),style = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Serif))

                    Text(temperature.value+"℃" , Modifier.constrainAs(text4) {
                        centerTo(parent)
                    }, style = TextStyle(fontSize = 35.sp, fontFamily = FontFamily.Serif))

                    Text("Humidity: " + humidity.value + "%", modifier = Modifier.constrainAs(text5)
                    {bottom.linkTo(parent.bottom)}.padding(220.dp, 0.dp, 5.dp, 15.dp),style = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Serif)
                    )
                }
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    @Composable
    private fun CityNameInput() {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(10.dp).fillMaxWidth().height(60.dp).drawBorder(Border(8.dp, gradient)),
            backgroundColor = Color(R.color.darkblue),
            contentColor = Color.White,
        ) {
            TextField(
                value = cityNameInput.value,
                onValueChange = { newValue ->
                    cityNameInput.value = newValue
                },
                modifier = Modifier.height(60.dp),
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                placeholder = { Text("Enter a city name", style = TextStyle(color = Color.White), fontSize = 16.sp, fontFamily = FontFamily.SansSerif) },
                leadingIcon = {  Icon(
                    asset = Icons.Filled.Search,
                    modifier = Modifier.clickable(
                        onClick = {
                            getWeatherData(Weather.NONE)
                        }, indication = RippleIndication(
                            radius = 40.dp,
                            bounded = false
                        )
                    ),
                ) },
                imeAction = ImeAction.Done,
                onImeActionPerformed = { action, controller ->
                    if (action == ImeAction.Search || action == ImeAction.Done) {
                        getWeatherData(Weather.NONE)
                        controller?.hideSoftwareKeyboard()
                    }
                },
            )
        }
        SampleExamples()
    }

    @Composable
    private fun SampleExamples() {
        Column {
            if (!isWeatherDataFetched.value) {
                Text(text = "Sample Examples:", modifier = Modifier.fillMaxWidth().padding(10.dp), style = TextStyle(color = Color.DarkGray, fontSize = 18.sp))
                Row() {
                    OutlinedButton(onClick = {getWeatherData(Weather.SUNNY)},
                        backgroundColor = Color.Transparent,
                        shape = CircleShape,
                        contentColor = Color.DarkGray,
                        border = BorderStroke(width = 1.dp, color = Color.DarkGray)
                    ) {
                        Text("Sunny")
                    }

                    Spacer(modifier = Modifier.padding(5.dp))

                    OutlinedButton(onClick = {getWeatherData(Weather.CLOUDY)},
                        backgroundColor = Color.Transparent,
                        shape = CircleShape,
                        contentColor = Color.DarkGray,
                        border = BorderStroke(width = 1.dp, color = Color.DarkGray)
                    ) {
                        Text("Cloudy")
                    }

                    Spacer(modifier = Modifier.padding(5.dp))

                    OutlinedButton(onClick = {getWeatherData(Weather.RAINY)},
                        backgroundColor = Color.Transparent,
                        shape = CircleShape,
                        contentColor = Color.DarkGray,
                        border = BorderStroke(width = 1.dp, color = Color.DarkGray)
                    ) {
                        Text("Rainy")
                    }

                    Spacer(modifier = Modifier.padding(5.dp))

                    OutlinedButton(onClick = {getWeatherData(Weather.SNOW)},
                        backgroundColor = Color.Transparent,
                        shape = CircleShape,
                        contentColor = Color.DarkGray,
                        border = BorderStroke(width = 1.dp, color = Color.DarkGray)
                    ) {
                        Text("Snow")
                    }


                }
            }
        }
       
    }

    private fun getWeatherData(weather: Weather) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        if (weather != Weather.NONE) {
            cityNameInput.value = "London"
            isExampleTry = true
        }
        val call = service.getWeatherDataOfLocation(cityNameInput.value, AppId, "metric")
        call?.enqueue(object : Callback<WeatherResponse?> {
            override fun onResponse(
                call: Call<WeatherResponse?>,
                response: Response<WeatherResponse?>
            ) {
                if (response.code() == 200) {
                    val weatherResponse = response.body()!!
                    temperature.value = weatherResponse.main?.temp.toString()
                    pressure.value = weatherResponse.main?.pressure.toString()
                    desc.value = weatherResponse.weather[0].description.toString()
                    city.value = weatherResponse.name.toString()
                    if (weather == Weather.NONE) {
                        SetWeatherIcon(weatherResponse.weather[0].id)
                    } else {
                        SetWeatherIconForExamples(weather)
                    }
                    humidity.value = weatherResponse.main?.humidity.toString()
                    dt.value = weatherResponse.dt.toDouble()
                    windSpeed.value = weatherResponse.wind?.speed.toString()
                    isWeatherDataFetched.value = true
                } else {
                    Toast.makeText(applicationContext, "Enter a valid city name", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<WeatherResponse?>, t: Throwable) {
                Toast.makeText(applicationContext, "Enter a valid city name", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun SetWeatherIconForExamples(weather: Weather) {
        var weatherCode = 0
        if (weather == Weather.SUNNY) {
            weatherCode = 800
        } else if(weather == Weather.RAINY) {
            weatherCode = 300
        }else if(weather == Weather.CLOUDY) {
            weatherCode = 801
        }else if(weather == Weather.SNOW) {
            weatherCode = 200
        }
        SetWeatherIcon(weatherCode)
    }

    @Composable
    private fun WeatherIcon() {
        Column {
            Row (modifier = Modifier.padding(0.dp, 15.dp, 0.dp, 0.dp).fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.Center) {
                if (image.value != 0) {

                    var interactionState = remember { InteractionState() }
                    Stack(
                        modifier = Modifier.fillMaxSize().padding(5.dp),
                        alignment = Alignment.Center
                    ) {
                        Stack {
                            val state = transition(
                                definition = rotationTransitionDefinition,
                                initState = "A",
                                toState = "B"
                            )
                            Canvas(
                                modifier = Modifier.preferredSize(100.dp).gravity(Alignment.Center)
                            ) {
                                scale(state[rotation]) {
                                    drawCircle(color = Color(230, 244, 249), 1.1f)
                                    drawCircle(color = Color(167, 221, 240), 1.0f)
                                    drawCircle(color = Color(95, 194, 230), 0.8f)
                                }
                            }
                            Canvas(
                                modifier = Modifier.preferredSize(100.dp).gravity(Alignment.Center)
                                    .indication(
                                        interactionState = interactionState,
                                        indication = RippleIndication(
                                            bounded = true,
                                            radius = 10.dp,
                                            color = Color.DarkGray
                                        )
                                    )
                            ) {
                                scale(state[rotation]) {
                                    drawCircle(color = Color(230, 244, 249), 0.7f)
                                    drawCircle(color = Color(167, 221, 240), 0.5f)
                                    drawCircle(color = Color(95, 194, 230), 0.2f)
                                }
                            }
                            Image(
                                asset = imageResource(id = image.value),
                                modifier = Modifier.width(100.dp).height(100.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    fun SetWeatherIcon(weatherCode: Int) {
        when {
            weatherCode / 100 == 2 -> {
                image.value = R.drawable.ic_storm_weather
                bg_image.value = R.drawable.snow
            }
            weatherCode / 100 == 3 -> {
                image.value = R.drawable.ic_rainy_weather
                bg_image.value = R.drawable.light_rain
            }
            weatherCode / 100 == 5 -> {
                image.value = R.drawable.ic_rainy_weather
                bg_image.value = R.drawable.light_rain
            }
            weatherCode / 100 == 6 -> {
                image.value = R.drawable.ic_snow_weather
                bg_image.value = R.drawable.snow
            }
            weatherCode / 100 == 7 -> {
                image.value = R.drawable.ic_fog
                bg_image.value = R.drawable.day_fog
            }
            weatherCode == 800 -> {
                image.value = R.drawable.ic_clear_day
                bg_image.value = R.drawable.day_sunny
            }
            weatherCode == 801 -> {
                image.value = R.drawable.ic_few_clouds
                bg_image.value = R.drawable.day_partly_cloudy
            }
            weatherCode == 803 -> {
                image.value = R.drawable.ic_broken_clouds
                bg_image.value = R.drawable.day_partly_cloudy
            }
            weatherCode / 100 == 8 -> {
                image.value = R.drawable.ic_cloudy_weather
                bg_image.value = R.drawable.day_most_cloud
            }
            else -> {
                image.value = R.drawable.ic_clear_day
                bg_image.value = R.drawable.day_sunny
            }
        }
    }

    @ExperimentalAnimationApi
    @Preview(
        showDecoration = true,
        device = Devices.NEXUS_5
    )
    @Composable
    fun Preview() {
        Content()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(isExampleTry || isWeatherDataFetched.value) {
            startActivity(Intent(this, WeatherActivity::class.java))
        }
    }
}







