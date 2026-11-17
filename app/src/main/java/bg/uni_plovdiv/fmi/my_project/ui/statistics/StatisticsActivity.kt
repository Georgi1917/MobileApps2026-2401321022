package bg.uni_plovdiv.fmi.my_project.ui.statistics

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.AppDatabase
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var tvStatTotalTasks: TextView
    private lateinit var tvStatCompletedTasks: TextView
    private lateinit var tvStatPendingTasks: TextView
    private lateinit var tvStatOverdueTasks: TextView
    private lateinit var tvStatTotalExams: TextView
    private lateinit var tvCompletionPercent: TextView
    private lateinit var progressCompletion: LinearProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        tvStatTotalTasks = findViewById(R.id.tvStatTotalTasks)
        tvStatCompletedTasks = findViewById(R.id.tvStatCompletedTasks)
        tvStatPendingTasks = findViewById(R.id.tvStatPendingTasks)
        tvStatOverdueTasks = findViewById(R.id.tvStatOverdueTasks)
        tvStatTotalExams = findViewById(R.id.tvStatTotalExams)
        tvCompletionPercent = findViewById(R.id.tvCompletionPercent)
        progressCompletion = findViewById(R.id.progressCompletion)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        loadStatistics()
    }

    // READ: get tasks and exams from the database and count them
    private fun loadStatistics() {
        val taskDao = AppDatabase.getDatabase(this).taskDao()
        val examDao = AppDatabase.getDatabase(this).examDao()

        // Today as yyyy-MM-dd, the same format used to save deadlines
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        AppDatabase.databaseExecutor.execute {
            val tasks = taskDao.getAllTasks()
            val exams = examDao.getAllExams()

            val totalTasks = tasks.size
            val completedTasks = tasks.count { it.isCompleted }
            val pendingTasks = totalTasks - completedTasks

            // Overdue = not completed, has a deadline, and the deadline is before today
            val overdueTasks = tasks.count {
                !it.isCompleted && it.deadline.isNotEmpty() && it.deadline < today
            }

            val percent = if (totalTasks == 0) 0 else completedTasks * 100 / totalTasks

            runOnUiThread {
                tvStatTotalTasks.text = totalTasks.toString()
                tvStatCompletedTasks.text = completedTasks.toString()
                tvStatPendingTasks.text = pendingTasks.toString()
                tvStatOverdueTasks.text = overdueTasks.toString()
                val upcomingExams = exams.count { it.examDate >= today }
                tvStatTotalExams.text = upcomingExams.toString()
                progressCompletion.progress = percent
                tvCompletionPercent.text = getString(R.string.progress_completed, percent)
            }
        }
    }
}