package clima.tempo.weather

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel

//funções do repositorio => WeatherModel

class WeatherViewModel()  : ViewModel() {
    private val _repository: WeatherRepository = WeatherRepository()
//private val _info = mutableStateof(repository.getInformation().info)
}