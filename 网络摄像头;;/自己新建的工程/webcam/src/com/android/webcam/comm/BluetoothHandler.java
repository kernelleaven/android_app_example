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

package com.android.webcam.comm;

import java.io.IOException;
import java.util.UUID;

import com.android.webcam.wifi.R;
import com.android.webcam.wifi.SmartCamActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BluetoothHandler extends CommHandler{
	private static final String TAG = "webcam";
    // Name for the SDP record when creating server socket
    private static final String SMARTCAM_BT_SERVICE_NAME = "SmartCam";

    // Unique UUID for this application
    private static final UUID SMARTCAM_BT_SERVICE_UUID = UUID.fromString("b9dec6d2-2930-4338-a079-aae560053238");

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    
    public BluetoothHandler(Context context, Handler handler, BluetoothAdapter btAdapter)    {
    	super(context, handler);
    	this.btAdapter = btAdapter;
    }

    protected void doConnect(Object addr) throws IOException    {
    	String btDevAddr = (String) addr;
    	BluetoothDevice btDev = btAdapter.getRemoteDevice(btDevAddr);
        try{
        	btSocket = btDev.createRfcommSocketToServiceRecord(SMARTCAM_BT_SERVICE_UUID);
        }catch(IOException e){
			Message msg = handler.obtainMessage(SmartCamActivity.MESSAGE_SHOW_COULD_NOT_CONECT_MSG);
			msg.obj = context.getString(R.string.can_not_connect_bt);
			msg.sendToTarget();
            Log.e(TAG, "createRfcommSocketToServiceRecord() failed", e);
            throw e;
        }

        // Cancel discovery because it will slow down the connection
    	if(btAdapter.isDiscovering())
    		btAdapter.cancelDiscovery();
        // Make a connection to the BluetoothSocket
        try{
        	// This is a blocking call and will only return on a
            // successful connection or an exception
            btSocket.connect();
        }catch(IOException e){
			Message msg = handler.obtainMessage(SmartCamActivity.MESSAGE_SHOW_COULD_NOT_CONECT_MSG);
			msg.obj = context.getString(R.string.can_not_connect_bt);
			msg.sendToTarget();
            // Close the socket
        	disconnect();
            Log.e(TAG, "btSocket.connect() failed", e);
            throw e;
        }

        try{
        	outputStream = btSocket.getOutputStream();
        }catch(IOException e){
			Message msg = handler.obtainMessage(SmartCamActivity.MESSAGE_SHOW_COULD_NOT_CONECT_MSG);
			msg.obj = context.getString(R.string.can_not_connect_bt);
			msg.sendToTarget();
            Log.e(TAG, "btSocket.getOutputStream() failed", e);
            throw e;
        }
    }

    protected void doDisconnect()    {
        try{
        	if(outputStream != null){
        		outputStream.close();
        	}
            btSocket.close();
        }catch(IOException ioe){
            Log.e(TAG, "unable to close() socket during connection failure", ioe);
        }
    }
}
