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
import com.example.gettoknowbulgaria.ui.adapter.PhotoAdapter
import com.example.gettoknowbulgaria.ui.adapter.PhotoItem
import okhttp3.*
import org.json.JSONObject
import android.util.Base64
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException


class LandmarkDetailsActivity : AppCompatActivity() {

    private lateinit var similarRecyclerView: RecyclerView
    private lateinit var similarAdapter: LandmarkSmallAdapter
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            uploadImageToImgBB(uri)
        }
    }
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter


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
        photoRecyclerView = findViewById(R.id.recyclerViewPhotos)
        photoAdapter = PhotoAdapter(mutableListOf()) // започваме с празен списък
        photoRecyclerView.layoutManager = LinearLayoutManager(this)
        photoRecyclerView.adapter = photoAdapter



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

    private fun uploadImageToImgBB(imageUri: Uri) {
        val apiKey = "724f70c0f804c51ccbeaba2fd1062585"

        val inputStream = contentResolver.openInputStream(imageUri)
        val bytes = inputStream?.readBytes()
        val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

        val requestBody = FormBody.Builder()
            .add("key", apiKey)
            .add("image", base64Image)
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LandmarkDetailsActivity, "Грешка при качване ❌", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val data = json.getJSONObject("data")
                    val imageUrl = data.getString("display_url")  // 🟢 това винаги е изображение
                    Log.d("ImgBB", "Img URL used: $imageUrl")

                    Log.d("ImgBB", "Получен imageUrl: $imageUrl")

                    runOnUiThread {
                        saveImageUrlToFirestore(imageUrl)
                        Toast.makeText(this@LandmarkDetailsActivity, "Снимката е качена успешно ✅", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LandmarkDetailsActivity, "ImgBB грешка ❌", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

    }


    private fun saveImageUrlToFirestore(imageUrl: String) {
        val landmarkName = intent.getStringExtra("name") ?: return
        val db = FirebaseFirestore.getInstance()
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("username", "непознат") ?: "непознат"

        val imageData = hashMapOf(
            "landmark_name" to landmarkName,
            "image_url" to imageUrl,
            "uploader" to username,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("landmark_photos")
            .add(imageData)
            .addOnSuccessListener {
                Toast.makeText(this, "Снимката е запазена успешно ✅", Toast.LENGTH_SHORT).show()
                loadPhotosForLandmark() // ⬅️ Презарежда снимките след запис
            }
            .addOnFailureListener {
                Toast.makeText(this, "Грешка при запис в базата 🛑", Toast.LENGTH_SHORT).show()
            }

        Log.d("FirestoreSave", "Записваме URL: $imageUrl за $landmarkName")

    }


    private fun loadPhotosForLandmark() {
        val landmarkName = intent.getStringExtra("name") ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("landmark_photos")
            .whereEqualTo("landmark_name", landmarkName)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                val photoList = documents.mapNotNull {
                    val url = it.getString("image_url")
                    val uploader = it.getString("uploader") ?: "неизвестен"
                    if (url != null) PhotoItem(url, uploader) else null
                }
                Log.d("FirestoreDebug", "Намерени снимки: ${photoList.size}")
                photoAdapter.updateData(photoList) // ⬅️ най-важното
            }
            .addOnFailureListener {
                Toast.makeText(this, "Грешка при зареждане на снимките 🛑", Toast.LENGTH_SHORT).show()
            }
    }
}
