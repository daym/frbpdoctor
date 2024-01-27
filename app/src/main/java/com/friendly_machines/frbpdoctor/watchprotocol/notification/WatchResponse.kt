package com.friendly_machines.frbpdoctor.watchprotocol.notification

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import com.friendly_machines.frbpdoctor.watchprotocol.WatchMessageDecodingException
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.CurrentHeatDataBlock
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.CurrentStepDataBlock
import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class WatchResponse {
    data class DeviceInfo(val romVersion: Int, val soc: Int, val protocolVersion: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): DeviceInfo {
                // I think 4.2 is the protocol version
                val romVersion = buf.int
                val soc = buf.int
                val protocolVersion = buf.short // this might be the protocol version (4.2)
                // It has a lot of other crap, too. The next byte is maybe a "bound" flag
                return DeviceInfo(romVersion = romVersion, soc = soc, protocolVersion = protocolVersion)
            }
        }
    }

    data class OtaGetFirmwareVersion(val soc: Int, val firmwareVersion: Int) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): OtaGetFirmwareVersion {
                val soc = buf.int
                val firmwareVersion = buf.int
                return OtaGetFirmwareVersion(soc = soc, firmwareVersion = firmwareVersion)
            }
        }
    }
    data class OtaSendInfo(val flag: Byte, val romVersion: Int, val maxSize: Short): WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): OtaSendInfo {
                val flag: Byte = buf.get()

                // TODO The following not for fonts.

                val romVersion = buf.int
                val maxSize = buf.short
                return OtaSendInfo(flag = flag, romVersion = romVersion, maxSize = maxSize)
            }
        }
    }
    data class OtaNegotiateFileOffset(val type: Byte, val offset: Int) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): OtaNegotiateFileOffset {
                val type: Byte = buf.get()
                val offset = buf.int
                return OtaNegotiateFileOffset(type = type, offset = offset)
            }
        }
    }

    data class OtaSendStart(val type: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): OtaSendStart {
                val type: Byte = buf.get()
                // TODO more stuff, maybe.
                return OtaSendStart(type = type)
            }
        }
    }

    data class OtaSendFinish(val type: Byte, val state: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): OtaSendFinish {
                val type: Byte = buf.get()
                val state = buf.short // 1 ok
                return OtaSendFinish(type = type, state = state)
            }
        }
    }

    // TODO 0x1004 OTA_SEND_BIG (big)

    data class Bind(val status: Byte) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Bind {
                val status: Byte = buf.get()
                return Bind(status = status)
            }
        }
    }

    data class Unbind(
        val status: Byte
    ) : WatchResponse() // kinda verified
    {
        companion object {
            fun parse(buf: ByteBuffer): Unbind {
                val status: Byte = buf.get()
                return Unbind(status = status)
            }
        }
    }

    data class GetStepData(val count: Int) : WatchResponse() { // verified
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetStepData

            if (count != other.count) return false

            return true
        }

        override fun hashCode(): Int {
            return count
        }

        companion object {
            fun parse(buf: ByteBuffer): GetStepData {
                val count = buf.int
//                val b = ByteArray(buf.remaining()) // [0]
//                buf.get(b)
                return GetStepData(count = count)
            }
        }
    }

    data class GetSleepData(val dummy: Byte): WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): GetSleepData {
                // There seem to be 10 B, all 0.
                return GetSleepData(1.toByte())
            }
        }
    }
    data class GetHeatData(val count: Int) : WatchResponse() { // verified
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetHeatData

            if (count != other.count) return false

            return true
        }

        override fun hashCode(): Int {
            return count
        }

        companion object {
            fun parse(buf: ByteBuffer): GetHeatData {
                val count = buf.int
//                val b = ByteArray(buf.remaining()) // [0]
//                buf.get(b)
                return GetHeatData(count = count)
            }
        }
    }

    data class CurrentHeat(val data: CurrentHeatDataBlock) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): CurrentHeat {
                // current heat (big)
                val item = CurrentHeatDataBlock.parse(buf)
                return CurrentHeat(item)
            }
        }
    }

    data class GetBpData(val count: Int) : WatchResponse() { // verified
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetBpData

            if (count != other.count) return false

            return true
        }

        override fun hashCode(): Int {
            return count
        }

        companion object {
            fun parse(buf: ByteBuffer): GetBpData {
                val count = buf.int
//                val b = ByteArray(buf.remaining()) // [0]
//                buf.get(b)
                return GetBpData(count = count)
            }
        }
    }

    data class GetSportData(val count: Int) : WatchResponse() { // verified
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetSportData

            if (count != other.count) return false

            return true
        }

        override fun hashCode(): Int {
            return count
        }

        companion object {
            fun parse(buf: ByteBuffer): GetSportData {
                val count = buf.int
//                val b = ByteArray(buf.remaining()) // [0]
//                buf.get(b)
                return GetSportData(count = count)
            }
        }
    }

    data class GetBatteryState(val id: Byte, val voltage: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): GetBatteryState {
                val id: Byte = buf.get()
                val voltage: Short = buf.short
                return GetBatteryState(id = id, voltage = voltage)
            }
        }
    }

    data class SetTime(val timestamp: Int, val timezone: Int) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): SetTime {
                val timestamp: Int = buf.int
                val timezone: Int = buf.int
                return SetTime(timestamp = timestamp, timezone = timezone)
            }
        }
    }

    data class SetWeather(
        val status: Byte // verified
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): SetWeather {
                val status: Byte = buf.get()
                return SetWeather(status = status)
            }
        }
    }

    data class GetDeviceConfig(val body: ByteArray) : WatchResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetDeviceConfig

            if (!body.contentEquals(other.body)) return false

            return true
        }

        override fun hashCode(): Int {
            return body.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): GetDeviceConfig {
                // big
                val count = buf.remaining()
                val body = ByteArray(count)
                buf.get(body)
                return GetDeviceConfig(body)
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    data class GetWatchFace(val count: Short, val content: UIntArray) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): GetWatchFace {
                val countMinus1: Byte = buf.get()
                val count: Short = (countMinus1 + 1).toShort()
                val arr = UIntArray(count.toInt())
                for (i in 0 until count) {
                    arr[i] = buf.int.toUInt()
                }
                return GetWatchFace(count, arr)
            }
        }
    }

    data class SetWatchFace(val status: Byte) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): SetWatchFace {
                val status: Byte = buf.get()
                return SetWatchFace(status = status)
            }
        }
    }

    // This one can happen without us sending a command! So it's not really a response.
    data class NotificationFromWatch(val eventCode: Byte) : WatchResponse() {
        companion object {
            const val EVENT_CODE_ANSWER_PHONE_CALL: Byte = 0
            const val EVENT_CODE_RECONFIGURE_WATCH: Byte = 1 // (for example language; but not alarm)
            fun parse(buf: ByteBuffer): NotificationFromWatch {
                val eventCode: Byte = buf.get()
                return NotificationFromWatch(eventCode = eventCode)
            }
        }

    }

    data class SetProfile(val status: Byte) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): SetProfile {
                val status = buf.get()
                return SetProfile(status = status)
            }
        }
    }
    // TODO 54 STEP_GOAL

    data class SetAlarm(
        val status: Byte // verified
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): SetAlarm {
                val status = buf.get()
                return SetAlarm(status = status)
            }
        }
    }

    data class GetAlarm(val count: Int) : WatchResponse() { // verified
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetAlarm

            if (count != other.count) return false

            return true
        }

        override fun hashCode(): Int {
            return count
        }

        companion object {
            fun parse(buf: ByteBuffer): GetAlarm {
                val count = buf.int
//                val b = ByteArray(buf.remaining()) // [0]
//                buf.get(b)
                return GetAlarm(count = count)
            }
        }
    }

    data class CurrentStep(val block: CurrentStepDataBlock) : WatchResponse() // (big)
    {
        companion object {
            fun parse(buf: ByteBuffer): CurrentStep {
                // (big)
                return CurrentStep(CurrentStepDataBlock.parse(buf))
            }
        }
    }

    data class SetMessage(
        val status: Byte
    ) : WatchResponse() // verified
    {
        companion object {
            fun parse(buf: ByteBuffer): SetMessage {
                val status = buf.get()
                return SetMessage(status = status)
            }
        }
    }

    data class SetStepGoal(
        val status: Byte
    ) : WatchResponse() // verified
    {
        companion object {
            fun parse(buf: ByteBuffer): SetStepGoal {
                val status = buf.get()
                return SetStepGoal(status = status)
            }
        }
    }

    data class Unknown(val code: Short, val arguments: ByteArray) : WatchResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Unknown

            if (code != other.code) return false
            if (!arguments.contentEquals(other.arguments)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = code.toInt()
            result = 31 * result + arguments.contentHashCode()
            return result
        }
    }

    companion object {
        fun parse(code: Short, buf: ByteBuffer): WatchResponse {
            buf.order(ByteOrder.BIG_ENDIAN)
            val operation = try {
                WatchOperation.parse(code)
            } catch (e: WatchMessageDecodingException) {
                val b = ByteArray(buf.remaining())
                buf.get(b)
                return Unknown(code, b)
            }
            return when (operation) {
                WatchOperation.DeviceInfo -> DeviceInfo.parse(buf)
                WatchOperation.OtaGetFirmwareVersion -> OtaGetFirmwareVersion.parse(buf)
                WatchOperation.OtaSendInfo -> OtaSendInfo.parse(buf)
                WatchOperation.OtaNegotiateFileOffset -> OtaNegotiateFileOffset.parse(buf)
                WatchOperation.OtaSendStart -> OtaSendStart.parse(buf)
                WatchOperation.OtaSendFinish -> OtaSendFinish.parse(buf)
                WatchOperation.Bind -> Bind.parse(buf)
                WatchOperation.Unbind -> Unbind.parse(buf)
                WatchOperation.GetStepData -> GetStepData.parse(buf)
                WatchOperation.GetSleepData -> GetSleepData.parse(buf)
                WatchOperation.GetHeatData -> GetHeatData.parse(buf)
                WatchOperation.CurrentHeat -> CurrentHeat.parse(buf)
                WatchOperation.GetSportData -> GetSportData.parse(buf)
                WatchOperation.GetBpData -> GetBpData.parse(buf)
                WatchOperation.GetBatteryState -> GetBatteryState.parse(buf)
                WatchOperation.SetTime -> SetTime.parse(buf)
                WatchOperation.SetWeather -> SetWeather.parse(buf)
                WatchOperation.GetDeviceConfig -> GetDeviceConfig.parse(buf)
                WatchOperation.GetWatchFace -> GetWatchFace.parse(buf)
                WatchOperation.SetWatchFace -> SetWatchFace.parse(buf)
                WatchOperation.NotificationFromWatch -> NotificationFromWatch.parse(buf)
                WatchOperation.SetProfile -> SetProfile.parse(buf)
                WatchOperation.SetStepGoal -> SetStepGoal.parse(buf)
                WatchOperation.SetAlarm -> SetAlarm.parse(buf)
                WatchOperation.GetAlarm -> GetAlarm.parse(buf)
                WatchOperation.CurrentStep -> CurrentStep.parse(buf)
                WatchOperation.SetMessage -> SetMessage.parse(buf)
                else -> { // TODO remove
                    val b = ByteArray(buf.remaining())
                    buf.get(b)
                    Unknown(operation.code, b)
                }
            }
        }
    }
}