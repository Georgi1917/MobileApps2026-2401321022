package bg.uni_plovdiv.fmi.my_project.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [Task::class, Exam::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun examDao(): ExamDao

    companion object {
        private var instance: AppDatabase? = null

        // Room does not allow database work on the main thread,
        // so all Activities run their database code on this one background thread.
        val databaseExecutor: ExecutorService = Executors.newSingleThreadExecutor()

        // Version 1 -> 2: adds the exams table and keeps all existing tasks
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `exams` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`subject` TEXT NOT NULL, " +
                            "`examDate` TEXT NOT NULL, " +
                            "`examTime` TEXT NOT NULL, " +
                            "`location` TEXT NOT NULL, " +
                            "`notes` TEXT NOT NULL)"
                )
            }
        }

        // Returns the same database object every time (only one is needed)
        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_planner_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}