/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.lineageparts.health;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.android.settingslib.widget.SliderPreference;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import lineageos.health.HealthInterface;

import org.lineageos.lineageparts.R;

public class ChargingLimitPreference extends SliderPreference
        implements Slider.OnSliderTouchListener {
    private static final String TAG = ChargingLimitPreference.class.getSimpleName();

    static final String BYPASS_CHARGE_ENABLED = "bypass_charge_enabled";
    static final String GAMING_MODE_ACTIVE = "ax_gaming_mode_active";

    private static final int MIN_LIMIT = 70;
    private static final int MAX_LIMIT = 100;

    private Slider mSlider;

    private TextView mChargingLimitValue;

    private final HealthInterface mHealthInterface;

    public ChargingLimitPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        mHealthInterface = HealthInterface.getInstance(context);
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mChargingLimitValue = (TextView) holder.findViewById(android.R.id.summary);
        mChargingLimitValue.setVisibility(View.VISIBLE);

        mSlider = (Slider) holder.findViewById(R.id.slider);
        mSlider.addOnSliderTouchListener(this);
        mSlider.setLabelBehavior(LabelFormatter.LABEL_FLOATING);
        mSlider.setStepSize(1);
        mSlider.setTickVisible(false);
        mSlider.setValueFrom(MIN_LIMIT);
        mSlider.setValueTo(MAX_LIMIT);

        setValue(getSetting());
    }

    @Override
    public void onStartTrackingTouch(final Slider slider) {
    }

    @Override
    public void onStopTrackingTouch(final Slider slider) {
        if (isBypassActive()) {
            setValue(getSetting());
            return;
        }
        final int newLimit = (int) slider.getValue();

        setSetting(newLimit);
        updateValue(newLimit);
    }

    public void setValue(final int value) {
        final boolean bypassActive = isBypassActive();
        setEnabled(!bypassActive);
        if (mSlider != null) {
            mSlider.setValue(Math.max(MIN_LIMIT, Math.min(MAX_LIMIT, value)));
            mSlider.setEnabled(isEnabled());
        }
        updateValue(value);
    }

    protected int getSetting() {
        return mHealthInterface.getLimit();
    }

    protected void setSetting(final int chargingLimit) {
        mHealthInterface.setLimit(chargingLimit);
    }

    private boolean isBypassActive() {
        return Settings.System.getInt(getContext().getContentResolver(),
                BYPASS_CHARGE_ENABLED, 0) == 1
                && Settings.Secure.getInt(getContext().getContentResolver(),
                        GAMING_MODE_ACTIVE, 0) == 1
                && mHealthInterface.getEnabled()
                && mHealthInterface.getMode() == HealthInterface.MODE_LIMIT;
    }

    private void updateValue(final int value) {
        if (mChargingLimitValue != null) {
            mChargingLimitValue.setText(getContext().getString(isBypassActive()
                    ? R.string.charging_control_limit_bypass_summary
                    : R.string.charging_control_limit_summary, value));
        }
    }
}
