@file:Suppress("DEPRECATION")

package com.zoho.assist.customer.demo

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import android.app.ProgressDialog
import android.view.WindowManager


var progressBar: ProgressDialog? = null

fun Context.onShowLoader(){
    progressBar = ProgressDialog(this)
    progressBar.let {
        progressBar!!.setMessage("Connecting....")
        progressBar!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressBar!!.progress = 0
        progressBar!!.max = 100
        try {

            if(progressBar!=null) {
                progressBar!!.show()
            }

        }catch (e: WindowManager.BadTokenException){
            e.printStackTrace()
        }
    }
}

fun  Context.onDismiss() {
    try {
            progressBar?.let {
                it.dismiss()
            }
    } catch (ex: Exception) {

    }
}

fun closeCustomDialog(alertDialog: AlertDialog) {
    try {
        alertDialog.context.onDismiss()
        alertDialog.let {
            alertDialog.dismiss()
        }
    } catch (ex: Exception) {

    }

}



