package com.zoho.assist.customer.demo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.demo.Constants.SDK_TOKEN
import com.zoho.assist.customer.demo.Constants.SESSION_KEY
import com.zoho.assist.customer.listener.AddonAvailabilityCallback
import kotlinx.android.synthetic.main.activity_join.*
import kotlinx.android.synthetic.main.content_join.*
import java.lang.RuntimeException


class JoinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        val sessionKeyEditText = findViewById<EditText>(R.id.key_edittext)
        val authTokenEditText = findViewById<EditText>(R.id.authtoken_edittext)

        sessionKeyEditText.setText("")
        authTokenEditText.setText("")

        fab.setOnClickListener {
            val sessionKey = sessionKeyEditText.text.toString()
            val authToken = authTokenEditText.text.toString()

            Log.d("Done", sessionKey)
            if (authToken.isNotEmpty()) {
                startSession(sessionKey, authToken)
            } else {
                authTokenEditText.error = "Please enter the AuthToken"
            }

            //Hide the keyboard incase it's open.
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }

        val addonAppId = AssistSession.INSTANCE.getAddonApplicationId()

        checkAddon.setOnClickListener {
            AssistSession.INSTANCE.checkAddonAvailability(object : AddonAvailabilityCallback {
                override fun onAddonInstalled() {
                    addonAvailabilityState.text = "Addon already installed"
                }

                override fun onAddonAvailable(addonApplicationId: String) {
                    addonAvailabilityState.text = "Addon available : $addonApplicationId"
                    this@JoinActivity.startActivityForResult(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=${addonApplicationId}")
                        ), 14892
                    )
                }

                override fun onAddonUnavailable() {
                    addonAvailabilityState.text = "Addon is unavailable"
                }
            })
        }

    }


    private fun startSession(sessionKey: String, authToken: String) {
        val intent = Intent(this@JoinActivity, MainActivity::class.java)
        intent.putExtra(SESSION_KEY, sessionKey)
        intent.putExtra(SDK_TOKEN, authToken)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("JoinActivity destroyed ", "Sad", Throwable(" >_< "))
    }
}
