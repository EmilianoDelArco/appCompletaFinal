package com.example.jetpackroom.meteorologia.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.jetpackroom.meteorologia.constant.Const.Companion.LOADING
import com.example.jetpackroom.meteorologia.constant.Const.Companion.NA
import com.example.jetpackroom.meteorologia.model.weather.WeatherResult
import com.example.jetpackroom.meteorologia.utils.Utils.Companion.buildIcon
import com.example.jetpackroom.meteorologia.utils.Utils.Companion.timestampToHumanDateVal
import com.example.jetpackroom.meteorologia.viewModel.STATE
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType
import com.guru.fontawesomecomposelib.FaIcons

@Composable
fun WeatherSection(weatherResponse: WeatherResult) {

    //TITLE
    var title =""

    if(!weatherResponse.name.isNullOrEmpty()){
        weatherResponse.name?.let {
            title= it
        }
    }else{
        weatherResponse.coord?.let {
            title = "${it.lat}/${it.lon}"
        }
    }

    //SubTITLE
    var subTitle =""

val dateVal = (weatherResponse.dt ?:0)
    subTitle = if(dateVal == 0 ) LOADING
    else timestampToHumanDateVal(dateVal.toLong(), "dd-MM-yyyy")

    //ICON
    var icon = ""
    var description =""
    weatherResponse.weather.let {

        if (it!!.size > 0){
            description = if (it[0].description == null) LOADING else it[0].description!!
            icon = if(it[0].icon == null) LOADING else it[0].icon!!
        }
    }

    //TEMP
    var temp = ""
    weatherResponse.main.let {
        temp = "${it?.temp} °C"
    }

    //WIND
    var wind = ""
    weatherResponse.wind.let{
        wind= if (it == null) LOADING else "${it.speed}"
    }

    //CLOUDS
    var clouds = ""
    weatherResponse.clouds.let{
        clouds= if (it == null) LOADING else "${it.all}"
    }

    //SNOW
    var snow = ""
    weatherResponse.snow.let{
        snow= if (it!!.d1h == null) NA else "${it.d1h}"
    }

    WeatherTitleSection(text= title, subText= subTitle, fontSize= 30.sp)
    WeatherImagen(icon = icon)
    WeatherTitleSection(text= temp, subText= description, fontSize= 60.sp)
    Row (
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround

    ){
        WeatherInfo(icon = FaIcons.Wind, text= wind )
        WeatherInfo(icon = FaIcons.Cloud, text= clouds )
        WeatherInfo(icon = FaIcons.Snowflake, text= snow )

    }
}
@Composable
fun WeatherInfo(icon: FaIconType.SolidIcon, text: String) {
    Column{
        FaIcon(faIcon = icon, size= 48.dp, tint= Color.White)
        Text(text, fontSize = 24.sp, color = Color.White)
    }
}

@Composable
fun WeatherImagen(icon: String) {
    AsyncImage(model = buildIcon(icon), contentDescription = icon,
        modifier = Modifier.width(200.dp).height(200.dp),
        contentScale = ContentScale.FillBounds)
}

@Composable
fun WeatherTitleSection(text: String, subText: String, fontSize: TextUnit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text, fontSize= fontSize, color = Color.White, fontWeight = FontWeight.Bold)
        Text(subText, fontSize= 14.sp, color = Color.White)
    }
}
