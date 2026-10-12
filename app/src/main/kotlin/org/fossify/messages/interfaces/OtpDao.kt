package org.fossify.messages.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fossify.messages.models.OtpMessage

@Dao
interface OtpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(otpMessage: OtpMessage): Long

    @Query("SELECT * FROM otp_messages ORDER BY date DESC")
    fun getAll(): List<OtpMessage>

    @Query("SELECT COUNT(*) FROM otp_messages")
    fun getCount(): Int

    @Query("DELETE FROM otp_messages WHERE id = :id")
    fun delete(id: Long)

    @Query("DELETE FROM otp_messages")
    fun deleteAll()
}
