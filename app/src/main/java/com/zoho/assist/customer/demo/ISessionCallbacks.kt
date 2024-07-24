package com.zoho.assist.customer.demo

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.SessionCallbacks
import com.zoho.assist.customer.SessionStartFailure
import com.zoho.assist.customer.demo.databinding.ActivityMainBinding
import com.zoho.assist.customer.model.ChatModel
import com.zoho.assist.customer.model.ParticipantState
import java.util.logging.Level


class ISessionCallbacks(private val activity: Activity, private val binding: ActivityMainBinding) :
    SessionCallbacks {

    /**
     *   param - response
     *   To perform any operation using the response from validating the token and session key
     */


    /**
     * Callback used to perform any operation using the response gotten from validating the token and session key.
     */
    override fun onValidationResponse(response: String, responseCode: AssistSession.ApiResponse) {
        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show()
        when (responseCode) {
            AssistSession.ApiResponse.SUCCESS -> {
                binding.logView.text = ("Validation successful")
//                activity.onDismiss()
            }
            AssistSession.ApiResponse.ERROR -> {
                Toast.makeText(activity, "Validation failed $response", Toast.LENGTH_SHORT).show()
                returnToJoinSessionActivity()
            }
        }
    }

    override fun reconnectionLimitExceeded() {
        Toast.makeText(activity,"Reconnection Limit reached",Toast.LENGTH_SHORT).show()
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
        when (failure) {
            SessionStartFailure.BELOW_MIN_API_LEVEL -> {}
            SessionStartFailure.CONTEXT_NOT_AVAILABLE -> {}
            SessionStartFailure.INVALID_SDK_TOKEN -> {}
            SessionStartFailure.INVALID_SESSION_KEY -> {}
            SessionStartFailure.POST_NOTIFICATION_PERMISSION_DENIED -> {}
            else -> {}
        }
        Toast.makeText(activity.applicationContext, failure.message, Toast.LENGTH_SHORT).show()
//        activity.finish()
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
        activity. runOnUiThread {
            (activity as MainActivity).getChatFragment().onReceived(model)
        }
//        if (model.type == ChatModel.ChatMode.RECEIVED) {
//            binding.logView.append("\n${model.senderName}: ${model.msg}")
//            binding.logViewScrollView.postDelayed({
//                binding.logViewScrollView.fullScroll(View.FOCUS_DOWN)
//            }, 200)
//        }
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
     * Called when screen share is started, and can be used in conjunction
     * with `startRemoteControlOnStart(false)` to prompt user for remote control permission
     */
    override fun onScreenShareStarted() {
//        AssistSession.INSTANCE.startAddon()
        if (AssistSession.INSTANCE.isScreenSharing()) {
            binding.startShare.isEnabled = false
            binding.stopShare.isEnabled = true
        } else {
            binding.startShare.isEnabled = true
            binding.stopShare.isEnabled = false
        }
    }

    /**
     * Called when participant status changes
     */
    override fun onParticipantStateChange(participantState: ParticipantState, participantName: String) {
        when(participantState){
            ParticipantState.DOWN->{

            }
            ParticipantState.LOST->{

            }
            ParticipantState.UP->{

            }
        }
    }

    /**
     * Share name
     * Optional
     */
    override fun getClientName(): String? {
        return null
    }

    override fun logMsg(level: Level, msg: String) {
    }

    /**
     * Requesting the customer to trigger the addon download via playstore.
     */
    override fun onAddOnAvailableForDownload() {
//        AssistSession.INSTANCE.startAddon()
    }

    private fun returnToJoinSessionActivity() {
        if (activity.isTaskRoot) {
            activity.startActivity(Intent(activity, JoinActivity::class.java).setAction("your.custom.action"))
            activity.finish()
        } else {
            activity.onBackPressed()
        }
    }






}
