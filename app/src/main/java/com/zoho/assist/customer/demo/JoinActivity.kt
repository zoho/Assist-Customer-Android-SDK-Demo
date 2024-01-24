package com.zoho.assist.customer.demo

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.demo.databinding.ActivityJoinBinding
import com.zoho.assist.customer.listener.AddonAvailabilityCallback
import java.lang.Exception
import java.util.logging.Level

class JoinActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener{

    companion object {
        const val SESSION_KEY = "Session_key"

    }

    private lateinit var binding: ActivityJoinBinding
    var authToken="wSsVR60n+hf1Ca8ozjSrde47yA5QB1v/EEV42FH16SX9F6vC8cc5lEGfDFOgTaMYEWdsQGZHprh8kRYD1DcIiNotzVlSDyiF9mqRe1U4J3x1pLnvkT7OV21dkxOILYgAwQxunQ=="
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.contentLayoutId.radioGroup.setOnCheckedChangeListener(this)
        binding.contentLayoutId.sdkToken.setText(authToken)
        binding.contentLayoutId.sessionKey.setText("306236250")
        binding.fab.setOnClickListener { view ->
            AssistSession.INSTANCE.setLogLevel(Level.ALL)
            var sessionKey =  binding.contentLayoutId.sessionKey.text.toString()
            sessionKey.let {
                Log.i("Done", sessionKey)
                var key = if (sessionKey.isEmpty()) {
                    ""
                } else {
                    sessionKey
                }

                if(!binding.contentLayoutId.sdkToken.text.toString().isEmpty()) {
                    onStartSession(sessionKey, binding.contentLayoutId.sdkToken.text.toString())
                }else{
                    binding.contentLayoutId.sdkToken.error = "Please enter the AuthToken"
                }
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            }
        }
        binding.contentLayoutId.checkAddon.setOnClickListener {
            disableScreen()
            AssistSession.INSTANCE.checkAddonAvailability(object : AddonAvailabilityCallback {
                override fun onAddonInstalled() {
                    binding.contentLayoutId.addonAvailabilityState.text = "Addon already installed"
                    enableScreen()
                }

                override fun onAddonAvailable(addonApplicationId: String) {
                    binding.contentLayoutId.addonAvailabilityState.text = "Addon available : $addonApplicationId"
                    this@JoinActivity.startActivityForResult(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=${addonApplicationId}")
                        ), 14892
                    )
                    enableScreen()
                }

                override fun onAddonUnavailable() {
                    binding.contentLayoutId.addonAvailabilityState.text = "Addon is unavailable"
                    enableScreen()
                }
            })
        }
        if(ContextCompat.checkSelfPermission(this@JoinActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE).toInt()== PackageManager.PERMISSION_DENIED)
        {
            // Permission is not granted
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)
        }




    }

    private fun disableScreen() {
        binding.contentLayoutId. sessionKey.isEnabled = false
        binding.contentLayoutId.  sdkToken.isEnabled = false
        binding .fab.isEnabled = false
        binding.contentLayoutId.checkAddon.isEnabled = false
    }

    private fun enableScreen() {
        binding.contentLayoutId.sessionKey.isEnabled = true
        binding.contentLayoutId. sdkToken.isEnabled = true
        binding. fab.isEnabled = true
        binding.contentLayoutId. checkAddon.isEnabled = true
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.getClassName()) {
                Log.i("isMyServiceRunning?", true.toString() + "")
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString() + "")
        return false
    }


    private fun onStartSession(sessionKey: String, authToken: String) {
        val intent = Intent(this@JoinActivity, MainActivity::class.java)
        intent.putExtra(SESSION_KEY, sessionKey)
        intent.putExtra("AuthToken", authToken)
        intent.putExtra("SERVER", serverURL)
        startActivity(intent)
    }

    val progressBar: ProgressDialog? = null
    fun onDismiss() {
        try {
            progressBar?.let {
                it.dismiss()
            }
        } catch (ex: Exception) {

        }
    }
    var serverURL="https://assist.zoho.com"
    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (checkedId) {
            R.id.live_option -> serverURL = "https://assist.zoho.com"
            R.id.local_option -> serverURL ="https://assist.zoho.com"
        }


    }



    fun checkPermission(permission: String, requestCode: Int) {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this@JoinActivity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@JoinActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@JoinActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }
    private val STORAGE_PERMISSION_CODE = 100
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE ) {
            var permission="Camera"
            if(requestCode == STORAGE_PERMISSION_CODE){
                permission="STORAGE"
                try {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Showing the toast message
                        Toast.makeText(this@JoinActivity,
                            " onRequestPermissionsResult $permission Permission Granted",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@JoinActivity,
                            "onRequestPermissionsResult $permission Permission Denied",
                            Toast.LENGTH_SHORT).show()
                    }
                }catch (ex: Exception){
                    ex.printStackTrace()
                }
            }

        }
    }

}
