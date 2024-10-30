// LoginActivity.kt
package clima.tempo.weather.View

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import clima.tempo.weather.RegisterActivity
import clima.tempo.weather.ViewModel.LoginViewModel
import clima.tempo.weather.ViewModel.LoginState
import clima.tempo.weather.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa a ViewModel
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.btnLogin.setOnClickListener { view ->

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                showSnackbar(view, "Preencha todos os campos!", 0xFFFF0000.toInt())
            } else {
                viewModel.login(email, password)
            }
        }

        binding.btnCadastro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this, Observer { state ->
            when (state) {
                is LoginState.Loading -> {
                }
                is LoginState.Success -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is LoginState.Error -> {
                    showSnackbar(binding.root, state.message ?: "Erro ao fazer login", 0xFF00008B.toInt())
                }
            }
        })
    }

    private fun showSnackbar(view: android.view.View, message: String, color: Int) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(color)
        snackbar.show()
    }
}
