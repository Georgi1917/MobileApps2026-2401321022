package bg.uni_plovdiv.fmi.my_project.utils

import android.content.Context
import bg.uni_plovdiv.fmi.my_project.R
import java.util.Calendar
import kotlin.math.roundToInt

// Counts the days from today until the given date (yyyy-MM-dd).
// 0 = today, 1 = tomorrow, negative = the date already passed.
fun getDaysUntil(date: String): Int {
    val parts = date.split("-")

    val targetDay = Calendar.getInstance()
    targetDay.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
    targetDay.set(Calendar.MILLISECOND, 0)

    // Today at midnight
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val millisPerDay = 24 * 60 * 60 * 1000.0
    return ((targetDay.timeInMillis - today.timeInMillis) / millisPerDay).roundToInt()
}

// Turns a date into friendly text: "Today", "Tomorrow", "In 5 days", "3 days overdue"
fun getFriendlyDaysText(context: Context, date: String): String {
    val days = getDaysUntil(date)
    return when {
        days < -1 -> context.getString(R.string.friendly_overdue_days, -days)
        days == -1 -> context.getString(R.string.friendly_overdue_one)
        days == 0 -> context.getString(R.string.friendly_today)
        days == 1 -> context.getString(R.string.friendly_tomorrow)
        else -> context.getString(R.string.friendly_in_days, days)
    }
}