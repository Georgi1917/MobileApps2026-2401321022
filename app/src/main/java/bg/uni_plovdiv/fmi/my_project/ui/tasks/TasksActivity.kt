package bg.uni_plovdiv.fmi.my_project.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.AppDatabase
import bg.uni_plovdiv.fmi.my_project.data.Task
import bg.uni_plovdiv.fmi.my_project.data.TaskDao
import bg.uni_plovdiv.fmi.my_project.utils.setupBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class TasksActivity : AppCompatActivity() {

    private lateinit var recyclerTasks: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskDao: TaskDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        recyclerTasks = findViewById(R.id.recyclerTasks)
        tvEmpty = findViewById(R.id.tvEmpty)
        val fabAddTask = findViewById<ExtendedFloatingActionButton>(R.id.fabAddTask)
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
}