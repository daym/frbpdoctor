package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageDecodingException
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchUnknownResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceNameCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceScreenInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetMainThemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetRealTemperatureCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetAllHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetAmbientLightHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBackgroundReminderRecordHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBloodHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBloodOxygenHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetComprehensiveMeasurementDataCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetFallHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetHealthMonitoringHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetHeartHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetSleepHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetSportHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetTemperatureAndHumidityHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetTemperatureHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHHistorySportModeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetLanguageCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetMainThemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWSetCurrentWatchDialCommand
import java.nio.ByteBuffer
import java.nio.ByteOrder

/*
 * Hi byte: large section of operation: (note: Lo byte: detail opcode inside that section)
 * 1 settings
 * 2 get
 * 3 app control
 *    for example wave upload
 * 4 dev control: There are a lot of commands we send out as a reaction, around 0x400...0x413, 0x415
 * 5 health
 *      4 ?; array with size 20 B per item; then another array; very complicated.
 *          TODO
 * 6 realtime
 *      0 sport
 *      1 heart
 *      2 blood oxygen
 *      3 blood
 *      4 ppg
 *      5 ecg
 *      6
 *      7 respiration
 *      8 sensor
 *      9 ambient light
 *      10 comprehensive
 *      11 schedule
 *      12 event reminder
 *      13 oga
 * 7 collect files
 * 9 dial (W)
 *      0 watch dial download // payload new byte[]{(byte) i2, (byte) length, (byte) ((length >> 8) & 255), (byte) ((length >> 16) & 255), (byte) ((length >> 24) & 255), (byte) i3, (byte) ((i3 >> 8) & 255), (byte) ((i3 >> 16) & 255), (byte) ((i3 >> 24) & 255), (byte) i4, (byte) ((i4 >> 8) & 255), (byte) i5, (byte) ((i5 >> 8) & 255), (byte) crc16_compute, (byte) ((crc16_compute >> 8) & 255)}
 * 12 self inspection
 *      0 self inspection
 * 13 customize
 *      1 cgm
 *      2 wit
 *      117 customize
 * 14 test tool
 *      6 vibration motor control // payload [1, i2]; or [2, i2 u16 le, i3 u16 le, i4 u16 le]
 */
object WatchResponseFactory {
    const val D_RESPONSE_CODE_OFFSET: Short = 1024
    inline fun <reified T : Any> parseDataBlockArray(count: Int, buf: ByteBuffer, callback: (ByteBuffer) -> T): List<T> {
        return List(count) { callback(buf) }
    }

    fun parse(code: Short, buf: ByteBuffer): WatchResponse {
        buf.order(ByteOrder.BIG_ENDIAN)
        val operation = try {
            WatchOperation.parse(code)
        } catch (e: WatchMessageDecodingException) {
            val b = ByteArray(buf.remaining())
            buf.get(b)
            return WatchUnknownResponse(code, b)
        }
        buf.order(ByteOrder.LITTLE_ENDIAN)
        return when (operation) {
            // "Settings" section

            WatchOperation.SSetMainTheme -> WatchSSetMainThemeCommand.Response.parse(buf)
            WatchOperation.SSetLanguage -> WatchSSetLanguageCommand.Response.parse(buf)

            // "Get" section

            WatchOperation.GGetDeviceName -> WatchGGetDeviceNameCommand.Response.parse(buf)
            // WatchOperation.GGetDeviceInfo -> GGetDeviceInfo.parse(buf)
            WatchOperation.GGetRealTemperature -> WatchGGetRealTemperatureCommand.Response.parse(buf)
            WatchOperation.GGetMainTheme -> WatchGGetMainThemeCommand.Response.parse(buf)
            WatchOperation.GGetDeviceScreenInfo -> WatchGGetDeviceScreenInfoCommand.Response.parse(buf)

            // "Watch remote controls phone" section

            WatchOperation.DFindMobile -> DFindMobile.parse(buf)
            WatchOperation.DLostReminder -> DLostReminder.parse(buf)
            WatchOperation.DPhoneCallControl -> DPhoneCallControl.parse(buf)
            WatchOperation.DPhotoControl -> DPhotoControl.parse(buf)
            WatchOperation.DMusicControl -> DMusicControl.parse(buf)
            WatchOperation.DSos -> DSos.parse(buf)
            WatchOperation.DDrinking -> DDrinking.parse(buf)
            WatchOperation.DConnectOrDisconnect -> DConnectOrDisconnect.parse(buf)
            WatchOperation.DSportMode -> DSportMode.parse(buf)
            WatchOperation.DSyncContacts -> DSyncContacts.parse(buf)
            WatchOperation.DRest -> DRest.parse(buf)
            WatchOperation.DEndEcg -> DEndEcg.parse(buf)
            WatchOperation.DSportModeControl -> DSportModeControl.parse(buf)
            WatchOperation.DSwitchDial -> DSwitchDial.parse(buf)
            WatchOperation.DMeasurementResult -> DMeasurementResult.parse(buf)
            WatchOperation.DAlarmData -> DAlarmData.parse(buf)
            WatchOperation.DInflatedBloodMeasurementResult -> DInflatedBloodMeasurementResult.parse(buf)
            WatchOperation.DUpgradeResult -> DUpgradeResult.parse(buf)
            WatchOperation.DPpiData -> DPpiData.parse(buf)
            WatchOperation.DMeasurementStatusAndResult -> DMeasurementStatusAndResult.parse(buf)
            WatchOperation.DDynamicCode -> DDynamicCode.parse(buf)

            // "Health" section

            WatchOperation.HGetSportHistory -> WatchHGetSportHistoryCommand.Response.parse(buf)
            WatchOperation.HGetSleepHistory -> WatchHGetSleepHistoryCommand.Response.parse(buf)
            WatchOperation.HGetHeartHistory -> WatchHGetHeartHistoryCommand.Response.parse(buf)
            WatchOperation.HGetBloodHistory -> WatchHGetBloodHistoryCommand.Response.parse(buf)
            WatchOperation.HGetAllHistory -> WatchHGetAllHistoryCommand.Response.parse(buf)
            WatchOperation.HGetBloodOxygenHistory -> WatchHGetBloodOxygenHistoryCommand.Response.parse(buf)
            WatchOperation.HGetTemperatureAndHumidityHistory -> WatchHGetTemperatureAndHumidityHistoryCommand.Response.parse(buf)
            WatchOperation.HGetTemperatureHistory -> WatchHGetTemperatureHistoryCommand.Response.parse(buf)
            WatchOperation.HGetAmbientLightHistory -> WatchHGetAmbientLightHistoryCommand.Response.parse(buf)
            WatchOperation.HGetFallHistory -> WatchHGetFallHistoryCommand.Response.parse(buf)
            WatchOperation.HGetHealthMonitoringHistory -> WatchHGetHealthMonitoringHistoryCommand.Response.parse(buf)
            WatchOperation.HHistorySportMode -> WatchHHistorySportModeCommand.Response.parse(buf)
            WatchOperation.HGetComprehensiveMeasurementData -> WatchHGetComprehensiveMeasurementDataCommand.Response.parse(buf)
            WatchOperation.HGetBackgroundReminderRecordHistory -> WatchHGetBackgroundReminderRecordHistoryCommand.Response.parse(buf)

            WatchOperation.WSetCurrentWatchDial -> WatchWSetCurrentWatchDialCommand.Response.parse(buf)
            else -> { // TODO remove
                val b = ByteArray(buf.remaining())
                buf.get(b)
                WatchUnknownResponse(operation.code, b)
            }
        }
    }
}