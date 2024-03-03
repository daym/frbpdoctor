package com.friendly_machines.fr_yhe_pro

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageDecodingException

enum class WatchOperation(val code: Short) {
    /*
     * Hi byte: large section of operation.
     * Lo byte: detail opcode inside that section
     */

    // Settings

    SSetTime(0x0100), SAlarm(0x0101), SGoal(0x0102), SUserInfo(0x0103), SUnit(0x0104), SSetWatchWearingArm(0x0108), SNotification(0x010A), SFindPhone(0x010D), SRestoreFactory(0x010E), SSetDnd(0x010F), SSetLanguage(0x0112), SRaiseScreen(0x0113), SDisplayBrightness(0x0114), SSetSkin(0x0115), SSetMainTheme(0x0119), SSetSleepReminder(0x011A), SGetChipScheme(0x011B), SSetData(0x011E), SSetTemperatureAlarm(0x011F), SSetTemperatureMonitor(0x0120), SSetScheduleSwitch(0x0129), SSetStepCountingTime(0x012A), SSetUploadReminder(0x012B), SRegularReminder(0x013D), SSetTimeLayout(0x0141),

    // "Get"

    GGetDeviceInfo(0x0200),
    GGetMacAddress(0x0202),
    GGetDeviceName(0x0203),
    GGetUserConfig(0x0207),
    GGetMainTheme(0x0209),
    GGetElectrodeLocation(0x020A),
    GGetDeviceScreenInfo(0x020B),
    GGetRealTemperature(0x020E),
    GGetRealBloodOxygen(0x0211),
    GGetManualModeStatus(0x0219),
    GGetEventReminderInfo(0x021A),
    GGetChipScheme(0x021B),
    GGetScreenInfo(0x020F),
    GGetScreenParameters(0x0223),

    // Health

    HGetSportHistory(0x0502), HGetSleepHistory(0x0504), HGetHeartHistory(0x0506), HGetBloodHistory(0x0508 /* FIXME 0x0503 0x0508 ? */), HGetAllHistory(0x0509), HGetBloodOxygenHistory(0x051A), HGetTemperatureAndHumidityHistory(0x051C), HGetTemperatureHistory(0x051E), HGetAmbientLightHistory(0x0520), HGetFallHistory(0x0529), HGetHealthMonitoringHistory(0x052B), HHistorySportMode(0x052D), HGetComprehensiveMeasurementData(0x052F), HGetBackgroundReminderRecordHistory(0x0531), HDeleteSportHistory(0x0540), HDeleteSleepHistory(0x0541), HDeleteHeartHistory(0x0542), HDeleteBloodHistory(0x0543), HDeleteAllHistory(0x0544), HDeleteBloodOxygenHistory(0x0545), HDeleteTemperatureAndHumidityHistory(0x0546), HDeleteTemperatureHistory(0x0547), HDeleteAmbientLightHistory(0x0548), HDeleteFallHistory(0x0549), HDeleteHealthMonitoringHistory(0x054A), HDeleteSportModeHistory(0x054B), HDeleteComprehensiveMeasurementData(0x054C), HDeleteBackgroundReminderRecordHistory(0x054D),
    // TODO acks

    // App Control

    AFindDevice(0x0300), AHeartTest(0x0301), ABloodTest(0x0302), ABloodTest2(0x0303), AAppExit(0x0304), ABindDevice(0x0306), AUnbindDevice(0x0307), ANotificationPush(0x0308), ARealData(0x0309), AQuerySampleRate(0x030A), AWaveUpload(0x030B), ASetRunMode(0x030C), ATakePhoto(0x030e), ASetTodayWeather(0x0312), ASetTomorrowWeather(0x0313), AEcgRealStatus(0x0314), AShutdown(0x0316), ATemperatureCorect(0x0317), ATemperatureMeasurementControl(0x0318), AEmoticonIndex(0x0319), AUserInfo(0x031C), APushCallState(0x032B), // etc

    // TODO 0x0330 push specific information (two strings, limit first to 32, limit second to 512)

    // Dev Control (we just send back that we heard it)

    DFindMobile(0x0400), DLostReminder(0x0401), DPhoneCallControl(0x0402), DPhotoControl(0x0403), DMusicControl(0x0404), DSos(0x0405), DDrinking(0x0406), DConnectOrDisconnect(0x0407), DSportMode(0x0408), DSyncContacts(0x0409), DRest(0x040A), DEndEcg(0x040B), DSportModeControl(0x040C), DSwitchDial(0x040D), DMeasurementResult(0x040E), DAlarmData(0x040F), DInflatedBloodMeasurementResult(0x0410), DUpgradeResult(0x0411), DPpiData(0x0412), DMeasurementStatusAndResult(0x0413), DDynamicCode(0x0415),

    // Real Data

    RSport(0x0600),
    RHeart(0x0601),
    RBloodOxygen(0x0602),
    RBloodPressure(0x0603),
    RPpg(0x0604),
    REcg(0x0605),
    RRun(0x0606),
    RRespiration(0x0607),
    RSensor(0x0608),
    RAmbientLight(0x0609),
    RComprehensive(0x060A),
    RSchedule(0x060B),
    REventReminder(0x060C),
    ROga(0x060D),
    RInflatedBlood(0x060E),

    // Collect

    // TODO: 0x0700: parse: type: Byte, number: Short

    CGetSummary(0x0701),
    CGetFileCount(0x0705),
    CGetFileList(0x0706),
    CGetFileMetaData(0x0707),
    CVerifyFile(0x0727),

    // Watch Dial

    WGetWatchDialInfo(0x0903),
    WDeleteWatchDial(0x0904),
    WSetCurrentWatchDial(0x0905);

    // TODO 0xd75 start customize data sync (arg: byte 4; or bytes 128, type, 0)

    companion object {
        fun parse(code: Short) = values().find { it.code == code } ?: throw WatchMessageDecodingException("unknown command code $code")
    }
}