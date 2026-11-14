package bg.uni_plovdiv.fmi.my_project.ui.tasks

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.Task

// Shows the list of tasks. The Activity decides what happens on each click.
class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit,
    private val onCompletedChange: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // Holds the views of one row
    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBoxCompleted: CheckBox = view.findViewById(R.id.checkBoxCompleted)
        val tvTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvSubject: TextView = view.findViewById(R.id.tvTaskSubject)
        val tvDeadline: TextView = view.findViewById(R.id.tvTaskDeadline)
        val tvPriority: TextView = view.findViewById(R.id.tvTaskPriority)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        val context = holder.itemView.context

        holder.tvTitle.text = task.title
        holder.tvSubject.text = task.subject.ifEmpty { context.getString(R.string.no_subject) }

        if (task.deadline.isEmpty()) {
            holder.tvDeadline.text = context.getString(R.string.no_deadline)
        } else {
            holder.tvDeadline.text = context.getString(R.string.due_date, task.deadline)
        }

        // Priority text with a color
        holder.tvPriority.text = task.priority
        val priorityColor = when (task.priority) {
            "High" -> R.color.priority_high
            "Low" -> R.color.priority_low
            else -> R.color.priority_medium
        }
        holder.tvPriority.setTextColor(ContextCompat.getColor(context, priorityColor))

        // Remove the old listener first, so setting isChecked does not trigger it
        holder.checkBoxCompleted.setOnCheckedChangeListener(null)
        holder.checkBoxCompleted.isChecked = task.isCompleted
        holder.checkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
            onCompletedChange(task, isChecked)
        }

        // Completed tasks: crossed-out title and slightly faded row
        if (task.isCompleted) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.alpha = 0.6f
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemView.alpha = 1f
        }

        holder.itemView.setOnClickListener { onTaskClick(task) }
        holder.btnDelete.setOnClickListener { onDeleteClick(task) }
    }

    // Called by the Activity when the list from the database changes
    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}