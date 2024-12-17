package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ReportSubmissionActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var uploadedImageUrl: String = ""

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploadImageToFirebase(uri)
        } else {
            Toast.makeText(this, "Image selection failed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_submission)

        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnUploadImage = findViewById<Button>(R.id.btnUploadImage)
        val btnSubmitReport = findViewById<Button>(R.id.btnSubmitReport)

        // Initialize Fused Location Provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fetchCurrentLocation()

        btnUploadImage.setOnClickListener {
            // Open file picker to select an image
            imagePickerLauncher.launch("image/*")
        }

        btnSubmitReport.setOnClickListener {
            val description = etDescription.text.toString().trim()
            if (description.isEmpty()) {
                Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (uploadedImageUrl.isEmpty()) {
                Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show()
            } else {
                uploadReport(description, uploadedImageUrl, currentLatitude, currentLongitude)
            }
        }
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                Log.d("Location", "Lat: $currentLatitude, Lon: $currentLongitude")
            } else {
                Log.e("Location", "Failed to get location")
            }
        }.addOnFailureListener {
            Log.e("Location", "Error fetching location", it)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val storageRef = Firebase.storage.reference.child("images/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("FirebaseStorage", "Image uploaded successfully: $uri")
                    uploadedImageUrl = uri.toString()
                    Toast.makeText(this, "Image uploaded!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseStorage", "Error uploading image", e)
                Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadReport(description: String, imageUrl: String, latitude: Double, longitude: Double) {
        val firestore = Firebase.firestore
        val report = hashMapOf(
            "description" to description,
            "imageUrl" to imageUrl,
            "location" to GeoPoint(latitude, longitude),
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("reports")
            .add(report)
            .addOnSuccessListener {
                Log.d("Firestore", "com.example.myapplication.Report uploaded successfully: ${it.id}")
                Toast.makeText(this, "com.example.myapplication.Report submitted!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error uploading report", e)
                Toast.makeText(this, "Failed to submit report.", Toast.LENGTH_SHORT).show()
            }
    }
}
