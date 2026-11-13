package bg.uni_plovdiv.fmi.my_project

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

// Connects the bottom navigation bar to the 5 screens using simple Intents
fun setupBottomNavigation(
    activity: Activity,
    bottomNavigation: BottomNavigationView,
    currentItemId: Int
) {
    // Highlight the item of the screen we are currently on
    bottomNavigation.selectedItemId = currentItemId

    bottomNavigation.setOnItemSelectedListener { item ->
        // Already on this screen, do nothing
        if (item.itemId == currentItemId) {
            return@setOnItemSelectedListener true
        }

        val targetActivity = when (item.itemId) {
            R.id.nav_home -> MainActivity::class.java
            R.id.nav_tasks -> TasksActivity::class.java
            R.id.nav_exams -> ExamsActivity::class.java
            R.id.nav_schedule -> ScheduleActivity::class.java
            R.id.nav_settings -> SettingsActivity::class.java
            else -> null
        }

        if (targetActivity != null) {
            val intent = Intent(activity, targetActivity)
            // If the screen is already open, bring it to the front instead of creating a copy
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            activity.startActivity(intent)
        }
        true
    }
}