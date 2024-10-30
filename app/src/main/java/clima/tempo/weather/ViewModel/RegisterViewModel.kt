package clima.tempo.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> get() = _registerState

    fun registerUser(email: String, password: String, name: String) {
        _registerState.value = RegisterState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val userMap = hashMapOf(
                        "nickName" to name,
                        "email" to email,
                        "senha" to password
                    )
                    db.collection("Usuários").document(name)
                        .set(userMap)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                _registerState.value = RegisterState.Success
                            } else {
                                _registerState.value = RegisterState.Error(dbTask.exception?.message)
                            }
                        }
                } else {
                    _registerState.value = RegisterState.Error(authTask.exception?.message)
                }
            }
    }
}

sealed class RegisterState {
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String?) : RegisterState()
}
