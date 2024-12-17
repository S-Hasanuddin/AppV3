package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ReportAdapter(private val reports: List<Report>, private val context: Context) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.title.text = report.title
        holder.description.text = report.description
        Log.d("ReportAdapter", "Passing report ID: ${report.id}")
        Glide.with(holder.itemView.context)
            .load(report.imageUrl) // Use the correct field from your Firestore data // Optional placeholder image // Optional error image
            .into(holder.imageView)
        holder.btnViewDetails.setOnClickListener {
            val intent = Intent(context, ReportDetailsActivity::class.java)
            intent.putExtra("REPORT_ID", report.id)
            Log.d("ReportAdapter", "Passing report ID: ${report.id}")// Pass report ID
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = reports.size

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvReportTitle)
        val description: TextView = itemView.findViewById(R.id.tvReportDescription)
        val imageView: ImageView = itemView.findViewById(R.id.ivReportImage)
        val btnViewDetails: Button = itemView.findViewById(R.id.btnViewReportDetails)
    }
}
