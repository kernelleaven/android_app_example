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

package com.smartcam.webcam.comm;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.smartcam.webcam.ui.SmartCamActivity;

public abstract class CommHandler
{
	public enum ConnectionStatus
	{
		DISCONNECTED,
		CONNECTING,
		CONNECTED
	}

	private static final String TAG = "SmartCam:CommHandler";
	protected Handler handler = null;
	protected Context context = null;
	protected OutputStream outputStream = null;
	private ArrayList<byte[]> sendQueue = new ArrayList<byte[]>();
	private static final int MAX_SENDQ_SIZE = 2;
	private int dropped = 0;
	protected ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
	private SendThread sendThread = null;
	private ConnectThread connectThread = null;

	protected CommHandler(Context context, Handler handler)
	{
		this.context = context;
		this.handler = handler;
	}

	public ConnectionStatus getConnectionStatus()
	{
		return connectionStatus;
	}

	public boolean isConnected()
	{
		return connectionStatus == ConnectionStatus.CONNECTED;
	}
	
	public final void clearQ()
	{
		synchronized(sendQueue)
		{
			sendQueue.clear();
		}
	}

	public final void sendQ(byte[] data)
	{
		synchronized(sendQueue)
		{
			if(sendQueue.size() == MAX_SENDQ_SIZE)
			{
				++ dropped;
				Log.i(TAG, "dropped " + dropped + " pkgs");
				//frameQ.remove(0);
				//frameQ.add(data);
				return;
			}
			if(sendQueue.size() == 0)
			{
				sendQueue.add(data);
				sendQueue.notify();
				return;
			}
			sendQueue.add(data);
		}
	}

	public final boolean canAcceptMore()
	{
		synchronized(sendQueue)
		{
			if(sendQueue.size() == MAX_SENDQ_SIZE)
				return false;
		}
		return true;
	}

	public final void doWrite(byte[] data)
	{
		Log.i(TAG, "doWrite()");
		try
		{
			outputStream.write(data);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			Log.e(TAG, "doWrite() failed", ioe);
			connectionStatus = ConnectionStatus.DISCONNECTED;
			synchronized(sendQueue)
			{
				sendQueue.clear();
			}
			handler.obtainMessage(SmartCamActivity.MESSAGE_DISCONNECTED).sendToTarget();
			disconnect();
		}
	}

	public void connect(Object address)
	{
		synchronized(connectionStatus)
		{
			if(connectionStatus == ConnectionStatus.CONNECTING)
			{
				return;
			}
			if(connectionStatus == ConnectionStatus.CONNECTED)
			{
				return;
			}
			if(connectionStatus == ConnectionStatus.DISCONNECTED)
			{
				synchronized(sendQueue)
				{
					sendQueue.clear();
				}
				connectThread = new ConnectThread(address);
				connectThread.start();
			}
		}
	}

	protected abstract void doConnect(Object addr) throws IOException;
	
	public void disconnect()
	{
		if(connectionStatus == ConnectionStatus.DISCONNECTED)
		{
			return;
		}
		if(connectionStatus == ConnectionStatus.CONNECTING)
		{
			connectThread.interrupt();
			doDisconnect();
			connectionStatus = ConnectionStatus.DISCONNECTED;
			handler.obtainMessage(SmartCamActivity.MESSAGE_DISCONNECTED).sendToTarget();
			return;
		}
		if(connectionStatus == ConnectionStatus.CONNECTED)
		{
			if(sendThread != null)
			{
				sendThread.alive = false;
				sendThread.interrupt();
				try
				{
					sendThread.join();
				}
				catch(InterruptedException ie)
				{
					ie.printStackTrace();
				}
			}
			doDisconnect();
			connectionStatus = ConnectionStatus.DISCONNECTED;
			handler.obtainMessage(SmartCamActivity.MESSAGE_DISCONNECTED).sendToTarget();
		}
	}

	protected abstract void doDisconnect();

	private class ConnectThread extends Thread
	{
		Object address = null;

		public ConnectThread(Object address)
		{
			this.address = address;
		}

		public void run()
		{
			try
			{
				connectionStatus = ConnectionStatus.CONNECTING;
				doConnect(address);
				connectionStatus = ConnectionStatus.CONNECTED;
				sendThread = new SendThread();
				sendThread.start();
				handler.obtainMessage(SmartCamActivity.MESSAGE_CONNECTED).sendToTarget();
			}
			catch(IOException ioe)
			{
				connectionStatus = ConnectionStatus.DISCONNECTED;
				Log.e(TAG, "doConnect() failed", ioe);
				return;
			}
		}
	}

	private class SendThread extends Thread
	{
		public boolean alive = false; 
		public void run()
		{
			alive = true;
			while(alive)
			{
				byte[] data = null;
				synchronized(sendQueue)
				{
					if(sendQueue.size() == 0)
					{
						try
						{
							sendQueue.wait();
						}
						catch(InterruptedException ie)
						{
							continue;
						}
					}
					data = sendQueue.remove(0);
				}
				doWrite(data);
			}
		}
	}
}
