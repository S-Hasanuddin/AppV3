package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(private val commentList: MutableList<Comment>, private val activity: ReportDetailsActivity) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.tvUserName)
        val commentText: TextView = itemView.findViewById(R.id.tvCommentText)
        val timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val deleteButton: Button = itemView.findViewById(R.id.btnDeleteComment)  // The delete button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.userName.text = comment.userId  // Display the user's email or UID
        holder.commentText.text = comment.text

        // Format the timestamp dynamically using SimpleDateFormat
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(comment.timestamp))
        holder.timestamp.text = String.format(activity.getString(R.string.comment_timestamp), formattedDate)  // Concatenate formatted date

        // Set up the delete button to call removeComment
        holder.deleteButton.setOnClickListener {
            activity.removeComment(position)  // Call removeComment in ReportDetailsActivity
        }
        holder.itemView.setOnClickListener {
            activity.updateUIWithComment(comment)  // Call updateUIWithComment when a comment is clicked
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }
}
