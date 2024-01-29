package com.friendly_machines.fr_yhe_api.watchprotocol

data class WatchUnknownResponse(val code: Short, val arguments: ByteArray) : WatchResponse() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WatchUnknownResponse

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