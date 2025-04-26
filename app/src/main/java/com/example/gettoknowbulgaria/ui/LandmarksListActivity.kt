package com.example.gettoknowbulgaria.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gettoknowbulgaria.R
import com.google.firebase.firestore.FirebaseFirestore
import com.example.gettoknowbulgaria.data.Landmark

class LandmarkAdapter(private val context: Context, private var landmarks: List<Landmark>) :
    RecyclerView.Adapter<LandmarkAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageViewLandmark)
        val name: TextView = view.findViewById(R.id.textViewName)
        val type: TextView = view.findViewById(R.id.textViewType)

        init {
            view.setOnClickListener {
                val landmark = landmarks[adapterPosition]
                val intent = Intent(context, LandmarkDetailsActivity::class.java).apply {
                    putExtra("name", landmark.name)
                    putExtra("description", landmark.description)
                    putExtra("image_path", landmark.image_path)
                    putExtra("type", landmark.type)
                    putExtra("city", landmark.city)
                    putExtra("manicipality", landmark.manicipality)
                    putExtra("location", landmark.location)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_landmark, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val landmark = landmarks[position]
        holder.name.text = landmark.name
        holder.type.text = landmark.type
        Glide.with(context)
            .load(landmark.image_path)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.image)
    }

    override fun getItemCount(): Int = landmarks.size

    fun updateList(newList: List<Landmark>) {
        landmarks = newList
        notifyDataSetChanged()
    }
}

class LandmarksListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LandmarkAdapter
    private var fullLandmarkList: List<Landmark> = emptyList()

    private lateinit var editTextSearch: EditText
    private lateinit var buttonToggleFilters: Button
    private lateinit var filterLayout: LinearLayout
    private lateinit var spinnerType: Spinner
    private lateinit var spinnerRegion: Spinner
    private lateinit var spinnerCity: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmarks_list)

        // Инициализация на view-тата
        recyclerView = findViewById(R.id.recyclerViewLandmarks)
//        localRecyclerView = findViewById(R.id.recyclerViewLocal)

        editTextSearch = findViewById(R.id.editTextSearch)
        buttonToggleFilters = findViewById(R.id.buttonToggleFilters)
        filterLayout = findViewById(R.id.filterLayout)
        spinnerType = findViewById(R.id.spinnerType)
        spinnerRegion = findViewById(R.id.spinnerRegion)
        spinnerCity = findViewById(R.id.spinnerCity)

        // Спинери
        val types = listOf("Вид:", "Исторически", "Природни", "Културни")
        val regions = listOf("Регион:", "Северозападен", "Северен централен", "Североизточен", "Югозападен", "Южен централен", "Югоизточен")
        val cities = listOf("Град:", "Пловдив", "Видин", "Враца", "Велико Търново", "Бургас")

        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        spinnerRegion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, regions)
        spinnerCity.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)

        // Основен списък от база данни
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LandmarkAdapter(this, emptyList())
        recyclerView.adapter = adapter

        val db = FirebaseFirestore.getInstance()

        db.collection("landmarks")
            .get()
            .addOnSuccessListener { result ->
                val landmarks = result.map { it.toObject(Landmark::class.java) }
                fullLandmarkList = landmarks
                adapter.updateList(landmarks)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Грешка при зареждане от Firebase", Toast.LENGTH_SHORT).show()
            }

        buttonToggleFilters.setOnClickListener {
            filterLayout.visibility =
                if (filterLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filterAndSearch()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterAndSearch()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerType.onItemSelectedListener = filterListener
        spinnerRegion.onItemSelectedListener = filterListener
        spinnerCity.onItemSelectedListener = filterListener

        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun filterAndSearch() {
        val query = editTextSearch.text.toString().trim()
        val selectedType = spinnerType.selectedItem?.toString()?.takeIf { it != "Вид:" } ?: ""
        val selectedRegion = spinnerRegion.selectedItem?.toString()?.takeIf { it != "Регион:" } ?: ""
        val selectedCity = spinnerCity.selectedItem?.toString()?.takeIf { it != "Град:" } ?: ""

        val filteredList = fullLandmarkList.filter { landmark ->
            (query.isEmpty() || landmark.name.contains(query, ignoreCase = true)) &&
                    (selectedType.isEmpty() || landmark.type == selectedType) &&
                    (selectedRegion.isEmpty() || landmark.manicipality == selectedRegion) &&
                    (selectedCity.isEmpty() || landmark.city == selectedCity)
        }
        adapter.updateList(filteredList)
    }


}
