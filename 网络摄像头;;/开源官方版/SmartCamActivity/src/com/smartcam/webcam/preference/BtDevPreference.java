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

package com.smartcam.webcam.preference;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.DialogPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.smartcam.webcam.R;

public class BtDevPreference extends DialogPreference
{
    private EditText btDevEditText;
    private String btDevAddr;
    private String btDevName;
    private char btDevSep = '|';
    private String btDevAddrAndName;
    public static final int ADDR_STR_LEN = 18;

    private BroadcastReceiver btBcastRcvr = null;

    private void init(Context context)
    {
        setDialogLayoutResource(R.layout.bt_dev_pref);
        btBcastRcvr = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                if(action.equals("android.bluetooth.devicepicker.action.DEVICE_SELECTED"))
                {
                	BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                	if(btDevice != null)
                	{
                		btDevAddr = btDevice.getAddress();
                		btDevName = btDevice.getName();
                		if(btDevName == null || btDevName.trim().length() == 0)
                		{
                			btDevName = btDevAddr;
                		}
                		btDevEditText.setText(btDevName);
                	}
                }
            }
        };

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter("android.bluetooth.devicepicker.action.DEVICE_SELECTED");
        context.registerReceiver(btBcastRcvr, filter);
    }

    public BtDevPreference(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    public BtDevPreference(Context context, AttributeSet attrs)
    {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public BtDevPreference(Context context)
    {
    	this(context, null, android.R.attr.dialogPreferenceStyle);
    }

    @Override
    protected View onCreateDialogView()
    {
    	final View parent = (View)super.onCreateDialogView();
    	btDevEditText = (EditText)parent.findViewById(R.id.btDevEditText);
        btDevEditText.setInputType(InputType.TYPE_NULL);
        btDevEditText.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
        		Intent intent = new Intent("android.bluetooth.devicepicker.action.LAUNCH");
                getContext().startActivity(intent);
            }
        });

    	return parent;
    }

    @Override
    protected void onBindDialogView(View v)
    {
        super.onBindDialogView(v);
        btDevEditText.setText(btDevName);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue)
    {
        if(restore)
        {
        	btDevAddrAndName = getPersistedString("");
        }
        else
        {
        	btDevAddrAndName = (String) defaultValue;
        }
        parseBtDevAddrAndName();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
    	super.onDialogClosed(positiveResult);
    	if(positiveResult)
    	{
    		persistBtDevAddrAndName();
        }
    }

    private void parseBtDevAddrAndName()
    {
    	if(btDevAddrAndName == null || btDevAddrAndName.length() < ADDR_STR_LEN)
    	{
    		btDevAddr = "";
    		btDevName = "";
    	}
    	else
    	{
    		btDevAddr = btDevAddrAndName.substring(0, ADDR_STR_LEN - 1);
    		btDevName = btDevAddrAndName.substring(ADDR_STR_LEN, btDevAddrAndName.length());
    	}
    }

    private void persistBtDevAddrAndName()
    {
    	btDevAddrAndName = btDevAddr + btDevSep + btDevName;
    	persistString(btDevAddrAndName);
    }
}