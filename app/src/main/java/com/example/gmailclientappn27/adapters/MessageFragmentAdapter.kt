package com.example.gmailclientappn27.adapters

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RemoteViewsService
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gmailclientappn27.R
import com.example.gmailclientappn27.UserMessagesModelClass
import com.example.gmailclientappn27.database.Messages
import com.example.gmailclientappn27.databinding.RecyclerViewLayoutBinding
import com.example.gmailclientappn27.fragments.messagesfragment.MessagesFragment
import com.example.gmailclientappn27.fragments.messagesfragment.MessagesFragmentViewModel
import com.google.api.services.gmail.Gmail
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base64
import java.io.File
import java.io.FileOutputStream

class MessageFragmentAdapter(
    private val list: List<Messages>?,
    private val context: Context,
    private val service: Gmail,
    private val mMessagesFragmentViewModel: MessagesFragmentViewModel
) :
    RecyclerView.Adapter<MessageFragmentAdapter.MessageAdapterViewHolder>() {

    class MessageAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = RecyclerViewLayoutBinding.bind(itemView)
        var fromTextView: TextView? = null
        var subjectTextView: TextView? = null
        var dateTextView: TextView? = null
        var attachImageView: ImageView? = null

        init {
            fromTextView = binding.fromFieldTextView
            subjectTextView = binding.subjectFieldTextView
            dateTextView = binding.dateFieldTextView
            attachImageView = binding.attachImageView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapterViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_layout, parent, false)
        return MessageAdapterViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MessageAdapterViewHolder, position: Int) {

        when (isOnline(context)) {
            true -> {
                val currentMessage = UserMessagesModelClass.dataObject[position]
                holder.dateTextView?.text = currentMessage.date
                holder.subjectTextView?.text = currentMessage.subject
                holder.fromTextView?.text = currentMessage.from
                if (currentMessage.attachmentId != "") {
                    holder.attachImageView?.visibility = View.VISIBLE
                    holder.attachImageView?.setOnClickListener {
//                        Toast.makeText(context, currentMessage.attachmentId, Toast.LENGTH_LONG)
//                            .show()
                        CoroutineScope(Dispatchers.IO).launch {
                            mMessagesFragmentViewModel.getData(service,currentMessage.messageId,currentMessage.attachmentId,currentMessage.filename)
                        }
                    }
                }
            }
            false -> {
                val currentMessage = list?.get(position)
                holder.dateTextView?.text = currentMessage?.date
                holder.subjectTextView?.text = currentMessage?.subject
                holder.fromTextView?.text = currentMessage?.form
                if (currentMessage?.attachmentId != "") {
                    holder.attachImageView?.visibility = View.VISIBLE
                    holder.attachImageView?.setOnClickListener {
                        Toast.makeText(context, "No internet connection =(", Toast.LENGTH_LONG)
                            .show()

                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getItemCount(): Int {
        return when (isOnline(context)) {
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
