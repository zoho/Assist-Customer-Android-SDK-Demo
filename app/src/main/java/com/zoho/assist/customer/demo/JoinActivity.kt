package com.zoho.assist.customer.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.zoho.assist.customer.demo.Constants.SDK_TOKEN
import com.zoho.assist.customer.demo.Constants.SESSION_KEY
import kotlinx.android.synthetic.main.activity_join.*

import java.lang.Exception


class JoinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        val edittext = findViewById<EditText>(R.id.key_edittext)
        val authtoken = findViewById<EditText>(R.id.authtoken_edittext)
        fab.setOnClickListener {
           var sessionKey = edittext.text.toString()
            sessionKey.let {
                Log.d("Done", sessionKey)
                var key = if (sessionKey.isEmpty()) {
                    ""
                } else {
                    sessionKey
                }

                if (!authtoken.text.toString().isNullOrEmpty()) {
                    onStartSession(sessionKey, authtoken.text.toString())
                } else {
                    authtoken.error = "Please enter the AuthToken"
                }
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            }
        }


    }


    private fun onStartSession(sessionKey: String, authToken: String) {

        val intent = Intent(this@JoinActivity, MainActivity::class.java)
        intent.putExtra(SESSION_KEY, sessionKey)
        intent.putExtra(SDK_TOKEN, authToken)
        startActivity(intent)

    }


}
