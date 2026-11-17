package bg.uni_plovdiv.fmi.my_project.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.AppDatabase
import bg.uni_plovdiv.fmi.my_project.data.Exam
import bg.uni_plovdiv.fmi.my_project.data.Task
import bg.uni_plovdiv.fmi.my_project.data.ExamDao
import bg.uni_plovdiv.fmi.my_project.data.TaskDao
import bg.uni_plovdiv.fmi.my_project.ui.schedule.ScheduleActivity
import bg.uni_plovdiv.fmi.my_project.utils.setupBottomNavigation
import bg.uni_plovdiv.fmi.my_project.ui.tasks.TasksActivity
import bg.uni_plovdiv.fmi.my_project.ui.exams.ExamsActivity
import bg.uni_plovdiv.fmi.my_project.ui.statistics.StatisticsActivity
import bg.uni_plovdiv.fmi.my_project.utils.getFriendlyDaysText

class MainActivity : AppCompatActivity() {

    private lateinit var tvTotalTasks: TextView
    private lateinit var tvCompletedTasks: TextView
    private lateinit var tvPendingTasks: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvClosestDeadlines: TextView
    private lateinit var tvUpcomingExams: TextView
    private lateinit var progressTasks: LinearProgressIndicator
    private lateinit var taskDao: TaskDao
    private lateinit var examDao: ExamDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val btnOpenTasks = findViewById<MaterialButton>(R.id.btnOpenTasks)
        val btnStatistics = findViewById<MaterialButton>(R.id.btnStatistics)
        val btnOpenExams = findViewById<MaterialButton>(R.id.btnOpenExams)
        val btnOpenSchedule = findViewById<MaterialButton>(R.id.btnOpenSchedule)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        tvTotalTasks = findViewById(R.id.tvTotalTasks)
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks)
        tvPendingTasks = findViewById(R.id.tvPendingTasks)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        tvClosestDeadlines = findViewById(R.id.tvClosestDeadlines)
        tvUpcomingExams = findViewById(R.id.tvUpcomingExams)
        progressTasks = findViewById(R.id.progressTasks)

        taskDao = AppDatabase.getDatabase(this).taskDao()
        examDao = AppDatabase.getDatabase(this).examDao()

        // Greeting depends on the time of day
        tvGreeting.text = getGreetingText()

        // Today's date, for example "Friday, November 13"
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())

        // Buttons open the other screens with Intents
        btnOpenTasks.setOnClickListener {
            startActivity(Intent(this, TasksActivity::class.java))
        }
        btnStatistics.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
        btnOpenExams.setOnClickListener {
            startActivity(Intent(this, ExamsActivity::class.java))
        }
        btnOpenSchedule.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
        }

        setupBottomNavigation(this, bottomNavigation, R.id.nav_home)
    }

    // Reload the numbers every time the dashboard is shown again
    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun getGreetingText(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> getString(R.string.greeting_morning)
            hour < 18 -> getString(R.string.greeting_afternoon)
            else -> getString(R.string.greeting_evening)
        }
    }

    // READ: get tasks and exams from the database, then fill the dashboard
    private fun loadDashboardData() {
        // Today as yyyy-MM-dd, the same format used to save deadlines and exam dates
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        AppDatabase.databaseExecutor.execute {
            val tasks = taskDao.getAllTasks()
            val exams = examDao.getAllExams()

            runOnUiThread {
                showTaskNumbers(tasks)
                showClosestDeadlines(tasks)
                showUpcomingExams(exams, today)
            }
        }
    }

    private fun showTaskNumbers(tasks: List<Task>) {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.isCompleted }
        val pendingTasks = totalTasks - completedTasks

        // Percentage of completed tasks (0 when there are no tasks yet)
        val percent = if (totalTasks == 0) 0 else completedTasks * 100 / totalTasks

        tvTotalTasks.text = totalTasks.toString()
        tvCompletedTasks.text = completedTasks.toString()
        tvPendingTasks.text = pendingTasks.toString()
        progressTasks.progress = percent
        tvProgressPercent.text = getString(R.string.progress_completed, percent)
    }

    // The 3 unfinished tasks with the earliest deadlines
    private fun showClosestDeadlines(tasks: List<Task>) {
        val closestTasks = tasks
            .filter { !it.isCompleted && it.deadline.isNotEmpty() }
            .sortedBy { it.deadline }
            .take(3)

        if (closestTasks.isEmpty()) {
            tvClosestDeadlines.text = getString(R.string.no_upcoming_deadlines)
            return
        }

        val lines = closestTasks.map { task ->
            // For example "• Math homework · In 3 days" or "• Essay · 2 days overdue"
            getString(R.string.dashboard_item_line, task.title, getFriendlyDaysText(this, task.deadline))
        }
        tvClosestDeadlines.text = lines.joinToString("\n")
    }

    // The next 3 exams (exams from today on). getAllExams() is already sorted by date.
    private fun showUpcomingExams(exams: List<Exam>, today: String) {
        val upcomingExams = exams
            .filter { it.examDate >= today }
            .take(3)

        if (upcomingExams.isEmpty()) {
            tvUpcomingExams.text = getString(R.string.no_upcoming_exams)
            return
        }

        val lines = upcomingExams.map { exam ->
            // For example "• Math · Tomorrow, 09:30"
            var whenText = getFriendlyDaysText(this, exam.examDate)
            if (exam.examTime.isNotEmpty()) {
                whenText += ", " + exam.examTime
            }
            getString(R.string.dashboard_item_line, exam.subject, whenText)
        }
        tvUpcomingExams.text = lines.joinToString("\n")
    }
}