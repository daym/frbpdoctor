package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer


class WatchGGetUserConfigCommand : WatchCommand(WatchOperation.GGetUserConfig, "CF".toByteArray(charset = Charsets.US_ASCII)) {
    data class Response(
        val targetSteps: Int, // default: 10000
        val targetCalories: Int,
        val targetDistance: Int,
        val targetSleep: Short,
        val userHeight: UByte,
        val userWeight: Byte,
        val userSex: Byte,
        val userAge: Byte,
        val distanceUnit: Byte,
        val weightUnit: Byte,
        val temperatureUnit: Byte,
        val timeUnit: Byte,
        val longSitting1StartHour: Byte, // 9
        val longSitting1StartMinute: Byte, // 0
        val longSitting1EndHour: Byte, // 12
        val longSitting1EndMinute: Byte, // 30
        val longSitting2StartHour: Byte, // 14
        val longSitting2StartMinute: Byte, // 0
        val longSitting2EndHour: Byte, // 18
        val longSitting2EndMinute: Byte, // 30
        val longSittingInterval: Byte, // 30
        val longSittingRepeat: Byte, // 0
        val antiLossType: Byte,
        val antiLossRssi: Byte, // default -90; ACTUALLY signed.
        val antiLossDelay: Byte,
        val antiLossDisableDelay: Byte,
        val antiLossRepeat: Byte,
        val messageTotalSwitch: Byte, // 1
        val messageSwitch1: Byte, // -1
        val messageSwitch2: Byte, // -2
        val heartHand: Byte,
        val heartAlarmSwitch: Byte,
        val heartAlarmValue: UByte, // default 160
        val heartMonitorType: Byte, // default 1
        val heartMonitorInterval: Byte, // default 60
        val language: Byte,
        val handRaiseSwitch: Byte,
        val screen: Byte, // 1
        val skinColor: Byte, // 2
        val screenDown: Byte, // 3
        val blueBreakSwitch: Byte,
        val dataUploadSwitch: Byte,
        val dndSwitch: Byte,
        val dndBeginHour: Byte, // default 22
        val dndBeginMinute: Byte, // default 0
        val dndEndHour: Byte, // default 7
        val dndEndMinute: Byte, // default 0
        val sleepSwitch: Byte,
        val sleepBeginHour: Byte,
        val sleepBeginMinute: Byte, // -20 ?!
        val scheduleSwitch: Byte,
        val eventSwitch: Byte,
        val accidentSwitch: Byte,
        val temperatureSwitch: Byte,
    ) : WatchResponse() {
        override fun toString(): String {
            return buildString {
                appendLine("Target:")
                appendLine("  Steps=$targetSteps")
                appendLine("  Calories=$targetCalories")
                appendLine("  Distance=$targetDistance")
                appendLine("  Sleep=$targetSleep")

                appendLine("User:")
                appendLine("  Height=$userHeight")
                appendLine("  Weight=$userWeight")
                appendLine("  Sex=$userSex")
                appendLine("  Age=$userAge")

                appendLine("Units:")
                appendLine("  Distance=$distanceUnit")
                appendLine("  Weight=$weightUnit")
                appendLine("  Temperature=$temperatureUnit")
                appendLine("  Time=$timeUnit")

                appendLine("Long Sitting 1:")
                appendLine("  Start Hour=$longSitting1StartHour")
                appendLine("  Start Minute=$longSitting1StartMinute")
                appendLine("  End Hour=$longSitting1EndHour")
                appendLine("  End Minute=$longSitting1EndMinute")

                appendLine("Long Sitting 2:")
                appendLine("  Start Hour=$longSitting2StartHour")
                appendLine("  Start Minute=$longSitting2StartMinute")
                appendLine("  End Hour=$longSitting2EndHour")
                appendLine("  End Minute=$longSitting2EndMinute")
                appendLine("  Interval=$longSittingInterval")
                appendLine("  Repeat=$longSittingRepeat")

                appendLine("Anti-loss:")
                appendLine("  Type=$antiLossType")
                appendLine("  Rssi=$antiLossRssi")
                appendLine("  Delay=$antiLossDelay")
                appendLine("  Disable Delay=$antiLossDisableDelay")
                appendLine("  Repeat=$antiLossRepeat")

                appendLine("Message:")
                appendLine("  Total Switch=$messageTotalSwitch")
                appendLine("  Switch 1=$messageSwitch1")
                appendLine("  Switch 2=$messageSwitch2")

                appendLine("Heart:")
                appendLine("  Hand=$heartHand")
                appendLine("  Alarm Switch=$heartAlarmSwitch")
                appendLine("  Alarm Value=$heartAlarmValue")
                appendLine("  Monitor Type=$heartMonitorType")
                appendLine("  Monitor Interval=$heartMonitorInterval")

                appendLine("Language:")
                appendLine("  Language=$language")

                appendLine("Hand Raise:")
                appendLine("  Switch=$handRaiseSwitch")

                appendLine("Screen:")
                appendLine("  Screen=$screen")
                appendLine("  Skin Color=$skinColor")
                appendLine("  Screen Down=$screenDown")

                appendLine("Blue Break:")
                appendLine("  Switch=$blueBreakSwitch")

                appendLine("Data Upload:")
                appendLine("  Switch=$dataUploadSwitch")

                appendLine("Do Not Disturb:")
                appendLine("  Switch=$dndSwitch")
                appendLine("  Begin Hour=$dndBeginHour")
                appendLine("  Begin Minute=$dndBeginMinute")
                appendLine("  End Hour=$dndEndHour")
                appendLine("  End Minute=$dndEndMinute")

                appendLine("Sleep:")
                appendLine("  Switch=$sleepSwitch")
                appendLine("  Begin Hour=$sleepBeginHour")
                appendLine("  Begin Minute=$sleepBeginMinute")

                appendLine("Schedule:")
                appendLine("  Switch=$scheduleSwitch")

                appendLine("Event:")
                appendLine("  Switch=$eventSwitch")

                appendLine("Accident:")
                appendLine("  Switch=$accidentSwitch")

                appendLine("Temperature:")
                appendLine("  Switch=$temperatureSwitch")
            }
        }

        companion object {
            private fun read24BitLeInt(buf: ByteBuffer): Int {
                val byte1 = buf.get().toInt() and 0xFF
                val byte2 = buf.get().toInt() and 0xFF
                val byte3 = buf.get().toInt() and 0xFF
                return (byte3 shl 16) or (byte2 shl 8) or byte1
            }

            fun parse(buf: ByteBuffer): Response {
                val version = buf.remaining()
                val result = Response(
                    targetSteps = read24BitLeInt(buf),
                    targetCalories = read24BitLeInt(buf),
                    targetDistance = read24BitLeInt(buf),
                    targetSleep = buf.short,
                    userHeight = buf.get().toUByte(),
                    userWeight = buf.get(),
                    userSex = buf.get(),
                    userAge = buf.get(),
                    distanceUnit = buf.get(),
                    weightUnit = buf.get(),
                    temperatureUnit = buf.get(),
                    timeUnit = buf.get(),
                    longSitting1StartHour = buf.get(),
                    longSitting1StartMinute = buf.get(),
                    longSitting1EndHour = buf.get(),
                    longSitting1EndMinute = buf.get(),
                    longSitting2StartHour = buf.get(),
                    longSitting2StartMinute = buf.get(),
                    longSitting2EndHour = buf.get(),
                    longSitting2EndMinute = buf.get(),
                    longSittingInterval = buf.get(),
                    longSittingRepeat = buf.get(),
                    antiLossType = buf.get(),
                    antiLossRssi = buf.get(),
                    antiLossDelay = buf.get(),
                    antiLossDisableDelay = buf.get(),
                    antiLossRepeat = buf.get(),
                    messageTotalSwitch = buf.get(),
                    messageSwitch1 = buf.get(),
                    messageSwitch2 = buf.get(),
                    heartHand = buf.get(),
                    heartAlarmSwitch = buf.get(),
                    heartAlarmValue = buf.get().toUByte(),
                    heartMonitorType = buf.get(),
                    heartMonitorInterval = buf.get(),
                    language = buf.get(),
                    handRaiseSwitch = buf.get(),
                    screen = buf.get(),
                    skinColor = buf.get(),
                    screenDown = buf.get(),
                    blueBreakSwitch = buf.get(),
                    dataUploadSwitch = buf.get(),
                    dndSwitch = buf.get(),
                    dndBeginHour = buf.get(),
                    dndBeginMinute = buf.get(),
                    dndEndHour = buf.get(),
                    dndEndMinute = buf.get(),
                    // TODO: If version >= 65, continue
                    sleepSwitch = buf.get(),
                    sleepBeginHour = buf.get(),
                    sleepBeginMinute = buf.get(), // definitely wrong (-20)
                    scheduleSwitch = buf.get(),
                    eventSwitch = buf.get(),
                    accidentSwitch = buf.get(),
                    temperatureSwitch = buf.get()
                )
                val a0 = buf.get() // 5
                val a1 = buf.get() // 0
                val a2 = buf.get() // 0
                val a3 = buf.get() // 0
                val a4 = buf.get() // 2
                // Note: should be 66 B
                return result
            }
        }
    }
}