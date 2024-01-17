package com.friendly_machines.frbpdoctor.logger

import android.widget.TextView

object Logger {
    private var logTextView: TextView? = null
    fun setLogTextView(textView: TextView?) {
        logTextView = textView
    }

    fun log(message: String) {
        if (logTextView != null) {
            logTextView!!.append("\n$message")
        }
    }
}