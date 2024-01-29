package com.friendly_machines.fr_yhe_api.watchprotocol

import android.os.IBinder

interface IWatchCommunication: IBinder {
    fun setProfile(height: Byte, weight: Byte, sex: WatchProfileSex, age: Byte)
    fun setWeather(
        weatherType: Short, temp: Byte, maxTemp: Byte, minTemp: Byte, dummy: Byte/*0*/, month: Byte, dayOfMonth: Byte, dayOfWeekMondayBased: Byte, location: String
    )

    fun setMessage(type: com.friendly_machines.fr_yhe_api.commondata.MessageTypeMed, time: Int, title: String, content: String)
    fun setMessage2(type: Byte, time: Int, title: String, content: String) // FIXME remove
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
    fun getWatchFace()
    fun getSportData()
    fun setStepGoal(steps: Int)
    fun addListener(watchListener: IWatchListener): IWatchCommunication
    fun removeListener(it: IWatchCommunication)
    fun resetSequenceNumbers()
    fun analyzeResponse(response: WatchResponse, expectedResponseType: WatchResponseType): WatchResponseAnalyzationResult
}