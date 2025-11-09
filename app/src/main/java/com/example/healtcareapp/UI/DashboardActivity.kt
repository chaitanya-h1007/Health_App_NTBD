package com.example.healtcareapp.UI

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.healtcareapp.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.util.UUID

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private var pdfUri: Uri? = null

    // ✅ Always connect explicitly to your correct Firebase Storage bucket
    private val firebaseStorage =
        FirebaseStorage.getInstance("gs://health-care-e9c9d.firebasestorage.app")

    // PDF Picker
    private val pdfPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                pdfUri = result.data!!.data
                pdfUri?.let { uri ->
                    Toast.makeText(this, "PDF Selected: ${uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
                    uploadPdfToFirebase(uri)
                }
            }
        }

    // Permission Check
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openFilePicker()
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }

    // Document Scanner
    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                scanningResult?.pdf?.let { pdf ->
                    val pdfUri = pdf.uri
                    Toast.makeText(this, "Uploading scanned document...", Toast.LENGTH_SHORT).show()
                    uploadPdfToFirebase(pdfUri)
                }
            } else {
                Toast.makeText(this, "Scanning canceled!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupClickListeners()
        setupBottomNavigation()
        setupOnBackPressed()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.etSearch.setOnClickListener {
            Toast.makeText(this, "Search feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.cardUploadDocument.setOnClickListener {
            checkPermissionAndPickPdf()
        }

        binding.cardSavedDocuments.setOnClickListener {
            startActivity(Intent(this, SavedDocumentsActivity::class.java))
        }

        binding.cardScanDocuments.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
            } else {
                openDocScanner()
            }
        }

        binding.cardAppointments.setOnClickListener {
            Toast.makeText(this, "Appointments clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.tabHome.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        binding.tabSearch.setOnClickListener {
            Toast.makeText(this, "Search feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.tabProfile.setOnClickListener {
            Toast.makeText(this, "Profile feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.tabCalendar.setOnClickListener {
            Toast.makeText(this, "Calendar feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this) {
            AlertDialog.Builder(this@DashboardActivity)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes") { _, _ -> finishAffinity() }
                .setNegativeButton("No", null)
                .show()
        }
    }

    fun openDocScanner() {
        try {
            val options = GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setResultFormats(
                    GmsDocumentScannerOptions.RESULT_FORMAT_PDF,
                    GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
                )
                .setGalleryImportAllowed(false)
                .setPageLimit(10)
                .build()

            GmsDocumentScanning.getClient(options)
                .getStartScanIntent(this)
                .addOnSuccessListener { intentSender: IntentSender ->
                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
                .addOnFailureListener { e ->
                    Log.e("ScannerError", e.message ?: "Unknown error")
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPermissionAndPickPdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openFilePicker()
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openFilePicker()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        pdfPicker.launch(intent)
    }

    // ✅ FIXED: Use your correct Firebase Storage bucket directly
    private fun uploadPdfToFirebase(uri: Uri) {
        val storageRef = firebaseStorage.reference
        val fileName = "document_${UUID.randomUUID()}.pdf"
        val fileRef = storageRef.child("documents/$fileName")

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    Log.d("UPLOAD_SUCCESS", "File uploaded: $downloadUrl")
                    saveToFirestore(fileName, downloadUrl)
                }
            }
            .addOnFailureListener { e ->
                Log.e("UPLOAD_ERROR", "Upload failed: ${e.message}")
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToFirestore(name: String, url: String) {
        val db = FirebaseFirestore.getInstance()
        val documentData = hashMapOf(
            "name" to name,
            "url" to url,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("documents").add(documentData)
            .addOnSuccessListener {
                Toast.makeText(this, "PDF uploaded successfully ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
