package bg.uni_plovdiv.fmi.my_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ExamsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exams)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        setupBottomNavigation(this, bottomNavigation, R.id.nav_exams)
    }
}