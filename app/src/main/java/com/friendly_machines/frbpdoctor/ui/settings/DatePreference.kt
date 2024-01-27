package com.friendly_machines.frbpdoctor.ui.settings

import android.content.Context
import android.content.res.TypedArray
import android.text.TextUtils
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.friendly_machines.frbpdoctor.R

/**
 * A dialog preference that shown calendar in the dialog.
 *
 *
 * Saves a string value.
 */
class DatePreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    private var dateValue: String? = null
    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        date = getPersistedString(defaultValue as String?)
    }
    var date: String?
        get() = dateValue
        set(text) {
            val wasBlocking = shouldDisableDependents()
            dateValue = text
            persistString(text)
            val isBlocking = shouldDisableDependents()
            if (isBlocking != wasBlocking) {
                notifyDependencyChange(isBlocking)
            }
            notifyChanged()
        }

    class SimpleSummaryProvider private constructor() : SummaryProvider<DatePreference> {
        override fun provideSummary(preference: DatePreference): CharSequence? {
            return if (TextUtils.isEmpty(preference.date)) {
                preference.context.getString(R.string.not_set)
            } else {
                preference.date
            }
        }

        companion object {
            val instance: SimpleSummaryProvider by lazy {
                SimpleSummaryProvider()
            }
        }
    }

    init {
        summaryProvider = SimpleSummaryProvider.instance
    }
}