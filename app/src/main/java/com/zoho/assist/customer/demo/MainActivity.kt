package com.zoho.assist.customer.demo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import java.util.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.base.BaseActivity
import com.zoho.assist.customer.model.ChatModel
import com.zoho.assist.customer.demo.R
import com.zoho.assist.customer.demo.databinding.ActivityMainBinding
import com.zoho.assist.customer.util.Constants


@SuppressLint("Registered")
class MainActivity : BaseActivity<ActivityMainBinding>() {

    lateinit var dialog: AlertDialog

    /**
     * Returns the layout resource of the activity
     */
    override fun getContentView(): Int {
        return R.layout.activity_main
    }

    /**
     * Returns the app icon
     */
    override fun getLauncherIcon(): Int {
        return R.mipmap.ic_launcher
    }



    /**
     * To perform any operation when session gets connected successfully
     */
    override fun onSessionStarted() {
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

    /**
     * To perform any operation after session closed
     */
    override fun onSessionClosed() {
        binding.helloText.append("\n Session Closed")//no i18n
    }

    /**
     * To perform any operation when any technician leaves or gets lost from the session
     */
    override fun onConnectionDown() {
        binding.helloText.append("\n Connection Lost")//no i18n
    }

    /**
     * param viewersCount
     *
     * To perform any operation when any technician joins the session
     */
    override fun onConnectionUp(viewersCount: Int) {
        binding.helloText.append("\n Technicians:: $viewersCount")//no i18n
    }

    /**
     * param - msg
     * To show any message
     */
    override fun showToast(msg: String) {
    }

    /**
     * param - isAccepted
     *  To perform any operation during a switch role request based on the acceptance state
     */

    override fun onSwitchRoles(isAccepted: Boolean) {
        binding.helloText.append("\n Switched Roles:: $isAccepted")//no i18n
    }

    /**
     * param - isSharing
     * To perform any operation when customer toggles the screen sharing status
     *
     */
    override fun onStartStopShare(isSharing: Boolean) {
    }

    /**
     *  param - chatModel
     * To manipulate the chat message object for addition to the chat history list
     */
    override fun onReceivedMessage(chatModel: ChatModel) {
        binding.helloText.append("\nReceived message:: " + chatModel.msg)//no i18n
    }


    private lateinit var callback: ISessionCallbacks

    override fun onViewCreate(savedInstanceState: Bundle?) {
        callback = ISessionCallbacks(this, binding)
        if(intent!=null) {


            if (intent.getStringExtra(Constants.SESSION_KEY).isEmpty()) {
                joinSessionActivity()

            } else if(intent.action==null){
                val authToken= intent.getStringExtra("AuthToken")
                onStartSession(intent.getStringExtra(Constants.SESSION_KEY),authToken )
            }
        }

        //click listeners
        binding.closeSession.setOnClickListener {
            onCloseSession(object : ICloseCallback {
                override fun onDone() {
                    binding.startShare.isEnabled = false
                    binding.stopShare.isEnabled = false
                    binding.sendMessage.isEnabled = false
                    binding.startSession.isEnabled = true
                    binding.closeSession.isEnabled = false
                    sessionKey = ""
                    binding.helloText.append("\nSession Stopped")//no i18n
                    joinSessionActivity()
                    Toast.makeText(this@MainActivity, "Closed the session", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onCancel() {
                }

            })

        }
        binding.startSession.setOnClickListener(View.OnClickListener {

            if (intent.getStringExtra(Constants.SESSION_KEY).isEmpty()) {
                joinSessionActivity()

            } else if(intent.action==null){
                val authToken= intent.getStringExtra("AuthToken")
                onStartSession(intent.getStringExtra(Constants.SESSION_KEY),authToken )
            }

        })

        binding.startShare.setOnClickListener(View.OnClickListener {
            //to restart the screen sharing
            onStartShare()
            binding.helloText.append("\n Restart Sharing")//no i18n
            binding.startShare.isEnabled = false
            binding.stopShare.isEnabled = true
        })

        binding.stopShare.setOnClickListener(View.OnClickListener {
            //to stop the screen sharing
            onStopShare()
            binding.helloText.append("\nStop Sharing")//no i18n
            binding.stopShare.isEnabled = false
            binding.startShare.isEnabled = true
        })

        binding.sendMessage.setOnClickListener(View.OnClickListener {
            //send a chat message to the viewers
            binding.helloText.append("\nSending message:: " + Date().toString())//no i18n
            AssistSession.INSTANCE.onSendMessage("message:: " + Date().toString())//no i18n
        })
//        binding.startSession.visibility = View.GONE



    }




    private fun joinSessionActivity() {
        startActivity( Intent(this@MainActivity,JoinActivity::class.java))
        finish()

    }


    private fun onStartSession(key: String, authToken: String) {
        //Assist Agent init
        sessionKey=key
        AssistSession.INSTANCE
            .setCallbacks(callback)//pass over an instance of class implementing AssistSessionCallbacks.
            // All events will be triggered here. (In Background thread)
            .setUserName("Guest") // share username - Default valus is Guest
            .setUserEmail("guest@zoho.com")// email - Default value is Guest
            .setToken(authToken)
            .onOverlayFloating(MainActivity::class.java,R.drawable.assist_flat) //  drawable / -1
            .init(this, sessionKey) //this represent Activity Context,
    }




    override fun onBackPressed() {
        binding.helloText.text = ("\nBack pressed")//no i18n
        if(sessionKey.isNullOrEmpty()) {
            super.onBackPressed()
        }else{
            onCloseSession(object:ICloseCallback{
                override fun onDone() {
                    if (::dialog.isLateinit) {
                        binding.startShare.isEnabled = false
                        binding.stopShare.isEnabled = false
                        binding.sendMessage.isEnabled = false
                        binding.startSession.isEnabled = true
                        binding.closeSession.isEnabled = false
                        sessionKey = ""
                        binding.helloText.append("\nSession Stopped")//no i18n
                        joinSessionActivity()
                        binding.startSession.visibility = View.GONE
                        finish()
                    }

                }

                override fun onCancel() {

                }

            })
        }

    }

}

