package com.example.jetpackroom.meteorologia.model.weather

import com.google.gson.annotations.SerializedName

data class Coord(
    @SerializedName("lon") var lon:Double? = null,
    @SerializedName("lat") var lat:Double? = null
)
