package clima.tempo.weather

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import clima.tempo.weather.View.LoginActivity
import clima.tempo.weather.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        binding.btnRegister.setOnClickListener { view ->
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val name = binding.etName.text.toString()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                showSnackbar(view, "Preencha todos os campos!", 0xFFFF0000.toInt())
            } else {
                viewModel.registerUser(email, password, name)
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Loading -> {
                }

                is RegisterState.Success -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    showSnackbar(binding.root, "Usuário cadastrado!", 0xFF008000.toInt())
                }

                is RegisterState.Error -> {
                    showSnackbar(
                        binding.root,
                        state.message ?: "Erro ao cadastrar",
                        0xFFFF0000.toInt()
                    )
                }

                else -> {}
            }
        }
    }

    private fun showSnackbar(view: View, message: String, color: Int) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(color)
        snackbar.show()
    }
}
