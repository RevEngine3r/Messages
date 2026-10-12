package org.fossify.messages.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "otp_messages",
    indices = [(Index(value = ["message_id", "is_mms"], unique = true))]
)
data class OtpMessage(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long? = null,
    @ColumnInfo(name = "message_id") var messageId: Long,
    @ColumnInfo(name = "is_mms") var isMms: Boolean,
    @ColumnInfo(name = "thread_id") var threadId: Long,
    @ColumnInfo(name = "sender") var sender: String,
    @ColumnInfo(name = "code") var code: String,
    @ColumnInfo(name = "body") var body: String,
    @ColumnInfo(name = "date") var date: Long,
)
