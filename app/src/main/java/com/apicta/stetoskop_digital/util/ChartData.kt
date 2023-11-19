package com.apicta.stetoskop_digital.util

object ChartData {

    //for plot
    val pcgArray = arrayListOf<Float>()
    val timeArray = arrayListOf<Float>()

    //for time
    private var currentTime: String? = null

    fun setCurrentTime(currentTIme: String) {
        currentTime = currentTIme
    }

    fun getCurrentTime(): String? {
        return currentTime
    }
}