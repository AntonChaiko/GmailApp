package com.example.gmailclientappn27.fragments.messagesfragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.gmailclientappn27.R
import com.example.gmailclientappn27.adapters.MessageFragmentAdapter
import com.example.gmailclientappn27.database.Messages
import com.example.gmailclientappn27.database.MessagesDatabase
import com.example.gmailclientappn27.databinding.FragmentMessagesBinding
import com.example.gmailclientappn27.fragments.basefragment.BaseFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth


class MessagesFragment : BaseFragment<FragmentMessagesBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val messagesDatabase = Room.databaseBuilder(requireContext(),MessagesDatabase::class.java,"messages_table")
            .allowMainThreadQueries()
            .build()

        val d:List<Messages>? = messagesDatabase.messagesDao()?.getAllMessages()

        binding.messagesRecyclerView.adapter = MessageFragmentAdapter(d,requireContext())

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()

        binding.exitButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            googleSignInClient.signOut()
            findNavController().navigate(R.id.action_messagesFragment_to_loginFragment)
        }

    }

    override fun getFragmentView(): Int {
        return R.layout.fragment_messages
    }
}