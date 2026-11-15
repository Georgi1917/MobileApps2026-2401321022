package bg.uni_plovdiv.fmi.my_project.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// One row of the "exams" table in the database
@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // 0 = Room creates a new id
    val subject: String,
    val examDate: String,       // saved as yyyy-MM-dd, for example 2026-12-05
    val examTime: String,       // saved as HH:mm, for example 09:30 (can be empty)
    val location: String,
    val notes: String
)
