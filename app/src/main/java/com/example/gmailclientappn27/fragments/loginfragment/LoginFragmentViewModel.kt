package com.example.gmailclientappn27.fragments.loginfragment

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class LoginFragmentViewModel : ViewModel() {

    fun createRequest(clientId: String, fragmentActivity: FragmentActivity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(fragmentActivity, gso)
    }

     fun firebaseAuthWithGoogle(idToken: String,mAuth: FirebaseAuth) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
    }


}