package bg.uni_plovdiv.fmi.my_project.ui.exams

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import java.util.Locale
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.ExamDao
import bg.uni_plovdiv.fmi.my_project.data.Exam
import bg.uni_plovdiv.fmi.my_project.data.AppDatabase

class ExamFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXAM_ID = "exam_id"
    }

    private lateinit var tvFormTitle: TextView
    private lateinit var layoutSubject: TextInputLayout
    private lateinit var layoutDate: TextInputLayout
    private lateinit var editSubject: TextInputEditText
    private lateinit var editExamDate: TextInputEditText
    private lateinit var editExamTime: TextInputEditText
    private lateinit var editLocation: TextInputEditText
    private lateinit var editNotes: TextInputEditText
    private lateinit var examDao: ExamDao

    private var examId = 0                 // 0 means "new exam"
    private var selectedDate = ""          // yyyy-MM-dd or empty
    private var selectedTime = ""          // HH:mm or empty

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam_form)

        tvFormTitle = findViewById(R.id.tvFormTitle)
        layoutSubject = findViewById(R.id.layoutSubject)
        layoutDate = findViewById(R.id.layoutDate)
        editSubject = findViewById(R.id.editSubject)
        editExamDate = findViewById(R.id.editExamDate)
        editExamTime = findViewById(R.id.editExamTime)
        editLocation = findViewById(R.id.editLocation)
        editNotes = findViewById(R.id.editNotes)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        examDao = AppDatabase.getDatabase(this).examDao()

        // If an exam id was sent, we are editing an existing exam
        examId = intent.getIntExtra(EXTRA_EXAM_ID, 0)
        if (examId != 0) {
            tvFormTitle.text = getString(R.string.edit_exam)
            loadExam()
        }

        editExamDate.setOnClickListener { showDatePicker() }
        editExamTime.setOnClickListener { showTimePicker() }
        btnSave.setOnClickListener { saveExam() }
    }

    // Load the exam from the database and fill the form
    private fun loadExam() {
        AppDatabase.databaseExecutor.execute {
            val exam = examDao.getExamById(examId)
            runOnUiThread {
                if (exam != null) {
                    editSubject.setText(exam.subject)
                    selectedDate = exam.examDate
                    editExamDate.setText(exam.examDate)
                    selectedTime = exam.examTime
                    editExamTime.setText(exam.examTime)
                    editLocation.setText(exam.location)
                    editNotes.setText(exam.notes)
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)

        // If a date is already chosen, open the picker on that date
        if (selectedDate.isNotEmpty()) {
            val parts = selectedDate.split("-")
            year = parts[0].toInt()
            month = parts[1].toInt() - 1   // months start at 0 in the picker
            day = parts[2].toInt()
        }

        val datePicker = DatePickerDialog(this, { _, pickedYear, pickedMonth, pickedDay ->
            selectedDate = String.format(
                Locale.US, "%04d-%02d-%02d", pickedYear, pickedMonth + 1, pickedDay
            )
            editExamDate.setText(selectedDate)
            layoutDate.error = null
        }, year, month, day)
        datePicker.show()
    }

    private fun showTimePicker() {
        var hour = 9
        var minute = 0

        // If a time is already chosen, open the picker on that time
        if (selectedTime.isNotEmpty()) {
            val parts = selectedTime.split(":")
            hour = parts[0].toInt()
            minute = parts[1].toInt()
        }

        val timePicker = TimePickerDialog(this, { _, pickedHour, pickedMinute ->
            // Always saved as HH:mm so it is easy to sort
            selectedTime = String.format(Locale.US, "%02d:%02d", pickedHour, pickedMinute)
            editExamTime.setText(selectedTime)
        }, hour, minute, DateFormat.is24HourFormat(this))
        timePicker.show()
    }

    // CREATE or UPDATE
    private fun saveExam() {
        val subject = editSubject.text.toString().trim()

        // Validation: subject and date are required
        var isValid = true
        if (subject.isEmpty()) {
            layoutSubject.error = getString(R.string.error_subject_required)
            isValid = false
        } else {
            layoutSubject.error = null
        }
        if (selectedDate.isEmpty()) {
            layoutDate.error = getString(R.string.error_date_required)
            isValid = false
        } else {
            layoutDate.error = null
        }
        if (!isValid) {
            return
        }

        val exam = Exam(
            id = examId,
            subject = subject,
            examDate = selectedDate,
            examTime = selectedTime,
            location = editLocation.text.toString().trim(),
            notes = editNotes.text.toString().trim()
        )

        AppDatabase.databaseExecutor.execute {
            if (examId == 0) {
                examDao.insert(exam)
            } else {
                examDao.update(exam)
            }
            runOnUiThread {
                Toast.makeText(this, R.string.exam_saved, Toast.LENGTH_SHORT).show()
                finish()   // go back to the exam list
            }
        }
    }
}