package kaleidot725.sample.data

import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getUsers() = userDao.getAll()
    fun getUsersWithOverAge(age: Int): Flow<List<User>> = userDao.getAllWithOverAge(age)

    fun tryUpdateRecentUsersCache() {
        userDao.deleteAll()
        userDao.insert(User(1, "A", "G", 20))
        userDao.insert(User(2, "B", "F", 21))
        userDao.insert(User(3, "C", "E", 22))
        userDao.insert(User(4, "D", "D", 23))
        userDao.insert(User(5, "E", "C", 24))
        userDao.insert(User(6, "F", "B", 25))
        userDao.insert(User(7, "G", "A", 26))
    }
}