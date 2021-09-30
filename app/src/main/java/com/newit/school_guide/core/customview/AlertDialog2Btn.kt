package com.newit.school_guide.core.customview

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.newit.school_guide.R

class AlertDialog2Btn(
    context: Context?,
    private val title: String,
    private val titleButton: String,
    private val titleCancel: String,
    private val message: String,
    private val iClick: (() -> Unit)?,
    private val isShowCheckBox: Boolean = false
) :
    AlertDialog(context!!), View.OnClickListener {
    private var tvMessage: TextView? = null
    private var btnOk: TextView? = null
    private var tvTitle: TextView? = null
    private var btn_cancel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert_dialog_layout2)
        tvTitle = findViewById<View>(R.id.tv_title) as TextView?
        tvMessage = findViewById<View>(R.id.tv_message) as TextView?
        btnOk = findViewById<View>(R.id.btn_ok) as TextView?
        btnOk = findViewById<View>(R.id.btn_cancel) as TextView?
        if (!title.isEmpty()) {
            tvTitle!!.text = title
            tvTitle!!.visibility = View.VISIBLE
        }
        if (!titleButton.isEmpty()) {
            btnOk!!.text = titleButton
        }
        if (!titleCancel.isEmpty()) {
            btn_cancel?.text = titleCancel
        }
        tvMessage!!.text = message
        btnOk!!.setOnClickListener(this)
        btn_cancel?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_ok -> {
                dismiss()
                iClick?.invoke()
            }
            R.id.btn_cancel -> {
                dismiss()
            }
        }
    }
}