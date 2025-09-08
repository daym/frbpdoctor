package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun decodeIntegerDouble(integerPart: Byte, floatPart: Byte): Double {
    if (floatPart >= 10) {
        throw RuntimeException("safety triggered because floatPart >= 10")
    }
    return String.format("%d.%d", integerPart, floatPart).toDouble()
}
const val B: Int = 1 // "Byte" unit

data class HSportDataBlock(
    val startTime: Timestamp2000,
    val endTime: Timestamp2000,
    val steps: UShort,
    val distance: Short,
    val calories: Short
) {
    companion object {
        const val SIZE: Int = (4 + 4 + 2 + 2 + 2)*B

        fun parsePro(buf: ByteBuffer): HSportDataBlock {
            return HSportDataBlock(
                startTime = Timestamp2000(buf.int),
                endTime = Timestamp2000(buf.int),
                steps = buf.short.toUShort(),
                distance = buf.short,
                calories = buf.short
            )
        }
    }
}

data class HSleepSegment(
    val sleepType: Byte,
    val sleepStartTime: Int,
    val sleepLen: Int  // 3 bytes in original
)

data class HSleepDataBlock(
    val startTime: Timestamp2000,
    val endTime: Timestamp2000,
    val deepSleepCount: Short,
    val lightSleepCount: Short,
    val deepSleepTotal: Short,  // in seconds if deepSleepCount != 0xFFFF, else rapidEyeMovementTotal
    val lightSleepTotal: Short,  // in seconds if deepSleepCount != 0xFFFF
    val sleepSegments: List<HSleepSegment>,
    val wakeCount: Int,
    val wakeDuration: Int
) {
    companion object {
        const val SIZE: Int = 20*B // Base header size, but has variable length segment array

        fun parsePro(buf: ByteBuffer): HSleepDataBlock {
            // Skip first 2 bytes (thrown away in decompiled source)
            buf.position(buf.position() + 2)
            
            // Read 2 bytes - some value (r4 in decompiled)
            val r4 = buf.short
            
            // Read times
            val startTime = buf.int
            val endTime = buf.int
            
            // Read deep sleep count (if 0xFFFF, special handling)
            val deepSleepCount = buf.short
            
            val lightSleepCount: Short
            val deepSleepTotal: Short
            val lightSleepTotal: Short
            
            if (deepSleepCount == 0xFFFF.toShort()) {
                // Special case - lines 3686-3713
                val rapidEyeMovement = buf.short  // stored as r6/r29
                lightSleepCount = 0  // not read in this case
                deepSleepTotal = buf.short  // stored as r14/r4 (not multiplied)
                lightSleepTotal = buf.short  // stored as r15/r26 (not multiplied)
            } else {
                // Normal case - lines 3716-3746
                lightSleepCount = buf.short  // stored as r5
                deepSleepTotal = (buf.short * 60).toShort()  // multiplied by 60
                lightSleepTotal = (buf.short * 60).toShort()  // multiplied by 60
            }
            
            // Read sleep segments (8 bytes each) - lines 3762-3832
            val segments = mutableListOf<HSleepSegment>()
            var wakeCount = 0
            var wakeDuration = 0
            
            // Segments continue until we've read (r4 - 20) bytes from start of segments
            val segmentStartPos = buf.position()
            while (buf.remaining() >= 8) {
                val segType = buf.get()
                buf.get() // skip 1 byte
                val segStartTime = buf.int
                // Read 3-byte length (little-endian)
                val segLen = (buf.get().toInt() and 0xFF) or 
                             ((buf.get().toInt() and 0xFF) shl 8) or
                             ((buf.get().toInt() and 0xFF) shl 16)
                
                // Type 0xF4 (244) is wake segment
                if (segType == 0xF4.toByte()) {
                    wakeCount++
                    wakeDuration += segLen
                }
                
                segments.add(HSleepSegment(segType, segStartTime, segLen))
                
                // Check if we've read enough based on r4 value
                if (buf.position() - segmentStartPos >= r4 - 20) break
            }
            
            return HSleepDataBlock(
                Timestamp2000(startTime), Timestamp2000(endTime), deepSleepCount, lightSleepCount,
                deepSleepTotal, lightSleepTotal, segments, wakeCount, wakeDuration
            )
        }
    }
}

data class HBloodDataBlock(
    val bloodStartTime: Timestamp2000,
    val isInflated: Byte,
    val bloodSystolicPressure: UByte,
    val bloodDiastolicPressure: UByte,
    val reserved: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 1 + 1 + 1)*B

        fun parsePro(buf: ByteBuffer): HBloodDataBlock {
            // Attempt 1: [96, 1, 16, 0], [0], [0, 0, 11], [0, 0]
            // Attempt 2: [97, 1, 17, 0], [0], [0, 8, 11], [0, 0]
            return HBloodDataBlock(
                bloodStartTime = Timestamp2000(buf.int),
                isInflated = buf.get(),
                bloodSystolicPressure = buf.get().toUByte(),
                bloodDiastolicPressure = buf.get().toUByte(),
                reserved = buf.get() // ...
            )
        }
    }
}
data class HHistoryHeartRateDataBlock(val timestamp: Timestamp2000, val heartRateInBpm: UByte) {
    companion object {
        fun parsePro(buf: ByteBuffer): HHistoryHeartRateDataBlock {
            val timestamp = buf.int // FIXME: It's in s; from 2000;
            val heartRateInBpm = buf.get().toUByte()
            return HHistoryHeartRateDataBlock(
                timestamp = Timestamp2000(timestamp),
                heartRateInBpm = heartRateInBpm,
            )
        }

        val SIZE = 6
    }
}
data class HHistoryAllDataBlock(
    val startTime: Timestamp2000,
    val stepValue: Short,
    val heartRate: Byte,
    val systolicBloodPressure: Byte,
    val diastolicBloodPressure: Byte,
    val bloodOxygen: Byte,
    val respiratoryRateValue: Byte,
    val hrv: Byte,
    val cvrr: Byte,
    val tempInt: Byte,
    val tempFloat: Byte,
    val bodyFatInt: Byte,
    val bodyFatFloat: Byte,
    val bloodSugar: Byte
) {
    companion object {
        const val SIZE: Int = 20*B

        fun parsePro(buf: ByteBuffer): HHistoryAllDataBlock {
            // FIXME parse 2 garbage bytes
            return HHistoryAllDataBlock(
                startTime = Timestamp2000(buf.int),
                stepValue = buf.short,
                heartRate = buf.get(),
                systolicBloodPressure = buf.get(),
                diastolicBloodPressure = buf.get(),
                bloodOxygen = buf.get(),
                respiratoryRateValue = buf.get(),
                hrv = buf.get(),
                cvrr = buf.get(),
                tempInt = buf.get(),
                tempFloat = buf.get(),
                bodyFatInt = buf.get(),
                bodyFatFloat = buf.get(),
                bloodSugar = buf.get()
            )
        }
    }
}

data class HBloodOxygenDataBlock(
    val startTime: Timestamp2000,
    val type: Byte,
    val bloodOxygen: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 1)*B

        fun parsePro(buf: ByteBuffer): HBloodOxygenDataBlock {
            return HBloodOxygenDataBlock(
                startTime = Timestamp2000(buf.int),
                type = buf.get(),
                bloodOxygen = buf.get()
            )
        }
    }
}

//          tempValue_int: u8 -> StringBuilder.append((int) value_int); StringBuilder.append(".")
//          tempValue_float: u8  -> StringBuilder.append((int) value_float)
//          humidValue_int: u8 -> StringBuilder.append((int) value_int); StringBuilder.append(".")
//          humidValue_float: u8  -> StringBuilder.append((int) value_float)
data class HTemperatureAndHumidityDataBlock(
    val startTime: Timestamp2000,
    val type: Byte,
    val temperatureInt: Byte,
    val temperatureFloat: Byte,
    val humidityInt: Byte,
    val humidityFloat: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 1 + 1 + 1 + 1)*B

        fun parsePro(buf: ByteBuffer): HTemperatureAndHumidityDataBlock {
            return HTemperatureAndHumidityDataBlock(
                startTime = Timestamp2000(buf.int),
                type = buf.get(),
                temperatureInt = buf.get(),
                temperatureFloat = buf.get(),
                humidityInt = buf.get(),
                humidityFloat = buf.get()
            )
        }
    }
}


data class HTemperatureDataBlock(
    val startTime: Timestamp2000,
    val type: Byte,
    val valueInt: Byte,
    val valueFloat: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 1 + 1)*B // TOO BIG

        fun parsePro(buf: ByteBuffer): HTemperatureDataBlock {
            return HTemperatureDataBlock(
                startTime = Timestamp2000(buf.int),
                type = buf.get(),
                valueInt = buf.get(),
                valueFloat = buf.get()
            )
        }
    }
}

data class HAmbientLightDataBlock(
    val startTime: Timestamp2000,
    val type: Byte,
    val value: Short
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 2)*B // TOO BIG

        fun parsePro(buf: ByteBuffer): HAmbientLightDataBlock {
            return HAmbientLightDataBlock(
                startTime = Timestamp2000(buf.int),
                type = buf.get(),
                value = buf.short
            )
        }
    }
}

data class HFallDataBlock(
    val time: Timestamp2000,
    val state: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1)*B

        fun parsePro(buf: ByteBuffer): HFallDataBlock {
            return HFallDataBlock(
                time = Timestamp2000(buf.int),
                state = buf.get()
            )
        }
    }
}

data class HHealthMonitoringDataBlock(
    val startTime: Timestamp2000,
    val stepValuer6: Int,
    val r11: Byte,
    val r4: Byte,
    val r5: Byte,
    val ooValue: Byte,
    val respiratoryRateValue: Byte,
    val hrvValue: Byte,
    val cvrrValue: Byte,
    val tempIntValue: Byte,
    val tempFloatValue: Byte,
    val humidIntValue: Byte,
    val humidFloatValue: Byte,
    val ambientLightValue: Short,
    val isSportMode: Byte,
    val r19: Short,
    val r39: Byte
) {
    companion object {
        const val SIZE: Int = 30*B // Fixed: 26 bytes of data + 4 bytes skipped

        fun parsePro(buf: ByteBuffer): HHealthMonitoringDataBlock {
            val result = HHealthMonitoringDataBlock(
                startTime = Timestamp2000(buf.int),
                stepValuer6 = buf.int,
                r11 = buf.get(),
                r4 = buf.get(),
                r5 = buf.get(),
                ooValue = buf.get(),
                respiratoryRateValue = buf.get(),
                hrvValue = buf.get(),
                cvrrValue = buf.get(),
                tempIntValue = buf.get(),
                tempFloatValue = buf.get(),
                humidIntValue = buf.get(),
                humidFloatValue = buf.get(),
                ambientLightValue = buf.short,
                isSportMode = buf.get(),
                r19 = buf.short,
                r39 = buf.get()
            )
            // Skip 4 bytes as shown in decompiled source line 2887
            buf.position(buf.position() + 4)
            return result
        }
    }
}

data class HHistorySportModeDataBlock(
    val startTime: Timestamp2000,
    val endTime: Timestamp2000,
    val steps: Int,
    val distance: Short,
    val calories: Short,
    val mode: Byte,
    val startMethod: Byte,
    val heartRate: Byte,
    val sportTime: Int,
    val minHeartRate: Byte,
    val maxHeartRate: Byte
) {
    companion object {
        const val SIZE: Int = 26*B // 25 bytes of data + 1 byte padding

        fun parsePro(buf: ByteBuffer): HHistorySportModeDataBlock {
            val result = HHistorySportModeDataBlock(
                startTime = Timestamp2000(buf.int),
                endTime = Timestamp2000(buf.int),
                steps = buf.int,
                distance = buf.short,
                calories = buf.short,
                mode = buf.get(),
                startMethod = buf.get(),
                heartRate = buf.get(),
                sportTime = buf.int,
                minHeartRate = buf.get(),
                maxHeartRate = buf.get()
            )
            buf.get() // Skip 1 byte as shown in decompiled source line 2672
            return result
        }
    }
}

data class HHistoryComprehensiveMeasurementDataBlock( // FIXME check
    val timestamp: Timestamp2000, // in seconds; starting at origin 2000-01-01 00:00:00 GMT
    val bloodSugarModel: Byte,
    val bloodSugarInteger: Byte,
    val bloodSugarFloat: Byte,
    val uricAcidModel: Byte,
    val uricAcid: Short,
    val bloodKetoneModel: Byte,
    val bloodKetoneInteger: Byte,
    val bloodKetoneFloat: Byte,
    val bloodFatModel: Byte,
    val cholesterolInteger: Byte,
    val cholesterolFloat: Byte,
    val highLipoproteinCholesterolInteger: Byte,
    val highLipoproteinCholesterolFloat: Byte,
    val lowLipoproteinCholesterolInteger: Byte,
    val lowLipoproteinCholesterolFloat: Byte,
    val triglycerideCholesterolInteger: Byte,
    val triglycerideCholesterolFloat: Byte
) {
    companion object {
        const val SIZE: Int = 44*B // 22 bytes of data + 22 bytes skipped

        fun parsePro(buf: ByteBuffer): HHistoryComprehensiveMeasurementDataBlock {
            val result = HHistoryComprehensiveMeasurementDataBlock(
                timestamp = Timestamp2000(buf.int),
                bloodSugarModel = buf.get(),
                bloodSugarInteger = buf.get(),
                bloodSugarFloat = buf.get(),
                uricAcidModel = buf.get(),
                uricAcid = buf.short,
                bloodKetoneModel = buf.get(),
                bloodKetoneInteger = buf.get(),
                bloodKetoneFloat = buf.get(),
                bloodFatModel = buf.get(),
                cholesterolInteger = buf.get(),
                cholesterolFloat = buf.get(),
                highLipoproteinCholesterolInteger = buf.get(),
                highLipoproteinCholesterolFloat = buf.get(),
                lowLipoproteinCholesterolInteger = buf.get(),
                lowLipoproteinCholesterolFloat = buf.get(),
                triglycerideCholesterolInteger = buf.get(),
                triglycerideCholesterolFloat = buf.get()
            )
            // Skip 22 bytes as shown in decompiled source line 2460
            buf.position(buf.position() + 22)
            return result
        }
    }
}

data class HBackgroundReminderRecordDataBlock(
    val time: Timestamp2000,
    val r9: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 3)*B // FIXME way too little

        fun parsePro(buf: ByteBuffer): HBackgroundReminderRecordDataBlock {
            val time = buf.int
            val r9 = buf.get()
            // Skip 3 bytes as shown in decompiled source line 2324
            buf.position(buf.position() + 3)
            return HBackgroundReminderRecordDataBlock(Timestamp2000(time), r9)
        }
    }
}

