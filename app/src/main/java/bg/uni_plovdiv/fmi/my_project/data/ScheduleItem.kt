package bg.uni_plovdiv.fmi.my_project.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// One class in the weekly schedule
@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // 0 = Room creates a new id
    val subject: String,
    val dayOfWeek: Int,         // 1 = Monday, 2 = Tuesday ... 7 = Sunday
    val startTime: String,      // saved as HH:mm, for example 08:30
    val endTime: String,        // saved as HH:mm, for example 10:00
    val room: String,
    val teacher: String
)
