package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

fun decodeIntegerDouble(integerPart: Byte, floatPart: Byte): Double {
    if (floatPart >= 10) {
        throw RuntimeException("safety triggered because floatPart >= 10")
    }
    return String.format("%d.%d", integerPart, floatPart).toDouble()
}
const val B: Int = 1 // "Byte" unit

data class HSportDataBlock(
    val startTime: UInt,
    val endTime: UInt,
    val steps: UShort,
    val distance: Short,
    val calories: Short
) {
    companion object {
        const val SIZE: Int = (4 + 4 + 2 + 2 + 2)*B

        fun parsePro(buf: ByteBuffer): HSportDataBlock {
            return HSportDataBlock(
                startTime = buf.int.toUInt(),
                endTime = buf.int.toUInt(),
                steps = buf.short.toUShort(),
                distance = buf.short,
                calories = buf.short
            )
        }
    }
}

data class HSleepDataBlock(
    val dummy: Byte
) {
    companion object {
        const val SIZE: Int = 20*B // FIXME

        fun parsePro(buf: ByteBuffer): HSleepDataBlock {
            // FIXME
            return HSleepDataBlock(0.toByte())
        }
    }
}

data class HHeartDataBlock(
    val heartStartTime: Int,
    val r11: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 1)*B

        fun parsePro(buf: ByteBuffer): HHeartDataBlock {
            // FIXME parse one more byte and throw it away
            return HHeartDataBlock(
                heartStartTime = buf.int,
                r11 = buf.get()
            )
        }
    }
}

data class HBloodDataBlock(
    val bloodStartTime: UInt,
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
                bloodStartTime = buf.int.toUInt(),
                isInflated = buf.get(),
                bloodSystolicPressure = buf.get().toUByte(),
                bloodDiastolicPressure = buf.get().toUByte(),
                reserved = buf.get() // ...
            )
        }
    }
}

data class HAllDataBlock(
    val startTime: Int,
    val r6: Short,
    val r11: Byte,
    val r4: Byte,
    val r5: Byte,
    val ooValue: Byte,
    val respiratoryRateValue: Byte,
    val hrvValue: Byte,
    val cvrrValue: Byte,
    val tempIntValue: Byte,
    val tempFloatValue: Byte,
    val bodyFatIntValue: Byte,
    val bodyFatFloatValue: Byte,
    val bloodSugarValue: Byte
) {
    companion object {
        const val SIZE: Int = 20*B

        fun parsePro(buf: ByteBuffer): HAllDataBlock {
            // FIXME parse 2 garbage bytes
            return HAllDataBlock(
                startTime = buf.int,
                r6 = buf.short,
                r11 = buf.get(),
                r4 = buf.get(),
                r5 = buf.get(),
                ooValue = buf.get(),
                respiratoryRateValue = buf.get(),
                hrvValue = buf.get(),
                cvrrValue = buf.get(),
                tempIntValue = buf.get(),
                tempFloatValue = buf.get(),
                bodyFatIntValue = buf.get(),
                bodyFatFloatValue = buf.get(),
                bloodSugarValue = buf.get()
            )
        }
    }
}

data class HBloodOxygenDataBlock(
    val startTime: Int,
    val type: Byte,
    val value: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 1)*B

        fun parsePro(buf: ByteBuffer): HBloodOxygenDataBlock {
            return HBloodOxygenDataBlock(
                startTime = buf.int,
                type = buf.get(),
                value = buf.get()
            )
        }
    }
}

//          tempValue_int: u8 -> StringBuilder.append((int) value_int); StringBuilder.append(".")
//          tempValue_float: u8  -> StringBuilder.append((int) value_float)
//          humidValue_int: u8 -> StringBuilder.append((int) value_int); StringBuilder.append(".")
//          humidValue_float: u8  -> StringBuilder.append((int) value_float)
data class HTemperatureAndHumidityDataBlock(
    val startTime: Int,
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
                startTime = buf.int,
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
    val startTime: UInt,
    val type: Byte,
    val valueInt: Byte,
    val valueFloat: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 1 + 1)*B // TOO BIG

        fun parsePro(buf: ByteBuffer): HTemperatureDataBlock {
            return HTemperatureDataBlock(
                startTime = buf.int.toUInt(),
                type = buf.get(),
                valueInt = buf.get(),
                valueFloat = buf.get()
            )
        }
    }
}

data class HAmbientLightDataBlock(
    val startTime: Int,
    val type: Byte,
    val value: Short
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 2)*B // TOO BIG

        fun parsePro(buf: ByteBuffer): HAmbientLightDataBlock {
            return HAmbientLightDataBlock(
                startTime = buf.int,
                type = buf.get(),
                value = buf.short
            )
        }
    }
}

data class HFallDataBlock(
    val time: Int,
    val state: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1)*B

        fun parsePro(buf: ByteBuffer): HFallDataBlock {
            return HFallDataBlock(
                time = buf.int,
                state = buf.get()
            )
        }
    }
}

data class HHealthMonitoringDataBlock(
    val startTime: Int,
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
        const val SIZE: Int = 25*B // FIXME maybe wrong

        fun parsePro(buf: ByteBuffer): HHealthMonitoringDataBlock {
            return HHealthMonitoringDataBlock(
                startTime = buf.int,
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
        }
    }
}

data class HHistorySportModeDataBlock(
    val startTime: Int,
    val endTime: Int,
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
        const val SIZE: Int = (4 + 4 + 4 + 2 + 2 + 1 + 1 + 1 + 4 + 1 + 1)*B // TODO pad

        fun parsePro(buf: ByteBuffer): HHistorySportModeDataBlock {
            return HHistorySportModeDataBlock(
                startTime = buf.int,
                endTime = buf.int,
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
        }
    }
}

data class HComprehensiveMeasurementDataBlock(
    val timestamp: Int, // in seconds; starting at origin 2000-01-01 00:00:00 GMT
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
        const val SIZE: Int = 22*B // FIXME need to skip 22 ?!

        fun parsePro(buf: ByteBuffer): HComprehensiveMeasurementDataBlock {
            return HComprehensiveMeasurementDataBlock(
                timestamp = buf.int,
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
        }
    }
}

data class HBackgroundReminderRecordDataBlock(
    val time: Int,
    val r9: Byte
) {
    companion object {
        const val SIZE: Int = (4 + 1 + 3)*B // FIXME way too little

        fun parsePro(buf: ByteBuffer): HBackgroundReminderRecordDataBlock {
            return HBackgroundReminderRecordDataBlock(
                time = buf.int,
                r9 = buf.get()
            )
        }
    }
}
