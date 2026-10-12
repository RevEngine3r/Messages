package org.fossify.messages.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossify.commons.extensions.formatDate
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.messages.activities.SimpleActivity
import org.fossify.messages.databinding.ItemOtpMessageBinding
import org.fossify.messages.models.OtpMessage

class OtpMessagesAdapter(
    private val activity: SimpleActivity,
    private val items: ArrayList<OtpMessage>,
    private val itemClick: (OtpMessage) -> Unit,
    private val itemLongClick: (OtpMessage) -> Unit,
    private val copyClick: (OtpMessage) -> Unit,
) : RecyclerView.Adapter<OtpMessagesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOtpMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClick(items[position])
                }
            }
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemLongClick(items[position])
                }
                true
            }
            binding.otpCopy.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    copyClick(items[position])
                }
            }
            binding.otpCopy.imageTintList = ColorStateList.valueOf(activity.getProperTextColor())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemOtpMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val item = items[position]
            binding.otpSender.text = item.sender
            binding.otpCode.text = item.code
            binding.otpBody.text = item.body
            binding.otpDate.text = item.date.formatDate(activity)
        }
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<OtpMessage>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
