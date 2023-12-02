package com.apicta.stetoskop_digital.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.apicta.stetoskop_digital.MainActivity
import com.apicta.stetoskop_digital.R

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed(Runnable {
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            finish()
        }, TIME)
    }

    companion object {
        private const val TIME = 3000L
    }
}