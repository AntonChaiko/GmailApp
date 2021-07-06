package com.example.gmailclientappn27.fragments.messagesfragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.room.Room
import com.example.gmailclientappn27.R
import com.example.gmailclientappn27.adapters.MessageFragmentAdapter
import com.example.gmailclientappn27.adapters.MessageFragmentAdapterDb
import com.example.gmailclientappn27.database.Messages
import com.example.gmailclientappn27.database.MessagesDatabase
import com.example.gmailclientappn27.database.MessagesViewModel
import com.example.gmailclientappn27.databinding.FragmentMessagesBinding
import com.example.gmailclientappn27.fragments.basefragment.BaseFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.gmail.Gmail
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import org.apache.commons.codec.binary.Base64
import java.io.File
import java.io.FileOutputStream


class MessagesFragment : BaseFragment<FragmentMessagesBinding>() {

    private lateinit var mMessagesFragmentViewModel: MessagesFragmentViewModel
    private lateinit var mMessageViewModel: MessagesViewModel
    lateinit var credential: GoogleAccountCredential
    lateinit var service: Gmail
    var job: Job? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()

        mMessagesFragmentViewModel =
            ViewModelProvider(this).get(MessagesFragmentViewModel::class.java)

        mMessageViewModel = ViewModelProvider(this).get(MessagesViewModel::class.java)

        credential = mMessagesFragmentViewModel.getCredential(requireActivity())
        service = mMessagesFragmentViewModel.getService(credential)


        val messagesDatabase =
            Room.databaseBuilder(requireContext(), MessagesDatabase::class.java, "messages_table")
                .allowMainThreadQueries()
                .build()

        val d: List<Messages>? = messagesDatabase.messagesDao()?.getAllMessages()

        val adapter =
            MessageFragmentAdapter { messageId, attachmentId, filename ->
                CoroutineScope(Dispatchers.IO).launch {
                    getData(messageId, attachmentId, filename)
                }
            }

        if (isOnline(requireContext())) {
            job = GlobalScope.launch {
                mMessagesFragmentViewModel.readEmail(service, mMessageViewModel)
            }
        } else {
            Log.d("asd", "no internet")
        }

        binding.messagesRecyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.messagesRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.messagesRecyclerView.adapter = adapter


        if (isOnline(requireContext())) {
            mMessagesFragmentViewModel.messages.observe(viewLifecycleOwner, { item ->
                when {
                    item.size < 10 -> {
                        binding.loadingCardView.visibility = View.VISIBLE
                        binding.messagesRecyclerView.visibility = View.GONE
                    }
                    item.size > 10 -> {
                        binding.loadingCardView.visibility = View.GONE
                        binding.messagesRecyclerView.visibility = View.VISIBLE
                        adapter.submitList(item)
                        adapter.notifyDataSetChanged()
                    }
                }
            })
        } else {
            val adapterDb = MessageFragmentAdapterDb()
            adapterDb.submitList(d)
            binding.messagesRecyclerView.adapter = adapterDb
        }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        binding.exitButton.setOnClickListener {
            job?.cancel()
            removePrefs()

            mMessageViewModel.deleteAllMessages()
            FirebaseAuth.getInstance().signOut()
            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            googleSignInClient.signOut()
            findNavController().navigate(R.id.action_messagesFragment_to_loginFragment)

        }
    }

    private fun removePrefs() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.apply {
            putBoolean("BOOLEAN_KEY", false)
        }?.apply()
    }

    override fun getFragmentView(): Int {
        return R.layout.fragment_messages
    }

    suspend fun getData(id: String, attachId: String, filename: String): String? {
        val dataResult = withContext(Dispatchers.IO) {
            service.users().messages().attachments()
                ?.get(
                    FirebaseAuth.getInstance().currentUser?.email,
                    id, attachId
                )?.execute()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val data = Base64.decodeBase64(dataResult?.data)
            val file = File(
                "${
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                }/${filename}"
            )
            file.createNewFile()
            val fOut = FileOutputStream(file)
            fOut.write(data)
            fOut.close()
        }
        return dataResult?.data
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ),
                1
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    return true
                }
            }
        }
        return false
    }


}