package bg.uni_plovdiv.fmi.my_project.ui.exams

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import kotlin.math.roundToInt
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.Exam

// Shows the list of exams. The Activity decides what happens on each click.
class ExamAdapter(
    private var exams: List<Exam>,
    private val onExamClick: (Exam) -> Unit,
    private val onDeleteClick: (Exam) -> Unit
) : RecyclerView.Adapter<ExamAdapter.ExamViewHolder>() {

    // Holds the views of one row
    class ExamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubject: TextView = view.findViewById(R.id.tvExamSubject)
        val tvDaysRemaining: TextView = view.findViewById(R.id.tvDaysRemaining)
        val tvDate: TextView = view.findViewById(R.id.tvExamDate)
        val tvTime: TextView = view.findViewById(R.id.tvExamTime)
        val tvLocation: TextView = view.findViewById(R.id.tvExamLocation)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteExam)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exam, parent, false)
        return ExamViewHolder(view)
    }

    override fun getItemCount(): Int = exams.size

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = exams[position]
        val context = holder.itemView.context

        holder.tvSubject.text = exam.subject
        holder.tvDate.text = exam.examDate
        holder.tvTime.text = exam.examTime.ifEmpty { context.getString(R.string.no_time) }
        holder.tvLocation.text = exam.location.ifEmpty { context.getString(R.string.no_location) }

        // Days remaining text and color
        val daysRemaining = getDaysRemaining(exam.examDate)
        when {
            daysRemaining < 0 -> {
                holder.tvDaysRemaining.text = context.getString(R.string.exam_passed)
                holder.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.planner_text_secondary))
            }
            daysRemaining == 0 -> {
                holder.tvDaysRemaining.text = context.getString(R.string.exam_today)
                holder.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.priority_high))
            }
            daysRemaining == 1 -> {
                holder.tvDaysRemaining.text = context.getString(R.string.exam_tomorrow)
                holder.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.priority_high))
            }
            else -> {
                holder.tvDaysRemaining.text = context.getString(R.string.exam_in_days, daysRemaining)
                // Red when the exam is within a week, normal color otherwise
                val colorRes = if (daysRemaining <= 7) R.color.priority_high else R.color.primary
                holder.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, colorRes))
            }
        }

        holder.itemView.setOnClickListener { onExamClick(exam) }
        holder.btnDelete.setOnClickListener { onDeleteClick(exam) }
    }

    // Counts the days from today until the exam date (negative if it already passed)
    private fun getDaysRemaining(examDate: String): Int {
        val parts = examDate.split("-")

        val examDay = Calendar.getInstance()
        examDay.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
        examDay.set(Calendar.MILLISECOND, 0)

        // Today at midnight
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val millisPerDay = 24 * 60 * 60 * 1000.0
        return ((examDay.timeInMillis - today.timeInMillis) / millisPerDay).roundToInt()
    }

    // Called by the Activity when the list from the database changes
    fun updateExams(newExams: List<Exam>) {
        exams = newExams
        notifyDataSetChanged()
    }
}
