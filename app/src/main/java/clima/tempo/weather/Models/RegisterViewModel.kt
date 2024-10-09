package clima.tempo.weather.Models
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import clima.tempo.weather.DB.User
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {
    var name: String = ""
    var email: String = ""
    var password: String = ""

    fun register() {
        viewModelScope.launch {
            repository.insert(User(name = name, email = email, password = password))
        }
    }

    fun goToLogin() {
        // Handle navigation to login screen
    }
}
