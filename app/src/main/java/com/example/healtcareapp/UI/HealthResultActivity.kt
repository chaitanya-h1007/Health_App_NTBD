package com.example.healtcareapp.UI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.healtcareapp.R

class HealthResultActivity : AppCompatActivity() {
    private lateinit var tvScore: TextView
    private lateinit var tvMessage: TextView
    private lateinit var btnFindDoctors: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_health_result)


        tvScore = findViewById(R.id.tvScore)
        tvMessage = findViewById(R.id.tvMessage)
        btnFindDoctors = findViewById(R.id.btnFindDoctors)

        val score = intent.getIntExtra("score", 0)
        val message = intent.getStringExtra("message") ?: "No analysis data."

        tvScore.text = "Health Score: $score / 10"
        tvMessage.text = message

        // Show button only if score is low
        if (score >= 8) {
            btnFindDoctors.visibility = View.GONE
        } else {
            btnFindDoctors.visibility = View.VISIBLE
        }

        btnFindDoctors.setOnClickListener {
            val uri = Uri.parse("geo:0,0?q=doctors+near+me")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

    }
}