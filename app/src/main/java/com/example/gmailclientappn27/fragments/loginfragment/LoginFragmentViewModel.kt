package com.example.gmailclientappn27.fragments.loginfragment

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.example.gmailclientappn27.UserMessagesModel
import com.example.gmailclientappn27.UserMessagesModelClass
import com.example.gmailclientappn27.database.Messages
import com.example.gmailclientappn27.database.MessagesViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LoginFragmentViewModel : ViewModel() {

    fun getService(credential: GoogleAccountCredential): Gmail {
        return Gmail.Builder(
            NetHttpTransport(), AndroidJsonFactory.getDefaultInstance(), credential
        )
            .setApplicationName("GmailClientAppN27")
            .build()
    }

    fun getCredential(fragmentActivity: FragmentActivity): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(
            fragmentActivity.applicationContext, listOf(GmailScopes.GMAIL_READONLY)
        )
            .setBackOff(ExponentialBackOff())
            .setSelectedAccountName(FirebaseAuth.getInstance().currentUser?.email)
    }

    fun createRequest(clientId: String, fragmentActivity: FragmentActivity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(fragmentActivity, gso)
    }

     suspend fun readEmail(service: Gmail, mMessagesViewModel: MessagesViewModel) {
        try {
            val executeResult = getResultContent(service)

            val message = executeResult?.messages

            var index = 0
            while (index < message?.size!!) {
                val messageRead = withContext(Dispatchers.IO) {
                    service.users().messages()
                        ?.get(
                            FirebaseAuth.getInstance().currentUser?.email,
                            message?.get(index)?.id
                        )
                        ?.setFormat("full")?.execute()

                }
                val headers = messageRead!!.payload.headers

                var date = ""
                var from = ""
                var subject = ""
                headers.forEach {
                    when (it.name) {
                        "Date" -> date = it.value
                        "Subject" -> subject = it.value
                        "From" -> from = it.value
                    }
                }
                writeData(from, date, subject)

                if (index < message.size) {
                    mMessagesViewModel.addMessage(Messages(0, date, subject, from))
                }

                index++
            }

        } catch (e: UserRecoverableAuthIOException) {
        }

    }

     suspend fun getResultContent(service: Gmail): ListMessagesResponse? {
        return withContext(Dispatchers.IO) {
            service.users().messages()?.list("me")?.setQ("to:me")?.execute()
        }
    }

    fun writeData(from: String, date: String, subject: String) {
        UserMessagesModelClass.dataObject.add(UserMessagesModel(date, from, subject))
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
     fun firebaseAuthWithGoogle(idToken: String,mAuth: FirebaseAuth) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
    }


}