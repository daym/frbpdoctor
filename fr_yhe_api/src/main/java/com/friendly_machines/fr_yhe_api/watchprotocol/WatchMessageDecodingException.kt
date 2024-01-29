package com.friendly_machines.fr_yhe_api.watchprotocol

class WatchMessageDecodingException: RuntimeException {
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(message: String) : super(message)
}
