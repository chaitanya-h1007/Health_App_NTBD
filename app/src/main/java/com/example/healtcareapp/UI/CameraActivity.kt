package com.example.healtcareapp.UI

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.healtcareapp.databinding.ActivityCameraBinding // Correction: Changed to ActivityCameraBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    // Correction: The binding class should match the activity name (activity_camera.xml)
    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService
    private val capturedImages = mutableListOf<Uri>()
    private var isFlashOn = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            capturedImages.add(it)
            updateCaptureCount()
            Toast.makeText(this, "Image added from gallery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Correction: Inflate the correct binding class
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupUI()
        checkCameraPermission()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCapture.setOnClickListener {
            takePhoto()
        }

        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnProcess.setOnClickListener {
            if (capturedImages.isNotEmpty()) {
                processDocuments()
            } else {
                Toast.makeText(this, "Please capture or select images first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }

        updateCaptureCount()
    }

    private fun checkCameraPermission() {
        // Correction: Use the correct manifest permission string
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    // Correction: The PreviewView ID should match the layout
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to start camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            externalMediaDirs.firstOrNull(),
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        binding.btnCapture.isEnabled = false

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    capturedImages.add(savedUri)
                    updateCaptureCount()
                    Toast.makeText(
                        this@CameraActivity, // Correction: Use correct activity context
                        "Photo captured successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnCapture.isEnabled = true
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity, // Correction: Use correct activity context
                        "Photo capture failed: ${exc.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnCapture.isEnabled = true
                }
            }
        )
    }

    private fun toggleFlash() {
        camera?.let {
            if (it.cameraInfo.hasFlashUnit()) {
                isFlashOn = !isFlashOn
                it.cameraControl.enableTorch(isFlashOn)
                binding.btnFlash.alpha = if (isFlashOn) 1.0f else 0.5f
                Toast.makeText(
                    this,
                    if (isFlashOn) "Flash On" else "Flash Off",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateCaptureCount() {
        binding.tvCaptureCount.text = "Captured: ${capturedImages.size}"
        binding.btnProcess.isEnabled = capturedImages.isNotEmpty()
    }

    private fun processDocuments() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnProcess.isEnabled = false
        binding.btnCapture.isEnabled = false

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val extractedTexts = mutableListOf<String>()
        var processedCount = 0

        capturedImages.forEach { uri ->
            try {
                val image = InputImage.fromFilePath(this, uri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        extractedTexts.add(visionText.text)
                        processedCount++

                        if (processedCount == capturedImages.size) {
                            onTextExtractionComplete(extractedTexts)
                        }
                    }
                    .addOnFailureListener { e ->
                        processedCount++
                        Toast.makeText(this, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()

                        if (processedCount == capturedImages.size) {
                            onTextExtractionComplete(extractedTexts)
                        }
                    }
            } catch (e: Exception) {
                processedCount++
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()

                if (processedCount == capturedImages.size) {
                    onTextExtractionComplete(extractedTexts)
                }
            }
        }
    }

    private fun onTextExtractionComplete(texts: List<String>) {
        binding.progressBar.visibility = View.GONE

        if (texts.isEmpty()) {
            Toast.makeText(this, "No text could be extracted from the images.", Toast.LENGTH_LONG).show()
            binding.btnProcess.isEnabled = true
            binding.btnCapture.isEnabled = true
            return
        }

        val combinedText = texts.joinToString("\n\n")

        // Correction: Ensure ReportAnalysisActivity exists and is declared
        // val intent = Intent(this, ReportAnalysisActivity::class.java)
        // intent.putExtra("extracted_text", combinedText)
        // intent.putStringArrayListExtra("image_uris", ArrayList(capturedImages.map { it.toString() }))
        // startActivity(intent)

        // For now, let's just display the text in a Toast and re-enable the UI
        Toast.makeText(this, "Text extraction complete!", Toast.LENGTH_LONG).show()
        binding.btnProcess.isEnabled = true
        binding.btnCapture.isEnabled = true

        finish() // Optionally finish this activity
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        // Turn off flash when closing the activity
        camera?.takeIf { it.cameraInfo.hasFlashUnit() }?.cameraControl?.enableTorch(false)
    }
}
