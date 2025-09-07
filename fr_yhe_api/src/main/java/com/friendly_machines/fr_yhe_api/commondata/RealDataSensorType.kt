package com.friendly_machines.fr_yhe_api.commondata

/**
 * Sensor types for real-time data streaming from watch.
 * Used with WatchAGetRealData command to request specific sensor data.
 */
enum class RealDataSensorType(val value: Byte) {
    SPORT(0),              // Sport/exercise data
    HEART(1),              // Heart rate data
    BLOOD_OXYGEN(2),       // Blood oxygen (SpO2) data
    BLOOD_PRESSURE(3),     // Blood pressure data
    PPG(4),                // Photoplethysmography waveform data
    ECG(5),                // Electrocardiogram waveform data
    RUN(6),                // Running/sport mode data
    RESPIRATION(7),        // Respiration rate data
    SENSOR(8),             // Raw sensor data
    AMBIENT_LIGHT(9),      // Ambient light sensor data
    COMPREHENSIVE(10),     // Comprehensive health metrics
    SCHEDULE(11),          // Schedule/calendar data
    EVENT_REMINDER(12),    // Event reminder data
    OGA(13),               // All real-time data (OGA = Omnibus/General/All)
    INFLATED_BLOOD(14),    // Inflated blood pressure measurement data
    MUL_PHOTOELECTRIC(15)  // Multi-wavelength photoelectric waveform data
}