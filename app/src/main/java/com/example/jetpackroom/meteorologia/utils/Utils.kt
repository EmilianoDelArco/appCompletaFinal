package com.example.jetpackroom.meteorologia.utils

import java.text.SimpleDateFormat

class Utils {
    companion object{
        fun timestampToHumanDateVal(timestamp:Long, format:String ): String{
            val sdf = SimpleDateFormat(format)
            return sdf.format(timestamp*1000)
        }

        fun buildIcon(icon:String, isBigSize : Boolean = true):String{
            return if(isBigSize) {
                    "https://openweathermap.org/img/wn/$icon@4x.png"
            }else{
                "https://openweathermap.org/img/wn/$icon.png"
            }
        }


    }
}