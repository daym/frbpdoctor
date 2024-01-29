package com.friendly_machines.fr_yhe_med

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageDecodingException
import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class WatchBigResponseMed {
    data class GetAlarm(val data: Array<com.friendly_machines.fr_yhe_api.commondata.AlarmDataBlock>) : WatchBigResponseMed() {
        companion object {
            fun parse(buf: ByteBuffer): GetAlarm {
                val data = ArrayList<com.friendly_machines.fr_yhe_api.commondata.AlarmDataBlock>()
                while (buf.hasRemaining()) {
                    val item = com.friendly_machines.fr_yhe_api.commondata.AlarmDataBlock.parseMed(buf)
                    data.add(item)
                }
                return GetAlarm(data = data.toTypedArray())
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetAlarm

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }

    data class GetSleepData(val data: Array<com.friendly_machines.fr_yhe_api.commondata.SleepDataBlock>) : WatchBigResponseMed() {
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
                val arr = ArrayList<com.friendly_machines.fr_yhe_api.commondata.SleepDataBlock>()
                while (buf.hasRemaining()) {
                    val item = com.friendly_machines.fr_yhe_api.commondata.SleepDataBlock.parseMed(buf)
                    arr.add(item)
                }
                return GetSleepData(arr.toTypedArray())
            }
        }
    }

    data class GetHeatData(val data: Array<com.friendly_machines.fr_yhe_api.commondata.HeatDataBlock>) : WatchBigResponseMed() {
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
                val arr = ArrayList<com.friendly_machines.fr_yhe_api.commondata.HeatDataBlock>()
                while (buf.hasRemaining()) {
                    val item = com.friendly_machines.fr_yhe_api.commondata.HeatDataBlock.parseMed(buf)
                    arr.add(item)
                }
                return GetHeatData(arr.toTypedArray())
            }
        }
    }

    data class GetStepData(val data: Array<com.friendly_machines.fr_yhe_api.commondata.StepsDataBlock>) : WatchBigResponseMed() {
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
                val arr = ArrayList<com.friendly_machines.fr_yhe_api.commondata.StepsDataBlock>()
                while (buf.hasRemaining()) {
                    val item = com.friendly_machines.fr_yhe_api.commondata.StepsDataBlock.parseMed(buf)
                    arr.add(item)
                }
                return GetStepData(arr.toTypedArray())
            }
        }
    }

    data class Unknown(val code: Short, val arguments: ByteArray) : WatchBigResponseMed() {
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

    data class GetSportData(val data: Array<com.friendly_machines.fr_yhe_api.commondata.SportDataBlock>) : WatchBigResponseMed() {
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
                val arr = ArrayList<com.friendly_machines.fr_yhe_api.commondata.SportDataBlock>()
                while (buf.hasRemaining()) {
                    val item = com.friendly_machines.fr_yhe_api.commondata.SportDataBlock.parseMed(buf)
                    arr.add(item)
                }
                return GetSportData(arr.toTypedArray())

            }
        }
    }

    data class GetBpData(val data: Array<com.friendly_machines.fr_yhe_api.commondata.BpDataBlock>) : WatchBigResponseMed() {
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
                val arr = ArrayList<com.friendly_machines.fr_yhe_api.commondata.BpDataBlock>()
                while (buf.hasRemaining()) {
                    val item = com.friendly_machines.fr_yhe_api.commondata.BpDataBlock.parseMed(buf)
                    arr.add(item)
                }
                return GetBpData(arr.toTypedArray())
            }
        }
    }

    companion object {
        const val RAW_BLOOD_PRESSURE: Short = 67.toShort() // (big)

        fun parse(code: Short, buf: ByteBuffer): WatchBigResponseMed {
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
