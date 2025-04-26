package com.example.gettoknowbulgaria.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.gettoknowbulgaria.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val fadeIn = AnimationUtils.loadAnimation(this,R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        val splashLayout = findViewById<LinearLayout>(R.id.splashLayout)
        splashLayout.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            splashLayout.startAnimation(fadeOut)

            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}