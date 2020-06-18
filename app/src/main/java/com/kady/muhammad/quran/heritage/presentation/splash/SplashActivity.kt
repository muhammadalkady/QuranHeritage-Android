package com.kady.muhammad.quran.heritage.presentation.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.kady.muhammad.quran.heritage.presentation.main.MainActivity

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
    }
}