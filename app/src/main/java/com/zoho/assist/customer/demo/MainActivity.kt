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
import java.util.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.demo.Constants.NOTIFICATION_ACTION
import com.zoho.assist.customer.demo.Constants.NOTIFICATION_PENDING_INTENT_ID
import com.zoho.assist.customer.demo.Constants.SDK_TOKEN
import com.zoho.assist.customer.demo.Constants.SESSION_KEY
import com.zoho.assist.customer.demo.databinding.ActivityMainBinding


@SuppressLint("Registered")
class MainActivity : AppCompatActivity() {

    private lateinit var viewDataBinding: ActivityMainBinding
    private lateinit var callback: ISessionCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        callback = ISessionCallbacks(this, viewDataBinding)
        AssistSession.INSTANCE.onCreate(this, callback)

        onViewCreate()
        viewDataBinding.executePendingBindings()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AssistSession.INSTANCE.onActivityResult(requestCode, resultCode, data)
        //Update screenSharing buttons based on whether or not user provided screen sharing permission.
        resetScreenSharingButtons()
    }

    /**
     * Note:-
     *
     * onStop(), onResume() are needed only when the Floating Head feature is enabled
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


    private fun onViewCreate() {
        if (AssistSession.INSTANCE.isSessionAlive()) {
            //Set the enabled states for the various buttons whilst in session.
            callback.onSessionStarted()
            resetScreenSharingButtons()
            Toast.makeText(this, "Session in progress", Toast.LENGTH_SHORT).show()
        } else {
            if (intent != null) {
                if (intent.getStringExtra(SESSION_KEY).isNullOrEmpty() && !AssistSession.INSTANCE.isSessionAlive()) {
                    returnToJoinSessionActivity()
                } else if (intent.action == null) {
                    val authToken = intent.getStringExtra(SDK_TOKEN)
                    val sessionKey = intent.getStringExtra(SESSION_KEY)
                    if (authToken != null && sessionKey != null) {
                        onStartSession(sessionKey, authToken)
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
            intent.putExtra(SESSION_KEY, "")
            viewDataBinding.logView.append("\nSession Stopped")//no i18n
            Toast.makeText(this@MainActivity, "Closed the session", Toast.LENGTH_SHORT).show()//no i18n
        }
        viewDataBinding.startSession.setOnClickListener {
            if (intent.getStringExtra(SESSION_KEY).isNullOrEmpty()) {
                returnToJoinSessionActivity()
            } else if (intent.action == null) {
                val authToken = intent.getStringExtra(SDK_TOKEN)
                val sessionKey = intent.getStringExtra(SESSION_KEY)
                if (authToken != null && sessionKey != null) {
                    onStartSession(sessionKey, authToken)
                }
            }
        }

        viewDataBinding.startShare.setOnClickListener {
            //to restart the screen sharing
            AssistSession.INSTANCE.onStartShare()
            viewDataBinding.logView.append("\nRestart Sharing")//no i18n
            viewDataBinding.startShare.isEnabled = false
            viewDataBinding.stopShare.isEnabled = true
        }

        viewDataBinding.stopShare.setOnClickListener {
            //to stop the screen sharing
            AssistSession.INSTANCE.onStopShare()
            viewDataBinding.logView.append("\nStop Sharing")//no i18n
            viewDataBinding.stopShare.isEnabled = false
            viewDataBinding.startShare.isEnabled = true
        }

        viewDataBinding.sendMessage.setOnClickListener {
            //send a chat message to the viewers
            viewDataBinding.logView.append("\nSending message:: " + Date().toString())//no i18n
            viewDataBinding.logViewScrollView.postDelayed({
                viewDataBinding.logViewScrollView.fullScroll(View.FOCUS_DOWN)
            }, 200)
            AssistSession.INSTANCE.onSendMessage("message:: " + Date().toString())//no i18n
        }
    }


    private fun returnToJoinSessionActivity() {
        if (this.isTaskRoot) {
            startActivity(Intent(this@MainActivity, JoinActivity::class.java))
            finish()
        } else {
            onBackPressed()
        }
    }

    private fun resetScreenSharingButtons() {
        if (AssistSession.INSTANCE.isScreenSharing()) {
            viewDataBinding.startShare.isEnabled = false
            viewDataBinding.stopShare.isEnabled = true
        } else {
            viewDataBinding.startShare.isEnabled = true
            viewDataBinding.stopShare.isEnabled = false
        }
    }

    private fun onStartSession(key: String, authToken: String) {
        //Assist Agent init
        AssistSession.INSTANCE
            .setCallbacks(callback)   //pass over an instance of class implementing SessionCallbacks.
            .setCustomerDetails(
                "guest",
                "email@emailcom"
            ) // share username and userEmail - Default value is Guest
            .setAuthToken(authToken)
            .enableFloatingHead(true)
            .shareScreenOnStart(true)
            .downloadAddonOnStart(true)
            .setKeepAliveNotification(getNotification()) // Optional
//            .startRemoteControlOnStart(false) // Default is true
            .start(
                key,
                MainActivity::class.java,
                R.drawable.assist_flat
            ) //this represent Activity Context,
    }

    private fun getNotification(): Notification? {
        val channelId = "channelId"
        val channelName = "channelName"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = NOTIFICATION_ACTION
        notificationIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_PENDING_INTENT_ID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
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
            "Assist Customer SDK Demo"//no i18n
        }
    }

    override fun onBackPressed() {
        viewDataBinding.logView.append("\nBack Pressed")//no i18n
        if (!AssistSession.INSTANCE.isSessionAlive()) {
            super.onBackPressed()
        } else {
            AssistSession.INSTANCE.onCustomerEndSession()
            viewDataBinding.startShare.isEnabled = false
            viewDataBinding.stopShare.isEnabled = false
            viewDataBinding.sendMessage.isEnabled = false
            viewDataBinding.startSession.isEnabled = true
            viewDataBinding.closeSession.isEnabled = false
            intent.putExtra(SESSION_KEY, "")
            viewDataBinding.logView.append("\nSession Stopped")//no i18n
            viewDataBinding.startSession.visibility = View.GONE
        }
    }

}

