package com.friendly_machines.frbpdoctor.ui.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.command.WatchCGetFileCountCommand
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand

class DeviceFilesFragment : DialogFragment() {
    private lateinit var handler: Handler

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_device_files, null)

        handler = Handler(Looper.getMainLooper())

        WatchCommunicationClientShorthand.bindPeriodic(handler, 2000, this.requireContext(), object : IWatchListener {
            override fun onWatchResponse(response: WatchResponse) {
                view?.let { view ->
                    when (response) {
                        is WatchCGetFileCountCommand.Response -> view.findViewById<TextView>(R.id.text_file_count).text = "$response"
                    }
                }
                super.onWatchResponse(response)
            }
        }) { binder ->
            binder.getFileCount()
            binder.getFileList()
        }

        return builder.setView(view).setPositiveButton("Close") { dialog, which ->
            dialog.dismiss()
        }.create()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        fun newInstance(): DeviceFilesFragment {
            return DeviceFilesFragment()
        }
    }
}
