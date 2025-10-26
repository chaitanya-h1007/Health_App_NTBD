package com.example.healtcareapp.UI

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.healtcareapp.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth

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
            // TODO: Navigate to Upload Document screen
        }

        binding.cardSavedDocuments.setOnClickListener {
            Toast.makeText(this, "Saved Documents clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Saved Documents screen
            // startActivity(Intent(this, SavedDocumentsActivity::class.java))
        }

        binding.cardScanDocuments.setOnClickListener {
            Toast.makeText(this, "Scan Documents clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Scan Documents screen

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 101)
            } else {
                openCamera()
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


    private fun openCamera(){
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }
}