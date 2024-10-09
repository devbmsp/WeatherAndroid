package clima.tempo.weather.Models

import androidx.room.Dao
import clima.tempo.weather.DB.User

class UserRepository(private val dao: Dao) {

    suspend fun insert(user: User) {
        dao.insert(user)
    }

    suspend fun login(email: String, password: String): User? {
        return dao.login(email, password)
    }
}
