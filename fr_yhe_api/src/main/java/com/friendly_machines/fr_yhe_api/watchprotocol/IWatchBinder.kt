package com.friendly_machines.fr_yhe_api.watchprotocol

import android.os.IBinder
import com.friendly_machines.fr_yhe_api.commondata.SkinColor
import com.friendly_machines.fr_yhe_api.commondata.SportState
import com.friendly_machines.fr_yhe_api.commondata.SportType
import com.friendly_machines.fr_yhe_api.commondata.RealDataSensorType
import com.friendly_machines.fr_yhe_api.commondata.RealDataMeasureType
import com.friendly_machines.fr_yhe_api.commondata.WatchWearingArm
import com.friendly_machines.fr_yhe_api.commondata.DayOfWeekPattern
import com.friendly_machines.fr_yhe_api.commondata.PushMessageType

interface IWatchBinder : IBinder {
    fun setProfile(height: Int, weight: Int, sex: WatchProfileSex, age: Byte, arm: WatchWearingArm?)
    fun setWeather(
        weatherType: Int, temp: Byte, maxTemp: Byte, minTemp: Byte, dummy: Byte/*0*/, month: Byte, dayOfMonth: Byte, dayOfWeekMondayBased: Byte, location: String
    )

    fun setMessage(type: com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed, time: Int, title: String, content: String)
    fun setMessage2(type: Byte, time: Int, title: String, content: String) // FIXME remove
    fun pushMessage(pushMessageType: PushMessageType, message: String)
    fun setTime()
    fun getBatteryState()
    fun getAlarm()
    fun addAlarm(
        id: Int, enabled: Boolean, hour: Byte, min: Byte, title: com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed, repeats: BooleanArray
    )

    fun editAlarm(
        id: Int, enabled: Boolean, hour: Byte, min: Byte, title: com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed, repeats: BooleanArray
    )

    fun bindWatch(userId: Long, key: ByteArray)
    fun unbindWatch()
    fun getDeviceConfig()
    fun getBpData()
    fun getSleepData(startTime: Int, endTime: Int)
    fun getRawBpData(startTime: Int, endTime: Int)
    fun getStepData()
    fun getHeatData()
    fun getWatchDial()
    fun selectWatchFace(id: Int)
    fun getSportData()
    fun setStepGoal(steps: Int)
    fun addListener(watchListener: IWatchListener): IWatchBinder
    fun removeListener(it: IWatchBinder)
    fun resetSequenceNumbers()
    fun analyzeResponse(response: WatchResponse, expectedResponseType: WatchResponseType): WatchResponseAnalysisResult

    fun getFileCount()
    fun getFileList()
    fun setWatchWearingArm(arm: WatchWearingArm)
    fun setDndSettings(mode: Byte, startTimeHour: Byte, startTimeMin: Byte, endTimeHour: Byte, endTimeMin: Byte)
    fun setWatchTimeLayout(watchTimePosition: WatchTimePosition, rgb565Color: UShort)

    fun getGDeviceInfo()
    fun getMainTheme() // not sure that's materially different from getWatchDial
    fun setMainTheme(index: Byte) // not sure that's materially different from selectWatchDial
    fun setLanguage(language: Byte)
    fun setUserSkinColor(enum: SkinColor)
    fun setUserSleep(hour: Byte, minute: Byte, repeats: UByte)
    fun setScheduleEnabled(enabled: Boolean)
    fun setRegularReminder(startHour: Byte, startMinute: Byte, endHour: Byte, endMinute: Byte, dayOfWeekPattern: Set<DayOfWeekPattern>, intervalInMinutes: Byte, message: String?)
    fun setHeartMonitoring(enabled: Boolean, interval: Byte, maxValue: UByte)
    fun setAccidentMonitoringEnabled(enabled: Boolean)
    fun setTemperatureMonitoring(enabled: Boolean, interval: Byte, maxValue: UByte)
    fun setLongSitting(startHour1: Byte, startMinute1: Byte, endHour1: Byte, endMinute1: Byte, startHour2: Byte, startMinute2: Byte, endHour2: Byte, endMinute2: Byte, repeats: UByte, interval: Byte)
    fun setScreenTimeLit(screenTimeLit: Byte)
    fun getChipScheme()
    fun setSportMode(sportState: SportState, sportType: SportType)
    fun getRealData(sensorType: RealDataSensorType, measureType: RealDataMeasureType = RealDataMeasureType.DEFAULT, duration: Byte = 2)

    fun startWatchFaceDownload(length: UInt, dialPlateId: Int, blockNumber: Short, version: Short, crc: UShort)
    fun sendWatchFaceDownloadChunk(chunk: ByteArray)
    fun nextWatchFaceDownloadChunkMeta(deltaOffset: kotlin.Int, packetCount: kotlin.UShort, crc: kotlin.UShort)
    fun stopWatchFaceDownload(length: kotlin.UInt)
}