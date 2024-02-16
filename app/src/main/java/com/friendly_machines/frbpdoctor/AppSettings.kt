package com.friendly_machines.frbpdoctor

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchProfileSex
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchTimePosition
import com.friendly_machines.fr_yhe_api.commondata.WatchWearingArm
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
    // TODO watchDndMode
    private const val KEY_WATCH_DND_START_TIME = "watchDndStartTime"
    private const val KEY_WATCH_DND_END_TIME = "watchDndEndTime"
    private const val KEY_WATCH_TIME_POSITION = "watchTimePosition"
    private const val KEY_WATCH_TIME_COLOR = "watchTimeColor"

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
}
