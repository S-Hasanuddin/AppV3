package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
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
            binding.tvReportLocation.text = report.location

            Glide.with(binding.root.context)
                .load(report.imageUrl)
                .into(binding.ivReportImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]
        holder.bind(report)
    }

    override fun getItemCount(): Int {
        return reportList.size
    }
}
