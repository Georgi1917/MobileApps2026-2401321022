package bg.uni_plovdiv.fmi.my_project.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.AppDatabase
import bg.uni_plovdiv.fmi.my_project.data.Task
import bg.uni_plovdiv.fmi.my_project.data.TaskDao
import bg.uni_plovdiv.fmi.my_project.utils.qrTextToTask
import bg.uni_plovdiv.fmi.my_project.utils.setupBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class TasksActivity : AppCompatActivity() {

    private lateinit var recyclerTasks: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskDao: TaskDao

    // QR SCANNER: this runs when the scanner screen closes
    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            // Something was scanned
            handleScannedText(result.contents)
        } else {
            // Nothing scanned: the user went back, or denied the camera permission
            val scanIntent = result.originalIntent
            if (scanIntent != null && scanIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Toast.makeText(this, R.string.camera_permission_needed, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        recyclerTasks = findViewById(R.id.recyclerTasks)
        tvEmpty = findViewById(R.id.tvEmpty)
        val fabAddTask = findViewById<ExtendedFloatingActionButton>(R.id.fabAddTask)
        val btnScanQr = findViewById<MaterialButton>(R.id.btnScanQr)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        taskDao = AppDatabase.getDatabase(this).taskDao()

        taskAdapter = TaskAdapter(
            tasks = emptyList(),
            onTaskClick = { task -> openTaskForm(task.id) },
            onDeleteClick = { task -> confirmDelete(task) },
            onCompletedChange = { task, isChecked -> updateCompleted(task, isChecked) }
        )
        recyclerTasks.layoutManager = LinearLayoutManager(this)
        recyclerTasks.adapter = taskAdapter

        // New task: open the form without a task id
        fabAddTask.setOnClickListener {
            startActivity(Intent(this, TaskFormActivity::class.java))
        }

        btnScanQr.setOnClickListener { startScanner() }

        setupBottomNavigation(this, bottomNavigation, R.id.nav_tasks)
    }

    // Reload the list every time we come back from the form
    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    // READ: get all tasks from the database
    private fun loadTasks() {
        AppDatabase.databaseExecutor.execute {
            val tasks = taskDao.getAllTasks()
            runOnUiThread {
                taskAdapter.updateTasks(tasks)
                tvEmpty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    // Edit an existing task: the form receives the task id
    private fun openTaskForm(taskId: Int) {
        val intent = Intent(this, TaskFormActivity::class.java)
        intent.putExtra(TaskFormActivity.EXTRA_TASK_ID, taskId)
        startActivity(intent)
    }

    // UPDATE: mark a task as completed / not completed
    private fun updateCompleted(task: Task, isCompleted: Boolean) {
        AppDatabase.databaseExecutor.execute {
            taskDao.update(task.copy(isCompleted = isCompleted))
        }
        loadTasks()
    }

    // Ask before deleting
    private fun confirmDelete(task: Task) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_task_title)
            .setMessage(getString(R.string.delete_task_message, task.title))
            .setPositiveButton(R.string.delete) { _, _ -> deleteTask(task) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // DELETE
    private fun deleteTask(task: Task) {
        AppDatabase.databaseExecutor.execute {
            taskDao.delete(task)
        }
        loadTasks()
    }

    // ---------- QR CODE SCANNING ----------

    // Opens the camera scanner screen
    private fun startScanner() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)   // only QR codes
        options.setPrompt(getString(R.string.scan_prompt))
        options.setBeepEnabled(false)
        options.setOrientationLocked(false)                     // allow portrait
        scanLauncher.launch(options)
    }

    // Read the scanned text and turn it into a task
    private fun handleScannedText(text: String) {
        val task = qrTextToTask(text)
        if (task == null) {
            showInvalidQrDialog()
        } else {
            showImportDialog(task)
        }
    }

    private fun showInvalidQrDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.qr_invalid_title)
            .setMessage(R.string.qr_invalid_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    // Preview of the scanned task, with the question if it should be imported
    private fun showImportDialog(task: Task) {
        val previewText = getString(
            R.string.import_preview,
            task.title,
            task.subject.ifEmpty { getString(R.string.no_subject) },
            task.deadline.ifEmpty { getString(R.string.no_deadline) },
            task.priority,
            task.description.ifEmpty { getString(R.string.no_description) }
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.import_task_title)
            .setMessage(previewText)
            .setPositiveButton(R.string.import_task) { _, _ -> importTask(task) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // CREATE: the task has id 0, so Room gives it a new id and nothing is overwritten
    private fun importTask(task: Task) {
        AppDatabase.databaseExecutor.execute {
            taskDao.insert(task)
        }
        Toast.makeText(this, R.string.task_imported, Toast.LENGTH_SHORT).show()
        loadTasks()
    }
}
