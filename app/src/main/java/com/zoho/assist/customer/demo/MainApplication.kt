package com.zoho.assist.customer.demo

import android.app.Application
import com.zoho.assist.customer.AssistSDKApplication
import com.zoho.assist.customer.AssistSession

class MainApplication : AssistSDKApplication() {

    override fun onCreate() {
        super.onCreate()
        AssistSession.INSTANCE.setContext(this)
        AssistSession.INSTANCE.setAuthToken("") // Set your auth token here to enable some features
        AssistSession.INSTANCE.enableServiceQueue(this,true)

    }

}