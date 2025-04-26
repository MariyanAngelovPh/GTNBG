package com.example.gettoknowbulgaria.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gettoknowbulgaria.R
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast


class RegisterActivity : AppCompatActivity() {
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var imageViewTogglePassword: ImageView
    private lateinit var imageViewToggleConfirmPassword: ImageView
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextUsername = findViewById(R.id.editTextRegisterUsername)
        editTextPassword = findViewById(R.id.editTextRegisterPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        imageViewTogglePassword = findViewById(R.id.imageViewTogglePasswordRegister)
        imageViewToggleConfirmPassword = findViewById(R.id.imageViewToggleConfirmPassword)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)
        val textViewLogin = findViewById<TextView>(R.id.textViewToLogin)

        imageViewTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(editTextPassword, imageViewTogglePassword, isPasswordVisible)
        }

        imageViewToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(editTextConfirmPassword, imageViewToggleConfirmPassword, isConfirmPasswordVisible)
        }

        buttonRegister.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()

            if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "Моля, попълнете всички полета", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Паролите не съвпадат", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

//            val db = AppDatabase.getDatabase(this)
//            val userDao = db.userDao()
//
//            val userExists = userDao.getUserByUsername(username) != null
//            if (userExists) {
//                Toast.makeText(this, "Потребителското име вече съществува", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val user = User(username = username, password = password)
//            userDao.insertUser(user)

//            Toast.makeText(this, "Регистрацията беше успешна", Toast.LENGTH_SHORT).show()
//
//            // ✅ Пренасочване към LoginActivity
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()

            // НОВО - Работи с Firebase Firestore

            val db = FirebaseFirestore.getInstance()

            db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        Toast.makeText(this, "Потребителското име вече съществува", Toast.LENGTH_SHORT).show()
                    } else {
                        val newUser = hashMapOf(
                            "username" to username,
                            "password" to password
                        )

                        db.collection("users")
                            .add(newUser)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Регистрацията беше успешна ✅", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Грешка при регистрацията ❌", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Грешка при проверка на потребителя ❌", Toast.LENGTH_SHORT).show()
                }

        }

        textViewLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun togglePasswordVisibility(editText: EditText, toggleIcon: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(R.drawable.visibility_off_24px)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(R.drawable.visibility_24px)
        }
        editText.setSelection(editText.text.length)
    }
}
