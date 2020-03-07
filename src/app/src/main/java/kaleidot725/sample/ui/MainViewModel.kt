package kaleidot725.sample.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kaleidot725.sample.data.User
import kaleidot725.sample.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val userRepository: UserRepository): ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.tryUpdateRecentUsersCache()
        }
    }

    val users: LiveData<List<User>> = userRepository.getUsers().asLiveData()
}