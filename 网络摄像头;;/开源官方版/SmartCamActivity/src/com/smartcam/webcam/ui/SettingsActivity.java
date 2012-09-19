/*
 * Copyright (C) 2010 Ionut Dediu <deionut@yahoo.com>
 *
 * Licensed under the GNU General Public License Version 2
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.smartcam.webcam.ui;

import java.util.List;

import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import com.smartcam.webcam.R;

public class SettingsActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.preferences);
		List<Camera.Size> pvSizes = SmartCamActivity.getInstance().getSupportedPreviewSizes();
		CharSequence[] entries = new CharSequence[pvSizes.size()];
		CharSequence[] entryValues = new CharSequence[pvSizes.size()];
		for(int i = 0; i < pvSizes.size(); i++)
        {
			Camera.Size crtSize = pvSizes.get(i);
			String crtResolutionEntry = String.valueOf(crtSize.width) + " x " + String.valueOf(crtSize.height);
			entries[i] = crtResolutionEntry;
			entryValues[i] = String.valueOf(i);
        }

		ListPreference camResolutionList = (ListPreference)findPreference(getString(R.string.settings_camera_resolution_key));
		camResolutionList.setEntries(entries);
		camResolutionList.setEntryValues(entryValues);
	}
}
