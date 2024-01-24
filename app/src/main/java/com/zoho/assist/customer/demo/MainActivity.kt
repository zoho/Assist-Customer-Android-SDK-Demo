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
import com.zoho.assist.customer.demo.JoinActivity.Companion.SESSION_KEY
import com.zoho.assist.customer.demo.databinding.ActivityMainBinding
import com.zoho.assist.customer.demo.BR
import com.zoho.assist.customer.util.Constants
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

//    /***
//     *
//     */
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        AssistSession.INSTANCE.onRequestPermissionsResult(requestCode,permissions,grantResults)
//    }

    /**
     *
     */
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
//            Toast.makeText(this, "Session in progress", Toast.LENGTH_SHORT).show()
        } else {
            if (intent != null) {
                if (intent.getStringExtra(SESSION_KEY).isNullOrEmpty() && !AssistSession.INSTANCE.isSessionAlive()) {
                    openSessionDialog()
                    finish()
                } else if (intent.action == null) {
                    val authToken = intent.getStringExtra("AuthToken")
                    intent.getStringExtra(SESSION_KEY)?.let {
                        if (authToken != null) {
                                onStartSession(
                                    it,
                                    authToken
                                )
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
            intent.putExtra(SESSION_KEY, "")
            openSessionDialog()
        }
        viewDataBinding.startSession.setOnClickListener(View.OnClickListener {
            //to stop the screen sharing

            if (intent.getStringExtra(SESSION_KEY).isNullOrEmpty()) {
                openSessionDialog()

            } else if (intent.action == null && intent.hasExtra("AuthToken") &&  intent.hasExtra(SESSION_KEY)
            ) {
                val authToken = intent.getStringExtra("AuthToken")
                intent.getStringExtra(SESSION_KEY)
                    ?.let { it1 ->
                        if (authToken != null) {
                                onStartSession(key=it1, authToken=authToken )
                        }
                    }
            }

        })

        viewDataBinding.startShare.setOnClickListener(View.OnClickListener {
            /***
             *
             */
            AssistSession.INSTANCE.onStartShare()
            viewDataBinding.startShare.isEnabled = false
            viewDataBinding.stopShare.isEnabled = true
        })

        viewDataBinding.stopShare.setOnClickListener(View.OnClickListener {
            /**
             *
             */
            AssistSession.INSTANCE.onStopShare()
            viewDataBinding.stopShare.isEnabled = false
            viewDataBinding.startShare.isEnabled = true
        })

        viewDataBinding.sendMessage.setOnClickListener(View.OnClickListener {
            //send a chat message to the viewers
            var chatModel = AssistSession.INSTANCE.onSendMessage("message:: ${Date()}")
            chatFragemnt.notifyDatasetChanged()
        })

    }


    private fun openSessionDialog() {
        this@MainActivity.startActivity(Intent(this@MainActivity, JoinActivity::class.java))

    }

    @SuppressLint("ServiceCast")
    private fun onStartSession(key: String, authToken: String ) {
        /***
         *
         */
//        AssistSession.INSTANCE.setContext(this.application.applicationContext)
        //Assist Agent init
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

//            .setBaseDomain(serverURL)
//            .onOverlayFloating(MainActivity::class.java) //  drawable / -1
//            .setQuality(Constants.ColorQualityFactors.QUALITY50) //(Default QUALITY to start with - 25/50/75/100)

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
            manager?.createNotificationChannel(serviceChannel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = "com.zoho.assist.agent.main"
        notificationIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this,
            1427,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
            ""
        }
    }

    /***
     *
     *
     */

    override fun onBackPressed() {
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
            openSessionDialog()
            viewDataBinding.startSession.visibility = View.GONE
            finish()
        }

    }

    fun getChatFragment(): ChatFragment {
        return chatFragemnt
    }

}

