package com.friendly_machines.fr_yhe_api.commondata

/**
 * Week pattern bitmask for reminder schedules.
 * Each bit represents a day of the week.
 */
enum class DayOfWeekPattern(val bitmask: Byte) {
    SUNDAY(0x01),
    MONDAY(0x02), 
    TUESDAY(0x04),
    WEDNESDAY(0x08),
    THURSDAY(0x10),
    FRIDAY(0x20),
    SATURDAY(0x40),
    ENABLED_FLAG(0x80.toByte()); // Bit 7 used as enable/disable flag
    
    companion object {
        /**
         * Creates a week pattern from a bitmask byte
         */
        fun fromByte(pattern: Byte): Set<DayOfWeekPattern> {
            return values().filter { (pattern.toInt() and it.bitmask.toInt()) != 0 }.toSet()
        }
        
        /**
         * Creates a bitmask byte from a set of week patterns
         */
        fun toByte(patterns: Set<DayOfWeekPattern>): Byte {
            return patterns.fold(0) { acc, pattern -> acc or pattern.bitmask.toInt() }.toByte()
        }
        
        /**
         * Helper to create weekdays only pattern (Monday-Friday)
         */
        fun weekdays(): Set<DayOfWeekPattern> = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
        
        /**
         * Helper to create weekend only pattern (Saturday-Sunday) 
         */
        fun weekend(): Set<DayOfWeekPattern> = setOf(SATURDAY, SUNDAY)
        
        /**
         * Helper to create daily pattern (all days)
         */
        fun daily(): Set<DayOfWeekPattern> = setOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    }
}