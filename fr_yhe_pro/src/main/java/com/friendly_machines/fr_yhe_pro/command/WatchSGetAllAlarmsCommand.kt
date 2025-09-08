package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// SAlarm is used in multiple commands
class WatchSGetAllAlarmsCommand : WatchCommand(WatchOperation.SAlarm, ByteArray(1)) {
    // FIXME: [0,10,0]
    data class Response(
        val alarmCommand: Byte,
        val maxAlarmCount: Byte, 
        val currentAlarmCount: Byte,
        val alarms: List<AlarmData>
    ) : WatchResponse() {
        data class AlarmData(
            val alarmType: Byte,
            val alarmHour: Byte, 
            val alarmMin: Byte,
            val alarmRepeat: Byte,
            val alarmDelayTime: Byte
        )
        
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val alarmCommand = buf.get()
                val maxAlarmCount = buf.get()
                val currentAlarmCount = buf.get()
                
                val alarms = mutableListOf<AlarmData>()
                repeat(currentAlarmCount.toInt()) {
                    alarms.add(AlarmData(
                        alarmType = buf.get(),
                        alarmHour = buf.get(),
                        alarmMin = buf.get(),
                        alarmRepeat = buf.get(),
                        alarmDelayTime = buf.get()
                    ))
                }
                
                return Response(alarmCommand, maxAlarmCount, currentAlarmCount, alarms)
            }
        }
    }
}
