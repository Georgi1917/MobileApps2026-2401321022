package bg.uni_plovdiv.fmi.my_project.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

private const val PREFS_NAME = "student_planner_settings"
private const val KEY_THEME_MODE = "theme_mode"

// Returns the saved theme:
// MODE_NIGHT_NO = light, MODE_NIGHT_YES = dark, MODE_NIGHT_FOLLOW_SYSTEM = system default
fun getSavedThemeMode(context: Context): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

// Applies the saved theme (called when the app starts)
fun applySavedTheme(context: Context) {
    AppCompatDelegate.setDefaultNightMode(getSavedThemeMode(context))
}

// Saves the new theme and applies it right away
fun saveThemeMode(context: Context, mode: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    AppCompatDelegate.setDefaultNightMode(mode)
}