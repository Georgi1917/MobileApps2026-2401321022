package bg.uni_plovdiv.fmi.my_project.ui.tasks

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bg.uni_plovdiv.fmi.my_project.R
import bg.uni_plovdiv.fmi.my_project.utils.taskToQrText
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.journeyapps.barcodescanner.BarcodeEncoder

// Shows the QR code of one task
class TaskQrActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "qr_title"
        const val EXTRA_SUBJECT = "qr_subject"
        const val EXTRA_DESCRIPTION = "qr_description"
        const val EXTRA_DEADLINE = "qr_deadline"
        const val EXTRA_PRIORITY = "qr_priority"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_qr)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvTaskTitle = findViewById<TextView>(R.id.tvTaskTitle)
        val cardQr = findViewById<View>(R.id.cardQr)
        val imageQr = findViewById<ImageView>(R.id.imageQr)
        val tvQrError = findViewById<TextView>(R.id.tvQrError)

        btnBack.setOnClickListener { finish() }

        // Read the task information sent by the form
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val subject = intent.getStringExtra(EXTRA_SUBJECT) ?: ""
        val description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: ""
        val deadline = intent.getStringExtra(EXTRA_DEADLINE) ?: ""
        val priority = intent.getStringExtra(EXTRA_PRIORITY) ?: "Medium"

        tvTaskTitle.text = title

        val qrText = taskToQrText(title, subject, description, deadline, priority)

        try {
            // UTF-8 makes sure letters like Cyrillic or "é" survive the QR code
            val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
            val bitmap = BarcodeEncoder().encodeBitmap(qrText, BarcodeFormat.QR_CODE, 800, 800, hints)
            imageQr.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // Happens when the text is too long to fit into a QR code
            cardQr.visibility = View.GONE
            tvQrError.visibility = View.VISIBLE
        }
    }
}
