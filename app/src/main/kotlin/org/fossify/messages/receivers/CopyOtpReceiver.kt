package org.fossify.messages.receivers

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import org.fossify.commons.extensions.notificationManager
import org.fossify.messages.R
import org.fossify.messages.helpers.COPY_OTP
import org.fossify.messages.helpers.OTP_CODE
import org.fossify.messages.helpers.THREAD_ID

// handles the "Copy OTP" action shown on incoming-message notifications: it copies the detected
// verification code into the clipboard and dismisses the notification
class CopyOtpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            COPY_OTP -> {
                val code = intent.getStringExtra(OTP_CODE)
                if (code.isNullOrEmpty()) {
                    return
                }

                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(code, code))

                val threadId = intent.getLongExtra(THREAD_ID, 0L)
                context.notificationManager.cancel(threadId.hashCode())

                Toast.makeText(context, context.getString(R.string.otp_copied), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
