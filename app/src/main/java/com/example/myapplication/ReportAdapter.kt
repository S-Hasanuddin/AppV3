package com.example.myapplication

import Report
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemReportBinding


class ReportAdapter(private val reportList: List<Report>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(private val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            binding.tvReportTitle.text = report.title
            binding.tvReportDescription.text = report.description

            // Convert GeoPoint to String
            val locationString = report.location?.let {
                "Lat: ${it.latitude}, Lon: ${it.longitude}"
            } ?: "Unknown Location"
            binding.tvReportLocation.text = locationString

            // Load image
            Glide.with(binding.root.context)
                .load(report.imageUrl)
                .into(binding.ivReportImage)
            binding.btnViewReportDetails.setOnClickListener {
                // Handle the button click, e.g., open a new activity or show details
                val intent = Intent(binding.root.context, ReportDetailsActivity::class.java)
                intent.putExtra("REPORT_ID", report.id)  // Pass the report ID or other data
                binding.root.context.startActivity(intent)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding =
            ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reportList[position])
    }
    override fun getItemCount(): Int = reportList.size
}
