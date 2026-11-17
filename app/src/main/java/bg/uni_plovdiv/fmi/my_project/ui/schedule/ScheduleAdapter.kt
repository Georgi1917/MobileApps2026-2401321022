package bg.uni_plovdiv.fmi.my_project.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.ScheduleItem

// Shows the classes of one day. The Activity decides what happens on each click.
class ScheduleAdapter(
    private var items: List<ScheduleItem>,
    private val onItemClick: (ScheduleItem) -> Unit,
    private val onDeleteClick: (ScheduleItem) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    // Holds the views of one row
    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStartTime: TextView = view.findViewById(R.id.tvStartTime)
        val tvEndTime: TextView = view.findViewById(R.id.tvEndTime)
        val tvSubject: TextView = view.findViewById(R.id.tvClassSubject)
        val tvRoom: TextView = view.findViewById(R.id.tvClassRoom)
        val tvTeacher: TextView = view.findViewById(R.id.tvClassTeacher)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteClass)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.tvStartTime.text = item.startTime
        holder.tvEndTime.text = item.endTime
        holder.tvSubject.text = item.subject

        if (item.room.isEmpty()) {
            holder.tvRoom.text = context.getString(R.string.no_room)
        } else {
            holder.tvRoom.text = context.getString(R.string.schedule_room, item.room)
        }

        if (item.teacher.isEmpty()) {
            holder.tvTeacher.text = context.getString(R.string.no_teacher)
        } else {
            holder.tvTeacher.text = context.getString(R.string.schedule_teacher, item.teacher)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    // Called by the Activity when the list from the database changes
    fun updateItems(newItems: List<ScheduleItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}