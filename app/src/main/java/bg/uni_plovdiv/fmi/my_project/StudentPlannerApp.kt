package bg.uni_plovdiv.fmi.my_project

import android.app.Application
import bg.uni_plovdiv.fmi.my_project.utils.applySavedTheme

class StudentPlannerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Apply the theme the user chose last time
        applySavedTheme(this)
    }
}