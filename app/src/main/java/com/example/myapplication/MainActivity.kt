package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private val firestore = Firebase.firestore
    private lateinit var reportList: MutableList<Report>
    private lateinit var adapter: ReportAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }

        // Initialize Firebase App
        FirebaseApp.initializeApp(this)?.let {
            Log.d("FirebaseInit", "Firebase initialized successfully.")
        } ?: Log.e("FirebaseInit", "Failed to initialize Firebase.")

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("Reports")
        database.get().addOnSuccessListener { snapshot ->
            Log.d("FirebaseTest", "Snapshot: ${snapshot.value}")
        }.addOnFailureListener { e ->
            Log.e("FirebaseTest", "Error: ${e.message}")
        }

        // Initialize report list and adapter
        reportList = mutableListOf()
        database = FirebaseDatabase.getInstance().getReference("reports")
        adapter = ReportAdapter(reportList, this)

        // Setup RecyclerView
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reportsRecyclerView.adapter = adapter

        // Fetch reports from Firebase
        fetchReports()

        // Set up FAB to navigate to report submission screen
        binding.fabAddReport.setOnClickListener {
            Log.d("FABClick", "Navigating to ReportSubmissionActivity")
            startActivity(Intent(this, ReportSubmissionActivity::class.java))
        }
        Log.d("RecyclerViewDebug", "com.example.myapplication.Report list size: ${reportList.size}")
        Log.d("RecyclerViewDebug", "Adapter item count: ${adapter.itemCount}")

    }

    private fun fetchReports() {
        firestore.collection("reports")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FetchReports", "Firestore error: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val newReportList = snapshot.toObjects(Report::class.java)

                    // Check for new or removed reports
                    val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                        override fun getOldListSize() = reportList.size
                        override fun getNewListSize() = newReportList.size

                        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                            return reportList[oldItemPosition].id == newReportList[newItemPosition].id
                        }

                        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                            return reportList[oldItemPosition] == newReportList[newItemPosition]
                        }
                    })

                    // Update the list and notify the adapter with specific changes
                    reportList.clear()
                    reportList.addAll(newReportList)
                    diffResult.dispatchUpdatesTo(adapter)
                    binding.tvEmptyMessage.visibility = if (reportList.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }


}
