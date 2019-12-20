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
fun Context.showCustomDialog(resId: Int, sessionKey: String, onButtonClick: (key: String?/*, gateway: String?*/) -> Unit): AlertDialog {



    val builder = AlertDialog.Builder(this)
    val view = LayoutInflater.from(this).inflate(resId, null)
    builder.setView(view)
    val edittext = with(view) { findViewById<EditText>(R.id.key_edittext) }
    var key = if (sessionKey.isEmpty()) {
        ""
    } else {
        sessionKey
    }

    edittext.setText(key)

    val alertDialog = builder.create()
    view.findViewById<Button>(R.id.ok_button).setOnClickListener {
        if (!edittext.text.toString().isEmpty())
        onButtonClick(edittext.text.toString())

    }

    try {
    alertDialog.setCanceledOnTouchOutside(false)
    alertDialog.setCancelable(false)
    alertDialog.show()
    }catch (ex:Exception){

    }
    return alertDialog
}

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
        if (progressBar != null) {
            progressBar.let {
                progressBar!!.dismiss()
            }
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



