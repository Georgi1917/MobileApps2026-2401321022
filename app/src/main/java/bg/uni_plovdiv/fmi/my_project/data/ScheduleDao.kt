package bg.uni_plovdiv.fmi.my_project.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// All database operations for the schedule
@Dao
interface ScheduleDao {

    @Insert
    fun insert(item: ScheduleItem)

    @Update
    fun update(item: ScheduleItem)

    @Delete
    fun delete(item: ScheduleItem)

    // The classes of one day, earliest first.
    // Times are saved as HH:mm, so sorting them as text sorts them by time.
    @Query("SELECT * FROM schedule_items WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getItemsForDay(day: Int): List<ScheduleItem>

    // The whole week
    @Query("SELECT * FROM schedule_items ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllItems(): List<ScheduleItem>

    @Query("SELECT * FROM schedule_items WHERE id = :itemId")
    fun getItemById(itemId: Int): ScheduleItem?
}