# ［Android］Coroutine Flow と Room を組み合わせたサンプル



Coroutine Flow と Room を組み合わせたサンプルを作成していきます。

アプリケーションは公式が推奨するアーキテクチャである MVVM を利用して作成します。



![img](https://developer.android.com/topic/libraries/architecture/images/final-architecture.png)



# Setup



```groovy
dependencies {
    　　　︙
    def room_version = "2.2.4"
    implementation "androidx.room:room-runtime:$room_version"
    implementation "androidx.room:room-ktx:$room_version"
    kapt "androidx.room:room-compiler:$room_version"

    def koin_version = "2.1.3"
    implementation "org.koin:koin-android:$koin_version"
    implementation "org.koin:koin-android-scope:$koin_version"
    implementation "org.koin:koin-android-viewmodel:$koin_version"
    implementation "org.koin:koin-android-ext:$koin_version"

    def coroutines_version = "1.3.4"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"

    def lifecycle_version = "2.2.0"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
}
```



# Model



```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?,
    val age: Int
)
```



```kotlin
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
}
```



```kotlin
@Database(entities = arrayOf(User::class), version = 1)
abstract class Database : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```



```kotlin
class UserRepository(private val userDao: UserDao) {
    fun getUsers() = userDao.getAll()

    fun getUserSortedByFirstName() = getUsers().map {
            it -> it.sortedBy { it.firstName }
    }

    fun getUserSortedByLastName() = getUsers().map {
            it -> it.sortedBy { it.lastName }
    }

    fun getUserSortedByAge() = getUsers().map {
            it -> it.sortedBy { it.age }
    }

    fun tryUpdateRecentUsersCache() {
        userDao.deleteAll()
        userDao.insert(User(1, "A", "G", 10))
        userDao.insert(User(2, "B", "F", 8))
        userDao.insert(User(3, "C", "E", 20))
        userDao.insert(User(4, "D", "D", 25))
        userDao.insert(User(5, "E", "C", 59))
        userDao.insert(User(6, "F", "B", 9))
        userDao.insert(User(7, "G", "A", 1))
    }
}
```



```Kotlin
val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), Database::class.java, "users").build()
    }

    single {
        get<Database>().userDao()
    }

    single {
        UserRepository(get())
    }

    viewModel {
        MainViewModel(get())
    }
}
```



## ViewModel



```Kotlin
class MainViewModel(private val repo: UserRepository): ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            repo.tryUpdateRecentUsersCache()
        }
    }

    val users: LiveData<List<User>> 
  			= repo.getUsers().asLiveData()
    val usersSortedByFirstName: LiveData<List<User>> 
  			= repo.getUserSortedByFirstName().asLiveData()
    val usersSortedByLastName: LiveData<List<User>> 
  			= repo.getUserSortedByLastName().asLiveData()
    val usersSortedByAge: LiveData<List<User>> 
  			= repo.getUserSortedByAge().asLiveData()
}
```



## View



```kotlin
class MainActivity : AppCompatActivity() {
    private val viewModel : MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(appModule)
        }

        val binding : ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel

        viewModel.users.observe(this, Observer {
            binding.mainText.text = it.toString()
        })

        viewModel.usersSortedByFirstName.observe(this, Observer {
            binding.sortFirstNameText.text = it.toString()
        })

        viewModel.usersSortedByLastName.observe(this, Observer {
            binding.sortLastNameText.text = it.toString()
        })

        viewModel.usersSortedByAge.observe(this, Observer {
            binding.sortAgeText.text = it.toString()
        })
    }
}
```



```XML
<layout>

    <data>
        <variable
            name="viewModel"
            type="kaleidot725.sample.ui.MainViewModel" />
    </data>

    <LinearLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        tools:context=".ui.MainActivity">

        <TextView
            android:id="@+id/main_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"/>

        <TextView
            android:id="@+id/sort_first_name_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"/>

        <TextView
            android:id="@+id/sort_last_name_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"/>

        <TextView
            android:id="@+id/sort_age_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"/>
    </LinearLayout>
</layout>
```



# 起動してみる



![Screenshot_1583677890](/Users/kaleidot725/Desktop/Screenshot_1583677890.png)





