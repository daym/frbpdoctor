package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.RealDataSensorType
import com.friendly_machines.fr_yhe_api.commondata.RealDataMeasureType
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Request real-time data streaming from watch sensors.
 * 
 * This command triggers the watch to start sending real-time data from the specified sensor.
 * The watch will respond with continuous data packets (R* indication responses) until stopped.
 * 
 * @param sensorType The type of sensor to read from
 * @param measureType The measurement variant (typically DEFAULT for single sensor, SEQUENCE_1-5 for multi-sensor)
 * @param durationInSeconds Duration for data collection:
 *                 - For single sensor: duration in seconds (typically 2)
 *                 - For multi-sensor: duration in min * 60 s/min; Units: seconds
 * 
 * See indication.R* classes for response data formats
 */
class WatchAGetRealData(
    sensorType: RealDataSensorType,
    measureType: RealDataMeasureType = RealDataMeasureType.DEFAULT,
    durationInSeconds: Byte = 2
): WatchCommand(WatchOperation.ARealData, byteArrayOf(sensorType.value, measureType.value, durationInSeconds)) {

    // FIXME: Should be real-time data streaming trigger command.  No status response.
    data class Response(val status: Byte = 0) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}