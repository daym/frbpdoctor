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
class DatePreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context!!, attrs) {
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

    /**
     * A simple [androidx.preference.Preference.SummaryProvider] implementation for an
     * [DatePreference]. If no value has been set, the summary displayed will be 'Not
     * set', otherwise the summary displayed will be the value set for this preference.
     */
    class SimpleSummaryProvider private constructor() : SummaryProvider<DatePreference> {
        override fun provideSummary(preference: DatePreference): CharSequence? {
            return if (TextUtils.isEmpty(preference.date)) {
                preference.context.getString(R.string.not_set)
            } else {
                preference.date
            }
        }

        companion object {
            private var sSimpleSummaryProvider: SimpleSummaryProvider? = null

            /**
             * Retrieve a singleton instance of this simple
             * [androidx.preference.Preference.SummaryProvider] implementation.
             *
             * @return a singleton instance of this simple
             * [androidx.preference.Preference.SummaryProvider] implementation
             */
            val instance: SimpleSummaryProvider?
                get() {
                    if (sSimpleSummaryProvider == null) {
                        sSimpleSummaryProvider = SimpleSummaryProvider()
                    }
                    return sSimpleSummaryProvider
                }
        }
    }
}