package com.friendly_machines.frbpdoctor.ui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import com.friendly_machines.frbpdoctor.R;
import com.polidea.rxandroidble3.RxBleDevice;

/**
 * A dialog preference that shown calendar in the dialog.
 * <p>
 * Saves a string value.
 */
public class RxBleDevicePreference extends DialogPreference {

    private RxBleDevice mValue = null;

    public RxBleDevicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        //setDate(getPersistedString((String) defaultValue));
        mValue = (RxBleDevice) defaultValue;
    }

    /**
     * Gets the date as a string from the current data storage.
     *
     * @return string representation of the date.
     */
    public RxBleDevice getDevice() {
        return mValue;
    }

    /**
     * Saves the date as a string in the current data storage.
     *
     * @param device string representation of the date to save.
     */
    public void setDevice(RxBleDevice device) {
        final boolean wasBlocking = shouldDisableDependents();

        mValue = device;

        persistString(device.getMacAddress());

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

        notifyChanged();
    }

    /**
     * A simple {@link SummaryProvider} implementation for an
     * {@link RxBleDevicePreference}. If no value has been set, the summary displayed will be 'Not
     * set', otherwise the summary displayed will be the value set for this preference.
     */
    public static final class SimpleSummaryProvider implements SummaryProvider<RxBleDevicePreference> {

        private static SimpleSummaryProvider sSimpleSummaryProvider;

        private SimpleSummaryProvider() {
        }

        /**
         * Retrieve a singleton instance of this simple
         * {@link SummaryProvider} implementation.
         *
         * @return a singleton instance of this simple
         * {@link SummaryProvider} implementation
         */
        public static SimpleSummaryProvider getInstance() {
            if (sSimpleSummaryProvider == null) {
                sSimpleSummaryProvider = new SimpleSummaryProvider();
            }
            return sSimpleSummaryProvider;
        }

        @Override
        public CharSequence provideSummary(RxBleDevicePreference preference) {
            if (preference.getDevice() == null) {
                return (preference.getContext().getString(R.string.not_set));
            } else {
                return preference.getDevice().getMacAddress();
            }
        }
    }
}
