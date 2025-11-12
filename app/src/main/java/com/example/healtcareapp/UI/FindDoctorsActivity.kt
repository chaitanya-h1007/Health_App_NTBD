package com.example.healtcareapp.UI

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healtcareapp.R
import com.example.healtcareapp.model.DoctorAdd
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import kotlin.math.*

class FindDoctorsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val doctorsList = mutableListOf<DoctorAdd>()
    private lateinit var adapter: DoctorAdapter


    private val apiKey: String by lazy {
        getApiKeyFromManifest()
    }

    private fun getApiKeyFromManifest(): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val key = appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
            Log.d("FindDoctors", "✅ API Key from manifest: $key")
            key
        } catch (e: Exception) {
            Log.e("FindDoctors", "❌ Error loading API key: ${e.message}")
            ""
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_doctors)

        recyclerView = findViewById(R.id.recyclerViewDoctors)
        progressBar = findViewById(R.id.progressBarDoctors)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DoctorAdapter(doctorsList)
        recyclerView.adapter = adapter

        Log.d("FindDoctors", "Google API Key Loaded: $apiKey")


        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Google API key not found. Please check your manifest.", Toast.LENGTH_LONG).show()
            return
        }

        getUserLocation()
    }

    // 📍 Get user's location safely
    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.d("FindDoctors", "User location: ${location.latitude}, ${location.longitude}")
                fetchNearbyDoctors(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "Unable to get location. Enable GPS.", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Location access failed: ${it.message}", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }

    // 🌍 Fetch doctors near given location using Google Places API
    private fun fetchNearbyDoctors(lat: Double, lng: Double) {
        showLoadingDoctorsDialog()
        progressBar.visibility = View.VISIBLE

        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=$lat,$lng&radius=5000&type=doctor&key=$apiKey"

        Log.d("FindDoctors", "Fetching nearby doctors...")
        Log.d("FindDoctors", "API URL: $url")

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                Log.d("FindDoctors", "Response Code: ${response.code}")
                Log.d("FindDoctors", "Response (first 300 chars): ${jsonData.take(300)}")

                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this, "API Request failed. Code: ${response.code}", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
                    return@Thread
                }

                val jsonObject = JSONObject(jsonData)
                val results = jsonObject.optJSONArray("results")

                if (results == null || results.length() == 0) {
                    runOnUiThread {
                        Toast.makeText(this, "No doctors found nearby.", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
                    return@Thread
                }

                doctorsList.clear()
                for (i in 0 until results.length()) {
                    val place = results.getJSONObject(i)
                    val name = place.optString("name")
                    val address = place.optString("vicinity")
                    val rating = place.optDouble("rating", 0.0)
                    val types = place.optJSONArray("types")?.join(", ") ?: "Doctor"
                    val location = place.getJSONObject("geometry").getJSONObject("location")
                    val latDoctor = location.getDouble("lat")
                    val lngDoctor = location.getDouble("lng")
                    val distance = calculateDistance(lat, lng, latDoctor, lngDoctor)

                    val mapsUrl = "https://www.google.com/maps/search/?api=1&query=${Uri.encode(name)}"

                    doctorsList.add(
                        DoctorAdd(
                            name = name,
                            address = address,
                            rating = rating,
                            specialization = types,
                            distance = String.format("%.2f km", distance),
                            mapsUrl = mapsUrl
                        )
                    )
                }

                runOnUiThread {
                    adapter.notifyDataSetChanged()
                    progressBar.visibility = View.GONE
                }

            } catch (e: IOException) {
                Log.e("FindDoctors", "Error fetching data: ${e.message}")
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // 🧮 Calculate distance between two coordinates
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun showLoadingDoctorsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_progress, null)
        val text = dialogView.findViewById<TextView>(R.id.tvProgressText)
        text.text = "Finding doctors near you..."

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            Toast.makeText(this, "✅ Doctors found near you!", Toast.LENGTH_SHORT).show()
        }, 3000)
    }

}
