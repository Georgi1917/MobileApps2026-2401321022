package bg.uni_plovdiv.fmi.my_project.ui.tasks

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.AppDatabase
import bg.uni_plovdiv.fmi.my_project.data.Task
import bg.uni_plovdiv.fmi.my_project.data.TaskDao
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import java.util.Locale

class TaskFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "task_id"
    }

    private lateinit var tvFormTitle: TextView
    private lateinit var layoutTitle: TextInputLayout
    private lateinit var editTitle: TextInputEditText
    private lateinit var editDescription: TextInputEditText
    private lateinit var editSubject: TextInputEditText
    private lateinit var editDeadline: TextInputEditText
    private lateinit var chipGroupPriority: ChipGroup
    private lateinit var taskDao: TaskDao

    private var taskId = 0                 // 0 means "new task"
    private var selectedDeadline = ""      // yyyy-MM-dd or empty
    private var isTaskCompleted = false    // keeps the completed state when editing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_form)

        tvFormTitle = findViewById(R.id.tvFormTitle)
        layoutTitle = findViewById(R.id.layoutTitle)
        editTitle = findViewById(R.id.editTitle)
        editDescription = findViewById(R.id.editDescription)
        editSubject = findViewById(R.id.editSubject)
        editDeadline = findViewById(R.id.editDeadline)
        chipGroupPriority = findViewById(R.id.chipGroupPriority)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        taskDao = AppDatabase.getDatabase(this).taskDao()

        // If a task id was sent, we are editing an existing task
        taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
        if (taskId != 0) {
            tvFormTitle.text = getString(R.string.edit_task)
            loadTask()
        }

        editDeadline.setOnClickListener { showDatePicker() }
        btnSave.setOnClickListener { saveTask() }
    }

    // Load the task from the database and fill the form
    private fun loadTask() {
        AppDatabase.databaseExecutor.execute {
            val task = taskDao.getTaskById(taskId)
            runOnUiThread {
                if (task != null) {
                    editTitle.setText(task.title)
                    editDescription.setText(task.description)
                    editSubject.setText(task.subject)
                    selectedDeadline = task.deadline
                    editDeadline.setText(task.deadline)
                    isTaskCompleted = task.isCompleted
                    when (task.priority) {
                        "Low" -> chipGroupPriority.check(R.id.chipLow)
                        "High" -> chipGroupPriority.check(R.id.chipHigh)
                        else -> chipGroupPriority.check(R.id.chipMedium)
                    }
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)

        // If a deadline is already chosen, open the picker on that date
        if (selectedDeadline.isNotEmpty()) {
            val parts = selectedDeadline.split("-")
            year = parts[0].toInt()
            month = parts[1].toInt() - 1   // months start at 0 in the picker
            day = parts[2].toInt()
        }

        val datePicker = DatePickerDialog(this, { _, pickedYear, pickedMonth, pickedDay ->
            selectedDeadline = String.format(
                Locale.US, "%04d-%02d-%02d", pickedYear, pickedMonth + 1, pickedDay
            )
            editDeadline.setText(selectedDeadline)
        }, year, month, day)
        datePicker.show()
    }

    private fun getSelectedPriority(): String {
        return when (chipGroupPriority.checkedChipId) {
            R.id.chipLow -> "Low"
            R.id.chipHigh -> "High"
            else -> "Medium"
        }
    }

    // CREATE or UPDATE
    private fun saveTask() {
        val title = editTitle.text.toString().trim()

        // Validation: the title cannot be empty
        if (title.isEmpty()) {
            layoutTitle.error = getString(R.string.error_title_required)
            return
        }
        layoutTitle.error = null

        val task = Task(
            id = taskId,
            title = title,
            description = editDescription.text.toString().trim(),
            subject = editSubject.text.toString().trim(),
            deadline = selectedDeadline,
            priority = getSelectedPriority(),
            isCompleted = isTaskCompleted
        )

        AppDatabase.databaseExecutor.execute {
            if (taskId == 0) {
                taskDao.insert(task)
            } else {
                taskDao.update(task)
            }
            runOnUiThread {
                Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show()
                finish()   // go back to the task list
            }
        }
    }
}