package com.friendly_machines.frbpdoctor

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec


object AppSettings {
    const val KEY_USER_ID = "userId"
    const val KEY_USER_WEIGHT = "userWeight"
    const val KEY_USER_HEIGHT = "userHeight"
    const val KEY_USER_SEX = "userSex"
    const val KEY_USER_BIRTHDAY = "userBirthday"
    private const val KEY_WATCH_KEY = "watchKey" // invisible to the user
    const val KEY_WATCH_MAC_ADDRESS = "watchMacAddress"

    private val MANDATORY_SETTINGS = listOf(KEY_WATCH_MAC_ADDRESS, KEY_WATCH_KEY, KEY_USER_ID, KEY_USER_WEIGHT, KEY_USER_HEIGHT, KEY_USER_SEX, KEY_USER_BIRTHDAY)

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
                );
                keyGenerator.generateKey()
            }
        }
    }

    private fun getKeyStoreSecretKey(context: Context): java.security.Key {
        enableKeyStore()
        return this.keyStore!!.getKey(MAIN_KEY_ALIAS, null);
    }

    private const val AES_MODE = "AES/GCM/NoPadding"
    private val FIXED_IV = ByteArray(12)
    internal fun encrypt(context: Context, clearText: ByteArray): ByteArray {
        val c = Cipher.getInstance(AES_MODE)
        c.init(Cipher.ENCRYPT_MODE, getKeyStoreSecretKey(context), GCMParameterSpec(128, FIXED_IV))
        return c.doFinal(clearText)
    }

    internal fun decrypt(context: Context, cipherText: ByteArray): ByteArray {
        val c = Cipher.getInstance(AES_MODE)
        c.init(Cipher.DECRYPT_MODE, getKeyStoreSecretKey(context), GCMParameterSpec(128, FIXED_IV))
        return c.doFinal(cipherText)
    }

    fun getWatchKey(context: Context, sharedPreferences: SharedPreferences): ByteArray? {
        val watchKeyString = sharedPreferences.getString(KEY_WATCH_KEY, "")
        if (!watchKeyString.isNullOrEmpty()) {
            return decrypt(context, Base64.decode(watchKeyString, Base64.DEFAULT))
        } else {
            return null
        }
    }

    fun setWatchKey(context: Context, sharedPreferences: SharedPreferences, key: ByteArray) {
        //val keyDigest = MessageDigest.getInstance("MD5").digest(key)
        sharedPreferences.edit().putString(
            KEY_WATCH_KEY, Base64.encodeToString(encrypt(context, key), Base64.DEFAULT)
        ).apply()
    }

    fun getUserId(sharedPreferences: SharedPreferences): Long? {
        val userIdString = sharedPreferences.getString(KEY_USER_ID, "")
        // TODO: If userId is null, synth one from the digits in device.name or something (and store it in SharedPreferences and also in Settings GUI)
        if (!userIdString.isNullOrEmpty() && userIdString.toLong() != 0L) {
            val userId = userIdString.toLong()
            return userId
        }
        return null
    }

    fun getMacAddress(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString(KEY_WATCH_MAC_ADDRESS, "")
    }

    fun areMandatorySettingsSet(sharedPreferences: SharedPreferences): Boolean {
        return MANDATORY_SETTINGS.all { key ->
            sharedPreferences.contains(key)
        }
    }

    fun clear(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit().clear().apply()
    }

    /** Note: If weight is not set, return 0 */
    private fun getWeight(sharedPreferences: SharedPreferences): Byte {
        return sharedPreferences.getInt(KEY_USER_WEIGHT, 0).toByte()
    }

    /** Note: If height is not set, return 0 */
    private fun getHeight(sharedPreferences: SharedPreferences): Byte {
        return sharedPreferences.getInt(KEY_USER_HEIGHT, 0).toByte()
    }

    /** Note: If birthday is not set, return "" */
    private fun getBirthday(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString(KEY_USER_BIRTHDAY, "")
    }

    private fun getSex(sharedPreferences: SharedPreferences): Byte? {
        val sexString = sharedPreferences.getString(KEY_USER_SEX, "")
        return if (!sexString.isNullOrEmpty()) {
            sexString.toInt().toByte()
        } else {
            null
        }
    }

    data class Profile(val height: Byte, val weight: Byte, val birthdayString: String, val sex: Byte)

    fun getProfileSettings(sharedPreferences: SharedPreferences): Profile? {
        val weight = getWeight(sharedPreferences)
        val height = getHeight(sharedPreferences)
        val birthday = getBirthday(sharedPreferences)
        val sex = getSex(sharedPreferences)
        if (birthday != null && sex != null) {
            return Profile(height = height, weight = weight, birthdayString = birthday, sex = sex)
        }
        return null
    }
}
