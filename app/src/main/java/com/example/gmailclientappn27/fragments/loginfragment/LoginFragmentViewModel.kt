package com.example.gmailclientappn27.fragments.loginfragment

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes


class LoginFragmentViewModel : ViewModel() {

    fun createRequest(clientId: String, fragmentActivity: FragmentActivity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestScopes(Scope(GmailScopes.MAIL_GOOGLE_COM))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(fragmentActivity, gso)
    }


}