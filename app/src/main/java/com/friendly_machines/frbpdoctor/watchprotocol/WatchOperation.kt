package com.friendly_machines.frbpdoctor.watchprotocol

import com.friendly_machines.frbpdoctor.watchprotocol.WatchMessageDecodingException

enum class WatchOperation(val code: Short) {
    DeviceInfo(0.toShort()),
    OtaGetFirmwareVersion(1.toShort()),
    OtaSendInfo(2.toShort()),
    OtaNegotiateFileOffset(3.toShort()),
    OtaSendStart(4.toShort()),
    OtaSendFinish(5.toShort()),
    Bind(17.toShort()),
    Unbind(18.toShort()),
    GetStepData(23.toShort()),
    GetSleepData(24.toShort()),
    GetHeatData(27.toShort()),
    CurrentHeat(28.toShort()),
    GetSportData(29.toShort()),
    GetBpData(30.toShort()),
    GetBatteryState(42.toShort()),
    SetTime(43.toShort()),
    SetWeather(44.toShort()),
    GetDeviceConfig(45.toShort()),
    GetWatchFace(46.toShort()),
    SetWatchFace(47.toShort()),
    NotificationFromWatch(52.toShort()), // there's no command we can send. It's pushed from the watch.
    SetProfile(53.toShort()),
    SetAlarm(55.toShort()),
    GetAlarm(56.toShort()),
    CurrentStep(63.toShort()),
    SetMessage(64.toShort()),
    OtaSendBig(4100.toShort());

    companion object {
        fun parse(code: Short) = values().find { it.code == code } ?: throw WatchMessageDecodingException("unknown command code $code")
    }
}