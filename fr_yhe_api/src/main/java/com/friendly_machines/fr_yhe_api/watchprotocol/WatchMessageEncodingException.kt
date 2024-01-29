package com.friendly_machines.fr_yhe_api.watchprotocol

class WatchMessageEncodingException: RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}