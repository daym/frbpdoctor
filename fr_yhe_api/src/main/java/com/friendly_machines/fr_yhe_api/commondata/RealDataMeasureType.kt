package com.friendly_machines.fr_yhe_api.commondata

/**
 * Measure type for real-time data streaming.
 * Used to specify measurement mode or sampling variant.
 */
enum class RealDataMeasureType(val value: Byte) {
    DEFAULT(0),        // Default/single measurement mode
    SEQUENCE_1(1),     // First in sequence
    SEQUENCE_2(2),     // Second in sequence
    SEQUENCE_3(3),     // Third in sequence
    SEQUENCE_4(4),     // Fourth in sequence
    SEQUENCE_5(5)      // Fifth in sequence
}
