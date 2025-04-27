package com.example.gettoknowbulgaria.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gettoknowbulgaria.R
import java.text.SimpleDateFormat
import java.util.*
import com.example.gettoknowbulgaria.data.Landmark
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage


class LandmarkDetailsActivity : AppCompatActivity() {

    private lateinit var similarRecyclerView: RecyclerView
    private lateinit var similarAdapter: LandmarkSmallAdapter
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            uploadImageToFirebase(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmark_details)

        val imageView = findViewById<ImageView>(R.id.imageViewLandmark)
        val textViewName = findViewById<TextView>(R.id.textViewLandmarkName)
        val textViewType = findViewById<TextView>(R.id.textViewType)
        val textViewCityRegion = findViewById<TextView>(R.id.textViewCityRegion)
        val textViewGPS = findViewById<TextView>(R.id.textViewGPS)
        val textViewDescription = findViewById<TextView>(R.id.textViewDescription)
        val buttonVisited = findViewById<Button>(R.id.buttonMarkVisited)
        val buttonAddPhoto = findViewById<Button>(R.id.buttonAddPhoto)
        val buttonBack = findViewById<Button>(R.id.buttonBackToList)

        similarRecyclerView = findViewById(R.id.recyclerViewSimilar)
        similarRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        similarAdapter = LandmarkSmallAdapter(this@LandmarkDetailsActivity, emptyList<Landmark>())
        similarRecyclerView.adapter = similarAdapter

        val db = FirebaseFirestore.getInstance()

        val name = intent.getStringExtra("name") ?: return
        val description = intent.getStringExtra("description") ?: ""
        val imagePath = intent.getStringExtra("image_path") ?: ""
        val type = intent.getStringExtra("type") ?: ""
        val city = intent.getStringExtra("city") ?: ""
        val manicipality = intent.getStringExtra("manicipality") ?: ""
        val gps = intent.getStringExtra("location") ?: ""

        textViewName.text = name
        textViewDescription.text = description
        textViewType.text = type
        textViewCityRegion.text = "$city, $manicipality"
        textViewGPS.text = gps

        Glide.with(this)
            .load(imagePath.ifBlank { null }) // Предпазно зареждане
            .placeholder(R.drawable.ic_launcher_foreground) // Drawable ресурс
            .into(imageView)

        buttonVisited.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val username = sharedPref.getString("username", null)

            if (username != null) {
                val visitedData = hashMapOf(
                    "username" to username,
                    "landmark_name" to name,
                    "imagePath" to imagePath,
                    "dateVisited" to SimpleDateFormat(
                        "dd.MM.yyyy HH:mm",
                        Locale.getDefault()
                    ).format(Date())
                )

                db.collection("visited_landmarks")
                    .add(visitedData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Добавено в посетени ✅", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Грешка при запис 🛑", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Грешка: Няма открит потребител", Toast.LENGTH_SHORT).show()
            }
        }

        buttonAddPhoto.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        buttonBack.setOnClickListener {
            finish()  // Това ще върне потребителя към предишната активност
        }

        db.collection("landmarks")
            .get()
            .addOnSuccessListener { result ->
                val allLandmarks = result.map { it.toObject(com.example.gettoknowbulgaria.data.Landmark::class.java) }
                val similar = allLandmarks.filter { it.type == type && it.name != name }.take(2)
                similarAdapter.setData(similar)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Грешка при зареждане на подобни забележителности.", Toast.LENGTH_SHORT).show()
            }

        loadPhotosForLandmark()

    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val imageRef = storageReference.child("landmark_photos/$fileName")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    addImageToLayout(uri.toString())
                    saveImageUrlToFirestore(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Грешка при качване на снимката", Toast.LENGTH_SHORT).show()
            }
    }


    private fun addImageToLayout(imageUrl: String) {
        val selectedImageView = ImageView(this)
        selectedImageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        selectedImageView.adjustViewBounds = true

        Glide.with(this)
            .load(imageUrl)
            .into(selectedImageView)

        val linearLayout = findViewById<LinearLayout>(R.id.linearLayoutPhotos)
        linearLayout.addView(selectedImageView)
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val landmarkName = intent.getStringExtra("name") ?: return
        val db = FirebaseFirestore.getInstance()

        val imageData = hashMapOf(
            "landmark_name" to landmarkName,
            "image_url" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("landmark_photos")
            .add(imageData)
            .addOnSuccessListener {
                Toast.makeText(this, "Снимката е запазена успешно ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Грешка при запис в базата 🛑", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPhotosForLandmark() {
        val landmarkName = intent.getStringExtra("name") ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("landmark_photos")
            .whereEqualTo("landmark_name", landmarkName)
            .orderBy("timestamp") // по ред на качване
            .get()
            .addOnSuccessListener { documents ->
                val linearLayout = findViewById<LinearLayout>(R.id.linearLayoutPhotos)

                for (document in documents) {
                    val imageUrl = document.getString("image_url")
                    if (imageUrl != null) {
                        val imageView = ImageView(this)
                        imageView.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        imageView.adjustViewBounds = true

                        Glide.with(this)
                            .load(imageUrl)
                            .into(imageView)

                        linearLayout.addView(imageView)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Грешка при зареждане на снимките 🛑", Toast.LENGTH_SHORT).show()
            }
    }


}
