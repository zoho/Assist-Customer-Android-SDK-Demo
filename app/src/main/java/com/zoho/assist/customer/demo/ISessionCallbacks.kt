package com.zoho.assist.customer.demo

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.SessionCallbacks
import com.zoho.assist.customer.demo.databinding.ActivityMainBinding


class ISessionCallbacks(private val activity: Activity, private val binding: ActivityMainBinding) :SessionCallbacks {
    /**
     *   param - response
     *   To perform any operation using the response from validating the token and session key
     */
    override fun onValidationResponse(response: String) {
        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show()
        if (response != "SUCCESS") {
            activity.onDismiss()
            AssistSession.INSTANCE.closeSession()
            activity.finish()
          activity.  startActivity( Intent(activity,JoinActivity::class.java))
        }else{
            activity.onDismiss()
        }

    }

    /**
     *
     * To perform any operation when session validation api call begins
     */
    override fun onApiCallBegin() {

        Log.d("CallbackImpl", "onApiCallBegin() called")//no i18n
        activity.runOnUiThread {

            activity.onShowLoader()
        }
    }

    /**
     *param - isReconnected
     * To perform any operation during network disconnection or reconnection
     */

    override fun onNetworkConnected(isReconnected: Boolean) {
//        println("onNetworkConnected---->>>>Callback------->>>>${isReconnected}")
    }


}



