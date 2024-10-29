package clima.tempo.weather.Models

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import clima.tempo.weather.R
import clima.tempo.weather.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

            binding.btnRegister.setOnClickListener{view ->

                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                val name = binding.etName.text.toString()

                if (email.isEmpty() || password.isEmpty() || name.isEmpty()){

                    val snackbar = Snackbar.make(view,"Preencha todos os campos!",Snackbar.LENGTH_SHORT)
                    snackbar.setBackgroundTint(0xFFFF0000.toInt())
                    snackbar.show()
                }else{

                    auth.createUserWithEmailAndPassword(email,password)
                    val userMap = hashMapOf(
                        "nickName" to name,
                        "email" to email,
                        "senha" to password
                    )
                    db.collection("Usuários").document(name)
                        .set(userMap).addOnCompleteListener{
                            Log.d("db", "success BD")
                        }
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)

                    val snackbar = Snackbar.make(view,"Usuario cadastrado!",Snackbar.LENGTH_SHORT)
                    snackbar.setBackgroundTint(0xFFFF0000.toInt())
                    snackbar.show()
                }


        }

        }
    }


