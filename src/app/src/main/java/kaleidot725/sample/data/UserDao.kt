package kaleidot725.sample.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    fun insert(user : User)

    @Delete
    fun delete(user : User)

    @Query("delete from users")
    fun deleteAll()

    @Query("select * from users")
    fun getAll(): Flow<List<User>>

    @Query("select * from users where age = :age")
    fun getAllWithOverAge(age: Int): Flow<List<User>>
}