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

package com.smartcam.webcam.video;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smartcam.webcam.comm.CommHandler;
import com.smartcam.webcam.ui.SmartCamActivity;

public class EncodeHandler
{
	private static final String TAG = "SmartCam:EncodeThread";
	private ArrayList<byte[]> frameQ = new ArrayList<byte[]>();
	private static final int MAX_FRAMEQ_SIZE = 2;
	private int dropped = 0;
	private int frameBufferSize = 0;
	private int frameWidth = 0;
	private int frameHeight = 0;

	private final Handler handler;
	private JpegHandler jpegHandler = null;
	private CommHandler commHandler = null;
	private EncodeThread encodeThread = null;

	public EncodeHandler(Handler handler, CommHandler commHandler)
	{
		this.handler = handler;
		this.commHandler = commHandler;
		jpegHandler = new JpegHandler();
	}

	public void setCommHandler(CommHandler commHandler)
	{
		this.commHandler = commHandler;
	}

	public void clearQ()
	{
		synchronized(frameQ)
		{
			// give all frames in the Q back to the camera
			for(int i = 0; i < frameQ.size(); i++)
			{
				Message msg = handler.obtainMessage(SmartCamActivity.MESSAGE_ENCODE_FINISHED);
				msg.obj = frameQ.get(i);
				msg.sendToTarget();
			}
			frameQ.clear();
		}
	}

	public final void encodeQ(byte[] frame)
	{
		synchronized(frameQ)
		{
			if(frameQ.size() == MAX_FRAMEQ_SIZE)
			{
				++ dropped;
				Log.i(TAG, "dropped " + dropped + " frames");
				// Q is full, give the frame buffer back to the camera
				Message msg = handler.obtainMessage(SmartCamActivity.MESSAGE_ENCODE_FINISHED);
				msg.obj = frame;
				msg.sendToTarget();

				//frameQ.remove(0);
				//frameQ.add(data);
				return;
			}
			if(frameQ.size() == 0)
			{
				frameQ.add(frame);
				frameQ.notify();
				return;
			}
			frameQ.add(frame);
		}
	}

	public void start(int frameBufferSize, int frameWidth, int frameHeight)
	{
		this.frameBufferSize = frameBufferSize;
		this.frameWidth = frameWidth;
		this.frameHeight= frameHeight; 
		encodeThread = new EncodeThread();
		encodeThread.start();
	}

	public void stop()
	{
		if(encodeThread != null)
		{
			encodeThread.alive = false;
			try
			{
				encodeThread.join();
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
	}
	private byte[] doEncode(byte[] frame)
	{
		return jpegHandler.encodeYUV420SP(frame);
	}

	private void initYuv()
	{
		byte[] headerData = jpegHandler.initYuv(frameBufferSize, frameWidth, frameHeight);
		commHandler.sendQ(headerData);
	}

	private class EncodeThread extends Thread
	{
		private boolean alive = false;
		public void run()
		{
			alive = true;
			initYuv();
			while(alive)
			{
				byte[] frame = null;
				synchronized(frameQ)
				{
					if(frameQ.size() == 0)
					{
						try
						{
							frameQ.wait();
						}
						catch(InterruptedException ie)
						{
							continue;
						}
					}
					frame = frameQ.remove(0);
				}
				byte[] jpegData = doEncode(frame);
				// finished encoding the frame, give the frame buffer back to the camera
				Message msg = handler.obtainMessage(SmartCamActivity.MESSAGE_ENCODE_FINISHED);
				msg.obj = frame;
				msg.sendToTarget();

				if(jpegData != null)
				{
					commHandler.sendQ(jpegData);
				}
				else
				{
	                handler.obtainMessage(SmartCamActivity.MESSAGE_ENCODE_ERROR).sendToTarget();
				}
			}
		}
	}
}
