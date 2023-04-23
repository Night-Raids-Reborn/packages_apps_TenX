/*
 * Copyright (C) 2020 TenX-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tenx.settings.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.preference.*;
import android.os.Handler;
import java.net.InetAddress;
import android.content.ContentResolver;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.tenx.support.preferences.SystemSettingMasterSwitchPreference;

public class Misc extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "Misc";

    private static final String GAMING_MODE_ENABLED = "gaming_mode_enabled";
    private static final String PREF_ADBLOCK = "persist.aicp.hosts_block";

    private Handler mHandler = new Handler();

    private SystemSettingMasterSwitchPreference mGamingMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tenx_settings_misc);

        mGamingMode = (SystemSettingMasterSwitchPreference) findPreference(GAMING_MODE_ENABLED);
        mGamingMode.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.GAMING_MODE_ENABLED, 0) == 1));
        mGamingMode.setOnPreferenceChangeListener(this);
        findPreference(PREF_ADBLOCK).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	if (preference == mGamingMode) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.GAMING_MODE_ENABLED, value ? 1 : 0);
            return true;
        }  else if (PREF_ADBLOCK.equals(preference.getKey())) {
            // Flush the java VM DNS cache to re-read the hosts file.
            // Delay to ensure the value is persisted before we refresh
            mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InetAddress.clearDnsCache();
                    }
            }, 1000);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.TENX;
    }
}
