package com.example.gmailclientappn27.fragments.messagesfragment

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.gmail.Gmail
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base64
import java.io.File
import java.io.FileOutputStream


class MessagesFragment : BaseFragment<FragmentMessagesBinding>() {

    private lateinit var mMessagesFragmentViewModel: MessagesFragmentViewModel
    private lateinit var mMessageViewModel: MessagesViewModel

    lateinit var credential: GoogleAccountCredential
    lateinit var service: Gmail
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMessagesFragmentViewModel =
            ViewModelProvider(this).get(MessagesFragmentViewModel::class.java)
        mMessageViewModel = ViewModelProvider(this).get(MessagesViewModel::class.java)
        mMessageViewModel.deleteAllMessages()
        connectAuthenticate()
        credential = mMessagesFragmentViewModel.getCredential(requireActivity())
        service = mMessagesFragmentViewModel.getService(credential)

        val messagesDatabase =
            Room.databaseBuilder(requireContext(), MessagesDatabase::class.java, "messages_table")
                .allowMainThreadQueries()
                .build()

        val d: List<Messages>? = messagesDatabase.messagesDao()?.getAllMessages()

        val adapter = MessageFragmentAdapter(d, requireContext(),service,mMessagesFragmentViewModel)

        CoroutineScope(Dispatchers.Main).launch {

            mMessagesFragmentViewModel.readEmail(
                service,
                mMessagesViewModel = mMessageViewModel
            )
            adapter.notifyDataSetChanged()
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
            UserMessagesModelClass.dataObject.clear()
            mMessageViewModel.deleteAllMessages()
            FirebaseAuth.getInstance().signOut()
            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            googleSignInClient.signOut()
            findNavController().navigate(R.id.action_messagesFragment_to_loginFragment)
        }

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

    suspend fun getData(id: String, attachId: String): String? {
        val dataResult = withContext(Dispatchers.IO) {
            service.users().messages().attachments()
                ?.get(
                    FirebaseAuth.getInstance().currentUser?.email,
                    id, attachId
                )?.execute()
        }

        CoroutineScope(Dispatchers.IO).launch {
            Log.w("asd", "saveAttachmentsAsync start")
            val data = Base64.decodeBase64(dataResult?.data)
            val file = File(
                "${
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                }/${dataResult?.data}"
            )

            file.createNewFile()
            val fOut = FileOutputStream(file)
            fOut.write(data)
            fOut.close()

            Log.w("asd", "saveAttachmentsAsync end")
        }
        return dataResult?.data
    }
}