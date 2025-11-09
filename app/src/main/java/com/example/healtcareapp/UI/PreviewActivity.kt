package com.example.healtcareapp.UI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.healtcareapp.R
import com.example.healtcareapp.helper.LabReportParser
import com.example.healtcareapp.helper.OcrTextExtractor
import com.example.healtcareapp.helper.PdfTextExtractor
import com.example.healtcareapp.helper.ReportAnalyzer
import com.example.healtcareapp.model.LabReport
import com.google.firebase.storage.FirebaseStorage
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.launch
import java.io.File

class PreviewActivity : AppCompatActivity() {

    private lateinit var btnAnalyze: Button
    private var pdfUrl: String? = null
    private var pdfName: String? = null
    private var localPdfUri: Uri? = null

    // ✅ Use your correct Firebase bucket explicitly
    private val firebaseStorage =
        FirebaseStorage.getInstance("gs://health-care-e9c9d.firebasestorage.app")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        btnAnalyze = findViewById(R.id.btnAnalyze)
        pdfUrl = intent.getStringExtra("pdfUrl")
        pdfName = intent.getStringExtra("pdfName")

        if (pdfUrl == null) {
            Toast.makeText(this, "No PDF URL found!", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ Download PDF before analysis
        downloadPdfForAnalysis(pdfUrl!!)

       /* btnAnalyze.setOnClickListener {
            if (localPdfUri == null) {
                Toast.makeText(this, "PDF not downloaded yet!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            PDFBoxResourceLoader.init(applicationContext)
            // Extract, parse, and analyze the lab report
            val text = PdfTextExtractor.extractText(this, localPdfUri!!)

            Log.d("ExtractedText", text) // 👈 Add this


            val report = LabReportParser.parse(text)
            Log.d("ParsedReport", report.toString())
            val result = ReportAnalyzer.analyze(report)

            // ✅ Show result in next screen
            val intent = Intent(this, HealthResultActivity::class.java)
            intent.putExtra("score", result.score)
            intent.putExtra("message", "${result.status}\n\n${result.summary}")
            startActivity(intent)
        }*/

        btnAnalyze.setOnClickListener {
            if (localPdfUri == null) {
                Toast.makeText(this, "PDF not downloaded yet!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use coroutine to handle ML Kit OCR
            lifecycleScope.launch {
                Toast.makeText(this@PreviewActivity, "Analyzing report, please wait...", Toast.LENGTH_SHORT).show()

                val text = OcrTextExtractor.extractTextFromPdf(this@PreviewActivity, localPdfUri!!)
                Log.d("ExtractedText", text)

                val report = LabReportParser.parse(text)
                Log.d("ParsedReport", report.toString())

                val result = ReportAnalyzer.analyze(report)

                val intent = Intent(this@PreviewActivity, HealthResultActivity::class.java)
                intent.putExtra("score", result.score)
                intent.putExtra("message", "${result.status}\n\n${result.summary}")
                startActivity(intent)
            }
        }

    }

    private fun downloadPdfForAnalysis(url: String) {
        try {
            // ✅ No need to replace the URL now — your bucket is correct
            val storageRef = firebaseStorage.getReferenceFromUrl(url)
            val localFile = File.createTempFile("tempReport", ".pdf")

            Toast.makeText(this, "Downloading report for analysis...", Toast.LENGTH_SHORT).show()

            storageRef.getFile(localFile)
                .addOnSuccessListener {
                    localPdfUri = Uri.fromFile(localFile)
                    Toast.makeText(this, "Report ready for analysis ✅", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to download: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
