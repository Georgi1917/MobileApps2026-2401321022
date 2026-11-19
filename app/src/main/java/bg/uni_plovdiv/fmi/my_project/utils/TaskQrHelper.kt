package bg.uni_plovdiv.fmi.my_project.utils

import bg.uni_plovdiv.fmi.my_project.data.Task
import org.json.JSONException
import org.json.JSONObject

// Every QR code made by this app contains this word, so we can recognize our own codes
private const val QR_APP_MARKER = "StudentPlannerTask"

// Turns the task information into the text that goes inside the QR code
fun taskToQrText(
    title: String,
    subject: String,
    description: String,
    deadline: String,
    priority: String
): String {
    val json = JSONObject()
    json.put("app", QR_APP_MARKER)
    json.put("title", title)
    json.put("subject", subject)
    json.put("description", description)
    json.put("deadline", deadline)
    json.put("priority", priority)
    return json.toString()
}

// Turns scanned QR text back into a Task.
// Returns null if the text is not a valid Student Planner task.
fun qrTextToTask(text: String): Task? {
    try {
        val json = JSONObject(text)

        // Must be one of our QR codes
        if (json.optString("app") != QR_APP_MARKER) {
            return null
        }

        // A task always needs a title
        val title = json.optString("title").trim()
        if (title.isEmpty()) {
            return null
        }

        // The deadline must be empty or look like 2026-11-20
        // (the rest of the app reads deadlines in this format)
        val deadline = json.optString("deadline").trim()
        if (deadline.isNotEmpty() && !deadline.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            return null
        }

        // Only these three priorities exist, anything else becomes Medium
        var priority = json.optString("priority")
        if (priority != "Low" && priority != "Medium" && priority != "High") {
            priority = "Medium"
        }

        // id = 0, so Room creates a NEW id and never overwrites an existing task
        return Task(
            title = title,
            description = json.optString("description").trim(),
            subject = json.optString("subject").trim(),
            deadline = deadline,
            priority = priority,
            isCompleted = false
        )
    } catch (e: JSONException) {
        // The text was not JSON at all (for example a QR code of a website)
        return null
    }
}
