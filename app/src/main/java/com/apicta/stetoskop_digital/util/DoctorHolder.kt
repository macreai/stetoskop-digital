package com.apicta.stetoskop_digital.util

object DoctorHolder {
    private var doctor: String? = null

    fun setDoctor(doctor: String) {
        this.doctor = doctor
    }

    fun getDoctor(): String? {
        return doctor
    }
}