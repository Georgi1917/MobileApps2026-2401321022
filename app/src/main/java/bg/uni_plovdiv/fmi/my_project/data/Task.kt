package bg.uni_plovdiv.fmi.my_project.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// One row of the "tasks" table in the database
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // 0 = Room creates a new id
    val title: String,
    val description: String,
    val subject: String,
    val deadline: String,       // saved as yyyy-MM-dd, for example 2026-11-20
    val priority: String,       // "Low", "Medium" or "High"
    val isCompleted: Boolean = false
)
