package com.example.gmailclientappn27.fragments.messagesfragment

import android.os.Environment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.example.gmailclientappn27.UserMessagesModel
import com.example.gmailclientappn27.UserMessagesModelClass
import com.example.gmailclientappn27.database.Messages
import com.example.gmailclientappn27.database.MessagesViewModel
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base64
import java.io.File
import java.io.FileOutputStream

class MessagesFragmentViewModel : ViewModel() {
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

    suspend fun readEmail(service: Gmail, mMessagesViewModel: MessagesViewModel) {
        try {
            val executeResult = getResultContent(service)

            val message = executeResult?.messages
            var index = 0
            mMessagesViewModel.deleteAllMessages()
            while (index < message?.size!!) {
                val messageRead = withContext(Dispatchers.IO) {
                    service.users().messages()
                        ?.get(
                            FirebaseAuth.getInstance().currentUser?.email,
                            message?.get(index)?.id
                        )
                        ?.setFormat("full")?.execute()
                }

                val id = messageRead?.id!!

                val headers = messageRead.payload.headers
                val body = messageRead.payload.parts
                var filename = ""
                var date = ""
                var from = ""
                var subject = ""
                var attachmentId = ""
                body.forEach {
                    if (!it.body.attachmentId.isNullOrEmpty() && !it.filename.isNullOrEmpty()) {
                        attachmentId = it.body.attachmentId
                        filename = it.filename
                    }
                }
                headers.forEach {
                    when (it.name) {
                        "Date" -> date = it.value
                        "Subject" -> subject = it.value
                        "From" -> from = it.value
                    }
                }
                writeData(from, date, subject, attachmentId, id, filename)

                if (index < message.size) {
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
                index++
            }

        } catch (e: UserRecoverableAuthIOException) {
        }

    }

    suspend fun getData(service: Gmail, id: String, attachId: String, filename:String): String? {
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

    private suspend fun getResultContent(service: Gmail): ListMessagesResponse? {
        return withContext(Dispatchers.IO) {
            service.users().messages()?.list("me")?.setQ("to:me")?.execute()
        }
    }

    private fun writeData(
        from: String,
        date: String,
        subject: String,
        attachmentId: String,
        messageId: String,
        filename: String
    ) {
        UserMessagesModelClass.dataObject.add(
            UserMessagesModel(
                date,
                from,
                subject,
                attachmentId,
                messageId,
                filename
            )
        )
    }


}