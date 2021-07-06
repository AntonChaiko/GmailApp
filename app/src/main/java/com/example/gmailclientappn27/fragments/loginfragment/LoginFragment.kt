package com.example.gmailclientappn27.fragments.loginfragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.gmailclientappn27.R
import com.example.gmailclientappn27.database.MessagesViewModel
import com.example.gmailclientappn27.databinding.FragmentLoginBinding
import com.example.gmailclientappn27.fragments.basefragment.BaseFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


private const val TAG = "LoginFragment"
private const val RC_SIGN_IN = 0

class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private lateinit var mMessagesViewModel: MessagesViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var mLoginFragmentViewModel: LoginFragmentViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        mMessagesViewModel = ViewModelProvider(this).get(MessagesViewModel::class.java)
        mLoginFragmentViewModel = ViewModelProvider(this).get(LoginFragmentViewModel::class.java)

        googleSignInClient = mLoginFragmentViewModel.createRequest(
            getString(R.string.default_web_client_id),
            requireActivity()
        )

        binding.signInButton.setOnClickListener {
            signIn()
        }

    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences =
            requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedBoolean = sharedPreferences?.getBoolean("BOOLEAN_KEY", false)
        if (savedBoolean == true) {
            findNavController().navigate(R.id.action_loginFragment_to_messagesFragment)
        }

    }

    private fun signIn() {
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.d(TAG, e.message.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    saveSharedPrefs(binding.rememberSwitch.isChecked)
                    findNavController().navigate(R.id.action_loginFragment_to_messagesFragment)

                } else {
                    Log.d("asd", task.exception?.message.toString())
                }
            }
    }

    private fun saveSharedPrefs(switcher:Boolean){
        val sharedPreferences =
            requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.apply {
            putBoolean("BOOLEAN_KEY", switcher)
        }?.apply()
    }

    override fun getFragmentView(): Int = R.layout.fragment_login
}


