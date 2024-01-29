package com.friendly_machines.fr_yhe_med.notification

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageDecodingException
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchUnknownResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import com.friendly_machines.fr_yhe_med.command.WatchBindCommand
import com.friendly_machines.fr_yhe_med.command.WatchChangeAlarmCommand
import com.friendly_machines.fr_yhe_med.command.WatchCurrentHeatCommand
import com.friendly_machines.fr_yhe_med.command.WatchCurrentStepCommand
import com.friendly_machines.fr_yhe_med.command.WatchDeviceInfoCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetAlarmCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetBatteryStateCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetBpDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetDeviceConfigCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetHeatDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetSleepDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetSportDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetStepDataCommand
import com.friendly_machines.fr_yhe_med.command.WatchGetWatchFaceCommand
import com.friendly_machines.fr_yhe_med.command.WatchOtaGetFirmwareVersionCommand
import com.friendly_machines.fr_yhe_med.command.WatchOtaNegotiateFileOffsetCommand
import com.friendly_machines.fr_yhe_med.command.WatchOtaSendFinishCommand
import com.friendly_machines.fr_yhe_med.command.WatchOtaSendInfoCommand
import com.friendly_machines.fr_yhe_med.command.WatchOtaSendStartCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetMessageCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetProfileCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetStepGoalCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetTimeCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetWatchFaceCommand
import com.friendly_machines.fr_yhe_med.command.WatchSetWeatherCommand
import com.friendly_machines.fr_yhe_med.command.WatchUnbindCommand
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WatchResponseFactory {
    fun parse(code: Short, buf: ByteBuffer): WatchResponse {
        buf.order(ByteOrder.BIG_ENDIAN)
        val operation = try {
            WatchOperation.parse(code)
        } catch (e: WatchMessageDecodingException) {
            val b = ByteArray(buf.remaining())
            buf.get(b)
            return WatchUnknownResponse(code, b)
        }
        return when (operation) {
            WatchOperation.DeviceInfo -> WatchDeviceInfoCommand.Response.parse(buf)
            WatchOperation.OtaGetFirmwareVersion -> WatchOtaGetFirmwareVersionCommand.Response.parse(buf)
            WatchOperation.OtaSendInfo -> WatchOtaSendInfoCommand.Response.parse(buf)
            WatchOperation.OtaNegotiateFileOffset -> WatchOtaNegotiateFileOffsetCommand.Response.parse(buf)
            WatchOperation.OtaSendStart -> WatchOtaSendStartCommand.Response.parse(buf)
            WatchOperation.OtaSendFinish -> WatchOtaSendFinishCommand.Response.parse(buf)
            WatchOperation.Bind -> WatchBindCommand.Response.parse(buf)
            WatchOperation.Unbind -> WatchUnbindCommand.Response.parse(buf)
            WatchOperation.GetStepData -> WatchGetStepDataCommand.Response.parse(buf)
            WatchOperation.GetSleepData -> WatchGetSleepDataCommand.Response.parse(buf)
            WatchOperation.GetHeatData -> WatchGetHeatDataCommand.Response.parse(buf)
            WatchOperation.CurrentHeat -> WatchCurrentHeatCommand.Response.parse(buf)
            WatchOperation.GetSportData -> WatchGetSportDataCommand.Response.parse(buf)
            WatchOperation.GetBpData -> WatchGetBpDataCommand.Response.parse(buf)
            WatchOperation.GetBatteryState -> WatchGetBatteryStateCommand.Response.parse(buf)
            WatchOperation.SetTime -> WatchSetTimeCommand.Response.parse(buf)
            WatchOperation.SetWeather -> WatchSetWeatherCommand.Response.parse(buf)
            WatchOperation.GetDeviceConfig -> WatchGetDeviceConfigCommand.Response.parse(buf)
            WatchOperation.GetWatchFace -> WatchGetWatchFaceCommand.Response.parse(buf)
            WatchOperation.SetWatchFace -> WatchSetWatchFaceCommand.Response.parse(buf)
            WatchOperation.NotificationFromWatch -> WatchNotificationFromWatch.parse(buf)
            WatchOperation.SetProfile -> WatchSetProfileCommand.Response.parse(buf)
            WatchOperation.SetStepGoal -> WatchSetStepGoalCommand.Response.parse(buf)
            WatchOperation.ChangeAlarm -> WatchChangeAlarmCommand.Response.parse(buf)
            WatchOperation.GetAlarm -> WatchGetAlarmCommand.Response.parse(buf)
            WatchOperation.CurrentStep -> WatchCurrentStepCommand.Response.parse(buf)
            WatchOperation.SetMessage -> WatchSetMessageCommand.Response.parse(buf)
            else -> { // TODO remove
                val b = ByteArray(buf.remaining())
                buf.get(b)
                WatchUnknownResponse(operation.code, b)
            }
        }
    }
}