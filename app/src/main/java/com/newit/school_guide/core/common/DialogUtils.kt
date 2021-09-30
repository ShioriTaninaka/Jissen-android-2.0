package com.newit.school_guide.core.common

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import com.newit.school_guide.R
import com.newit.school_guide.core.customview.AlertDialog1Btn
import com.newit.school_guide.core.customview.AlertDialog2Btn

object DialogUtils{
    var dialog: AlertDialog? = null
    fun showAlert1Btn(
        context: Context?,
        title: String="",
        titleButton: String="",
        message: String ,
        iClick: (() -> Unit)? = null
    ) {
        val alertDialog =
            AlertDialog1Btn(context, title, titleButton, message, false,iClick)
        alertDialog.setCancelable(true)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    fun showAlertCheckBox(
        context: Context?,
        title: String="",
        titleButton: String="",
        titleCancel :String,
        message: String ,
        iClick: (() -> Unit)? = null
    ) {
        val alertDialog =
            AlertDialog2Btn(context, title, titleButton,titleCancel, message, iClick,true)
        alertDialog.setCancelable(true)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    fun showAlert2Btn(
        context: Context?,
        title: String="",
        titleOk: String="",
        titleCancel: String="",
        message: String ,
        iClick: (() -> Unit)? = null
    ) {
        val alertDialog =
            AlertDialog2Btn(context, title, titleOk,titleCancel, message, iClick)
        alertDialog.setCancelable(true)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    fun showLoadingDialog(context: Context?) {
        var dialogBuilder = AlertDialog.Builder(context, R.style.CustomProgress)
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.progress_dialog, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)
        dialog?.dismiss()
        dialog = dialogBuilder.create()
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }


}