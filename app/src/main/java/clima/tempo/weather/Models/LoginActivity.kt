package clima.tempo.weather.Models

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import clima.tempo.weather.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { view ->

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {

                val snackbar =
                    Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
                snackbar.setBackgroundTint(0xFFFF0000.toInt())
                snackbar.show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { autenticacao ->

                        if (autenticacao.isSuccessful) {
                            val usuarioAtual = auth.currentUser
                            val emailUsuario = usuarioAtual?.email
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)

                        } else {
                            val snackbar = Snackbar.make(
                                view,
                                "Não foi possivel encontrar o Usuário",
                                Snackbar.LENGTH_SHORT
                            )
                            snackbar.setBackgroundTint(0xFF00008B.toInt())
                            snackbar.show()
                        }
                    }

            }
        }
        binding.btnCadastro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            }



    }
}


