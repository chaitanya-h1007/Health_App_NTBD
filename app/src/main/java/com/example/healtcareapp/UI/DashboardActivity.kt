package com.example.healtcareapp.UI

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.healtcareapp.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.util.UUID


class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private var pdfUri: Uri? = null

    //FOR PDF PICKER
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
    //permission check
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openFilePicker()
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }

    //FOR DOC SCANNER
    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)

                // ✅ If a PDF was generated, upload it to Firebase
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
        // Back button
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Search functionality
        binding.etSearch.setOnClickListener {
            Toast.makeText(this, "Search feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Dashboard cards
        binding.cardUploadDocument.setOnClickListener {
            Toast.makeText(this, "Upload Document clicked", Toast.LENGTH_SHORT).show()
            checkPermissionAndPickPdf()


        }

        binding.cardSavedDocuments.setOnClickListener {
            Toast.makeText(this, "Saved Documents clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Saved Documents screen
            startActivity(Intent(this, SavedDocumentsActivity::class.java))
        }

        binding.cardScanDocuments.setOnClickListener {
            Toast.makeText(this, "Scan Documents clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Scan Documents screen

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 101)
            } else {
                openDocScanner()
            }
        }

        binding.cardAppointments.setOnClickListener {
            Toast.makeText(this, "Appointments clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Appointments screen
            // startActivity(Intent(this, AppointmentsActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        // Home tab (current screen)
        binding.tabHome.setOnClickListener {
            // Already on home screen
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        // Search tab
        binding.tabSearch.setOnClickListener {
            Toast.makeText(this, "Search feature coming soon", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Search screen
        }

        // Profile tab
        binding.tabProfile.setOnClickListener {
            Toast.makeText(this, "Profile feature coming soon", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Profile screen
            // startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Calendar tab
        binding.tabCalendar.setOnClickListener {
            Toast.makeText(this, "Calendar feature coming soon", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Calendar screen
            // startActivity(Intent(this, CalendarActivity::class.java))
        }
    }



    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this) {
            // Show exit confirmation dialog
            AlertDialog.Builder(this@DashboardActivity)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes") { _, _ ->

                    finishAffinity()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }


    fun openDocScanner(){
        try{
            val options = GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF,GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                .setGalleryImportAllowed(false)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setPageLimit(10)
            GmsDocumentScanning.getClient(options.build())
                .getStartScanIntent(this)
                .addOnSuccessListener { intentSender: IntentSender ->
                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
                .addOnFailureListener() { e: Exception ->
                    e.message?.let { Log.e("error", it) }
                }
        }catch (e : Exception ){
            e.stackTrace

        }

    }

    private fun checkPermissionAndPickPdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openFilePicker() // Android 13+ auto-grants access
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


    private fun uploadPdfToFirebase(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "document_${UUID.randomUUID()}.pdf"
        val fileRef = storageRef.child("documents/$fileName")

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

        val uploadTask = fileRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                saveToFirestore(fileName, downloadUrl.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Save PDF info in Firestore
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