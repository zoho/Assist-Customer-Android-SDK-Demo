package com.zoho.assist.customer.demo

import android.app.Activity
import android.app.ActivityManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.method.KeyListener
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.demo.databinding.ActivityJoinBinding
import com.zoho.assist.customer.deviceregistration.enrollment.BaseUrl
import com.zoho.assist.customer.deviceregistration.enrollment.EnrollmentCallback
import com.zoho.assist.customer.deviceregistration.unenrollment.UnenrollmentCallback
import java.util.*
import java.util.logging.Level


class JoinActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener, KeyListener{

    companion object {
        const val SESSION_KEY = "Session_key"
    }
    private lateinit var binding: ActivityJoinBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.contentLayoutId.radioGroup.setOnCheckedChangeListener(this)
        binding.contentLayoutId.sdkToken.setText("")
        binding.contentLayoutId.sessionKey.setText("598413214")
        binding.contentLayoutId.deviceName.setText(" Testing_Device")
        AssistSession.INSTANCE.setLogLevel(Level.ALL)
        binding.contentLayoutId.joinButton.setOnClickListener { view ->
            var sessionKey =    binding.contentLayoutId.sessionKey.text.toString()
            sessionKey.let {
                Log.d("Done", sessionKey)
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

        binding.contentLayoutId.enrollButton.setOnClickListener {
            if(! binding.contentLayoutId.sdkToken.text.isNullOrEmpty() && ! binding.contentLayoutId.deviceName.text.isNullOrEmpty()) {
                AssistSession.INSTANCE.enroll(BaseUrl.COM,  binding.contentLayoutId.deviceName.text.toString(), binding.contentLayoutId. sdkToken.text.toString(), object: EnrollmentCallback {
                    override fun onEnrollmentSuccess(ursKey: String) {
                        Toast.makeText(this@JoinActivity, "Enrolled successfully", Toast.LENGTH_SHORT).show()
                        Log.w("FromApp Enrollment", "Success")
                    }
                    override fun onEnrollmentFailure(exception: Exception) {
                        Toast.makeText(this@JoinActivity, "Failed to enroll : ${exception.message}", Toast.LENGTH_SHORT).show()
                        exception.message?.let { it1 -> Log.w("FromApp Enrollment", it1) }
                    }
                })
            }
        }

        binding.contentLayoutId.unenrollButton.setOnClickListener {
            AssistSession.INSTANCE.unenroll(object: UnenrollmentCallback {
                override fun onUnenrollmentSuccess() {
                    Toast.makeText(this@JoinActivity, "Unenrolled successfully", Toast.LENGTH_SHORT).show()
                    Log.w("FromApp Unenrollment", "Success")
                }

                override fun onUnenrollmentFailure(exception: Exception) {
                    Toast.makeText(this@JoinActivity, "Unenrollment failed : ${exception.message}", Toast.LENGTH_SHORT).show()
                    exception.message?.let { it1 -> Log.w("FromApp Unenrollment", it1) }
                }
            })
        }
        binding.contentLayoutId.checkAddon.setOnClickListener {
            disableScreen()
            AssistSession.INSTANCE.startAddon()
        }
    }

    private fun disableScreen() {
        binding.contentLayoutId.sessionKey.isEnabled = false
        binding.contentLayoutId.sdkToken.isEnabled = false
        binding.contentLayoutId. enrollButton.isEnabled = false
        binding.contentLayoutId.  joinButton.isEnabled = false
        binding.contentLayoutId.   unenrollButton.isEnabled = false
        binding.contentLayoutId.  checkAddon.isEnabled = false
    }

    private fun enableScreen() {
        binding.contentLayoutId.sessionKey.isEnabled = true
        binding.contentLayoutId.  sdkToken.isEnabled = true
        binding.contentLayoutId. enrollButton.isEnabled = true
        binding.contentLayoutId. joinButton.isEnabled = true
        binding.contentLayoutId. unenrollButton.isEnabled = true
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
//        finish()

    }


    fun Context.onShowLoader() {
        progressBar = ProgressDialog(this)
        progressBar.let {
            progressBar?.setMessage("Connecting....")
            progressBar?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressBar?.progress = 0
            progressBar!!.max = 100
            progressBar!!.show()
        }
    }

    fun onDismiss() {
        try {
            if (progressBar != null) {
                progressBar.let {
                    progressBar!!.dismiss()
                }
            }
        } catch (ex: Exception) {

        }
    }
    var serverURL="https://assist.zoho.com"
    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (checkedId) {
            R.id.live_option -> serverURL = "https://assist.zoho.com"
            R.id.local_option -> serverURL = "https://assist.localzoho.com"
        }


    }


    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
    }
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {

        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    override fun getInputType(): Int {
        return  InputType.TYPE_CLASS_TEXT
    }


    override fun onKeyDown(p0: View?, p1: Editable?, p2: Int, event: KeyEvent?): Boolean {
        return true
    }

    override fun onKeyUp(p0: View?, p1: Editable?, p2: Int, event: KeyEvent?): Boolean {
        return true
    }

    override fun onKeyOther(p0: View?, p1: Editable?, event: KeyEvent?): Boolean {
        return true
    }

    override fun clearMetaKeyState(p0: View?, p1: Editable?, p2: Int) {
    }


}
