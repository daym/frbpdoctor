package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchAInsuranceIntegrationCommand(policyType: Byte, planCode: Byte, coverageLevel: Byte, status: Byte, timestamp: Int, message: String) : WatchCommand(WatchOperation.AInsuranceIntegration, run {
    val messageBytes = message.toByteArray(Charsets.UTF_8)
    val buf = ByteBuffer.allocate(8 + messageBytes.size).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(policyType)
    buf.put(planCode)
    buf.put(coverageLevel)
    buf.put(status)
    buf.putInt(timestamp)
    buf.put(messageBytes)
    buf.array()
}) {
    
    data class Response(val result: Byte, val tpeResult: Byte?) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // FIXME names
                val result = if (buf.hasRemaining()) buf.get() else 0
                val tpeResult = if (buf.hasRemaining()) buf.get() else null
                return Response(result = result, tpeResult = tpeResult)
            }
        }
    }
}
