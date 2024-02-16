package com.friendly_machines.frbpdoctor

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
                    // FIXME
                    val textInfo = view.findViewById<TextView>(R.id.text_info)
                    val textName = view.findViewById<TextView>(R.id.text_name)
                    val textScreenInfo = view.findViewById<TextView>(R.id.text_screen_info)
                    val textElectrodeLocation = view.findViewById<TextView>(R.id.text_electrode_location)
                    val textEventReminderInfo = view.findViewById<TextView>(R.id.text_event_reminder_info)
                    val textMacAddress = view.findViewById<TextView>(R.id.text_mac_address)
                    val textMainTheme = view.findViewById<TextView>(R.id.text_main_theme)
                    val textManualModeStatus = view.findViewById<TextView>(R.id.text_manual_mode_status)
                    val textRealBloodOxygen = view.findViewById<TextView>(R.id.text_real_blood_oxygen)
                    val textRealTemperature = view.findViewById<TextView>(R.id.text_real_temperature)
                    val textScreenParameters = view.findViewById<TextView>(R.id.text_screen_parameters)
                    val textUserConfig = view.findViewById<TextView>(R.id.text_user_config)
                    when (response) {
                        is WatchGGetDeviceInfoCommand.Response -> textInfo.text = "$response"
                        is WatchGGetDeviceNameCommand.Response -> textName.text = "$response"
                        is WatchGGetScreenInfoCommand.Response -> textScreenInfo.text = "$response"
                        is WatchGGetElectrodeLocationCommand.Response -> textElectrodeLocation.text = "$response"
                        is WatchGGetEventReminderInfoCommand.Response -> textEventReminderInfo.text = "$response"
                        is WatchGGetMacAddressCommand.Response -> textMacAddress.text = "$response"
                        is WatchGGetMainThemeCommand.Response -> textMainTheme.text = "$response"
                        is WatchGGetManualModeStatusCommand.Response -> textManualModeStatus.text = "$response"
                        is WatchGGetRealBloodOxygenCommand.Response -> textRealBloodOxygen.text = "$response"
                        is WatchGGetRealTemperatureCommand.Response -> textRealTemperature.text = "$response"
                        is WatchGGetScreenParametersCommand.Response -> textScreenParameters.text = "$response"
                        is WatchGGetUserConfigCommand.Response -> textUserConfig.text = "$response"
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
