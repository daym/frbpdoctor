package com.friendly_machines.frbpdoctor.watchprotocol

class WatchMessageEncodingException: RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}