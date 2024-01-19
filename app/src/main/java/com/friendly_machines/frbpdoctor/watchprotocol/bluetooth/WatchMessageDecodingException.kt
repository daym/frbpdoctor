package com.friendly_machines.frbpdoctor.watchprotocol.bluetooth

class WatchMessageDecodingException: RuntimeException {
    constructor(message: String, cause: Throwable): super(message, cause) {
    }
    constructor(message: String) : super(message) {
    }
}
