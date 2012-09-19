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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.android.webcam.wifi.R;
import com.android.webcam.wifi.SmartCamActivity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class InetHandler extends CommHandler{
	private static final String TAG = "webcam";
	private static final int DEFAULT_INET_PORT = 9361;

	private Socket inetSocket = null;
    public InetHandler(Context context, Handler handler){
    	super(context, handler);
    }

    protected void doConnect(Object addr) throws IOException{
    	inetSocket = new Socket();
    	String[] inetAddrAndPort = (String[]) addr;
    	String inetAddrStr = inetAddrAndPort[0];
    	String inetPortStr = inetAddrAndPort[1];
    	
    	int inetPort = DEFAULT_INET_PORT;
    	try{
    		inetPort = Integer.parseInt(inetPortStr);
    	}catch(NumberFormatException nfe){
    		Log.e(TAG, "doConnect(): could not parse inet port");
    	}

    	InetAddress inetAddr = null;
    	try{
    		inetAddr = InetAddress.getByName(inetAddrStr);
    	}catch(UnknownHostException uhe){
			Message msg = handler.obtainMessage(SmartCamActivity.MESSAGE_SHOW_COULD_NOT_CONECT_MSG);
			msg.obj = context.getString(R.string.unknown_inet_host);
			msg.sendToTarget();

            Log.e(TAG, "inetSocket.getOutputStream() failed", uhe);
            throw uhe;
    	}
    	InetSocketAddress inetSockAddr = new InetSocketAddress(inetAddr, inetPort);

        // Make a connection to the inet socket
        try{
        	// This is a blocking call and will only return on a
            // successful connection or an exception
            inetSocket.connect(inetSockAddr);
        }catch(IOException e){
			Message msg = handler.obtainMessage(SmartCamActivity.MESSAGE_SHOW_COULD_NOT_CONECT_MSG);
			msg.obj = context.getString(R.string.can_not_connect_inet);
			msg.sendToTarget();

            // Close the socket
        	disconnect();
        	Log.e(TAG, "inetSocket.connect() failed", e);
        	throw e;
        }

        try{
        	outputStream = inetSocket.getOutputStream();
        }catch(IOException e){
			Message msg = handler.obtainMessage(SmartCamActivity.MESSAGE_SHOW_COULD_NOT_CONECT_MSG);
			msg.obj = context.getString(R.string.can_not_connect_inet);
			msg.sendToTarget();

            Log.e(TAG, "inetSocket.getOutputStream() failed", e);
            throw e;
        }

    }

    protected void doDisconnect(){
        try{
        	if(outputStream != null){
        		outputStream.close();
        	}
        	inetSocket.close();
        }catch(IOException ioe){
            Log.e(TAG, "unable to close() socket during connection failure", ioe);
        }
    }
}
