package com.pawga.mynearbyapplication.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.pawga.mynearbyapplication.R

/**
 * Created by sivannikov on 12.02.21 1:16
 */
class ListAdapter(private val list: LiveData<List<String>>) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    init {
        list.observeForever {
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = list.value?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = list.value ?: return
        val item = list[position]
        holder.bind(item)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView){
        val message: TextView = itemView.findViewById(R.id.message_row)

        fun bind(item: String) {
            message.text = item
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.message_row, parent, false)
                return ViewHolder(view)
            }
        }
    }
}