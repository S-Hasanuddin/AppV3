package com.example.myapplication


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DateFormat
import java.util.Date

class ReportDetailsActivity : AppCompatActivity() {

    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()
    private lateinit var db: FirebaseFirestore
    private lateinit var reportId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)

        db = FirebaseFirestore.getInstance()
        reportId = intent.getStringExtra("REPORT_ID") ?: ""

        Log.d("ReportDetailsActivity", "Received report ID: $reportId") // Debug log

        if (reportId.isEmpty()) {
            Toast.makeText(this, "Invalid report ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchReportById(reportId)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewComments)
        recyclerView.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(commentList, this)
        recyclerView.adapter = commentAdapter

        // Fetch comments from Firestore
        fetchComments()
        // Handle add comment button click
        val btnAddComment: Button = findViewById(R.id.btnAddComment)
        btnAddComment.setOnClickListener {
            val commentText: EditText = findViewById(R.id.etNewComment)
            val comment = commentText.text.toString().trim()

            if (comment.isNotEmpty()) {
                addComment(comment)
                commentText.text.clear() // Clear the input field after posting
            }
            val tvReportTitle: TextView = findViewById(R.id.tvReportTitle)
            val tvReportDescription: TextView = findViewById(R.id.tvReportDescription)
            fetchReportById(reportId)
            // Use the reportId to fetch the report details from Firestore or any data source
            // For example, set the title and description based on the reportId
            tvReportTitle.text = String.format(getString(R.string.report_title2), reportId)
            tvReportDescription.text = String.format(getString(R.string.report_description2), reportId)
        }
    }

    private fun fetchComments() {
        db.collection("reports").document(reportId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("ReportDetails", "Listen failed.", e)
                    return@addSnapshotListener
                }

                commentList.clear()
                snapshot?.documents?.forEach { document ->
                    val comment = document.toObject(Comment::class.java)
                    comment?.let { it.id = document.id
                        commentList.add(it) }
                }
            }
    }

    private fun addComment(commentText: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: "Anonymous"  // Use email or fallback to "Anonymous"

        val comment = Comment(
            userId = userEmail,  // Store the user's email
            text = commentText,
            timestamp = System.currentTimeMillis()
        )

        db.collection("reports").document(reportId)
            .collection("comments")
            .add(comment)
            .addOnSuccessListener {
                commentList.add(comment)
                // Notify the adapter that a new item has been inserted
                commentAdapter.notifyItemInserted(commentList.size - 1)
                Log.d("ReportDetails", "Comment added successfully!")
            }
            .addOnFailureListener { e ->
                Log.w("ReportDetails", "Error adding comment", e)
            }
    }
    fun removeComment(position: Int) {
        db.collection("reports").document(reportId)
            .collection("comments")
            .document(commentList[position].id)
            .delete()
            .addOnSuccessListener {
                // Remove the comment from the list
                commentList.removeAt(position)
                // Notify the adapter that an item has been removed
                commentAdapter.notifyItemRemoved(position)
            }
            .addOnFailureListener { e ->
                Log.w("ReportDetails", "Error removing comment", e)
            }
    }


    // Example of using string resources with placeholders in the activity
    fun updateUIWithComment(comment: Comment) {
        val userNameTextView: TextView = findViewById(R.id.tvUserName)
        val commentTextView: TextView = findViewById(R.id.tvCommentText)
        val timestampTextView: TextView = findViewById(R.id.tvTimestamp)

        // Use string resources with placeholders
        userNameTextView.text = String.format(getString(R.string.user_comment), comment.userId)
        commentTextView.text = comment.text

        // Format the timestamp using DateFormat and use the string resource with placeholders
        val formattedDate = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(comment.timestamp))
        timestampTextView.text = String.format(getString(R.string.comment_timestamp), formattedDate)
    }
    private fun fetchReportById(reportId: String) {
        db.collection("reports").document(reportId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title") ?: "No Title"
                    val description = document.getString("description") ?: "No Description"
                    findViewById<TextView>(R.id.tvReportTitle).text = title
                    findViewById<TextView>(R.id.tvReportDescription).text = description
                } else {
                    Toast.makeText(this, "Report not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching report", Toast.LENGTH_SHORT).show()
                Log.e("ReportDetailsActivity", "Error fetching document", e)
                finish()
            }
    }
}
