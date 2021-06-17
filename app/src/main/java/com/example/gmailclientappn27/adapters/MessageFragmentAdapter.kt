package com.example.gmailclientappn27.adapters

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.UserDictionary
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.gmailclientappn27.R
import com.example.gmailclientappn27.UserMessagesModelClass
import com.example.gmailclientappn27.database.Messages
import com.example.gmailclientappn27.database.MessagesViewModel
import com.example.gmailclientappn27.databinding.RecyclerViewLayoutBinding

class MessageFragmentAdapter(private val list: List<Messages>?, private val context: Context) :
    RecyclerView.Adapter<MessageFragmentAdapter.MessageAdapterViewHolder>() {

    class MessageAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = RecyclerViewLayoutBinding.bind(itemView)
        var fromTextView: TextView? = null
        var subjectTextView: TextView? = null
        var dateTextView: TextView? = null

        init {
            fromTextView = binding.fromFieldTextView
            subjectTextView = binding.subjectFieldTextView
            dateTextView = binding.dateFieldTextView

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapterViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_layout, parent, false)
        return MessageAdapterViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MessageAdapterViewHolder, position: Int) {

        when (isOnline(context)){
            true -> {
                val currentMessage = UserMessagesModelClass.dataObject[position]
                holder.dateTextView?.text = currentMessage.date
                holder.subjectTextView?.text = currentMessage.subject
                holder.fromTextView?.text = currentMessage.from
            }
            false -> {
                val currentMessage = list?.get(position)
                holder.dateTextView?.text = currentMessage?.date
                holder.subjectTextView?.text = currentMessage?.subject
                holder.fromTextView?.text = currentMessage?.form
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getItemCount(): Int  {
       return when(isOnline(context)) {
            true -> UserMessagesModelClass.dataObject.size
            false -> list?.size!!
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        return false
    }
}
