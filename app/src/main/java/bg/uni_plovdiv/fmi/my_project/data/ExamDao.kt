package bg.uni_plovdiv.fmi.my_project.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// All database operations for exams
@Dao
interface ExamDao {

    @Insert
    fun insert(exam: Exam)

    @Update
    fun update(exam: Exam)

    @Delete
    fun delete(exam: Exam)

    // Upcoming exams first (closest date on top), exams that already passed go to the bottom.
    // Dates are saved as yyyy-MM-dd, so sorting them as text sorts them by date.
    @Query(
        "SELECT * FROM exams " +
                "ORDER BY (examDate < date('now', 'localtime')) ASC, examDate ASC, examTime ASC"
    )
    fun getAllExams(): List<Exam>

    @Query("SELECT * FROM exams WHERE id = :examId")
    fun getExamById(examId: Int): Exam?
}