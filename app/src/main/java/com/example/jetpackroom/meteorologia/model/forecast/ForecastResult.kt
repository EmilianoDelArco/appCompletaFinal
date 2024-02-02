package com.example.jetpackroom.meteorologia.model.forecast

import com.google.gson.annotations.SerializedName

data class ForecastResult (
    @SerializedName("cod") var cod:Int?=null,
    @SerializedName("message") var message:Int?=null,
    @SerializedName("cnt") var cnt:Int?=null,
    @SerializedName("list") var list:ArrayList<CustomList>?= arrayListOf(),
    @SerializedName("city") var city: City?= City()

)