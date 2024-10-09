import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import clima.tempo.weather.Models.RegisterViewModel
import clima.tempo.weather.Models.RegisterViewModelFactory
import clima.tempo.weather.Models.UserRepository

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val dao = AppDatabase.getDatabase(application).dao()
        val repository = UserRepository(dao)
        viewModel = ViewModelProvider(this, RegisterViewModelFactory(repository)).get(RegisterViewModel::class.java)

        val etName = findViewById<EditText>(R.id.et_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnGoToLogin = findViewById<Button>(R.id.btn_go_to_login)

        btnRegister.setOnClickListener {
            viewModel.name = etName.text.toString()
            viewModel.email = etEmail.text.toString()
            viewModel.password = etPassword.text.toString()
            viewModel.register()
            // Navigate to LoginActivity after registration
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnGoToLogin.setOnClickListener {
            // Handle navigation to login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
