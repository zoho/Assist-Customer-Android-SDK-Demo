package com.zoho.assist.customer.demo

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.SessionCallbacks
import com.zoho.assist.customer.demo.databinding.ActivityMainBinding
import com.zoho.assist.customer.model.ChatModel


class ISessionCallbacks(private val activity: Activity, private val binding: ActivityMainBinding) :SessionCallbacks {


    lateinit var dialog: AlertDialog
    /**
     *   param - response
     *   To perform any operation using the response from validating the token and session key
     */
    override fun onValidationResponse(response: String,responseCode:AssistSession.ApiResponse) {
        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show()
        when (responseCode) {
            AssistSession.ApiResponse.SUCCESS -> {
                activity.onDismiss()

            }
            AssistSession.ApiResponse.ERROR -> {
                activity.onDismiss()
                activity.finish()
                activity.startActivity(Intent(activity, JoinActivity::class.java))

            }
        }

    }

    /**
     * To perform any operation when session gets connected successfully
     */
    override fun onSessionStarted() {
        activity.runOnUiThread {
            binding.helloText.text = ("\nStarting Session")//no i18n
            binding.closeSession.isEnabled = true
            binding.startShare.isEnabled = false
            binding.stopShare.isEnabled = true
            binding.sendMessage.isEnabled = true
            binding.startSession.isEnabled = false

            if (::dialog.isLateinit) {
                if (::dialog.isInitialized) {
                    closeCustomDialog(dialog)
                }
            }
        }
    }



    /**
     * To perform any operation after session ended
     */
    override fun onSessionEnded() {
        activity. runOnUiThread{
            binding.helloText.append("\n Session Ended")//no i18n
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


    /**
     *  param - chatModel
     * To manipulate the chat message object for addition to the chat history list and other info
     */
    override fun onMessageReceived(chatModel: ChatModel) {
//        activity. runOnUiThread {
//            (activity as MainActivity).getChatFragemnt().onReceived(chatModel)
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
     * Requesting the customer to trigger the addon download via playstore.
     */
    override fun onAddOnAvailableForDownload() {
        /***
         * Triggers addon download
         */
        AssistSession.INSTANCE.onAddOnDownload()
    }


}



