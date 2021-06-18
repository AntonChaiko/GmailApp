package com.example.gmailclientappn27.fragments.messagesfragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.room.Room
import com.example.gmailclientappn27.R
import com.example.gmailclientappn27.UserMessagesModelClass
import com.example.gmailclientappn27.adapters.MessageFragmentAdapter
import com.example.gmailclientappn27.database.Messages
import com.example.gmailclientappn27.database.MessagesDatabase
import com.example.gmailclientappn27.database.MessagesViewModel
import com.example.gmailclientappn27.databinding.FragmentMessagesBinding
import com.example.gmailclientappn27.fragments.basefragment.BaseFragment
import com.example.gmailclientappn27.fragments.loginfragment.RQ_FIREBASE_AUTH
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.gmail.Gmail
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MessagesFragment : BaseFragment<FragmentMessagesBinding>() {

    private lateinit var mMessagesFragmentViewModel: MessagesFragmentViewModel
    private lateinit var mMessageViewModel: MessagesViewModel

    lateinit var credential: GoogleAccountCredential
    lateinit var service: Gmail
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        checkPermissions()
        mMessagesFragmentViewModel =
            ViewModelProvider(this).get(MessagesFragmentViewModel::class.java)
        mMessageViewModel = ViewModelProvider(this).get(MessagesViewModel::class.java)

        connectAuthenticate()
        credential = mMessagesFragmentViewModel.getCredential(requireActivity())
        service = mMessagesFragmentViewModel.getService(credential)

        val messagesDatabase =
            Room.databaseBuilder(requireContext(), MessagesDatabase::class.java, "messages_table")
                .allowMainThreadQueries()
                .build()

        val d: List<Messages>? = messagesDatabase.messagesDao()?.getAllMessages()

        val adapter =
            MessageFragmentAdapter(d, requireContext(), service, mMessagesFragmentViewModel)
        if(isOnline(requireContext())){
            CoroutineScope(Dispatchers.Main).launch {
                binding.loadingCardView.visibility = View.VISIBLE
                mMessagesFragmentViewModel.readEmail(
                    service = service,
                    mMessagesViewModel = mMessageViewModel
                )
                adapter.notifyDataSetChanged()
                binding.loadingCardView.visibility = View.GONE

            }
        } else{
            Toast.makeText(requireContext(),"no internet",Toast.LENGTH_LONG).show()
        }

        binding.messagesRecyclerView.adapter = adapter
        binding.messagesRecyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.messagesRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        binding.exitButton.setOnClickListener {
            val sharedPreferences =
                requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.apply {
                putBoolean("BOOLEAN_KEY", false)
            }?.apply()
            UserMessagesModelClass.dataObject.clear()
            mMessageViewModel.deleteAllMessages()
            FirebaseAuth.getInstance().signOut()
            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            googleSignInClient.signOut()
            mMessageViewModel.deleteAllMessages()
            findNavController().navigate(R.id.action_messagesFragment_to_loginFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserMessagesModelClass.dataObject.clear()
    }
    override fun getFragmentView(): Int {
        return R.layout.fragment_messages
    }

    private fun connectAuthenticate() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                RQ_FIREBASE_AUTH
            )
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
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