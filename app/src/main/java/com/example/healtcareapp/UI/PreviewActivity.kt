package com.example.healtcareapp.UI

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.healtcareapp.R
import com.example.healtcareapp.helper.LabReportParser
import com.example.healtcareapp.helper.OcrTextExtractor
import com.example.healtcareapp.helper.ReportAnalyzer
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

class PreviewActivity : AppCompatActivity() {

    private lateinit var btnAnalyze: Button
    private var pdfUrl: String? = null
    private var pdfName: String? = null
    private var localPdfUri: Uri? = null

    // ✅ Your Firebase bucket (fixed to match console)
    private val firebaseStorage =
        FirebaseStorage.getInstance("gs://health-care-e9c9d.firebasestorage.app")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        btnAnalyze = findViewById(R.id.btnAnalyze)
        btnAnalyze.isEnabled = false // disabled until download completes

        pdfUrl = intent.getStringExtra("pdfUrl")
        pdfName = intent.getStringExtra("pdfName")

        if (pdfUrl == null) {
            Toast.makeText(this, "No PDF URL found!", Toast.LENGTH_SHORT).show()
            return
        }
        downloadPdfForAnalysis(pdfUrl!!)
    }

    /**
     * ✅ Custom reusable progress dialog
     */
    private fun showProgressDialog(message: String): AlertDialog {
        val dialogView = layoutInflater.inflate(R.layout.dialog_progress, null)
        val text = dialogView.findViewById<TextView>(R.id.tvProgressText)
        val progress = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        text.text = message
        progress.isIndeterminate = true

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()
        return dialog
    }

    /**
     * ✅ Download PDF for analysis
     */
    private fun downloadPdfForAnalysis(url: String) {
        try {
            val storageRef = firebaseStorage.getReferenceFromUrl(url)
            val localFile = File.createTempFile("tempReport", ".pdf")

            val downloadDialog = showProgressDialog("Downloading your report...")

            storageRef.getFile(localFile)
                .addOnSuccessListener {
                    downloadDialog.dismiss()
                    localPdfUri = Uri.fromFile(localFile)

                    // ✅ Show ready popup
                    AlertDialog.Builder(this)
                        .setTitle("Report Ready ✅")
                        .setMessage("Your report has been downloaded successfully. You can now analyze it.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            btnAnalyze.isEnabled = true
                        }
                        .show()
                }
                .addOnFailureListener {
                    downloadDialog.dismiss()
                    Toast.makeText(this, "❌ Failed to download: ${it.message}", Toast.LENGTH_LONG).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        /**
         * Step 2: Analyze Button Flow
         */
        btnAnalyze.setOnClickListener {
            if (localPdfUri == null) {
                Toast.makeText(this, "PDF not downloaded yet!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val analyzingDialog = showProgressDialog("Analyzing your report...")

            lifecycleScope.launch {
                try {
                    withTimeout(20000) {
                        val text = withContext(Dispatchers.IO) {
                            OcrTextExtractor.extractTextFromPdf(this@PreviewActivity, localPdfUri!!)
                        }

                        Log.d("ExtractedText", text.take(200)) // limit logs
                        val report = LabReportParser.parse(text)
                        Log.d("ParsedReport", report.toString())

                        val result = withContext(Dispatchers.Default) {
                            ReportAnalyzer.analyze(report)
                        }

                        delay(1500) // small pause for smooth transition
                        analyzingDialog.dismiss()

                        showSuccessPopup(result.score, result.status, result.summary)
                    }
                } catch (e: TimeoutCancellationException) {
                    analyzingDialog.dismiss()
                    Toast.makeText(
                        this@PreviewActivity,
                        "⏳ OCR analysis took too long. Please retry.",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    analyzingDialog.dismiss()
                    Toast.makeText(this@PreviewActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    if (analyzingDialog.isShowing) analyzingDialog.dismiss()
                }
            }
        }

    }

    /**
     * ✅ Show popup after analysis
     */
    private fun showSuccessPopup(score: Int, status: String, summary: String) {
        AlertDialog.Builder(this)
            .setTitle("Analysis Complete ✅")
            .setMessage("Your health report has been analyzed successfully.\n\nTap Continue to view your result.")
            .setCancelable(false)
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, HealthResultActivity::class.java)
                intent.putExtra("score", score)
                intent.putExtra("message", "$status\n\n$summary")
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            .show()
    }
}
