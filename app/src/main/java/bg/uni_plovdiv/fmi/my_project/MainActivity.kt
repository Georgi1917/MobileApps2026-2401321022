package bg.uni_plovdiv.fmi.my_project

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val btnOpenTasks = findViewById<MaterialButton>(R.id.btnOpenTasks)
        val btnOpenExams = findViewById<MaterialButton>(R.id.btnOpenExams)
        val btnOpenSchedule = findViewById<MaterialButton>(R.id.btnOpenSchedule)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Greeting depends on the time of day
        tvGreeting.text = getGreetingText()

        // Today's date, for example "Friday, November 13"
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())

        // Card buttons open the other screens with Intents
        btnOpenTasks.setOnClickListener {
            startActivity(Intent(this, TasksActivity::class.java))
        }
        btnOpenExams.setOnClickListener {
            startActivity(Intent(this, ExamsActivity::class.java))
        }
        btnOpenSchedule.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
        }

        setupBottomNavigation(this, bottomNavigation, R.id.nav_home)
    }

    private fun getGreetingText(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> getString(R.string.greeting_morning)
            hour < 18 -> getString(R.string.greeting_afternoon)
            else -> getString(R.string.greeting_evening)
        }
    }
}