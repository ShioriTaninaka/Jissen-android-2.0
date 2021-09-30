package com.newit.school_guide.core.customview

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.newit.school_guide.R
import com.newit.school_guide.databinding.AlertDialogLayoutBinding

class AlertDialog1Btn(
    context: Context?,
    private val title: String,
    private val titleButton: String,
    private val message: String,
    private val isShowCheckBox : Boolean = false,
    private val iClick: (() -> Unit)?
) :
    AlertDialog(context!!), View.OnClickListener {
    private var tvMessage: TextView? = null
    private var btnOk: TextView? = null
    private var tvTitle: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert_dialog_layout)
        tvTitle = findViewById<View>(R.id.tv_title) as TextView?
        tvMessage = findViewById<View>(R.id.tv_message) as TextView?
        btnOk = findViewById<View>(R.id.btn_ok) as TextView?
        if (!title.isEmpty()) {
            tvTitle!!.text = title
            tvTitle!!.visibility = View.VISIBLE
        }
        if (!titleButton.isEmpty()) {
            btnOk!!.text = titleButton
        }
        tvMessage!!.text = message
        btnOk!!.setOnClickListener(this)
        btnOk!!.visibility = View.VISIBLE
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_ok -> {
                dismiss()
                iClick?.invoke()
            }
        }
    }
}