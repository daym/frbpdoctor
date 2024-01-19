package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class WatchBigResponse {
    data class GetAlarm(val items: List<AlarmDataBlock>) : WatchBigResponse() {
        companion object {
            fun parse(buf: ByteBuffer): GetAlarm {
                val items = ArrayList<AlarmDataBlock>()
                while (buf.hasRemaining()) {
                    val item = AlarmDataBlock.parse(buf)
                    items.add(item)
                }
                return GetAlarm(items = items)
            }
        }
    }

    data class SleepData(val data: Array<SleepDataBlock>) : WatchBigResponse() {
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

    data class HeatData(val data: Array<HeatDataBlock>) : WatchBigResponse() {
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

    data class StepData(val data: Array<StepsDataBlock>) : WatchBigResponse() {
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

    data class Unknown(val code: Short, val arguments: ByteArray) : WatchBigResponse() {
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

    data class SportData(val data: Array<SportDataBlock>) : WatchBigResponse() {
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

    data class BpData(val data: Array<BpDataBlock>) : WatchBigResponse() {
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

    companion object {
        val RAW_BLOOD_PRESSURE: Short = 67.toShort() // (big)

        @OptIn(ExperimentalUnsignedTypes::class)
        fun parse(code: Short, buf: ByteBuffer): WatchBigResponse {
            buf.order(ByteOrder.BIG_ENDIAN)
            return when (code) {
                23.toShort() -> StepData.parse(buf)
                27.toShort() -> HeatData.parse(buf)
                29.toShort() -> SportData.parse(buf)
                30.toShort() -> BpData.parse(buf)
                56.toShort() -> GetAlarm.parse(buf)
                else -> {
                    val b = ByteArray(buf.remaining())
                    buf.get(b)
                    Unknown(code, b)
                }
            }
        }
    }
}
