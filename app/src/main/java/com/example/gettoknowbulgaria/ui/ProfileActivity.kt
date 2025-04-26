package com.example.gettoknowbulgaria.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gettoknowbulgaria.R
import com.example.gettoknowbulgaria.ui.adapter.VisitedLandmarkAdapter
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.example.gettoknowbulgaria.data.VisitedLandmark

class ProfileActivity : AppCompatActivity() {

    private lateinit var imageViewProfile: ImageView
    private lateinit var textViewUsername: TextView
    private lateinit var buttonLogout: Button
    private lateinit var recyclerViewVisited: RecyclerView
    private lateinit var visitedLandmarkAdapter: VisitedLandmarkAdapter
    private lateinit var buttonHome: Button

    private val visitedLandmarks: MutableList<VisitedLandmark> = mutableListOf()

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imageViewProfile = findViewById(R.id.imageViewProfile)
        textViewUsername = findViewById(R.id.textViewUsername)
        buttonLogout = findViewById(R.id.buttonLogout)
        recyclerViewVisited = findViewById(R.id.recyclerViewVisited)
        buttonHome = findViewById(R.id.buttonHome)

        db = FirebaseFirestore.getInstance()

        //  Зареждаме потребителското име
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("username", "Потребител")
        textViewUsername.text = username

        visitedLandmarkAdapter = VisitedLandmarkAdapter(visitedLandmarks)
        recyclerViewVisited.layoutManager = LinearLayoutManager(this)
        recyclerViewVisited.adapter = visitedLandmarkAdapter

        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        buttonLogout.setOnClickListener {
            sharedPref.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        loadVisitedLandmarks()
    }

    // Зареждане на посетени обекти при всяко отваряне
    override fun onStart() {
        super.onStart()
        loadVisitedLandmarks()
    }

    private fun loadVisitedLandmarks() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("username", "")

        if (!username.isNullOrEmpty()) {
            db.collection("visited_landmarks")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { documents ->
                    val updatedList = mutableListOf<VisitedLandmark>()

                    for (document in documents) {
                        val visitedLandmark = document.toObject(VisitedLandmark::class.java)
                        updatedList.add(visitedLandmark)
                    }

                    visitedLandmarkAdapter.updateList(updatedList)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Грешка при зареждане на посетени обекти", Toast.LENGTH_SHORT).show()
                }
        }
    }
}





