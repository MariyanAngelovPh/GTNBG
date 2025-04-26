package com.example.gettoknowbulgaria.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.gettoknowbulgaria.R
import com.example.gettoknowbulgaria.ui.adapter.ImageSliderAdapter
import com.example.gettoknowbulgaria.ui.LandmarksListActivity


class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var sliderHandler: Handler
    private lateinit var sliderRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)

        val imageList = listOf(
            R.drawable.plovdiv,
            R.drawable.vidin,
            R.drawable.vratsa
        )

        val adapter = ImageSliderAdapter(imageList)
        viewPager.adapter = adapter

        sliderHandler = Handler(Looper.getMainLooper())
        sliderRunnable = Runnable {
            val nextItem = (viewPager.currentItem + 1) % imageList.size
            viewPager.setCurrentItem(nextItem, true)
            sliderHandler.postDelayed(sliderRunnable, 3000)
        }

        sliderHandler.postDelayed(sliderRunnable, 3000)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000)
            }
        })

        val buttonLandmarks = findViewById<Button>(R.id.buttonLandmarks)
        val buttonProfile = findViewById<Button>(R.id.buttonProfile)

        buttonLandmarks.setOnClickListener {
            startActivity(Intent(this, LandmarksListActivity::class.java))
        }

        buttonProfile.setOnClickListener {
            // ✅ Вземаме името от SharedPreferences
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val username = sharedPref.getString("username", "Потребител")

            // ✅ Подаваме го към ProfileActivity
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }
}
