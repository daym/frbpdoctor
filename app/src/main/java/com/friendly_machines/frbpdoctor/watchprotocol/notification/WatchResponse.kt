package com.friendly_machines.frbpdoctor.watchprotocol.notification

import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.BpDataBlock
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.CurrentHeatDataBlock
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.CurrentStepDataBlock
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.HeatDataBlock
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.SleepDataBlock
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.SportDataBlock
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.StepsDataBlock
import java.nio.ByteBuffer

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

    // TODO 1 OTA_REQUEST
    // TODO 2 OTA_INFO
    // TODO 3 OTA_FILE_OFFSET
    // TODO 4 OTA_SEND_START
    // TODO 5 OTA_SEND_FINISH
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

    data class StepData(val data: Array<StepsDataBlock>) : WatchResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StepData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): StepData {
                // step data (big)
                val arr = ArrayList<StepsDataBlock>()
                while (buf.hasRemaining()) {
                    val item = StepsDataBlock.parse(buf)
                    arr.add(item)
                }
                return StepData(arr.toTypedArray())
            }
        }
    }

    data class SleepData(val data: Array<SleepDataBlock>) : WatchResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SleepData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): SleepData {
                // sleep data (big)
                val arr = ArrayList<SleepDataBlock>()
                while (buf.hasRemaining()) {
                    val item = SleepDataBlock.parse(buf)
                    arr.add(item)
                }
                return SleepData(arr.toTypedArray())
            }
        }
    }

    data class HeatData(val data: Array<HeatDataBlock>) : WatchResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HeatData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): HeatData {
                // heat data (big)
                val arr = ArrayList<HeatDataBlock>()
                while (buf.hasRemaining()) {
                    val item = HeatDataBlock.parse(buf)
                    arr.add(item)
                }
                return HeatData(arr.toTypedArray())
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

    data class SportData(val data: Array<SportDataBlock>) : WatchResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SportData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): SportData {
                // sport data (big)
                val arr = ArrayList<SportDataBlock>()
                while (buf.hasRemaining()) {
                    val item = SportDataBlock.parse(buf)
                    arr.add(item)
                }
                return SportData(arr.toTypedArray())

            }
        }
    }

    data class BpData(val data: Array<BpDataBlock>) : WatchResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BpData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): BpData {
                // bp data (big)
                val arr = ArrayList<BpDataBlock>()
                while (buf.hasRemaining()) {
                    val item = BpDataBlock.parse(buf)
                    arr.add(item)
                }
                return BpData(arr.toTypedArray())
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

    data class SyncTime(val timestamp: Int, val timezone: Int) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): SyncTime {
                val timestamp: Int = buf.int
                val timezone: Int = buf.int
                return SyncTime(timestamp = timestamp, timezone = timezone)
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
    // TODO 52 CMD_MSG_WATCH_TO_APP

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

    data class GetAlarm(val successCount: Byte, val data: ByteArray) : WatchResponse() { // verified
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetAlarm

            if (successCount != other.successCount) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = successCount.toInt()
            result = 31 * result + data.contentHashCode()
            return result
        }

        companion object {
            fun parse(buf: ByteBuffer): GetAlarm {
                // (big)
                val successCount: Byte = buf.get()
                val b = ByteArray(buf.remaining())
                buf.get(b)
                return GetAlarm(successCount = successCount, data = b)
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

    // TODO 67 RAW_BP_DATA

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
        @OptIn(ExperimentalUnsignedTypes::class)
        fun parse(code: Short, buf: ByteBuffer): WatchResponse {
            return when (code) {
                0.toShort() -> DeviceInfo.parse(buf)
                17.toShort() -> Bind.parse(buf)
                18.toShort() -> Unbind.parse(buf)
                23.toShort() -> StepData.parse(buf)
                27.toShort() -> HeatData.parse(buf)
                28.toShort() -> CurrentHeat.parse(buf)
                29.toShort() -> SportData.parse(buf)
                30.toShort() -> BpData.parse(buf)
                42.toShort() -> GetBatteryState.parse(buf)
                43.toShort() -> SyncTime.parse(buf)
                44.toShort() -> SetWeather.parse(buf)
                45.toShort() -> GetDeviceConfig.parse(buf)
                46.toShort() -> GetWatchFace.parse(buf)
                47.toShort() -> SetWatchFace.parse(buf)
                53.toShort() -> SetProfile.parse(buf)
                55.toShort() -> SetAlarm.parse(buf)
                56.toShort() -> GetAlarm.parse(buf)
                63.toShort() -> CurrentStep.parse(buf)
                64.toShort() -> SetMessage.parse(buf)
                else -> {
                    val b = ByteArray(buf.remaining())
                    buf.get(b)
                    Unknown(code, b)
                }
            }
        }
    }
}