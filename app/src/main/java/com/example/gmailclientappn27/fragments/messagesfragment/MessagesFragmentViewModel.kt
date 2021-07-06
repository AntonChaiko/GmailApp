package com.example.gmailclientappn27.fragments.messagesfragment

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gmailclientappn27.database.Messages
import com.example.gmailclientappn27.database.MessagesViewModel
import com.example.gmailclientappn27.models.UserMessagesModel
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagesFragmentViewModel : ViewModel() {

    private var _messages = MutableLiveData<List<UserMessagesModel>>()
    val messages: MutableLiveData<List<UserMessagesModel>> = _messages

    private val list = ArrayList<UserMessagesModel>()

    fun getService(credential: GoogleAccountCredential): Gmail {
        return Gmail.Builder(
            NetHttpTransport(), AndroidJsonFactory.getDefaultInstance(), credential
        )
            .setApplicationName("GmailClientAppN27")
            .build()

    }

    fun getCredential(fragmentActivity: FragmentActivity): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(
            fragmentActivity.applicationContext, listOf(GmailScopes.MAIL_GOOGLE_COM)
        )
            .setBackOff(ExponentialBackOff())
            .setSelectedAccountName(FirebaseAuth.getInstance().currentUser?.email)
    }


    suspend fun readEmail(service: Gmail, mMessagesViewModel: MessagesViewModel) {
        try {
            val executeResult: ListMessagesResponse? = getResultContent(service)
            val message: List<Message>? = executeResult!!.messages

            for (i in message!!.indices) {
                val messageRead: Message? = messagesRead(i, service, message)
                val id = messageRead?.id!!
                val headers = messageRead.payload.headers


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


                var filename = ""
                var attachmentId = ""
                messageRead.payload.parts?.forEach {
                    if (!it.body.attachmentId.isNullOrEmpty() && !it.filename.isNullOrEmpty()) {
                        attachmentId = it.body.attachmentId
                        filename = it.filename
                    }
                }

                list.add(UserMessagesModel(date, from, subject, attachmentId, id, filename))
                _messages.postValue(list)

                if (i < message.size) {
                    mMessagesViewModel.addMessage(
                        Messages(
                            0,
                            date,
                            subject,
                            from,
                            attachmentId,
                            id
                        )
                    )
                }
            }
        } catch (e: UserRecoverableAuthIOException) {
            Log.d("asd", e.message.toString())
        }

    }


    private suspend fun getResultContent(service: Gmail): ListMessagesResponse? {
        return withContext(Dispatchers.IO) {
            service.users().messages()?.list("me")?.setQ("to:me")?.execute()
        }
    }

    private suspend fun messagesRead(i: Int, service: Gmail, message: List<Message>?): Message? {
        return withContext(Dispatchers.IO) {
            service.users().messages()
                ?.get(FirebaseAuth.getInstance().currentUser?.email, message!![i].id)
                ?.setFormat("full")?.execute()
        }
    }

}