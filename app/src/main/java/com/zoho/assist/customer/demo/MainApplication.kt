package com.zoho.assist.customer.demo

import android.app.Application
import com.zoho.assist.customer.AssistSDKApplication
import com.zoho.assist.customer.AssistSession

class MainApplication : AssistSDKApplication() {

    override fun onCreate() {
        super.onCreate()
        AssistSession.INSTANCE.setContext(this)
        AssistSession.INSTANCE.setAuthToken("wSsVR60n+hf1Ca8ozjSrde47yA5QB1v/EEV42FH16SX9F6vC8cc5lEGfDFOgTaMYEWdsQGZHprh8kRYD1DcIiNotzVlSDyiF9mqRe1U4J3x1pLnvkT7OV21dkxOILYgAwQxunQ==")

    }

}