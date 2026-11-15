package bg.uni_plovdiv.fmi.my_project.ui.exams

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import bg.uni_plovdiv.fmi.my_project.utils.setupBottomNavigation
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.ExamDao
import bg.uni_plovdiv.fmi.my_project.data.Exam
import bg.uni_plovdiv.fmi.my_project.data.AppDatabase

class ExamsActivity : AppCompatActivity() {

    private lateinit var recyclerExams: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var examAdapter: ExamAdapter
    private lateinit var examDao: ExamDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exams)

        recyclerExams = findViewById(R.id.recyclerExams)
        tvEmpty = findViewById(R.id.tvEmpty)
        val fabAddExam = findViewById<ExtendedFloatingActionButton>(R.id.fabAddExam)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        examDao = AppDatabase.getDatabase(this).examDao()

        examAdapter = ExamAdapter(
            exams = emptyList(),
            onExamClick = { exam -> openExamForm(exam.id) },
            onDeleteClick = { exam -> confirmDelete(exam) }
        )
        recyclerExams.layoutManager = LinearLayoutManager(this)
        recyclerExams.adapter = examAdapter

        // New exam: open the form without an exam id
        fabAddExam.setOnClickListener {
            startActivity(Intent(this, ExamFormActivity::class.java))
        }

        setupBottomNavigation(this, bottomNavigation, R.id.nav_exams)
    }

    // Reload the list every time we come back from the form
    override fun onResume() {
        super.onResume()
        loadExams()
    }

    // READ: get all exams from the database (already sorted by the DAO)
    private fun loadExams() {
        AppDatabase.databaseExecutor.execute {
            val exams = examDao.getAllExams()
            runOnUiThread {
                examAdapter.updateExams(exams)
                tvEmpty.visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    // Edit an existing exam: the form receives the exam id
    private fun openExamForm(examId: Int) {
        val intent = Intent(this, ExamFormActivity::class.java)
        intent.putExtra(ExamFormActivity.EXTRA_EXAM_ID, examId)
        startActivity(intent)
    }

    // Ask before deleting
    private fun confirmDelete(exam: Exam) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_exam_title)
            .setMessage(getString(R.string.delete_exam_message, exam.subject))
            .setPositiveButton(R.string.delete) { _, _ -> deleteExam(exam) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // DELETE
    private fun deleteExam(exam: Exam) {
        AppDatabase.databaseExecutor.execute {
            examDao.delete(exam)
        }
        loadExams()
    }
}