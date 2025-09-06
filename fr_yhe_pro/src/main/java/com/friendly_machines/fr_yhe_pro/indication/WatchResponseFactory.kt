package com.friendly_machines.fr_yhe_pro.indication

// import com.friendly_machines.fr_yhe_pro.command.WatchWDialBlockVerifyCommand // FIXME: Unused - uses WNextDownloadChunkMeta operation
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageDecodingException
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchUnknownResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.command.WatchABloodSugarCalibCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAControlAmbientLightCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAControlTempHumidityCommand
import com.friendly_machines.fr_yhe_pro.command.WatchADataConfirmationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAGetRealData
import com.friendly_machines.fr_yhe_pro.command.WatchAHealthAlertConfigCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAHealthConfigCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAHealthDataAckCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAHeartValidationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAInsuranceIntegrationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchALipidCalibCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAMobileDeviceInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchANotificationPushCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAPushCallStateCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAPushMessageCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetCardIdentifierCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetDeviceUUIDCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetLocationIdentifierCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetMeasureIdentifierCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetPDIdentifierCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetProductInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetRunModeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetTodayWeatherCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASetTomorrowWeatherCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAShutdown
import com.friendly_machines.fr_yhe_pro.command.WatchASleepDataAckCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAStepValidationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASyncEmergencyContactsCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASyncMenstrualDataCommand
import com.friendly_machines.fr_yhe_pro.command.WatchASyncTempHumidityCalibCommand
import com.friendly_machines.fr_yhe_pro.command.WatchATemperatureCalibrationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAToggleSensorsCommand
import com.friendly_machines.fr_yhe_pro.command.WatchATriggerBloodTestCommand
import com.friendly_machines.fr_yhe_pro.command.WatchATriggerMeasurementCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAUpgradeNotificationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchAUricAcidCalibCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCDeleteByIndexCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCDeleteByTimestampCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCFileSyncCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetByIndexCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetByTimestampCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetFileCountCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetFileListCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCGetFileMetaDataCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCSyncDataCommand
import com.friendly_machines.fr_yhe_pro.command.WatchCVerifyFileCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetChipSchemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceNameCommand
import com.friendly_machines.fr_yhe_pro.command.WatchGGetDeviceScreenInfoCommand
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
import com.friendly_machines.fr_yhe_pro.command.WatchHHistoryBlockCommand
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
import com.friendly_machines.fr_yhe_pro.command.WatchRUploadECGHrvCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRUploadECGRRCommand
import com.friendly_machines.fr_yhe_pro.command.WatchRUploadMulPhotoelectricWaveformCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSFindPhoneCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSGetAllAlarmsCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetAccidentMonitoringCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetDataCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetDndModeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetEventReminderCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetEventReminderModeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetHeartAlarmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetHeartMonitorCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetLanguageCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetLongSittingCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetMainThemeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetNotificationCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetRegularReminderCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetScheduleCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetScheduleSwitchCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetScreenLitTimeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetSkinColorCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetSleepReminderCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetStepCountingTimeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTemperatureAlarmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTemperatureMonitorCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTimeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetTimeLayoutCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetUnitCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetUploadReminderCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetUserInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchSSetWatchWearingArmCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWControlDownloadCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWDeleteWatchDialCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWGetWatchDialInfoCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWNextDownloadChunkCommand
import com.friendly_machines.fr_yhe_pro.command.WatchWNextDownloadChunkMetaCommand
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
        val result = when (operation) {
            // "Settings" section

            WatchOperation.SSetMainTheme -> WatchSSetMainThemeCommand.Response.parse(buf)
            WatchOperation.SSetLanguage -> WatchSSetLanguageCommand.Response.parse(buf)
            WatchOperation.SSetTime -> WatchSSetTimeCommand.Response.parse(buf)
            WatchOperation.SSetWatchWearingArm -> WatchSSetWatchWearingArmCommand.Response.parse(buf)
            WatchOperation.SUserInfo -> WatchSSetUserInfoCommand.Response.parse(buf)
            WatchOperation.SSetTimeLayout -> WatchSSetTimeLayoutCommand.Response.parse(buf)
            WatchOperation.SSetScheduleSwitch -> WatchSSetScheduleSwitchCommand.Response.parse(buf)
            WatchOperation.SAlarm -> WatchSGetAllAlarmsCommand.Response.parse(buf) // FIXME: SAlarm maps to multiple operations - need context-based routing
            // WatchOperation.SAlarm -> WatchSAddAlarmCommand.Response.parse(buf) // Note: SAlarm can be both add/delete - need proper mapping
            WatchOperation.SSetSleepReminder -> WatchSSetSleepReminderCommand.Response.parse(buf)
            WatchOperation.SSetRegularReminder -> WatchSSetRegularReminderCommand.Response.parse(buf)
            WatchOperation.SHeartAlarm -> WatchSSetHeartAlarmCommand.Response.parse(buf)
            WatchOperation.SHeartMonitor -> WatchSSetHeartMonitorCommand.Response.parse(buf)
            WatchOperation.SSetAccidentMonitoring -> WatchSSetAccidentMonitoringCommand.Response.parse(buf)
            WatchOperation.SSetEventReminder -> WatchSSetEventReminderCommand.Response.parse(buf)
            WatchOperation.SSetEventReminderMode -> WatchSSetEventReminderModeCommand.Response.parse(buf)
            WatchOperation.SFindPhone -> WatchSFindPhoneCommand.Response.parse(buf)
            WatchOperation.SSetLongSitting -> WatchSSetLongSittingCommand.Response.parse(buf)
            WatchOperation.SNotification -> WatchSSetNotificationCommand.Response.parse(buf)
            WatchOperation.SSetScreenLitTime -> WatchSSetScreenLitTimeCommand.Response.parse(buf)
            WatchOperation.SSetSchedule -> WatchSSetScheduleCommand.Response.parse(buf)
            WatchOperation.SSetData -> WatchSSetDataCommand.Response.parse(buf)
            WatchOperation.SSetDnd -> WatchSSetDndModeCommand.Response.parse(buf)
            WatchOperation.SSetSkin -> WatchSSetSkinColorCommand.Response.parse(buf)
            WatchOperation.SSetStepCountingTime -> WatchSSetStepCountingTimeCommand.Response.parse(buf)
            WatchOperation.SSetUploadReminder -> WatchSSetUploadReminderCommand.Response.parse(buf)
            WatchOperation.SSetTemperatureAlarm -> WatchSSetTemperatureAlarmCommand.Response.parse(buf)
            WatchOperation.SSetTemperatureMonitor -> WatchSSetTemperatureMonitorCommand.Response.parse(buf)
            WatchOperation.SUnit -> WatchSSetUnitCommand.Response.parse(buf)
            WatchOperation.GGetChipScheme -> WatchGGetChipSchemeCommand.Response.parse(buf)

            // "Get" section

            WatchOperation.GGetDeviceName -> WatchGGetDeviceNameCommand.Response.parse(buf)
            WatchOperation.GGetRealTemperature -> WatchGGetRealTemperatureCommand.Response.parse(buf)
            WatchOperation.GGetMainTheme -> WatchGGetMainThemeCommand.Response.parse(buf)
            WatchOperation.GGetDeviceScreenInfo -> WatchGGetDeviceScreenInfoCommand.Response.parse(buf)

            WatchOperation.GGetDeviceInfo -> WatchGGetDeviceInfoCommand.Response.parse(buf)
//            WatchOperation.GGetDeviceName -> WatchGGetDeviceNameCommand.Response.parse(buf)
            WatchOperation.GGetScreenInfo -> WatchGGetScreenInfoCommand.Response.parse(buf)
            WatchOperation.GGetElectrodeLocation -> WatchGGetElectrodeLocationCommand.Response.parse(buf)
            WatchOperation.GGetEventReminderInfo -> WatchGGetEventReminderInfoCommand.Response.parse(buf)
            WatchOperation.GGetMacAddress -> WatchGGetMacAddressCommand.Response.parse(buf)
            WatchOperation.GGetManualModeStatus -> WatchGGetManualModeStatusCommand.Response.parse(buf)
            WatchOperation.GGetRealBloodOxygen -> WatchGGetRealBloodOxygenCommand.Response.parse(buf)
            WatchOperation.GGetScreenParameters -> WatchGGetScreenParametersCommand.Response.parse(buf)
            WatchOperation.GGetUserConfig -> WatchGGetUserConfigCommand.Response.parse(buf)

            // "Watch remote controls phone" section

            WatchOperation.DFindMobile -> DFindMobile.parse(buf)
            WatchOperation.DLostReminder -> DLostReminder.parse(buf)
            WatchOperation.DPhoneCallControl -> DPhoneCallControl.parse(buf)
            WatchOperation.DCameraControl -> DCameraControl.parse(buf)
            WatchOperation.DMusicControl -> DMusicControl.parse(buf)
            WatchOperation.DSos -> DSos.parse(buf)
            WatchOperation.DRegularReminder -> DRegularReminder.parse(buf)
            WatchOperation.DConnectOrDisconnect -> DConnectOrDisconnect.parse(buf)
            WatchOperation.DSportMode -> DSportMode.parse(buf)
            WatchOperation.DSyncContacts -> DSyncContacts.parse(buf)
            WatchOperation.DSleepReminder -> DSleepReminder.parse(buf)
            WatchOperation.DEndEcg -> DEndEcg.parse(buf)
            WatchOperation.DSportModeControl -> DSportModeControl.parse(buf)
            WatchOperation.DSwitchDial -> DSwitchDial.parse(buf)
            WatchOperation.DMeasurementResult -> DMeasurementResult.parse(buf)
            WatchOperation.DAlarm -> DAlarm.parse(buf)
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
            WatchOperation.HHistoryBlock -> WatchHHistoryBlockCommand.Response.parse(buf)

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
            WatchOperation.RUploadMulPhotoelectricWaveform -> WatchRUploadMulPhotoelectricWaveformCommand.Response.parse(buf)
            WatchOperation.RUploadECGHrv -> WatchRUploadECGHrvCommand.Response.parse(buf)
            WatchOperation.RUploadECGRR -> WatchRUploadECGRRCommand.Response.parse(buf)

            // "W" section

            WatchOperation.WControlDownload -> WatchWControlDownloadCommand.Response.parse(buf)
            //WatchOperation.WNextDownloadChunk -> WatchWNextDownloadChunkCommand.Response.parse(buf)
            WatchOperation.WNextDownloadChunkMeta -> WatchWNextDownloadChunkMetaCommand.Response.parse(buf)
            WatchOperation.WSetCurrentWatchDial -> WatchWSetCurrentWatchDialCommand.Response.parse(buf)
            WatchOperation.WGetWatchDialInfo -> WatchWGetWatchDialInfoCommand.Response.parse(buf)
            WatchOperation.WDeleteWatchDial -> WatchWDeleteWatchDialCommand.Response.parse(buf)

            // "A" App/Action section
            WatchOperation.AHealthConfig -> WatchAHealthConfigCommand.Response.parse(buf)
            WatchOperation.AHealthDataAck -> WatchAHealthDataAckCommand.Response.parse(buf)
            WatchOperation.ASleepDataAck -> WatchASleepDataAckCommand.Response.parse(buf)
            WatchOperation.AUpgradeNotification -> WatchAUpgradeNotificationCommand.Response.parse(buf)
            WatchOperation.AControlAmbientLight -> WatchAControlAmbientLightCommand.Response.parse(buf)
            WatchOperation.ATemperatureCalibration -> WatchATemperatureCalibrationCommand.Response.parse(buf)
            WatchOperation.AControlTempHumidity -> WatchAControlTempHumidityCommand.Response.parse(buf)
            WatchOperation.AInsuranceIntegration -> WatchAInsuranceIntegrationCommand.Response.parse(buf)
            WatchOperation.AToggleSensors -> WatchAToggleSensorsCommand.Response.parse(buf)
            WatchOperation.AMobileDeviceInfo -> WatchAMobileDeviceInfoCommand.Response.parse(buf)
            WatchOperation.AStepValidation -> WatchAStepValidationCommand.Response.parse(buf)
            WatchOperation.AHeartValidation -> WatchAHeartValidationCommand.Response.parse(buf)
            WatchOperation.AHealthAlertConfig -> WatchAHealthAlertConfigCommand.Response.parse(buf)
            WatchOperation.ASyncEmergencyContacts -> WatchASyncEmergencyContactsCommand.Response.parse(buf)
            WatchOperation.ASyncTempHumidityCalib -> WatchASyncTempHumidityCalibCommand.Response.parse(buf)
            WatchOperation.ASyncMenstrualData -> WatchASyncMenstrualDataCommand.Response.parse(buf)
            WatchOperation.ADataConfirmation -> WatchADataConfirmationCommand.Response.parse(buf)
            WatchOperation.ATriggerBloodTest -> WatchATriggerBloodTestCommand.Response.parse(buf)
            WatchOperation.ATriggerMeasurement -> WatchATriggerMeasurementCommand.Response.parse(buf)
            WatchOperation.ABloodSugarCalib -> WatchABloodSugarCalibCommand.Response.parse(buf)
            WatchOperation.ASetPDIdentifier -> WatchASetPDIdentifierCommand.Response.parse(buf)
            WatchOperation.ASetLocationIdentifier -> WatchASetLocationIdentifierCommand.Response.parse(buf)
            WatchOperation.ASetCardIdentifier -> WatchASetCardIdentifierCommand.Response.parse(buf)
            WatchOperation.ASetMeasureIdentifier -> WatchASetMeasureIdentifierCommand.Response.parse(buf)
            WatchOperation.ASetProductInfo -> WatchASetProductInfoCommand.Response.parse(buf)
            WatchOperation.AUricAcidCalib -> WatchAUricAcidCalibCommand.Response.parse(buf)
            WatchOperation.ALipidCalib -> WatchALipidCalibCommand.Response.parse(buf)
            WatchOperation.ASetDeviceUUID -> WatchASetDeviceUUIDCommand.Response.parse(buf)
            WatchOperation.ASetTodayWeather -> WatchASetTodayWeatherCommand.Response.parse(buf)
            WatchOperation.ASetTomorrowWeather -> WatchASetTomorrowWeatherCommand.Response.parse(buf)
            WatchOperation.ASetRunMode -> WatchASetRunModeCommand.Response.parse(buf)
            WatchOperation.ANotificationPush -> WatchANotificationPushCommand.Response.parse(buf)
            WatchOperation.APushCallState -> WatchAPushCallStateCommand.Response.parse(buf)
            WatchOperation.APushMessage -> WatchAPushMessageCommand.Response.parse(buf)
            WatchOperation.ARealData -> WatchAGetRealData.Response.parse(buf)
            WatchOperation.AShutdown -> WatchAShutdown.Response.parse(buf)

            // "Others" section

            WatchOperation.CGetByIndex -> WatchCGetByIndexCommand.Response.parse(buf)
            WatchOperation.CGetByTimestamp -> WatchCGetByTimestampCommand.Response.parse(buf)
            WatchOperation.CGetFileCount -> WatchCGetFileCountCommand.Response.parse(buf)
            WatchOperation.CGetFileList -> WatchCGetFileListCommand.Response.parse(buf)
            WatchOperation.CGetFileMetaData -> WatchCGetFileMetaDataCommand.Response.parse(buf)
            WatchOperation.CSyncData -> WatchCSyncDataCommand.Response.parse(buf)
            WatchOperation.CFileSync -> WatchCFileSyncCommand.Response.parse(buf)
            WatchOperation.CVerifyFile -> WatchCVerifyFileCommand.Response.parse(buf)
            WatchOperation.CDeleteByIndex -> WatchCDeleteByIndexCommand.Response.parse(buf)
            WatchOperation.CDeleteByTimestamp -> WatchCDeleteByTimestampCommand.Response.parse(buf)
            // 16: data block. 23: block. 39: send 0x727 (content: 0) to verify.

            else -> { // TODO remove
                val b = ByteArray(buf.remaining())
                buf.get(b)
                WatchUnknownResponse(operation.code, b)
            }
        }
        if (buf.hasRemaining()) {
            throw WatchMessageDecodingException("$operation: response has junk in the back")
        }
        return result
    }
}