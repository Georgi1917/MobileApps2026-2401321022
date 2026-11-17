package bg.uni_plovdiv.fmi.my_project.ui.schedule

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.ScheduleDao
import bg.uni_plovdiv.fmi.my_project.data.ScheduleItem
import bg.uni_plovdiv.fmi.my_project.data.AppDatabase

class ScheduleFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_DAY = "day"
    }

    private lateinit var tvFormTitle: TextView
    private lateinit var layoutSubject: TextInputLayout
    private lateinit var layoutStartTime: TextInputLayout
    private lateinit var layoutEndTime: TextInputLayout
    private lateinit var editSubject: TextInputEditText
    private lateinit var dropdownDay: MaterialAutoCompleteTextView
    private lateinit var editStartTime: TextInputEditText
    private lateinit var editEndTime: TextInputEditText
    private lateinit var editRoom: TextInputEditText
    private lateinit var editTeacher: TextInputEditText
    private lateinit var scheduleDao: ScheduleDao
    private lateinit var dayNames: Array<String>

    private var itemId = 0              // 0 means "new class"
    private var selectedDay = 1         // 1 = Monday ... 7 = Sunday
    private var selectedStartTime = ""  // HH:mm or empty
    private var selectedEndTime = ""    // HH:mm or empty

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_form)

        tvFormTitle = findViewById(R.id.tvFormTitle)
        layoutSubject = findViewById(R.id.layoutSubject)
        layoutStartTime = findViewById(R.id.layoutStartTime)
        layoutEndTime = findViewById(R.id.layoutEndTime)
        editSubject = findViewById(R.id.editSubject)
        dropdownDay = findViewById(R.id.dropdownDay)
        editStartTime = findViewById(R.id.editStartTime)
        editEndTime = findViewById(R.id.editEndTime)
        editRoom = findViewById(R.id.editRoom)
        editTeacher = findViewById(R.id.editTeacher)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        scheduleDao = AppDatabase.getDatabase(this).scheduleDao()

        // Fill the day dropdown with Monday ... Sunday
        dayNames = resources.getStringArray(R.array.days_of_week)
        dropdownDay.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dayNames)
        )
        dropdownDay.setOnItemClickListener { _, _, position, _ ->
            selectedDay = position + 1
        }

        // For a new class, start with the day that was open in the schedule
        selectedDay = intent.getIntExtra(EXTRA_DAY, 1)
        showSelectedDay()

        // If an item id was sent, we are editing an existing class
        itemId = intent.getIntExtra(EXTRA_ITEM_ID, 0)
        if (itemId != 0) {
            tvFormTitle.text = getString(R.string.edit_class)
            loadItem()
        }

        editStartTime.setOnClickListener {
            showTimePicker(selectedStartTime, "08:00") { time ->
                selectedStartTime = time
                editStartTime.setText(time)
                layoutStartTime.error = null
            }
        }
        editEndTime.setOnClickListener {
            showTimePicker(selectedEndTime, "09:00") { time ->
                selectedEndTime = time
                editEndTime.setText(time)
                layoutEndTime.error = null
            }
        }
        btnSave.setOnClickListener { saveItem() }
    }

    // Shows the selected day in the dropdown.
    // "false" = do not filter the dropdown list while setting the text.
    private fun showSelectedDay() {
        dropdownDay.setText(dayNames[selectedDay - 1], false)
    }

    // Load the class from the database and fill the form
    private fun loadItem() {
        AppDatabase.databaseExecutor.execute {
            val item = scheduleDao.getItemById(itemId)
            runOnUiThread {
                if (item != null) {
                    editSubject.setText(item.subject)
                    selectedDay = item.dayOfWeek
                    showSelectedDay()
                    selectedStartTime = item.startTime
                    editStartTime.setText(item.startTime)
                    selectedEndTime = item.endTime
                    editEndTime.setText(item.endTime)
                    editRoom.setText(item.room)
                    editTeacher.setText(item.teacher)
                }
            }
        }
    }

    // Opens a TimePickerDialog. "currentTime" is the time already chosen (or empty),
    // "defaultTime" is used when nothing is chosen yet.
    private fun showTimePicker(currentTime: String, defaultTime: String, onTimePicked: (String) -> Unit) {
        val startValue = if (currentTime.isNotEmpty()) currentTime else defaultTime
        val parts = startValue.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val timePicker = TimePickerDialog(this, { _, pickedHour, pickedMinute ->
            // Always saved as HH:mm so it is easy to sort
            onTimePicked(String.format(Locale.US, "%02d:%02d", pickedHour, pickedMinute))
        }, hour, minute, true)
        timePicker.show()
    }

    // CREATE or UPDATE
    private fun saveItem() {
        val subject = editSubject.text.toString().trim()

        // Validation: subject, start time and end time are required
        var isValid = true

        if (subject.isEmpty()) {
            layoutSubject.error = getString(R.string.error_subject_required)
            isValid = false
        } else {
            layoutSubject.error = null
        }

        if (selectedStartTime.isEmpty()) {
            layoutStartTime.error = getString(R.string.error_start_time_required)
            isValid = false
        } else {
            layoutStartTime.error = null
        }

        if (selectedEndTime.isEmpty()) {
            layoutEndTime.error = getString(R.string.error_end_time_required)
            isValid = false
        } else if (selectedStartTime.isNotEmpty() && selectedEndTime <= selectedStartTime) {
            // Both are HH:mm, so comparing them as text compares the times
            layoutEndTime.error = getString(R.string.error_end_before_start)
            isValid = false
        } else {
            layoutEndTime.error = null
        }

        if (!isValid) {
            return
        }

        val item = ScheduleItem(
            id = itemId,
            subject = subject,
            dayOfWeek = selectedDay,
            startTime = selectedStartTime,
            endTime = selectedEndTime,
            room = editRoom.text.toString().trim(),
            teacher = editTeacher.text.toString().trim()
        )

        AppDatabase.databaseExecutor.execute {
            if (itemId == 0) {
                scheduleDao.insert(item)
            } else {
                scheduleDao.update(item)
            }
            runOnUiThread {
                Toast.makeText(this, R.string.class_saved, Toast.LENGTH_SHORT).show()
                finish()   // go back to the schedule
            }
        }
    }
}