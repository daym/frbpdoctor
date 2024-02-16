package com.friendly_machines.frbpdoctor.ui.customization

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.friendly_machines.fr_yhe_api.commondata.WatchChangeWatchDialAction
import com.friendly_machines.frbpdoctor.R

class EditWatchDialDialog(private val action: WatchChangeWatchDialAction) : DialogFragment() {
    private lateinit var timeEditText: EditText
    private lateinit var dayCheckBoxes: List<CheckBox>

    // Interface to communicate the selected alarm data to the calling activity/fragment
    interface OnWatchDialSetListener {
        fun onWatchDialSet()
    }

    private var listener: OnWatchDialSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_edit_watch_dial, null)

        builder.setPositiveButton(
            when (action) {
                WatchChangeWatchDialAction.Add -> "Add Watch Dial"
                WatchChangeWatchDialAction.Edit -> "Change Watch Dial"
            }
        ) { _, _ ->
            // FIXME
        }

        // Set up negative button to cancel
        builder.setNegativeButton("Cancel") { _, _ -> dismiss() }
        builder.setView(view)
        return builder.create()
    }

    fun addListener(listener: OnWatchDialSetListener) {
        assert(this.listener == null)
        this.listener = listener
    }
}