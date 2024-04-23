package com.zoho.assist.customer.demo

import android.app.Application
import com.zoho.assist.customer.AssistSDKApplication
import com.zoho.assist.customer.AssistSession

class MainApplication : AssistSDKApplication() {

    override fun onCreate() {
        super.onCreate()
        AssistSession.INSTANCE.setContext(this)
        AssistSession.INSTANCE.setAuthToken("OlyO55SENBUmnpF1K5fIjveyz0mjpadfj3ZAbq2whmDClHG3AB31SsNVM36eOZhzzP4coagKx/2gH5XfynmR5pqnVbqk/o0KeItwz2yAguFPVDkeB5ZbSGcuf7P6upypJUA=") // Set your auth token here to enable some features
        AssistSession.INSTANCE.enableServiceQueue(this,true)

    }

}