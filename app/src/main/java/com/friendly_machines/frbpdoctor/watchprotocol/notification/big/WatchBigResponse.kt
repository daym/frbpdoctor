package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchMessageDecodingException
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

    data class GetSleepData(val data: Array<SleepDataBlock>) : WatchBigResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetSleepData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): GetSleepData {
                // sleep data (big)
                val arr = ArrayList<SleepDataBlock>()
                while (buf.hasRemaining()) {
                    val item = SleepDataBlock.parse(buf)
                    arr.add(item)
                }
                return GetSleepData(arr.toTypedArray())
            }
        }
    }

    data class GetHeatData(val data: Array<HeatDataBlock>) : WatchBigResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetHeatData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): GetHeatData {
                // heat data (big)
                val arr = ArrayList<HeatDataBlock>()
                while (buf.hasRemaining()) {
                    val item = HeatDataBlock.parse(buf)
                    arr.add(item)
                }
                return GetHeatData(arr.toTypedArray())
            }
        }
    }

    data class GetStepData(val data: Array<StepsDataBlock>) : WatchBigResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetStepData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): GetStepData {
                // step data (big)
                val arr = ArrayList<StepsDataBlock>()
                while (buf.hasRemaining()) {
                    val item = StepsDataBlock.parse(buf)
                    arr.add(item)
                }
                return GetStepData(arr.toTypedArray())
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

    data class GetSportData(val data: Array<SportDataBlock>) : WatchBigResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetSportData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): GetSportData {
                // sport data (big)
                val arr = ArrayList<SportDataBlock>()
                while (buf.hasRemaining()) {
                    val item = SportDataBlock.parse(buf)
                    arr.add(item)
                }
                return GetSportData(arr.toTypedArray())

            }
        }
    }

    data class GetBpData(val data: Array<BpDataBlock>) : WatchBigResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetBpData

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): GetBpData {
                // bp data (big)
                val arr = ArrayList<BpDataBlock>()
                while (buf.hasRemaining()) {
                    val item = BpDataBlock.parse(buf)
                    arr.add(item)
                }
                return GetBpData(arr.toTypedArray())
            }
        }
    }

    companion object {
        const val RAW_BLOOD_PRESSURE: Short = 67.toShort() // (big)

        fun parse(code: Short, buf: ByteBuffer): WatchBigResponse {
            buf.order(ByteOrder.BIG_ENDIAN)
            val operation = try {
                WatchOperation.parse(code)
            } catch (e: WatchMessageDecodingException) {
                val b = ByteArray(buf.remaining())
                buf.get(b)
                return Unknown(code, b)
            }
            return when (operation) {
                WatchOperation.GetStepData -> GetStepData.parse(buf)
                WatchOperation.GetSleepData -> GetSleepData.parse(buf)
                WatchOperation.GetHeatData -> GetHeatData.parse(buf)
                WatchOperation.GetSportData -> GetSportData.parse(buf)
                WatchOperation.GetBpData -> GetBpData.parse(buf)
                WatchOperation.GetAlarm -> GetAlarm.parse(buf)
                else -> {
                    val b = ByteArray(buf.remaining())
                    buf.get(b)
                    Unknown(operation.code, b)
                }
            }
        }
    }
}
