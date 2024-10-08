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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.zoho.assist.customer.AssistSession
import com.zoho.assist.customer.demo.databinding.ActivityJoinBinding
import com.zoho.assist.customer.deviceregistration.enrollment.BaseUrl
import com.zoho.assist.customer.listener.AddonAvailabilityCallback
import com.zoho.unattendedaccess.connectivity.Request
import com.zoho.unattendedaccess.connectivity.ServiceQueueCallBack
import com.zoho.unattendedaccess.connectivity.ServiceQueueResponse
import com.zoho.unattendedaccess.deviceregistration.enrollment.EnrollmentCallback
import com.zoho.unattendedaccess.deviceregistration.unenrollment.UnEnrollmentCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Level

class JoinActivity : AppCompatActivity(), ServiceQueueCallBack,
    ServiceQueueResponse {

    companion object {
        const val SESSION_KEY = "Session_key"

    }

    private lateinit var binding: ActivityJoinBinding
    var authToken=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.contentLayoutId.sdkToken.setText(authToken)
        binding.contentLayoutId.sessionKey.setText("")
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

        //Service Queue Code changes
        AssistSession.INSTANCE.setAuthToken(binding.contentLayoutId.sdkToken.text.toString()) // Set your auth token here to enable some features
        binding.contentLayoutId.enrollButton.setText(if(AssistSession.INSTANCE.isEnrolled(this)) "Unenroll" else "Enroll")
        binding.contentLayoutId.serviceQueueButton.visibility = if(AssistSession.INSTANCE.isEnrolled(this)) View.VISIBLE else View.GONE
        binding.contentLayoutId.ServiceRequestResult.visibility = if(AssistSession.INSTANCE.isEnrolled(this)) View.VISIBLE else View.GONE
        binding.contentLayoutId.descriptionBox.visibility = if(AssistSession.INSTANCE.isEnrolled(this)) View.VISIBLE else View.GONE

        AssistSession.serviceQueueResponseCallback = this
        if(AssistSession.INSTANCE.isEnrolled(this)){
            AssistSession.INSTANCE.checkAndStartSession()
        }

        binding.contentLayoutId.enrollButton.setOnClickListener {
            try {
                if (AssistSession.INSTANCE.isEnrolled(this)) {
                    initUnEnroll()
                } else {
                   initEnrollment()
                }
            }catch (ex:Exception){
            }
        }

        binding.contentLayoutId.serviceQueueButton.setOnClickListener {
            if(binding.contentLayoutId.descriptionBox.text.toString().isNotEmpty() && binding.contentLayoutId.descriptionBox.text.toString().length<=500){

            AssistSession.INSTANCE.requestServiceQueue(binding.contentLayoutId.descriptionBox.text.toString(),"zoho", serviceQueueCallBack = object : ServiceQueueCallBack {
                override fun requestResponse(request: Request) {
                    when(request){
                        Request.SUCCESS -> {
                            lifecycleScope.launch{
                                Toast.makeText(this@JoinActivity,"Your request raised successfully", Toast.LENGTH_SHORT).show()
                                updateRequestStatus("Your request raised successfully")
                                binding.contentLayoutId.serviceQueueButton.isEnabled = false
                                binding.fab.isEnabled = false
                            }
                        }

                        Request.FAILURE -> {
                            lifecycleScope.launch {
                                Toast.makeText(
                                    this@JoinActivity,
                                    "Something went wrong.Please try again later",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            binding.contentLayoutId.serviceQueueButton.isEnabled = true
                            binding.fab.isEnabled = true
                        }
                        Request.IN_QUEUE -> {
                            lifecycleScope.launch{
                                Toast.makeText(this@JoinActivity,"Previous request is still in queue. Please wait for the Technician response.",Toast.LENGTH_SHORT).show()
                                updateRequestStatus("Previous request is still in queue. Please wait for the Technician response.")
                            }
                            binding.contentLayoutId.serviceQueueButton.isEnabled = false
                            binding.fab.isEnabled = false
                        }

                    }


                }

            })
            }else{
                if (binding.contentLayoutId.descriptionBox.text.isNullOrEmpty()){
                    binding.contentLayoutId.descriptionBox.error = "Please enter the description"
                }else if(binding.contentLayoutId.descriptionBox.text.toString().length>500){
                    binding.contentLayoutId.descriptionBox.error = "Description should not exist 500"
                }
            }

        }

        if(AssistSession.INSTANCE.isEnrolled(this)){
            disableScreen()
            try {
                Handler(Looper.getMainLooper()).postDelayed({
                    enableScreen()
                    binding.fab.isEnabled = !AssistSession.INSTANCE.hasActiveServiceQueue()
                    updateRequestStatus(if(AssistSession.INSTANCE.hasActiveServiceQueue())"Previous request in queue"  else "No active request is available")
                    binding.contentLayoutId.serviceQueueButton.visibility =  View.VISIBLE
                    binding.contentLayoutId.serviceQueueButton.isEnabled = !AssistSession.INSTANCE.hasActiveServiceQueue()
                },2000L)
            }catch (ex:Exception){
                println("Resume : ${ex.message} ")
                ex.printStackTrace()
                enableScreen()
            }
        }


    }

    private fun disableScreen() {
        binding.contentLayoutId. sessionKey.isEnabled = false
        binding.contentLayoutId.  sdkToken.isEnabled = false
        binding .fab.isEnabled = false
        binding.contentLayoutId.checkAddon.isEnabled = false
        binding.contentLayoutId.serviceQueueButton.isEnabled = false
    }

    private fun enableScreen() {
        binding.contentLayoutId.sessionKey.isEnabled = true
        binding.contentLayoutId. sdkToken.isEnabled = true
        binding. fab.isEnabled = true
        binding.contentLayoutId. checkAddon.isEnabled = true
        if(AssistSession.INSTANCE.isEnrolled(this)){
            binding.contentLayoutId.serviceQueueButton.visibility = View.VISIBLE
            binding.contentLayoutId.serviceQueueButton.isEnabled = !AssistSession.INSTANCE.hasActiveServiceQueue()
        }

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

    fun checkPermission(permission: String, requestCode: Int) {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this@JoinActivity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@JoinActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@JoinActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }
    private val STORAGE_PERMISSION_CODE = 100
     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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


    //Service Queue functions added
    override fun onResume() {
        super.onResume()

    }

    private fun initEnrollment(){
            AssistSession.INSTANCE.enrollDevice(BaseUrl.COM, callback = object :
                EnrollmentCallback {
                override fun onEnrollmentSuccess() {
                    binding.contentLayoutId.serviceQueueButton.visibility = View.VISIBLE
                    binding.contentLayoutId.ServiceRequestResult.visibility = View.VISIBLE
                    binding.contentLayoutId.descriptionBox.visibility = View.VISIBLE
                    binding.contentLayoutId.serviceQueueButton.isEnabled = true
                    binding.contentLayoutId.enrollButton.setText(resources.getString(R.string.app_un_enroll))
                    Toast.makeText(applicationContext,"Enrollment success.",Toast.LENGTH_SHORT).show()
                }

                override fun onEnrollmentFailure(exception: String) {
                    binding.contentLayoutId.serviceQueueButton.isEnabled = false
                    Toast.makeText(applicationContext,"Enrollment failed. Check you input details!.",Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun initUnEnroll(){
        AssistSession.INSTANCE.unEnroll(object : UnEnrollmentCallback {
            override fun onUnEnrollmentFailure(exception: Exception) {
                Toast.makeText(
                    this@JoinActivity,
                    "Un-Enrollment failed : ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                exception.message?.let { it1 -> Log.w("UN_Enrollment", "exception : $it1") }
            }

            override fun onUnEnrollmentSuccess() {
                Toast.makeText(this@JoinActivity, "Unenrolled successfully", Toast.LENGTH_SHORT)
                    .show()
                binding.contentLayoutId.enrollButton.setText(resources.getString(R.string.app_enroll))
                binding.contentLayoutId.serviceQueueButton.visibility = View.GONE
                binding.contentLayoutId.ServiceRequestResult.visibility = View.GONE
                binding.contentLayoutId.descriptionBox.visibility = View.GONE
            }
        })
    }

    private fun updateRequestStatus(s:String){
        binding.contentLayoutId.ServiceRequestResult.setText(s)
    }

    override fun requestResponse(request: Request) {
        when(request){
            Request.FAILURE->{
                updateRequestStatus("Something went wrong try again later")
                binding.contentLayoutId.serviceQueueButton.isEnabled = true
            }

            Request.SUCCESS -> {
                updateRequestStatus("Request raised successfully. Kindly wait for the technician response")
                binding.contentLayoutId.serviceQueueButton.isEnabled = false
                binding.fab.isEnabled = false
            }

            Request.IN_QUEUE -> {
                updateRequestStatus("Previous request still in pending")
                binding.contentLayoutId.serviceQueueButton.isEnabled = false
                binding.fab.isEnabled = false
            }
        }
    }

    override fun sessionEnded(reason: String) {
        CoroutineScope(Dispatchers.Main).launch{
            Toast.makeText(this@JoinActivity,"Your previous session has $reason",Toast.LENGTH_SHORT).show()
        }
        binding.contentLayoutId.serviceQueueButton.visibility = View.VISIBLE
        binding.contentLayoutId.serviceQueueButton.isEnabled = true
    }

    override fun sessionInitiated(sessionKey: String) {
        if(!binding.contentLayoutId.sdkToken.text.toString().isEmpty()) {
            lifecycleScope.launch (Dispatchers.Main) {
                updateRequestStatus("Your session starting....")
                onStartSession(sessionKey, binding.contentLayoutId.sdkToken.text.toString())
            }
        }else{
            binding.contentLayoutId.sdkToken.error = "Please enter the AuthToken"
        }
    }


}
