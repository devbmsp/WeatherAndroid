package clima.tempo.weather.Models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    var email: String = ""
    var password: String = ""

    fun login() {
        viewModelScope.launch {
            val user = repository.login(email, password)
            // Handle login success or failure
        }
    }
}

class UserRepository {
    fun login(email: String, password: String): Any {
            TODO("Not yet implemented")
    }

}
