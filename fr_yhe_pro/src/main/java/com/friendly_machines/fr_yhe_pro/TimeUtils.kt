package com.friendly_machines.fr_yhe_pro

/**
 * Time conversion utilities for watch protocol.
 * 
 * The watch firmware uses a custom epoch starting at 2000-01-01 00:00:00 UTC
 * instead of the standard Unix epoch (1970-01-01 00:00:00 UTC).
 * 
 * Time offset calculation:
 * - Unix epoch: 1970-01-01 00:00:00 UTC = 0 s
 * - Year 2000: 2000-01-01 00:00:00 UTC = 946684800 s since Unix epoch
 * 
 * This means:
 * - Watch time = seconds since 2000-01-01 00:00:00 UTC
 * - Unix time = seconds since 1970-01-01 00:00:00 UTC
 * - Unix time = Watch time + 946684800 s
 * - Watch time = Unix time - 946684800 s
 * 
 * The watch protocol typically sends timestamps as 32-bit integers representing
 * seconds since 2000, which need to be converted to Java/Unix milliseconds
 * (milliseconds since 1970) for use with standard Java time APIs.
 */
object TimeUtils {
    /**
     * Offset between Unix epoch (1970-01-01) and watch epoch (2000-01-01) in seconds.
     * This is exactly 30 years worth of seconds: 30 a * 365.25 d/a * 24 h/d * 60 min/h * 60 s/min = 946684800 s
     */
    private const val EPOCH_OFFSET_SECONDS = 946684800L
    
    /**
     * Convert watch time to Unix timestamp in milliseconds.
     * 
     * @param watchTimeSeconds Time in seconds since 2000-01-01 00:00:00 UTC (watch epoch)
     * @return Time in milliseconds since 1970-01-01 00:00:00 UTC (Unix epoch)
     */
    fun watchTimeToUnixMillis(watchTimeSeconds: Long): Long {
        return (watchTimeSeconds + EPOCH_OFFSET_SECONDS) * 1000L
    }
    
    /**
     * Convert watch time to Unix timestamp in milliseconds, with timezone offset.
     **
     * @param watchTimeSeconds Time in seconds since 2000-01-01 00:00:00 UTC (watch epoch)
     * @param timezoneOffsetMillis Local timezone offset in milliseconds (from TimeZone.getDefault().getOffset())
     * @return Time in milliseconds since 1970-01-01 00:00:00 UTC, adjusted for local timezone
     */
    fun watchTimeToLocalUnixMillis(watchTimeSeconds: Long, timezoneOffsetMillis: Long): Long {
        return watchTimeToUnixMillis(watchTimeSeconds) - timezoneOffsetMillis
    }
    
    /**
     * Convert Unix timestamp in milliseconds to watch time.
     * 
     * @param unixTimeMillis Time in milliseconds since 1970-01-01 00:00:00 UTC (Unix epoch)  
     * @return Time in seconds since 2000-01-01 00:00:00 UTC (watch epoch)
     */
    fun unixMillisToWatchTime(unixTimeMillis: Long): Long {
        return (unixTimeMillis / 1000L) - EPOCH_OFFSET_SECONDS
    }
    
    /**
     * Convert Unix timestamp in milliseconds to watch time, with timezone offset.
     *
     * @param unixTimeMillis Time in milliseconds since 1970-01-01 00:00:00 UTC (Unix epoch)
     * @param timezoneOffsetMillis Local timezone offset in milliseconds (from TimeZone.getDefault().getOffset())
     * @return Time in seconds since 2000-01-01 00:00:00 UTC (watch epoch)
     */
    fun localUnixMillisToWatchTime(unixTimeMillis: Long, timezoneOffsetMillis: Long): Long {
        return unixMillisToWatchTime(unixTimeMillis + timezoneOffsetMillis)
    }
}
