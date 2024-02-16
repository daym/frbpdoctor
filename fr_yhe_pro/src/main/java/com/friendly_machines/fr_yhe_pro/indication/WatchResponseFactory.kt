package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageDecodingException
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchUnknownResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.command.WatchCGetFileCountCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetFileListCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetSummaryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCVerifyFileCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceNameCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceScreenInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetElectrodeLocationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetMainThemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetManualModeStatusCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetRealBloodOxygenCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetRealTemperatureCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetScreenInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetScreenParametersCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetUserConfigCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetAllHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetAmbientLightHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBackgroundReminderRecordHistoryCommand
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
import com.friendly_machines.fr_yhe_pro.command.WatchRGetAmbientLightCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetBloodOxygenCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetBloodPressureCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetComprehensiveCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetEcgCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetEventReminderCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetHeartCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetInflatedBloodCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetOgaCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetPpgCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetRespirationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetRunCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetScheduleCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetSensorCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRGetSportCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetLanguageCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetMainThemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTimeLayoutCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetUserInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetWatchWearingArmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWGetWatchDialInfoCommand
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
            WatchOperation.SSetWatchWearingArm -> WatchSSetWatchWearingArmCommand.Response.parse(buf)
            WatchOperation.SUserInfo -> WatchSSetUserInfoCommand.Response.parse(buf)
            WatchOperation.SSetTimeLayout -> WatchSSetTimeLayoutCommand.Response.parse(buf)

            // "Get" section

            WatchOperation.GGetDeviceName -> WatchGGetDeviceNameCommand.Response.parse(buf)
            WatchOperation.GGetRealTemperature -> WatchGGetRealTemperatureCommand.Response.parse(buf)
            WatchOperation.GGetMainTheme -> WatchGGetMainThemeCommand.Response.parse(buf)
            WatchOperation.GGetDeviceScreenInfo -> WatchGGetDeviceScreenInfoCommand.Response.parse(buf)

            WatchOperation.GGetDeviceInfo -> WatchGGetDeviceInfoCommand.Response.parse(buf)
//            WatchOperation.GGetDeviceName -> WatchGGetDeviceNameCommand.Response.parse(buf)
            WatchOperation.GGetScreenInfo -> WatchGGetScreenInfoCommand.Response.parse(buf)
            WatchOperation.GGetElectrodeLocation -> WatchGGetElectrodeLocationCommand.Response.parse(buf)
//            WatchOperation.GGetEventReminderInfo -> WatchGGetEventReminderInfoCommand.Response.parse(buf)
//            WatchOperation.GGetMacAddress -> WatchGGetMacAddressCommand.Response.parse(buf)
            WatchOperation.GGetManualModeStatus -> WatchGGetManualModeStatusCommand.Response.parse(buf)
            WatchOperation.GGetRealBloodOxygen -> WatchGGetRealBloodOxygenCommand.Response.parse(buf)
//            WatchOperation.GGetRealTemperature -> WatchGGetRealTemperatureCommand.Response.parse(buf)
            WatchOperation.GGetScreenParameters -> WatchGGetScreenParametersCommand.Response.parse(buf)
            WatchOperation.GGetUserConfig -> WatchGGetUserConfigCommand.Response.parse(buf)
            WatchOperation.GGetManualModeStatus -> WatchGGetManualModeStatusCommand.Response.parse(buf)

            // "Watch remote controls phone" section

            WatchOperation.DFindMobile -> DFindMobile.parse(buf)
            WatchOperation.DLostReminder -> DLostReminder.parse(buf)
            WatchOperation.DPhoneCallControl -> DPhoneCallControl.parse(buf)
            WatchOperation.DPhotoControl -> DCameraControl.parse(buf)
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
            //WatchOperation.HGetBloodHistory -> WatchHGetBloodHistoryCommand.Response.parse(buf)
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

            // "Real" section

            WatchOperation.RSport -> WatchRGetSportCommand.Response.parse(buf)
            WatchOperation.RHeart -> WatchRGetHeartCommand.Response.parse(buf)
            WatchOperation.RBloodOxygen -> WatchRGetBloodOxygenCommand.Response.parse(buf)
            WatchOperation.RBloodPressure -> WatchRGetBloodPressureCommand.Response.parse(buf)
            WatchOperation.RPpg -> WatchRGetPpgCommand.Response.parse(buf)
            WatchOperation.REcg -> WatchRGetEcgCommand.Response.parse(buf)
            WatchOperation.RRun -> WatchRGetRunCommand.Response.parse(buf)
            WatchOperation.RRespiration -> WatchRGetRespirationCommand.Response.parse(buf)
            WatchOperation.RSensor -> WatchRGetSensorCommand.Response.parse(buf)
            WatchOperation.RAmbientLight -> WatchRGetAmbientLightCommand.Response.parse(buf)
            WatchOperation.RComprehensive -> WatchRGetComprehensiveCommand.Response.parse(buf)
            WatchOperation.RSchedule -> WatchRGetScheduleCommand.Response.parse(buf)
            WatchOperation.REventReminder -> WatchRGetEventReminderCommand.Response.parse(buf)
            WatchOperation.ROga -> WatchRGetOgaCommand.Response.parse(buf)
            WatchOperation.RInflatedBlood -> WatchRGetInflatedBloodCommand.Response.parse(buf)

            // "W" section

            WatchOperation.WSetCurrentWatchDial -> WatchWSetCurrentWatchDialCommand.Response.parse(buf)
            WatchOperation.WGetWatchDialInfo -> WatchWGetWatchDialInfoCommand.Response.parse(buf)

            // "Others" section

            WatchOperation.CGetSummary -> WatchCGetSummaryCommand.Response.parse(buf)
            WatchOperation.CGetFileCount -> WatchCGetFileCountCommand.Response.parse(buf)
            WatchOperation.CGetFileList -> WatchCGetFileListCommand.Response.parse(buf)
            WatchOperation.CVerifyFile -> WatchCVerifyFileCommand.Response.parse(buf)
            // 16: data block. 23: block. 39: send 0x727 (content: 0) to verify.

            else -> { // TODO remove
                val b = ByteArray(buf.remaining())
                buf.get(b)
                WatchUnknownResponse(operation.code, b)
            }
        }
    }
}