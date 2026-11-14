package bg.uni_plovdiv.fmi.my_project.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// All database operations for tasks
@Dao
interface TaskDao {

    @Insert
    fun insert(task: Task)

    @Update
    fun update(task: Task)

    @Delete
    fun delete(task: Task)

    // Unfinished tasks first, then by deadline. Tasks without a deadline go last.
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, (deadline = '') ASC, deadline ASC")
    fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Int): Task?
}