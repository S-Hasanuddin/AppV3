package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import android.widget.ImageView as ImageView1

class ReportActivity : AppCompatActivity() {
    private lateinit var descriptionInput: EditText
    private lateinit var imagePreview: ImageView1
    private lateinit var uploadImageButton: Button
    private lateinit var getLocationButton: Button
    private lateinit var submitReportButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private var uploadedImageUri: Uri? = null
    private var userLocation: Location? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        descriptionInput = findViewById(R.id.description_input)
        imagePreview = findViewById(R.id.image_preview)
        uploadImageButton = findViewById(R.id.upload_image_button)
        getLocationButton = findViewById(R.id.get_location_button)
        submitReportButton = findViewById(R.id.submit_report_button)
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle the result here
                result.data
                // Do something with the returned data
            }
        }
        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            activityResultLauncher.launch(intent)
        }
        // Image Upload Button
        uploadImageButton.setOnClickListener {
            // Launch an intent to pick an image

        }

        // Location Button
        getLocationButton.setOnClickListener {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            checkLocationPermission()
            // Retrieve GPS location (ensure permissions are granted)
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = location
                    Toast.makeText(this, "Location retrieved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to get location.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Submit Button
        submitReportButton.setOnClickListener {
            val description = descriptionInput.text.toString()
            if (description.isNotBlank() && (uploadedImageUri != null) && (userLocation != null)) {
                uploadReportWithCustomId(description, uploadedImageUri!!, userLocation!!)
            } else {
                Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            uploadedImageUri = data.data
            imagePreview.visibility = View.VISIBLE
            Glide.with(this).load(uploadedImageUri).into(imagePreview)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Permission already granted
            getLastLocation()
        }
    }
    private fun getLastLocation() {
        try {
            // Use FusedLocationProviderClient to get the last known location
            fusedLocationClient.lastLocation
                .addOnSuccessListener(this) { location ->
                    if (location != null) {
                        // Successfully retrieved location
                        val latitude = location.latitude
                        val longitude = location.longitude
                        Toast.makeText(
                            this,
                            "Latitude: $latitude, Longitude: $longitude",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Handle case where location is null
                        Toast.makeText(this, "Location not available", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: SecurityException) {
            // Handle exception if permission is not granted
            Toast.makeText(
                this,
                "Location permission is required to access location.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting location
                getLastLocation()
            } else {
                // Permission denied, inform the user
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun uploadReportWithCustomId(description: String, imageUri: Uri, location: Location) {
        val reportId = UUID.randomUUID().toString() // Generate a custom ID
        val storageRef = storage.reference.child("images/$reportId.jpg")

        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val report = hashMapOf(
                    "description" to description,
                    "imageUrl" to uri.toString(),
                    "location" to GeoPoint(location.latitude, location.longitude),
                    "timestamp" to FieldValue.serverTimestamp(),
                    "userId" to "user@example.com" // Replace with actual user ID
                )

                db.collection("reports").document(reportId).set(report)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Report submitted with ID: $reportId", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to submit report.", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
            }
        }
    }
}
