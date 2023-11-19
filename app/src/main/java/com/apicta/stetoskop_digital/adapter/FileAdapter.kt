package com.apicta.stetoskop_digital.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.apicta.stetoskop_digital.databinding.FileListContentBinding
import com.apicta.stetoskop_digital.listener.FileListener
import com.apicta.stetoskop_digital.model.remote.response.FileItem

class FileAdapter(private val file: List<FileItem>): RecyclerView.Adapter<FileAdapter.ViewHolder>(), Filterable {

    private var filteredFile = file

    private var fileListener: FileListener? = null

    fun setOnClickListener(fileListener: FileListener){
        this.fileListener = fileListener
    }
    class ViewHolder(private val binding: FileListContentBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(file: FileItem){
            binding.fileName.text = file.suara
            binding.date.text = file.createdAt
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(FileListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredFile[position])

        holder.itemView.setOnClickListener {
            fileListener?.onClick(file[holder.adapterPosition])
        }
    }

    override fun getItemCount(): Int = filteredFile.size
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint.isNullOrEmpty()) {
                    filterResults.values = file
                } else {
                    val filteredList = file.filter { it.suara!!.contains(constraint, true) }
                    filterResults.values = filteredList
                }
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredFile = results?.values as List<FileItem>
                notifyDataSetChanged()
            }
        }
    }
}