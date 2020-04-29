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
import com.zoho.assist.customer.chat.viewmodel.ChatViewModel
import com.zoho.assist.customer.demo.Constants.NOTIFICATION_ACTION
import com.zoho.assist.customer.demo.Constants.NOTIFICATION_PENDING_INTENT_ID
import com.zoho.assist.customer.demo.Constants.SDK_TOKEN
import com.zoho.assist.customer.demo.Constants.SESSION_KEY
import com.zoho.assist.customer.demo.databinding.ActivityMainBinding
import com.zoho.assist.customer.util.Constants


@SuppressLint("Registered")
class MainActivity : AppCompatActivity() {

    lateinit var viewDataBinding: ActivityMainBinding

    private lateinit var callback: ISessionCallbacks
    var sessionKey=""

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            viewDataBinding = DataBindingUtil.setContentView(this,  R.layout.activity_main)

            /**
             * SetContext
             */
            AssistSession.INSTANCE.setContext(this.application.applicationContext)
            callback = ISessionCallbacks(this, viewDataBinding)
            AssistSession.INSTANCE.onCreate(this,callback)


            onViewCreate()
            viewDataBinding.executePendingBindings()
        }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            data?.let { AssistSession.INSTANCE.onActivityResult(requestCode,resultCode, it) }
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


    private fun onViewCreate() {
        if (AssistSession.INSTANCE.isSessionAlive()) {
            callback.onSessionStarted()
            Toast.makeText(this, "Session in progress", Toast.LENGTH_SHORT).show()
        } else {
            if (intent != null) {
                if (intent.getStringExtra(SESSION_KEY).isNullOrEmpty() && !AssistSession.INSTANCE.isSessionAlive()) {
                    joinSessionActivity()
                } else if (intent.action == null) {
                    val authToken = intent.getStringExtra(SDK_TOKEN)
                    val sessionKey = intent.getStringExtra(SESSION_KEY)
                    if (authToken!=null && sessionKey!=null) {
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
                    intent.putExtra(SESSION_KEY,"")
                    viewDataBinding.helloText.append("\nSession Stopped")//no i18n
                    joinSessionActivity()
                    Toast.makeText(this@MainActivity, "Closed the session", Toast.LENGTH_SHORT).show()//no i18n

        }
        viewDataBinding.startSession.setOnClickListener(View.OnClickListener {

            if (intent.getStringExtra(SESSION_KEY).isNullOrEmpty()) {
                joinSessionActivity()

            } else if(intent.action==null){
                val authToken= intent.getStringExtra(SDK_TOKEN)
                val sessionKey = intent.getStringExtra(SESSION_KEY)
                if (authToken!=null && sessionKey!=null) {
                    onStartSession(sessionKey, authToken)
                }
            }

        })

        viewDataBinding.startShare.setOnClickListener(View.OnClickListener {
            //to restart the screen sharing
            AssistSession.INSTANCE.onStartShare()
            viewDataBinding.helloText.append("\n Restart Sharing")//no i18n
            viewDataBinding.startShare.isEnabled = false
            viewDataBinding.stopShare.isEnabled = true
        })

        viewDataBinding.stopShare.setOnClickListener(View.OnClickListener {
            //to stop the screen sharing
            AssistSession.INSTANCE.onStopShare()
            viewDataBinding.helloText.append("\nStop Sharing")//no i18n
            viewDataBinding.stopShare.isEnabled = false
            viewDataBinding.startShare.isEnabled = true
        })

        viewDataBinding.sendMessage.setOnClickListener(View.OnClickListener {
            //send a chat message to the viewers
            viewDataBinding.helloText.append("\nSending message:: " + Date().toString())//no i18n
            AssistSession.INSTANCE.onSendMessage("message:: " + Date().toString())//no i18n
        })
    }




    private fun joinSessionActivity() {
        startActivity( Intent(this@MainActivity,JoinActivity::class.java))
        finish()
    }


    private fun onStartSession(key: String, authToken: String) {
        sessionKey=key

        //Assist Agent init
        AssistSession.INSTANCE
            .setCallbacks(callback)   //pass over an instance of class implementing SessionCallbacks.
            // All events will be triggered here. (In Background thread)
            .setCustomerDetails("guest","email@emailcom") // share username and userEmail - Default value is Guest
            .setAuthToken(authToken )
            .enableFloatingHead(false)
            .shareScreenOnStart(true)
            .downloadAddonOnStart(true)
            .setKeepAliveNotification(getNotification()) // Optional
            .start(key,MainActivity::class.java,R.drawable.assist_flat) //this represent Activity Context,
    }

    private fun getNotification(): Notification? {
        val channelId = "channelId"
        val channelName ="channelName"
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
        notificationIntent.action = NOTIFICATION_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_PENDING_INTENT_ID, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT)
        val title= String.format( getApplicationName())
        val  message=  String.format("%s is currently running and the technician can see whatever is displayed on your screen",  getApplicationName())
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
        }catch (ex:Exception){
            ""
        }
    }




    override fun onBackPressed() {
        viewDataBinding.helloText.text = (" Back Pressed")//no i18n
        if(!AssistSession.INSTANCE.isSessionAlive()) {
            super.onBackPressed()
        }else{
            AssistSession.INSTANCE.onCustomerEndSession()
                    viewDataBinding.startShare.isEnabled = false
                    viewDataBinding.stopShare.isEnabled = false
                    viewDataBinding.sendMessage.isEnabled = false
                    viewDataBinding.startSession.isEnabled = true
                    viewDataBinding.closeSession.isEnabled = false
                    intent.putExtra("session_key","")
                    viewDataBinding.helloText.append("\nSession Stopped")//no i18n
                    joinSessionActivity()
                    viewDataBinding.startSession.visibility = View.GONE
                    finish()
        }

    }

}

