package com.example.jetpackroom.meteorologia.constant

class Const {
    companion object{
        var permissions = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

       const val apikey = "bd5e378503939ddaee76f12ad7a97608"

        const val colorBg1 = 0xff08203e
        const val colorBg2 =0xff557c93
        const val colorCard = 0xFF333639

        const val LOADING = "Loading..."
        const val NA = "N/A"
    }
}