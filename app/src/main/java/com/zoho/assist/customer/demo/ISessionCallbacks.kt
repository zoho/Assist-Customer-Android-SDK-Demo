package com.zoho.assist.customer.demo

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.SessionCallbacks
import com.zoho.assist.customer.SessionStartFailure
import com.zoho.assist.customer.demo.databinding.ActivityMainBinding
import com.zoho.assist.customer.model.ChatModel


class ISessionCallbacks(private val activity: Activity, private val binding: ActivityMainBinding) :
    SessionCallbacks {

    /**
     *   param - response
     *   To perform any operation using the response from validating the token and session key
     */
    override fun onValidationResponse(response: String, responseCode: AssistSession.ApiResponse) {
        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show()
        when (responseCode) {
            AssistSession.ApiResponse.SUCCESS -> {
                binding.logView.text = ("Validation successful") //no i18n
            }
            AssistSession.ApiResponse.ERROR -> {
                Toast.makeText(activity, "Validation failed $response", Toast.LENGTH_SHORT).show()
                returnToJoinSessionActivity()
            }
        }
    }

    /**
     * To perform any operation when session gets connected successfully
     */
    override fun onSessionStarted() {
        binding.logView.append("\nStarting Session")//no i18n
        binding.closeSession.isEnabled = true
        binding.sendMessage.isEnabled = true
        binding.startSession.isEnabled = false
        if (AssistSession.INSTANCE.isScreenSharing()) {
            binding.startShare.isEnabled = false
            binding.stopShare.isEnabled = true
        } else {
            binding.startShare.isEnabled = true
            binding.stopShare.isEnabled = false
        }
    }

    /**
     * To handle session start failure cases
     */
    override fun onSessionStartFailed(failure: SessionStartFailure) {
        val message: String = when (failure) {
            SessionStartFailure.BELOW_MIN_API_LEVEL -> "Minimum supported android version is Lollipop"
            SessionStartFailure.CONTEXT_NOT_AVAILABLE -> "Application context not provided"
            SessionStartFailure.INVALID_SDK_TOKEN -> "SDK Token is invalid"
            SessionStartFailure.INVALID_SESSION_KEY -> "Session Key is invalid"
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        returnToJoinSessionActivity()
    }

    /**
     * To perform any operation after session ended
     */
    override fun onSessionEnded() {
        binding.logView.append("\nSession Ended")//no i18n
        returnToJoinSessionActivity()
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

    /**
     *  param - chatModel
     * To manipulate the chat message object for addition to the chat history list and other info
     */
    override fun onMessageReceived(model: ChatModel) {
        if (model.type == ChatModel.ChatMode.RECEIVED) {
            binding.logView.append("\n${model.senderName}: ${model.msg}")
            binding.logViewScrollView.postDelayed({
                binding.logViewScrollView.fullScroll(View.FOCUS_DOWN)
            }, 200)
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

    /**
     * Requesting the customer to trigger the addon download via playstore.
     */
    override fun onAddOnAvailableForDownload() {
        AssistSession.INSTANCE.startAddon()
    }

    private fun returnToJoinSessionActivity() {
        if (activity.isTaskRoot) {
            activity.startActivity(Intent(activity, JoinActivity::class.java))
            activity.finish()
        } else {
            activity.onBackPressed()
        }
    }

}



