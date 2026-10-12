package org.fossify.messages.activities

import android.content.Intent
import android.os.Bundle
import org.fossify.commons.dialogs.ConfirmationDialog
import org.fossify.commons.extensions.areSystemAnimationsEnabled
import org.fossify.commons.extensions.beGoneIf
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.copyToClipboard
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.messages.R
import org.fossify.messages.adapters.OtpMessagesAdapter
import org.fossify.messages.databinding.ActivityOtpMessagesBinding
import org.fossify.messages.extensions.otpDB
import org.fossify.messages.helpers.THREAD_ID
import org.fossify.messages.helpers.THREAD_TITLE
import org.fossify.messages.models.OtpMessage

class OtpMessagesActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityOtpMessagesBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupOptionsMenu()

        setupEdgeToEdge(padBottomImeAndSystem = listOf(binding.otpMessagesList))
        setupMaterialScrollListener(
            scrollingView = binding.otpMessagesList,
            topAppBar = binding.otpAppbar
        )

        loadOtpMessages()
    }

    override fun onResume() {
        super.onResume()
        setupTopAppBar(binding.otpAppbar, NavigationIcon.Arrow)
        loadOtpMessages()
    }

    private fun setupOptionsMenu() {
        binding.otpToolbar.inflateMenu(R.menu.otp_menu)
        binding.otpToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete_all_otp -> confirmDeleteAll()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun updateOptionsMenu(itemsCount: Int) {
        binding.otpToolbar.menu.apply {
            findItem(R.id.delete_all_otp).isVisible = itemsCount > 0
        }
    }

    private fun loadOtpMessages() {
        binding.otpProgressBar.beVisibleIf(binding.otpMessagesList.adapter == null)
        ensureBackgroundThread {
            val items = try {
                otpDB.getAll()
            } catch (_: Exception) {
                ArrayList<OtpMessage>()
            }

            runOnUiThread {
                binding.otpProgressBar.beVisibleIf(false)
                setupItems(items)
            }
        }
    }

    private fun setupItems(items: List<OtpMessage>) {
        showOrHidePlaceholder(items.isEmpty())
        updateOptionsMenu(items.size)

        val adapter = getOrCreateAdapter()
        adapter.updateItems(items)
        if (areSystemAnimationsEnabled) {
            binding.otpMessagesList.scheduleLayoutAnimation()
        }
    }

    private fun getOrCreateAdapter(): OtpMessagesAdapter {
        var currAdapter = binding.otpMessagesList.adapter
        if (currAdapter == null) {
            currAdapter = OtpMessagesAdapter(
                activity = this,
                items = ArrayList(),
                itemClick = { handleItemClick(it) },
                itemLongClick = { confirmDeleteItem(it) },
                copyClick = { copyCode(it) }
            )
            binding.otpMessagesList.adapter = currAdapter
        }
        return currAdapter as OtpMessagesAdapter
    }

    private fun showOrHidePlaceholder(show: Boolean) {
        binding.otpFastscroller.beGoneIf(show)
        binding.noOtpPlaceholder.beVisibleIf(show)
        binding.noOtpPlaceholder.setTextColor(getProperTextColor())
        binding.noOtpPlaceholder.text = getString(R.string.no_otp_messages)
    }

    private fun copyCode(item: OtpMessage) {
        copyToClipboard(item.code)
    }

    private fun handleItemClick(item: OtpMessage) {
        if (item.threadId <= 0L) {
            return
        }

        Intent(this, ThreadActivity::class.java).apply {
            putExtra(THREAD_ID, item.threadId)
            putExtra(THREAD_TITLE, item.sender)
            startActivity(this)
        }
    }

    private fun confirmDeleteItem(item: OtpMessage) {
        ConfirmationDialog(
            activity = this,
            message = getString(R.string.delete_otp_message_confirmation),
            positive = org.fossify.commons.R.string.yes,
            negative = org.fossify.commons.R.string.no
        ) {
            ensureBackgroundThread {
                otpDB.delete(item.id!!)
                runOnUiThread {
                    loadOtpMessages()
                }
            }
        }
    }

    private fun confirmDeleteAll() {
        ConfirmationDialog(
            activity = this,
            message = getString(R.string.delete_all_otp_messages_confirmation),
            positive = org.fossify.commons.R.string.yes,
            negative = org.fossify.commons.R.string.no
        ) {
            ensureBackgroundThread {
                otpDB.deleteAll()
                runOnUiThread {
                    loadOtpMessages()
                }
            }
        }
    }
}
