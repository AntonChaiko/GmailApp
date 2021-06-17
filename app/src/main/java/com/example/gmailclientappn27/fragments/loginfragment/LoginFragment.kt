package com.example.gmailclientappn27.fragments.loginfragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.gmailclientappn27.R
import com.example.gmailclientappn27.UserMessagesModelClass
import com.example.gmailclientappn27.database.Messages
import com.example.gmailclientappn27.database.MessagesViewModel
import com.example.gmailclientappn27.databinding.FragmentLoginBinding
import com.example.gmailclientappn27.fragments.basefragment.BaseFragment
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.gmail.Gmail
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*


private const val TAG = "LoginFragment"
private const val RC_SIGN_IN = 0
private const val RQ_FIREBASE_AUTH = 1

class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private lateinit var mMessagesViewModel: MessagesViewModel
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var credential: GoogleAccountCredential
    private lateinit var service: Gmail
    private lateinit var auth: FirebaseAuth
    private lateinit var mLoginFragmentViewModel: LoginFragmentViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        mMessagesViewModel = ViewModelProvider(this).get(MessagesViewModel::class.java)
        mLoginFragmentViewModel = ViewModelProvider(this).get(LoginFragmentViewModel::class.java)

        mGoogleSignInClient = mLoginFragmentViewModel.createRequest(
            getString(R.string.default_web_client_id),
            requireActivity()
        )

        credential = mLoginFragmentViewModel.getCredential(requireActivity())

        service = mLoginFragmentViewModel.getService(credential)

        connectAuthenticate()

        binding.signInButton.setOnClickListener {
            signIn()
        }
        binding.loginButton.setOnClickListener {
            startMessageFragment()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        if (mLoginFragmentViewModel.isOnline(requireContext())) {
            startMessageFragment()
        } else {
            findNavController().navigate(R.id.action_loginFragment_to_messagesFragment)
        }
    }

    private fun startMessageFragment() {
        if (auth.currentUser != null) {
//            binding.progressBar.visibility = ProgressBar.VISIBLE
            GlobalScope.launch(Dispatchers.IO) {
                mLoginFragmentViewModel.readEmail(service, mMessagesViewModel)
                CoroutineScope(Dispatchers.Main).launch {
                    findNavController().navigate(R.id.action_loginFragment_to_messagesFragment)

                }
            }
        }
//                binding.progressBar.visibility = ProgressBar.GONE
    }



private fun signIn() {
    val signInIntent = mGoogleSignInClient.signInIntent;
    startActivityForResult(signInIntent, RC_SIGN_IN)
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

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == RC_SIGN_IN) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            mLoginFragmentViewModel.firebaseAuthWithGoogle(account.idToken!!, auth)
        } catch (e: ApiException) {
            Log.d(TAG, e.message.toString())
        }
    }
}

override fun getFragmentView(): Int {
    return R.layout.fragment_login
}
}


