package bg.uni_plovdiv.fmi.my_project.ui.settings

import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.utils.getSavedThemeMode
import bg.uni_plovdiv.fmi.my_project.utils.saveThemeMode
import bg.uni_plovdiv.fmi.my_project.utils.setupBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val radioGroupTheme = findViewById<RadioGroup>(R.id.radioGroupTheme)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Show the theme that is currently saved
        when (getSavedThemeMode(this)) {
            AppCompatDelegate.MODE_NIGHT_NO -> radioGroupTheme.check(R.id.radioLight)
            AppCompatDelegate.MODE_NIGHT_YES -> radioGroupTheme.check(R.id.radioDark)
            else -> radioGroupTheme.check(R.id.radioSystem)
        }

        // Save and apply the new theme when the user picks another option
        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val newMode = when (checkedId) {
                R.id.radioLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radioDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            saveThemeMode(this, newMode)
        }

        setupBottomNavigation(this, bottomNavigation, R.id.nav_settings)
    }
}