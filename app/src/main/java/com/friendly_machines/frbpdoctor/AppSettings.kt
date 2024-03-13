package com.friendly_machines.frbpdoctor

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.friendly_machines.fr_yhe_api.commondata.SkinColor
import com.friendly_machines.fr_yhe_api.commondata.WatchWearingArm
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchProfileSex
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchTimePosition
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec


object AppSettings {
    const val KEY_WATCH_COMMUNICATOR_CLASS = "watchCommunicatorClass"
    private const val KEY_USER_ID = "userId"
    private const val KEY_USER_WEIGHT = "userWeight"
    private const val KEY_USER_HEIGHT = "userHeight"
    private const val KEY_USER_SEX = "userSex"
    private const val KEY_USER_BIRTHDAY = "userBirthday"
    private const val KEY_USER_WATCH_WEARING_ARM = "userWatchWearingArm"
    private const val KEY_USER_SKIN_COLOR = "userSkinColor"

    private const val KEY_USER_SLEEP_START_TIME = "userSleepStartTime"
    private const val KEY_USER_SLEEP_MONDAYS = "userSleepMondays"
    private const val KEY_USER_SLEEP_TUESDAYS = "userSleepTuesdays"
    private const val KEY_USER_SLEEP_WEDNESDAYS = "userSleepWednesdays"
    private const val KEY_USER_SLEEP_THURSDAYS = "userSleepThursdays"
    private const val KEY_USER_SLEEP_FRIDAYS = "userSleepFridays"
    private const val KEY_USER_SLEEP_SATURDAYS = "userSleepSaturdays"
    private const val KEY_USER_SLEEP_SUNDAYS = "userSleepSundays"

    private const val KEY_USER_REGULAR_REMINDER_START_TIME = "userRegularReminderStartTime"
    private const val KEY_USER_REGULAR_REMINDER_END_TIME = "userRegularReminderEndTime"
    private const val KEY_USER_REGULAR_REMINDER_INTERVAL = "userRegularReminderInterval"
    private const val KEY_USER_REGULAR_REMINDER_MESSAGE = "userRegularReminderMessage"
    private const val KEY_USER_REGULAR_REMINDER_MONDAYS = "userRegularReminderMondays"
    private const val KEY_USER_REGULAR_REMINDER_TUESDAYS = "userRegularReminderTuesdays"
    private const val KEY_USER_REGULAR_REMINDER_WEDNESDAYS = "userRegularReminderWednesdays"
    private const val KEY_USER_REGULAR_REMINDER_THURSDAYS = "userRegularReminderThursdays"
    private const val KEY_USER_REGULAR_REMINDER_FRIDAYS = "userRegularReminderFridays"
    private const val KEY_USER_REGULAR_REMINDER_SATURDAYS = "userRegularReminderSaturdays"
    private const val KEY_USER_REGULAR_REMINDER_SUNDAYS = "userRegularReminderSundays"

    private const val KEY_USER_HEART_MONITORING_ENABLED = "userHeartMonitoringEnabled"
    private const val KEY_USER_HEART_MONITORING_INTERVAL = "userHeartMonitoringInterval"
    private const val KEY_USER_HEART_MONITORING_MAX_VALUE = "userHeartMonitoringMaxValue2"

    private const val KEY_USER_TEMPERATURE_MONITORING_ENABLED = "userTemperatureMonitoringEnabled"
    private const val KEY_USER_TEMPERATURE_MONITORING_INTERVAL = "userTemperatureMonitoringInterval"
    private const val KEY_USER_TEMPERATURE_MONITORING_MAX_VALUE = "userTemperatureMonitoringMaxValue"

    private const val KEY_USER_ACCIDENT_MONITORING_ENABLED = "userAccidentMonitoringEnabled"

    private const val KEY_USER_LONG_SITTING_1_START_TIME = "userLongSitting1StartTime"
    private const val KEY_USER_LONG_SITTING_1_END_TIME = "userLongSitting1EndTime"
    private const val KEY_USER_LONG_SITTING_2_START_TIME = "userLongSitting2StartTime"
    private const val KEY_USER_LONG_SITTING_2_END_TIME = "userLongSitting2EndTime"
    private const val KEY_USER_LONG_SITTING_INTERVAL = "userLongSittingInterval"
    private const val KEY_USER_LONG_SITTING_MONDAYS = "userLongSittingMondays"
    private const val KEY_USER_LONG_SITTING_TUESDAYS = "userLongSittingTuesdays"
    private const val KEY_USER_LONG_SITTING_WEDNESDAYS = "userLongSittingWednesdays"
    private const val KEY_USER_LONG_SITTING_THURSDAYS = "userLongSittingThursdays"
    private const val KEY_USER_LONG_SITTING_FRIDAYS = "userLongSittingFridays"
    private const val KEY_USER_LONG_SITTING_SATURDAYS = "userLongSittingSaturdays"
    private const val KEY_USER_LONG_SITTING_SUNDAYS = "userLongSittingSundays"

    // TODO watchDndMode
    private const val KEY_WATCH_DND_START_TIME = "watchDndStartTime"
    private const val KEY_WATCH_DND_END_TIME = "watchDndEndTime"
    private const val KEY_WATCH_TIME_POSITION = "watchTimePosition"
    private const val KEY_WATCH_TIME_COLOR = "watchTimeColor"

    private const val KEY_WATCH_SCHEDULE_ENABLED = "watchScheduleEnabled"

    private const val KEY_WATCH_SCREEN_TIME_LIT = "watchScreenLitTime"

    private const val KEY_WATCH_KEY = "watchKey" // invisible to the user
    const val KEY_WATCH_MAC_ADDRESS = "watchMacAddress"

    private val MANDATORY_SETTINGS = listOf(KEY_WATCH_COMMUNICATOR_CLASS, KEY_WATCH_MAC_ADDRESS, KEY_WATCH_KEY, KEY_USER_ID, KEY_USER_WEIGHT, KEY_USER_HEIGHT, KEY_USER_SEX, KEY_USER_BIRTHDAY, KEY_USER_WATCH_WEARING_ARM)

    private const val AndroidKeyStore = "AndroidKeyStore"
    private const val MAIN_KEY_ALIAS = "FpBpDoctor"

    private var keyStore: KeyStore? = null
    private fun enableKeyStore() {
        if (this.keyStore == null) {
            this.keyStore = KeyStore.getInstance(AndroidKeyStore)
            this.keyStore!!.load(null)
            if (!keyStore!!.containsAlias(MAIN_KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore)
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(MAIN_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(false).build()
                )
                keyGenerator.generateKey()
            }
        }
    }

    private fun getKeyStoreSecretKey(context: Context): java.security.Key {
        enableKeyStore()
        return this.keyStore!!.getKey(MAIN_KEY_ALIAS, null)
    }

    private const val AES_MODE = "AES/GCM/NoPadding"
    private val FIXED_IV = ByteArray(12)
    private fun encrypt(context: Context, clearText: ByteArray): ByteArray {
        val c = Cipher.getInstance(AES_MODE)
        c.init(Cipher.ENCRYPT_MODE, getKeyStoreSecretKey(context), GCMParameterSpec(128, FIXED_IV))
        return c.doFinal(clearText)
    }

    private fun decrypt(context: Context, cipherText: ByteArray): ByteArray {
        val c = Cipher.getInstance(AES_MODE)
        c.init(Cipher.DECRYPT_MODE, getKeyStoreSecretKey(context), GCMParameterSpec(128, FIXED_IV))
        return c.doFinal(cipherText)
    }

    fun getWatchKey(context: Context, sharedPreferences: SharedPreferences): ByteArray? {
        val watchKeyString = sharedPreferences.getString(KEY_WATCH_KEY, "")
        return if (!watchKeyString.isNullOrEmpty()) {
            decrypt(context, Base64.decode(watchKeyString, Base64.DEFAULT))
        } else {
            null
        }
    }

    fun setWatchCommunicatorSettings(context: Context, sharedPreferences: SharedPreferences, key: ByteArray, watchCommunicatorClassname: String) {
        //val keyDigest = MessageDigest.getInstance("MD5").digest(key)
        sharedPreferences.edit().putString(
            KEY_WATCH_KEY, Base64.encodeToString(encrypt(context, key), Base64.DEFAULT)
        ).putString(
            KEY_WATCH_COMMUNICATOR_CLASS, watchCommunicatorClassname
        ).apply()
    }

    fun getUserId(sharedPreferences: SharedPreferences): Long? {
        val userIdString = sharedPreferences.getString(KEY_USER_ID, "")
        // TODO: If userId is null, synth one from the digits in device.name or something (and store it in SharedPreferences and also in Settings GUI)
        if (!userIdString.isNullOrEmpty() && userIdString.toLong() != 0L) {
            return userIdString.toLong()
        }
        return null
    }

    fun getMacAddress(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString(KEY_WATCH_MAC_ADDRESS, "")
    }

    fun areMandatorySettingsSet(sharedPreferences: SharedPreferences): Boolean {
        return MANDATORY_SETTINGS.all { key ->
            sharedPreferences.contains(key)
        } && !sharedPreferences.getString(KEY_WATCH_COMMUNICATOR_CLASS, "").isNullOrEmpty()
    }

    fun clear(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit().clear().apply()
    }

    private fun getWeight(sharedPreferences: SharedPreferences): Int? {
        val result = sharedPreferences.getInt(KEY_USER_WEIGHT, 0)
        return if (result == 0)
            null
        else
            result
    }

    private fun getHeight(sharedPreferences: SharedPreferences): Int? {
        val result = sharedPreferences.getInt(KEY_USER_HEIGHT, 0)
        return if (result == 0)
            null
        else
            result
    }

    /** Note: If birthday is not set, return "" */
    private fun getBirthday(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString(KEY_USER_BIRTHDAY, "")
    }

    private fun getSex(sharedPreferences: SharedPreferences): WatchProfileSex? {
        val sexString = sharedPreferences.getString(KEY_USER_SEX, "")
        return if (!sexString.isNullOrEmpty()) {
            WatchProfileSex.valueOf(sexString)
        } else {
            null
        }
    }

    fun getUserWatchWearingArm(sharedPreferences: SharedPreferences): WatchWearingArm? {
        val watchWearingArmString = sharedPreferences.getString(KEY_USER_WATCH_WEARING_ARM, "")
        return if (!watchWearingArmString.isNullOrEmpty()) {
            WatchWearingArm.valueOf(watchWearingArmString)
        } else {
            null
        }
    }

    data class Profile(val height: Int, val weight: Int, val birthdayString: String, val sex: WatchProfileSex, val arm: WatchWearingArm?)

    fun isProfileSetting(key: String): Boolean {
        return key == KEY_USER_HEIGHT || key == KEY_USER_WEIGHT || key == KEY_USER_SEX || key == KEY_USER_BIRTHDAY || key == KEY_USER_WATCH_WEARING_ARM
    }

    fun getProfileSettings(sharedPreferences: SharedPreferences): Profile? {
        val weight = getWeight(sharedPreferences)
        val height = getHeight(sharedPreferences)
        val birthday = getBirthday(sharedPreferences)
        val sex = getSex(sharedPreferences)
        val arm = getUserWatchWearingArm(sharedPreferences)
        if (!birthday.isNullOrEmpty() && sex != null && weight != null && height != null) {
            return Profile(height = height, weight = weight, birthdayString = birthday, sex = sex, arm = arm)
        }
        return null
    }

    fun isDndSetting(key: String): Boolean {
        return key == KEY_WATCH_DND_END_TIME || key == KEY_WATCH_DND_START_TIME
    }

    data class DndTime(val hour: Byte, val minute: Byte)
    data class Sleep(val hour: Byte, val minute: Byte, val repeats: UByte)
    data class RegularReminder(val startHour: Byte, val startMinute: Byte, val endHour: Byte, val endMinute: Byte, val repeats: UByte, val interval: Byte, val message: String?)

    fun getDndStartTime(sharedPreferences: SharedPreferences): DndTime? {
        val timeString = sharedPreferences.getString(KEY_WATCH_DND_START_TIME, "")
        if (timeString.isNullOrEmpty()) {
            return null
        }
        val parts = timeString.split(":")
        val hour = parts[0].toByte()
        val minute = parts[1].toByte()
        return DndTime(hour = hour, minute = minute)
    }

    fun getDndEndTime(sharedPreferences: SharedPreferences): DndTime? {
        val timeString = sharedPreferences.getString(KEY_WATCH_DND_END_TIME, "")
        if (timeString.isNullOrEmpty()) {
            return null
        }
        val parts = timeString.split(":")
        val hour = parts[0].toByte()
        val minute = parts[1].toByte()
        return DndTime(hour = hour, minute = minute)
    }

    fun isUserWatchWearingArmSetting(key: String): Boolean {
        return key == KEY_USER_WATCH_WEARING_ARM
    }

    fun isUserSkinColorSetting(key: String): Boolean {
        return key == KEY_USER_SKIN_COLOR
    }

    fun isWatchTimeLayout(key: String): Boolean {
        return key == KEY_WATCH_TIME_POSITION || key == KEY_WATCH_TIME_COLOR
    }

    fun getWatchTimePosition(sharedPreferences: SharedPreferences): WatchTimePosition? {
        val watchTimePositionString = sharedPreferences.getString(KEY_WATCH_TIME_POSITION, "")
        return if (!watchTimePositionString.isNullOrEmpty()) {
            WatchTimePosition.valueOf(watchTimePositionString)
        } else {
            null
        }
    }

    fun getWatchTimeColor(sharedPreferences: SharedPreferences): UShort? {
        val watchTimeColorString = sharedPreferences.getString(KEY_WATCH_TIME_COLOR, "")
        if (!watchTimeColorString.isNullOrEmpty()) {
            android.graphics.Color.parseColor(watchTimeColorString)?.let { color ->
                val red5 = color.red ushr (8 - 5)
                val green6 = color.green ushr (8 - 6)
                val blue5 = color.blue ushr (8 - 5)
                return ((red5 shl (5 + 6)) or (green6 shl 5) or (blue5 shl 0)).toUShort()
            }
        }
        return null
    }

    fun getUserSkinColor(sharedPreferences: SharedPreferences): SkinColor? {
        val userSkinColorString = sharedPreferences.getString(KEY_USER_SKIN_COLOR, "")
        return if (!userSkinColorString.isNullOrEmpty()) {
            SkinColor.valueOf(userSkinColorString)
        } else {
            null
        }
    }

    fun isUserSleepSetting(key: String): Boolean {
        return setOf(KEY_USER_SLEEP_START_TIME, KEY_USER_SLEEP_MONDAYS, KEY_USER_SLEEP_TUESDAYS, KEY_USER_SLEEP_WEDNESDAYS, KEY_USER_SLEEP_THURSDAYS, KEY_USER_SLEEP_FRIDAYS, KEY_USER_SLEEP_SATURDAYS, KEY_USER_SLEEP_SUNDAYS).contains(key)
    }

    fun getUserSleep(sharedPreferences: SharedPreferences): Sleep? {
        val timeString = sharedPreferences.getString(KEY_USER_SLEEP_START_TIME, "")
        if (timeString.isNullOrEmpty()) {
            return null
        }
        val parts = timeString.split(":")
        val hour = parts[0].toByte()
        val minute = parts[1].toByte()

        var repeats = 0
        // FIXME test
        for (key in arrayOf(KEY_USER_SLEEP_MONDAYS, KEY_USER_SLEEP_TUESDAYS, KEY_USER_SLEEP_WEDNESDAYS, KEY_USER_SLEEP_THURSDAYS, KEY_USER_SLEEP_FRIDAYS, KEY_USER_SLEEP_SATURDAYS, KEY_USER_SLEEP_SUNDAYS)) {
            val s = sharedPreferences.getBoolean(key, false)
            repeats = repeats * 2 + when (s) {
                true -> 1
                false -> 0
            }
        }
        repeats = repeats * 2 + 1 // warn // FIXME setting

        return Sleep(hour = hour, minute = minute, repeats = repeats.toUByte())
    }

    fun isUserRegularReminderSetting(key: String): Boolean {
        return setOf(KEY_USER_REGULAR_REMINDER_START_TIME, KEY_USER_REGULAR_REMINDER_END_TIME, KEY_USER_REGULAR_REMINDER_INTERVAL, KEY_USER_REGULAR_REMINDER_MESSAGE, KEY_USER_REGULAR_REMINDER_MONDAYS, KEY_USER_REGULAR_REMINDER_TUESDAYS, KEY_USER_REGULAR_REMINDER_WEDNESDAYS, KEY_USER_REGULAR_REMINDER_THURSDAYS, KEY_USER_REGULAR_REMINDER_FRIDAYS, KEY_USER_REGULAR_REMINDER_SATURDAYS, KEY_USER_REGULAR_REMINDER_SUNDAYS).contains(key)
    }

    fun getUserRegularReminder(sharedPreferences: SharedPreferences): RegularReminder? {
        val startTimeString = sharedPreferences.getString(KEY_USER_REGULAR_REMINDER_START_TIME, "")
        val endTimeString = sharedPreferences.getString(KEY_USER_REGULAR_REMINDER_END_TIME, "")
        val interval = sharedPreferences.getInt(KEY_USER_REGULAR_REMINDER_INTERVAL, -1).toByte()
        if (startTimeString.isNullOrEmpty() || endTimeString.isNullOrEmpty() || interval == (-1).toByte()) {
            return null
        }
        val startTimeParts = startTimeString.split(":")
        val startHour = startTimeParts[0].toByte()
        val startMinute = startTimeParts[1].toByte()

        val endTimeParts = endTimeString.split(":")
        val endHour = endTimeParts[0].toByte()
        val endMinute = endTimeParts[1].toByte()

        var repeats = 0
        // FIXME test
        for (key in arrayOf(KEY_USER_REGULAR_REMINDER_MONDAYS, KEY_USER_REGULAR_REMINDER_TUESDAYS, KEY_USER_REGULAR_REMINDER_WEDNESDAYS, KEY_USER_REGULAR_REMINDER_THURSDAYS, KEY_USER_REGULAR_REMINDER_FRIDAYS, KEY_USER_REGULAR_REMINDER_SATURDAYS, KEY_USER_REGULAR_REMINDER_SUNDAYS)) {
            val s = sharedPreferences.getBoolean(key, false)
            repeats = repeats * 2 + when (s) {
                true -> 1
                false -> 0
            }
        }
        repeats = repeats * 2 + 1 // warn // FIXME setting

        val message = sharedPreferences.getString(KEY_USER_REGULAR_REMINDER_MESSAGE, "")
        return RegularReminder(startHour = startHour, startMinute = startMinute, endHour = endHour, endMinute = endMinute, interval = interval, message = message, repeats = repeats.toUByte())
    }

    data class HeartMonitoring(val enabled: Boolean, val interval: Byte, val maxValue: UByte)

    fun isUserHeartMonitoringSetting(key: String): Boolean {
        return setOf(KEY_USER_HEART_MONITORING_ENABLED, KEY_USER_HEART_MONITORING_INTERVAL, KEY_USER_HEART_MONITORING_MAX_VALUE).contains(key)
    }

    fun getUserHeartMonitoring(sharedPreferences: SharedPreferences): HeartMonitoring? {
        val enabled = sharedPreferences.getBoolean(KEY_USER_HEART_MONITORING_ENABLED, false)
        if (!enabled) {
            return null
        }
        val interval = sharedPreferences.getInt(KEY_USER_HEART_MONITORING_INTERVAL, -1).toByte()
        if (interval == (-1).toByte()) {
            return null
        }
        val maxValue = sharedPreferences.getInt(KEY_USER_HEART_MONITORING_MAX_VALUE, 0).toUByte()
        if (maxValue == 0.toUByte()) {
            return null
        }
        return HeartMonitoring(enabled = enabled, interval = interval, maxValue = maxValue)
    }

    data class TemperatureMonitoring(val enabled: Boolean, val interval: Byte, val maxValue: UByte)

    fun isUserTemperatureMonitoringSetting(key: String): Boolean {
        return setOf(KEY_USER_TEMPERATURE_MONITORING_ENABLED, KEY_USER_TEMPERATURE_MONITORING_INTERVAL, KEY_USER_TEMPERATURE_MONITORING_MAX_VALUE).contains(key)
    }

    fun getUserTemperatureMonitoring(sharedPreferences: SharedPreferences): TemperatureMonitoring? {
        val enabled = sharedPreferences.getBoolean(KEY_USER_TEMPERATURE_MONITORING_ENABLED, false)
        if (!enabled) {
            return null
        }
        val interval = sharedPreferences.getInt(KEY_USER_TEMPERATURE_MONITORING_INTERVAL, -1).toByte()
        if (interval == (-1).toByte()) {
            return null
        }
        val maxValue = sharedPreferences.getInt(KEY_USER_TEMPERATURE_MONITORING_MAX_VALUE, 0).toUByte()
        if (maxValue == 0.toUByte()) {
            return null
        }
        return TemperatureMonitoring(enabled = enabled, interval = interval, maxValue = maxValue)
    }

    fun isWatchScheduleEnabledSetting(key: String): Boolean {
        return key == KEY_WATCH_SCHEDULE_ENABLED
    }

    fun isWatchScheduleEnabled(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean(KEY_WATCH_SCHEDULE_ENABLED, true)
    }

    fun isUserAccidentMonitoringEnabled(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean(KEY_USER_ACCIDENT_MONITORING_ENABLED, true)
    }

    fun isUserAccidentMonitoringEnabledSetting(key: String): Boolean {
        return key == KEY_USER_ACCIDENT_MONITORING_ENABLED
    }

    fun isUserLongSittingSetting(key: String): Boolean {
        return setOf(
            KEY_USER_LONG_SITTING_1_START_TIME,
            KEY_USER_LONG_SITTING_1_END_TIME,
            KEY_USER_LONG_SITTING_2_START_TIME,
            KEY_USER_LONG_SITTING_2_END_TIME,
            KEY_USER_LONG_SITTING_INTERVAL,
            KEY_USER_LONG_SITTING_MONDAYS,
            KEY_USER_LONG_SITTING_TUESDAYS,
            KEY_USER_LONG_SITTING_WEDNESDAYS,
            KEY_USER_LONG_SITTING_THURSDAYS,
            KEY_USER_LONG_SITTING_FRIDAYS,
            KEY_USER_LONG_SITTING_SATURDAYS,
            KEY_USER_LONG_SITTING_SUNDAYS
        ).contains(key)
    }
    data class UserLongSitting(val startHour1: Byte, val startMinute1: Byte, val endHour1: Byte, val endMinute1: Byte, val startHour2: Byte, val startMinute2: Byte, val endHour2: Byte, val endMinute2: Byte, val interval: Byte, val repeats: UByte)
    fun getUserLongSitting(sharedPreferences: SharedPreferences): UserLongSitting? {
        val startTime1String = sharedPreferences.getString(KEY_USER_LONG_SITTING_1_START_TIME, "")
        val endTime1String = sharedPreferences.getString(KEY_USER_LONG_SITTING_1_END_TIME, "")
        val startTime2String = sharedPreferences.getString(KEY_USER_LONG_SITTING_2_START_TIME, "")
        val endTime2String = sharedPreferences.getString(KEY_USER_LONG_SITTING_2_END_TIME, "")

        val interval = sharedPreferences.getInt(KEY_USER_REGULAR_REMINDER_INTERVAL, -1).toByte()
        if (startTime1String.isNullOrEmpty() || endTime1String.isNullOrEmpty() || startTime2String.isNullOrEmpty() || endTime2String.isNullOrEmpty() || interval == (-1).toByte()) {
            return null
        }
        val startTimeParts1 = startTime1String.split(":")
        val startHour1 = startTimeParts1[0].toByte()
        val startMinute1 = startTimeParts1[1].toByte()

        val endTimeParts1 = endTime1String.split(":")
        val endHour1 = endTimeParts1[0].toByte()
        val endMinute1 = endTimeParts1[1].toByte()

        val startTimeParts2 = startTime1String.split(":")
        val startHour2 = startTimeParts2[0].toByte()
        val startMinute2 = startTimeParts2[1].toByte()

        val endTimeParts2 = endTime1String.split(":")
        val endHour2 = endTimeParts2[0].toByte()
        val endMinute2 = endTimeParts2[1].toByte()

        var repeats = 0
        // FIXME test
        for (key in arrayOf(KEY_USER_LONG_SITTING_MONDAYS, KEY_USER_LONG_SITTING_TUESDAYS, KEY_USER_LONG_SITTING_WEDNESDAYS, KEY_USER_LONG_SITTING_THURSDAYS, KEY_USER_LONG_SITTING_FRIDAYS, KEY_USER_LONG_SITTING_SATURDAYS, KEY_USER_LONG_SITTING_SUNDAYS)) {
            val s = sharedPreferences.getBoolean(key, false)
            repeats = repeats * 2 + when (s) {
                true -> 1
                false -> 0
            }
        }
        repeats = repeats * 2 + 1 // warn // FIXME setting

        return UserLongSitting(startHour1 = startHour1, startMinute1 = startMinute1, endHour1 = endHour1, endMinute1 = endMinute1, startHour2 = startHour2, startMinute2 = startMinute2, endHour2 = endHour2, endMinute2 = endMinute2,  interval = interval, repeats = repeats.toUByte())
    }

    fun isWatchScreenTimeLitSetting(key: String): Boolean {
        return key == KEY_WATCH_SCREEN_TIME_LIT
    }
    fun getWatchScreenTimeLit(sharedPreferences: SharedPreferences): Byte {
        return sharedPreferences.getInt(KEY_WATCH_SCREEN_TIME_LIT, 30).toByte()
    }
}
