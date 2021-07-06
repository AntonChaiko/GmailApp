package com.example.gmailclientappn27.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gmailclientappn27.R
import com.example.gmailclientappn27.database.Messages
import com.example.gmailclientappn27.databinding.RecyclerViewLayoutBinding
import com.example.gmailclientappn27.models.UserMessagesModel

class MessageFragmentAdapterDb(

) :
    ListAdapter<Messages, MessageFragmentAdapterDb.ItemViewHolder>(DiffCallback) {
    companion object DiffCallback : DiffUtil.ItemCallback<Messages>() {

        override fun areItemsTheSame(oldItem: Messages, newItem: Messages): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: Messages, newItem: Messages): Boolean {
            return oldItem == newItem
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = RecyclerViewLayoutBinding.bind(itemView)

        fun bind(item: Messages) {
            binding.fromFieldTextView.text = item.form
            binding.dateFieldTextView.text = item.date
            binding.subjectFieldTextView.text = item.subject
            binding.attachImageView.visibility = View.INVISIBLE

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_layout, parent, false)
        return ItemViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

}