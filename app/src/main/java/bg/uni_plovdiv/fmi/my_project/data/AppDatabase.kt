package bg.uni_plovdiv.fmi.my_project.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        private var instance: AppDatabase? = null

        // Room does not allow database work on the main thread,
        // so all Activities run their database code on this one background thread.
        val databaseExecutor: ExecutorService = Executors.newSingleThreadExecutor()

        // Returns the same database object every time (only one is needed)
        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_planner_db"
                ).build()
                instance = newInstance
                newInstance
            }
        }
    }
}