package com.friendly_machines.frbpdoctor

import android.content.SharedPreferences
import android.util.Base64

object AppSettings {
    const val KEY_USER_ID = "userId"
    const val KEY_USER_WEIGHT = "userWeight"
    const val KEY_USER_HEIGHT = "userHeight"
    const val KEY_USER_SEX = "userSex"
    const val KEY_USER_BIRTHDAY = "userBirthday"
    private const val KEY_WATCH_KEY_DIGEST = "watchKeyDigest" // invisible to the user
    const val KEY_WATCH_MAC_ADDRESS = "watchMacAddress"

    val MANDATORY_SETTINGS = listOf(KEY_WATCH_MAC_ADDRESS, KEY_WATCH_KEY_DIGEST, KEY_USER_ID, KEY_USER_WEIGHT, KEY_USER_HEIGHT, KEY_USER_SEX, KEY_USER_BIRTHDAY)

    fun getUserId(sharedPreferences: SharedPreferences): Long? {
        val userIdString = sharedPreferences.getString(AppSettings.KEY_USER_ID, "")
        // TODO: If userId is null, synth one from the digits in device.name or something (and store it in SharedPreferences and also in Settings GUI)
        if (!userIdString.isNullOrEmpty() && userIdString.toLong() != 0L) {
            val userId = userIdString.toLong()
            return userId
        }
        // TODO: If userId is null, synth one from the digits in device.name or something (and store it in SharedPreferences and also in Settings GUI)

        return null
    }

    fun setKeyDigest(sharedPreferences: SharedPreferences, keyDigest: ByteArray) {
        // FIXME update KEY_WATCH_KEY_DIGEST in GUI
        sharedPreferences.edit()
            .putString(
                AppSettings.KEY_WATCH_KEY_DIGEST,
                Base64.encodeToString(keyDigest, Base64.DEFAULT)
            ).apply()
    }

    fun getMacAddress(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString(AppSettings.KEY_WATCH_MAC_ADDRESS, "")
    }

    fun areMandatorySettingsSet(sharedPreferences: SharedPreferences): Boolean {
        return AppSettings.MANDATORY_SETTINGS.all { key ->
            sharedPreferences.contains(key)
        }
    }

    fun clear(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit().clear().apply()
    }

    fun getKeyDigest(sharedPreferences: SharedPreferences): ByteArray? {
        val keyDigestBase64 = sharedPreferences.getString(AppSettings.KEY_WATCH_KEY_DIGEST, null)
        if (keyDigestBase64 != null) {
            // TODO: This is a workaround to a dumb ordering bug, and in an ideal world it would be unnecessary
            return Base64.decode(
                keyDigestBase64, Base64.DEFAULT
            )
        } else {
            return null
        }
    }

    fun getWeight(sharedPreferences: SharedPreferences): Byte {
        return sharedPreferences.getInt(AppSettings.KEY_USER_WEIGHT, 0).toByte()
    }

    fun getHeight(sharedPreferences: SharedPreferences): Byte {
        return sharedPreferences.getInt(KEY_USER_HEIGHT, 0).toByte()
    }

    fun getBirthday(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString(KEY_USER_BIRTHDAY, "")
    }

    fun getSex(sharedPreferences: SharedPreferences): Byte? {
        val sexString =
            sharedPreferences.getString(AppSettings.KEY_USER_SEX, "")
        if (!sexString.isNullOrEmpty()) {
            return sexString.toInt().toByte()
        } else {
            return null
        }
    }
}
