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
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceNameCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetElectrodeLocationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetEventReminderInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetMacAddressCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetMainThemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetManualModeStatusCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetRealBloodOxygenCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetRealTemperatureCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetScreenInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetScreenParametersCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetUserConfigCommand
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand

class DeviceGInfoFragment : DialogFragment() {
    private lateinit var handler: Handler

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_device_g_info, null)

        handler = Handler(Looper.getMainLooper())

        WatchCommunicationClientShorthand.bindPeriodic(handler, 2000, this.requireContext(), object : IWatchListener {
            override fun onWatchResponse(response: WatchResponse) {
                view?.let { view ->
                    when (response) {
                        is WatchGGetDeviceInfoCommand.Response -> view.findViewById<TextView>(R.id.text_info).text = "$response"
                        is WatchGGetDeviceNameCommand.Response -> view.findViewById<TextView>(R.id.text_name).text = "$response"
                        is WatchGGetScreenInfoCommand.Response -> view.findViewById<TextView>(R.id.text_screen_info).text = "$response"
                        is WatchGGetElectrodeLocationCommand.Response -> view.findViewById<TextView>(R.id.text_electrode_location).text = "$response"
                        is WatchGGetEventReminderInfoCommand.Response -> view.findViewById<TextView>(R.id.text_event_reminder_info).text = "$response"
                        is WatchGGetMacAddressCommand.Response -> view.findViewById<TextView>(R.id.text_mac_address).text = "$response"
                        is WatchGGetMainThemeCommand.Response -> view.findViewById<TextView>(R.id.text_main_theme).text = "$response"
                        is WatchGGetManualModeStatusCommand.Response -> view.findViewById<TextView>(R.id.text_manual_mode_status).text = "$response"
                        is WatchGGetRealBloodOxygenCommand.Response -> view.findViewById<TextView>(R.id.text_real_blood_oxygen).text = "$response"
                        is WatchGGetRealTemperatureCommand.Response -> view.findViewById<TextView>(R.id.text_real_temperature).text = "$response"
                        is WatchGGetScreenParametersCommand.Response -> view.findViewById<TextView>(R.id.text_screen_parameters).text = "$response"
                        is WatchGGetUserConfigCommand.Response -> view.findViewById<TextView>(R.id.text_user_config).text = "$response"
                    }
                }
                super.onWatchResponse(response)
            }
        }) { binder ->
            // TODO maybe don't send so many commands at once?
            binder.getGDeviceInfo()
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
        fun newInstance(): DeviceGInfoFragment {
            return DeviceGInfoFragment()
        }
    }
}
