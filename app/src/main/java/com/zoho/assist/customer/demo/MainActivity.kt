package com.zoho.assist.customer.demo

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProviders
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.chat.view.ChatFragment
import com.zoho.assist.customer.chat.viewmodel.ChatViewModel
import com.zoho.assist.customer.util.Constants
import com.zoho.assist.customer.demo.JoinActivity.Companion.SESSION_KEY
import com.zoho.assist.customer.demo.databinding.ActivityMainBinding
import java.util.*

@SuppressLint("Registered")
class MainActivity : AppCompatActivity() {

    private lateinit var chatFragemnt: ChatFragment
    lateinit var viewDataBinding: ActivityMainBinding
    private val viewModelClass: Class<ChatViewModel>
        get() = ChatViewModel::class.java

    private val viewModel: AndroidViewModel by lazy {
        ViewModelProviders.of(this).get(viewModelClass)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        callback = ISessionCallbacks(this, viewDataBinding)
        /**
         *  callback - Retains the callback instance
         */
        AssistSession.INSTANCE.onCreate(this, callback)

        val result = viewDataBinding.setVariable(BR.chatScreenViewModel, viewModel)
        if (!result) {
            throw RuntimeException("ViewModel variable not set. Check the types")
        }
        chatFragemnt = ChatFragment()
        supportFragmentManager.beginTransaction().replace(R.id.chat_container, getChatFragment())
            .commit()
        onViewCreate(savedInstanceState)
        viewDataBinding.executePendingBindings()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val ack = AssistSession.INSTANCE.onActivityResult(requestCode, resultCode, data)
        resetStartStopButtons()
        if (requestCode == 100) {
        }
    }

    /**
     * ===============================================================================
     * onStop(), onResume() are needed only when the Floating Head feature is enabled
     *
     *
     */

    /**
     * Called when the app is no longer visible to the user
     */

    override fun onStop() {
        super.onStop()
        AssistSession.INSTANCE.onStop()
    }

    /**
     *Called after your app starts interacting with the user. This is an indicator that the app became active and visible to the user.
     */
    override fun onResume() {
        super.onResume()
        AssistSession.INSTANCE.onResume()
    }

    /**
     * ===============================================================================
     */

    private lateinit var callback: ISessionCallbacks

    private fun resetStartStopButtons() {
        if (AssistSession.INSTANCE.isScreenSharing()) {
            viewDataBinding.startShare.isEnabled = false
            viewDataBinding.stopShare.isEnabled = true
        } else {
            viewDataBinding.startShare.isEnabled = true
            viewDataBinding.stopShare.isEnabled = false
        }
    }

    private fun onViewCreate(savedInstanceState: Bundle?) {
        if (AssistSession.INSTANCE.isSessionAlive()) {
            callback.onSessionStarted()
            resetStartStopButtons()
        } else {
            if (intent != null) {
                if (intent.getStringExtra(SESSION_KEY).isNullOrEmpty() && !AssistSession.INSTANCE.isSessionAlive()) {
                    returnToJoinSessionActivity()
                } else if (intent.action == null) {
                    val authToken = intent.getStringExtra("AuthToken")
                    val serverURL = intent.getStringExtra("SERVER")
                    intent.getStringExtra(SESSION_KEY)?.let {
                        if (authToken != null) {
                            if (serverURL != null) {
                                onStartSession(
                                    it,
                                    authToken,
                                    serverURL
                                )
                            }
                        }
                    }
                }
            }
        }

        //click listeners
        viewDataBinding.closeSession.setOnClickListener {

            AssistSession.INSTANCE.onCustomerEndSession()

            viewDataBinding.startShare.isEnabled = false
            viewDataBinding.stopShare.isEnabled = false
            viewDataBinding.sendMessage.isEnabled = false
            viewDataBinding.startSession.isEnabled = true
            viewDataBinding.closeSession.isEnabled = false
            viewDataBinding.switchRole.isEnabled = false
            intent.putExtra(SESSION_KEY, "")
            viewDataBinding.helloText.append("\nSession Stopped")//no i18n

        }
        viewDataBinding.startSession.setOnClickListener(View.OnClickListener {
            //to stop the screen sharing

            if (intent.getStringExtra(SESSION_KEY).isNullOrEmpty()) {
                returnToJoinSessionActivity()
            } else if (intent.action == null && intent.hasExtra("AuthToken")
                && intent.hasExtra("SERVER") && intent.hasExtra(SESSION_KEY)
            ) {
                val authToken = intent.getStringExtra("AuthToken")
                val serverURL = intent.getStringExtra("SERVER")
                intent.getStringExtra(SESSION_KEY)?.let { it1 ->
                    if (authToken != null) {
                        if (serverURL != null) {
                            onStartSession(it1, authToken, serverURL)
                        }
                    }
                }
            }

        })

        viewDataBinding.startShare.setOnClickListener(View.OnClickListener {
            /***
             *
             */
            AssistSession.INSTANCE.onStartShare()
            viewDataBinding.helloText.append("\n Restart Sharing")//no i18n
            viewDataBinding.startShare.isEnabled = false
            viewDataBinding.stopShare.isEnabled = true
        })

        viewDataBinding.stopShare.setOnClickListener(View.OnClickListener {
            /**
             *
             */
            AssistSession.INSTANCE.onStopShare()
            viewDataBinding.helloText.append("\nStop Sharing")//no i18n
            viewDataBinding.stopShare.isEnabled = false
            viewDataBinding.startShare.isEnabled = true
        })
        viewDataBinding.switchRole.setOnClickListener(View.OnClickListener {
            AssistSession.INSTANCE.onStopShare()
            viewDataBinding.helloText.append("\nSwitchedRole")//no i18n
            viewDataBinding.switchRole.isEnabled = true

            /**
             *
             */
            AssistSession.INSTANCE.onRoleChangeInitiated()

        })

        viewDataBinding.sendMessage.setOnClickListener(View.OnClickListener {
            //send a chat message to the viewers
            viewDataBinding.helloText.append("\nSending message:: " + Date().toString())//no i18n
            var chatModel = AssistSession.INSTANCE.onSendMessage("message:: ${Date()}")
            chatFragemnt.notifyDatasetChanged()
        })

    }

    private fun returnToJoinSessionActivity() {
        if (this.isTaskRoot) {
            val intent = Intent(this@MainActivity, JoinActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        } else {
            onBackPressed()
        }
    }

    @SuppressLint("ServiceCast")
    private fun onStartSession(key: String, authToken: String, serverURL: String) {

        AssistSession.INSTANCE
            .setCallbacks(callback)   //pass over an instance of class implementing SessionCallbacks.
            // All events will be triggered here. (In Background thread)
            .setCustomerDetails(
                "surendran",
                "email@emailcom"
            ) // share username and userEmail - Default value is Guest
            .setAuthToken(authToken)
            .setPluginToast(true)
            .enableFloatingHead(true)
            .shareScreenOnStart(true)
            .downloadAddonOnStart(true)
            .setQuality(Constants.ColorQualityFactors.QUALITY75)
            .setKeepAliveNotification(getNotification())
            .start(
                key,
                MainActivity::class.java,
                R.drawable.assist_flat
            ) //this represent Activity Context,

    }

    fun getNotification(): Notification? {
        val channelId = getString(com.zoho.assist.customer.R.string.miscellaneous)
        val channelName = getString(com.zoho.assist.customer.R.string.miscellaneous)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = "com.zoho.assist.agent.main"
        notificationIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this,
                1427,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else{
            PendingIntent.getActivity(
                this,
                1427,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val title = String.format(getApplicationName())
        val message = String.format(
            "%s is currently running and the technician can see whatever is displayed on your screen",
            getApplicationName()
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(com.zoho.assist.customer.R.drawable.assist_flat)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun getApplicationName(): String {
        return try {
            val applicationInfo = applicationInfo
            val stringId = applicationInfo.labelRes
            if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else getString(stringId)
        } catch (ex: Exception) {
            ""
        }
    }

    override fun onBackPressed() {
        viewDataBinding.helloText.text = ("Back Pressed")//no i18n
        if (!AssistSession.INSTANCE.isSessionAlive()) {
            super.onBackPressed()
        } else {
            AssistSession.INSTANCE.onCustomerEndSession()
            viewDataBinding.startShare.isEnabled = false
            viewDataBinding.stopShare.isEnabled = false
            viewDataBinding.sendMessage.isEnabled = false
            viewDataBinding.startSession.isEnabled = true
            viewDataBinding.closeSession.isEnabled = false
            intent.putExtra("session_key", "")
            viewDataBinding.helloText.append("\nSession Stopped")//no i18n
            viewDataBinding.startSession.visibility = View.GONE
            returnToJoinSessionActivity()
        }

    }

    fun getChatFragment(): ChatFragment {
        return chatFragemnt
    }

}

