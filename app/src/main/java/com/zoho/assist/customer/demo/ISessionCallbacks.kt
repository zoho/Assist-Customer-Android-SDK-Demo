/* $Id$ */
package com.zoho.assist.customer.demo

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.SessionCallbacks
import com.zoho.assist.customer.SessionStartFailure
import com.zoho.assist.customer.demo.databinding.ActivityMainBinding
import com.zoho.assist.customer.model.ChatModel
import com.zoho.assist.customer.model.ParticipantState

class ISessionCallbacks(private val activity: Activity, private val binding: ActivityMainBinding) :SessionCallbacks {

    /**
     * Requesting the customer to trigger the addon download via playstore.
     */
    override fun onAddOnAvailableForDownload() {
        /***
         * Start addon after downloading via some other method
         */
        AssistSession.INSTANCE.startAddon()
    }


    lateinit var dialog: AlertDialog
    /**
     *   param - response
     *   To perform any operation using the response from validating the token and session key
     */
    override fun onValidationResponse(response: String,code:AssistSession.ApiResponse) {
        Toast.makeText(activity.applicationContext, response, Toast.LENGTH_SHORT).show()
        when (code) {
            AssistSession.ApiResponse.SUCCESS -> {
                activity.onDismiss()
            }
            AssistSession.ApiResponse.ERROR -> {
                activity.onDismiss()
                returnToJoinSessionActivity()
            }
        }
    }
    /**
     * To perform any operation when session gets connected successfully
     */
    override fun onSessionStarted() {
        activity.runOnUiThread {
            if (::dialog.isLateinit) {
                if (::dialog.isInitialized) {
                    closeCustomDialog(dialog)
                }
            }
            binding.helloText.text = ("\nStarting Session")//no i18n
            binding.closeSession.isEnabled = true
            binding.sendMessage.isEnabled = true
            binding.switchRole.isEnabled = true
            binding.startSession.isEnabled = false

        }
    }

    /**
     * Used to handle any exception that prevents session start
     */
    override fun onSessionStartFailed(failure: SessionStartFailure) {
        when (failure) {
            SessionStartFailure.BELOW_MIN_API_LEVEL -> {}
            SessionStartFailure.CONTEXT_NOT_AVAILABLE -> {}
            SessionStartFailure.INVALID_SDK_TOKEN -> {}
            SessionStartFailure.INVALID_SESSION_KEY -> {}
            else -> {

            }
        }
        Toast.makeText(activity.applicationContext, failure.message, Toast.LENGTH_SHORT).show()
        returnToJoinSessionActivity()
    }

    /**
     * To perform any operation after session ended
     */
    override fun onSessionEnded() {
        activity. runOnUiThread{
            binding.helloText.append("\n Session Closed")//no i18n
            Toast.makeText(activity.applicationContext, "Session closed", Toast.LENGTH_SHORT).show()
            returnToJoinSessionActivity()
        }
    }

    /**
     * To request screen share manually if not enabled in shareScreenOnStart
     */
    override fun onScreenShareRequest() {
        /**
         * true - Approve
         * false - Reject
         */

        AssistSession.INSTANCE.startScreenSharing(true)
    }

    override fun onScreenShareStarted() {
        // Show prompt for starting addon if needed.
        if (AssistSession.INSTANCE.isScreenSharing()) {
            binding.startShare.isEnabled = false
            binding.stopShare.isEnabled = true
        } else {
            binding.startShare.isEnabled = true
            binding.stopShare.isEnabled = false
        }
    }

    /**
     *  param - chatModel
     * To manipulate the chat message object for addition to the chat history list and other info
     */
    override fun onMessageReceived(chatModel: ChatModel) {
        activity. runOnUiThread {
            (activity as MainActivity).getChatFragment().onReceived(chatModel)
        }
    }

    /**
     * Switch the role of customer into technician
     */
    override fun onRoleChangeRequest() {
        /**
         * true - Approve
         * false - Reject
         */
        AssistSession.INSTANCE.onRoleChangeRequestAccepted(true)
    }

    private fun returnToJoinSessionActivity() {
        if (activity.isTaskRoot) {
            val intent = Intent(activity, JoinActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                activity.onBackPressed()
            }
        }
    }

    /**
     *
     */
    override fun onParticipantStateChange(participantState: ParticipantState, participantName: String) {
    }

    override fun getClientName(): String? {
        return null
    }

}
