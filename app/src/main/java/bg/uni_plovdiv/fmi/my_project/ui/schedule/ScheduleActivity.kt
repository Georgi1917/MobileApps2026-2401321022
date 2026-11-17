package bg.uni_plovdiv.fmi.my_project.ui.schedule

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.util.Calendar
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.data.ScheduleDao
import bg.uni_plovdiv.fmi.my_project.data.ScheduleItem
import bg.uni_plovdiv.fmi.my_project.data.AppDatabase
import bg.uni_plovdiv.fmi.my_project.utils.setupBottomNavigation

class ScheduleActivity : AppCompatActivity() {

    private lateinit var chipGroupDays: ChipGroup
    private lateinit var recyclerSchedule: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var scheduleDao: ScheduleDao

    // The day chips in order: the first chip is Monday = 1, the last is Sunday = 7
    private val dayChipIds = listOf(
        R.id.chipMon, R.id.chipTue, R.id.chipWed, R.id.chipThu,
        R.id.chipFri, R.id.chipSat, R.id.chipSun
    )

    private var selectedDay = 1   // 1 = Monday ... 7 = Sunday

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        chipGroupDays = findViewById(R.id.chipGroupDays)
        recyclerSchedule = findViewById(R.id.recyclerSchedule)
        tvEmpty = findViewById(R.id.tvEmpty)
        val fabAddClass = findViewById<ExtendedFloatingActionButton>(R.id.fabAddClass)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        scheduleDao = AppDatabase.getDatabase(this).scheduleDao()

        scheduleAdapter = ScheduleAdapter(
            items = emptyList(),
            onItemClick = { item -> openScheduleForm(item.id) },
            onDeleteClick = { item -> confirmDelete(item) }
        )
        recyclerSchedule.layoutManager = LinearLayoutManager(this)
        recyclerSchedule.adapter = scheduleAdapter

        // Start on today's day
        selectedDay = getTodayNumber()
        chipGroupDays.check(dayChipIds[selectedDay - 1])

        // When another day chip is tapped, show the classes of that day
        chipGroupDays.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                selectedDay = dayChipIds.indexOf(checkedIds[0]) + 1
                loadItems()
            }
        }

        // New class: open the form and preselect the day we are looking at
        fabAddClass.setOnClickListener {
            val intent = Intent(this, ScheduleFormActivity::class.java)
            intent.putExtra(ScheduleFormActivity.EXTRA_DAY, selectedDay)
            startActivity(intent)
        }

        setupBottomNavigation(this, bottomNavigation, R.id.nav_schedule)
    }

    // Reload the list every time we come back from the form
    override fun onResume() {
        super.onResume()
        loadItems()
    }

    // Android gives Sunday = 1 ... Saturday = 7. We want Monday = 1 ... Sunday = 7.
    private fun getTodayNumber(): Int {
        val androidDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return (androidDay + 5) % 7 + 1
    }

    // READ: get the classes of the selected day (already sorted by start time)
    private fun loadItems() {
        val day = selectedDay
        AppDatabase.databaseExecutor.execute {
            val items = scheduleDao.getItemsForDay(day)
            runOnUiThread {
                scheduleAdapter.updateItems(items)
                tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    // Edit an existing class: the form receives the item id
    private fun openScheduleForm(itemId: Int) {
        val intent = Intent(this, ScheduleFormActivity::class.java)
        intent.putExtra(ScheduleFormActivity.EXTRA_ITEM_ID, itemId)
        startActivity(intent)
    }

    // Ask before deleting
    private fun confirmDelete(item: ScheduleItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_class_title)
            .setMessage(getString(R.string.delete_class_message, item.subject))
            .setPositiveButton(R.string.delete) { _, _ -> deleteItem(item) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // DELETE
    private fun deleteItem(item: ScheduleItem) {
        AppDatabase.databaseExecutor.execute {
            scheduleDao.delete(item)
        }
        loadItems()
    }
}