package com.friendly_machines.frbpdoctor

object AppSettings {
    const val KEY_USER_ID = "userId"
    const val KEY_USER_WEIGHT = "userWeight"
    const val KEY_USER_HEIGHT = "userHeight"
    const val KEY_USER_SEX = "userSex"
    const val KEY_USER_BIRTHDAY = "userBirthday"
    const val KEY_WATCH_KEY_DIGEST = "watchKeyDigest" // invisible to the user
    const val KEY_WATCH_MAC_ADDRESS = "watchMacAddress"

    val MANDATORY_SETTINGS = listOf(KEY_WATCH_MAC_ADDRESS, KEY_WATCH_KEY_DIGEST, KEY_USER_ID, KEY_USER_WEIGHT, KEY_USER_HEIGHT, KEY_USER_SEX, KEY_USER_BIRTHDAY)
}
