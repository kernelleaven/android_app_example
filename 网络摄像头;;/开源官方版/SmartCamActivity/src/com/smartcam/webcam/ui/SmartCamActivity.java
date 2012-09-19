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

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.smartcam.webcam.R;
import com.smartcam.webcam.comm.BluetoothHandler;
import com.smartcam.webcam.comm.CommHandler;
import com.smartcam.webcam.comm.InetHandler;
import com.smartcam.webcam.preference.BtDevPreference;
import com.smartcam.webcam.video.EncodeHandler;

public class SmartCamActivity extends Activity implements SurfaceHolder.Callback, 
				PreviewCallback, OnSharedPreferenceChangeListener
{
	private static final String TAG = "SmartCam:SmartCamActivity";

	private static final int IDEAL_FRAME_WIDTH = 320;
	private static final int IDEAL_FRAME_HEIGHT = 240;
	private static final int FRAME_BUFFER_Q_SIZE = 5;
	
	private static final int CONNECT_BT_ID = Menu.FIRST;
	private static final int CONNECT_INET_ID = Menu.FIRST + 1;
	private static final int DISCONNECT_ID = Menu.FIRST + 2;
	private static final int SETTINGS_ID = Menu.FIRST + 3;
	private static final int HELP_ID = Menu.FIRST + 4;
	private static final int ABOUT_ID = Menu.FIRST + 5;
	private static final int EXIT_APP_ID = Menu.FIRST + 6;

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 1;

	private Camera camera;
	private Camera.Parameters camParams;
	private int frameWidth = IDEAL_FRAME_WIDTH;
	private int frameHeight = IDEAL_FRAME_HEIGHT;
	private byte[][] frameBuffer = null;
	private int frameBufferSize = 0;


	private String versionName = "1.4.0";
    // Local Bluetooth adapter
    private BluetoothAdapter btAdapter = null;
    private BluetoothHandler btHandler = null;
    private InetHandler inetHandler = null;
    private CommHandler commHandler = null;
    private EncodeHandler encodeHandler = null;
    private static SmartCamActivity instance = null;
    
    private ProgressDialog connectDialog = null;
    
    public static SmartCamActivity getInstance()
    {
    	return instance;
    }

    public void onPreviewFrame(byte[] data, Camera camera)
	{
    	//Log.d(TAG, "onPreviewFrame()");
		if(!commHandler.isConnected() || !commHandler.canAcceptMore())
		{
			camera.addCallbackBuffer(data);
			return;
		}
		else
		{
			encodeHandler.encodeQ(data);
		}
	}

    public List<Camera.Size> getSupportedPreviewSizes()
    {
    	return camParams.getSupportedPreviewSizes();
    }

    private void getCameraResolutionFromSettings(List<Camera.Size> pvResolutions)
    {
    	int camResIdx = -1;
    	String camResSettingsKey = getString(R.string.settings_camera_resolution_key);
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String camResIdxStr = prefs.getString(camResSettingsKey, "0");
    	try
    	{
    		camResIdx = Integer.parseInt(camResIdxStr.trim());
    		
    	}
    	catch(NumberFormatException nfe)
    	{
    		Log.e(TAG, "Could not parse camera resolution index");
    		frameWidth = IDEAL_FRAME_WIDTH;
    		frameHeight = IDEAL_FRAME_HEIGHT;
    		return;
    	}

    	if(camResIdx < 0 || camResIdx >= pvResolutions.size())
    	{
    		Log.e(TAG, "Out of bounds camera resolution index");
    		frameWidth = IDEAL_FRAME_WIDTH;
    		frameHeight = IDEAL_FRAME_HEIGHT;
    		return;
    	}
    	Camera.Size camRes = pvResolutions.get(camResIdx);
		frameWidth = camRes.width;
		frameHeight = camRes.height;
    }

    private void openCamera(SurfaceHolder holder)
    {
    	if(camera != null)
    		return;
        camera = Camera.open();

        camParams = camera.getParameters();
        // check if the camera supports the YCbCr_420_SP format
        List<Integer> pvFmts = camParams.getSupportedPreviewFormats();
        if(pvFmts == null || !pvFmts.contains(PixelFormat.YCbCr_420_SP))
        {
        	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Camera error");
            alertDialog.setMessage("The camera of your device is not currently supported...\nSmartCam will now exit.");
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int which)
            	{
            		exitApp();
            	}
            });
            alertDialog.show();
        }

        List<Camera.Size> pvResolutions = camParams.getSupportedPreviewSizes();
        getCameraResolutionFromSettings(pvResolutions);

        camParams.setPreviewSize(frameWidth, frameHeight);
        //camParams.getSupportedPreviewFrameRates();
        camParams.setPreviewFormat(PixelFormat.YCbCr_420_SP);
        // compute the frame buffer size
        PixelFormat pixelFmt = new PixelFormat();
		PixelFormat.getPixelFormatInfo(PixelFormat.YCbCr_420_SP, pixelFmt);
		frameBufferSize = frameWidth * frameHeight * pixelFmt.bitsPerPixel / 8;

		camera.setPreviewCallbackWithBuffer(this);

        // allocate the frame buffers and add them to the camera
        frameBuffer = new byte[FRAME_BUFFER_Q_SIZE][];
        for(int i = 0; i < FRAME_BUFFER_Q_SIZE; i++)
        {
        	frameBuffer[i] = new byte[frameBufferSize];
        	camera.addCallbackBuffer(frameBuffer[i]);
        }

        camera.setParameters(camParams);

    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String hideCamPreviewSettingsKey = getString(R.string.settings_cam_hide_preview_key);
    	boolean hideCamPreview = prefs.getBoolean(hideCamPreviewSettingsKey, false);
    	if(!hideCamPreview) // show preview, hide adds
    	{
	        try
	        {
	        	camera.setPreviewDisplay(holder);
	        }
	        catch(IOException ioe)
	        {
	        	Log.e(TAG, "openCamera(): can not set the preview display");
	        	ioe.printStackTrace();
	        }
    	}
    	else
    	{
    	}
    	camera.startPreview();
    }

    private void closeCamera()
    {
    	if(camera != null)
    	{
    		camera.setPreviewCallbackWithBuffer(null);
    		camera.stopPreview();
    		camera.release();
    		camera = null;
    	}
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SmartCamActivity.instance = this;
        this.setTitle(R.string.title_disconnected);
        setContentView(R.layout.main);
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	prefs.registerOnSharedPreferenceChangeListener(this);
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	// init the preview surface
    	SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
    	SurfaceHolder surfaceHolder = surfaceView.getHolder();
   		surfaceHolder.addCallback(this);
   		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Get local bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if(btAdapter == null)
        {
            Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_LONG).show();
        }else
                btHandler = new BluetoothHandler(this, msgHandler, btAdapter);
        inetHandler = new InetHandler(this, msgHandler);
		String btConnType = getString(R.string.settings_connection_type_bt);
		String connType = prefs.getString(getString(R.string.settings_connection_type_key), btConnType);
		if(connType.equals(btConnType))
		{
			commHandler = btHandler;
		}
		else
		{
			commHandler = inetHandler;
		}
        encodeHandler = new EncodeHandler(msgHandler, commHandler);
    }

    /** Called at the start of the visible lifetime. */
    @Override
    public void onStart()
    {
    	Log.i(TAG, "onStart()");
        super.onStart();
    }

    /** Called at the start of the active/focused lifetime. */
    @Override
    public void onResume()
    {
    	Log.i(TAG, "onResume()");
    	super.onResume();
    }

    /** Called at the end of the active/focused lifetime. */
    @Override
    public void onPause()
    {
    	Log.i(TAG, "onPause()");
    	super.onPause();
    }

    /** Called at the end of the visible lifetime. */
    @Override
    public void onStop()
    {
    	Log.i(TAG, "onStop()");
        super.onStop();
    }

    /** Called at the end of the full lifetime. */
    @Override
    public void onDestroy()
    {
    	Log.i(TAG, "onDestroy()");
    	closeCamera();
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	super.onPrepareOptionsMenu(menu);
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    	menu.clear();

    	if(commHandler.isConnected())
    	{
        	menu.add(0, DISCONNECT_ID, 0, R.string.disconnect)
        		.setIcon(R.drawable.disconnect);
    	}
    	else
    	{
	    	if(btAdapter == null)
	    	{
	        	menu.add(0, CONNECT_INET_ID, 0, R.string.connect_inet)
	        		.setIcon(R.drawable.connect_inet);
	    	}
	    	else
	    	{
	    		String btConnType = getString(R.string.settings_connection_type_bt);
	    		String connType = prefs.getString(getString(R.string.settings_connection_type_key), btConnType);
	    		if(connType.equals(btConnType))
	    		{
	    	    	menu.add(0, CONNECT_BT_ID, 0, R.string.connect_bt)
	    	    		.setIcon(R.drawable.connect_bt);
	    		}
	    		else
	    		{
	            	menu.add(0, CONNECT_INET_ID, 0, R.string.connect_inet)
	            		.setIcon(R.drawable.connect_inet);
	    		}
	    	}
    	}

    	MenuItem prefsMi = menu.add(0, SETTINGS_ID, 0, R.string.settings);
    	prefsMi.setIcon(android.R.drawable.ic_menu_preferences);
    	if(commHandler.isConnected())
    	{
    		prefsMi.setEnabled(false);
    	}
    	else
    	{
    		prefsMi.setEnabled(true);
    	}
    	menu.add(0, HELP_ID, 0, R.string.help)
    		.setIcon(android.R.drawable.ic_menu_help);
    	menu.add(0, ABOUT_ID, 0, R.string.about)
    		.setIcon(android.R.drawable.ic_menu_info_details);
    	menu.add(0, EXIT_APP_ID, 0, R.string.exit_app)
			.setIcon(android.R.drawable.ic_menu_close_clear_cancel);

    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case CONNECT_BT_ID:
    	{
    		if(btAdapter == null)
    		{
    			Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_LONG).show();
    			break;
    		}
            // If BT is not on, request that it be enabled.
            if(!btAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else
            {
            	connectBt();
            }

            break;
    	}
    	case CONNECT_INET_ID:
    	{
    		connectInet();
            break;
    	}
    	case DISCONNECT_ID:
    	{
    		commHandler.disconnect();
    		break;
    	}
    	case SETTINGS_ID:
    	{
    		Intent intent = new Intent(Intent.ACTION_VIEW);
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    		intent.setClassName(this, SettingsActivity.class.getName());
    		startActivity(intent);
    		break;
    	}

    	case HELP_ID:
    	{
    		Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.setClassName(this, HelpActivity.class.getName());
            startActivity(intent);
            break;
    	}

    	case ABOUT_ID:
    	{
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(getString(R.string.title_about) + versionName);
    		builder.setMessage(getString(R.string.msg_about) + "\nLicense: GPL v2\n" + getString(R.string.smartcam_url));
    		builder.setIcon(R.drawable.smartcam);
    		builder.setPositiveButton(R.string.button_open_browser, aboutListener);
    		builder.setNegativeButton(R.string.button_cancel, null);
    		builder.show();
    		break;
    	}
    	case EXIT_APP_ID:
    	{
    		exitApp();
    		break;
    	}
    	}
    	return super.onOptionsItemSelected(item);
    }

    private final DialogInterface.OnClickListener aboutListener =
        new DialogInterface.OnClickListener()
    	{
    		public void onClick(DialogInterface dialogInterface, int i)
    		{
    			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.smartcam_url)));
    			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    			startActivity(intent);
    		}
    	};

    /*
     * 			SurfaceHolder.Callback
     * 		1.surfaceChanged	
     * 		2.surfaceCreated
     * 		3.surfaceDestroyed
     * */
    public void surfaceCreated(SurfaceHolder holder)
    {
    	Log.i(TAG, "surfaceCreated()");
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String settingsKeepScreenOnKey = getString(R.string.settings_keep_screen_on_key);
    	boolean keepScreenOn = prefs.getBoolean(settingsKeepScreenOnKey, false);
    	if(keepScreenOn)
    		holder.setKeepScreenOn(true);

    	if(camera != null)
    	{
    		camera.stopPreview();
    		try
    		{
    			camera.setPreviewDisplay(holder);
    		}
    		catch(IOException ioe)
    		{
    			ioe.printStackTrace();
    		}
    		camera.startPreview();
    	}
    	else
    	{
    		openCamera(holder);
    	}
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
    	Log.i(TAG, "surfaceDestroyed()");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    	Log.i(TAG, "surfaceChanged()");
	}

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.i(TAG, "onActivityResult()");
        switch (requestCode)
        {
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if(resultCode == Activity.RESULT_OK)
            {
                // Bluetooth is now enabled
            	connectBt();
            }
            else
            {
                // User did not enable Bluetooth or an error occured
            	Log.i(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private void connectInet()
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String inetServerAddr = prefs.getString("inet_server_addr", null);
    	String inetServerPort = prefs.getString("inet_server_port", null);
    	if(inetServerAddr == null || inetServerAddr.trim().length() == 0 || inetServerPort == null || inetServerPort.trim().length() == 0)
    	{
    		Toast.makeText(this, R.string.select_inet_server, Toast.LENGTH_SHORT).show();
    		return;
    	}

    	// Show the please wait connecting dialog
    	connectDialog = ProgressDialog.show(SmartCamActivity.this, 
    			getString(R.string.please_wait), 
    			getString(R.string.connecting), true);

    	String[] inetServerAddrAndPort = new String[] {inetServerAddr, inetServerPort};
    	commHandler = inetHandler;
    	encodeHandler.setCommHandler(commHandler);
    	commHandler.connect(inetServerAddrAndPort);
    }

    private void connectBt()
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String btDevAddrAndName = prefs.getString("settings_bt_dev_key", null);
    	if(btDevAddrAndName == null || btDevAddrAndName.length() < BtDevPreference.ADDR_STR_LEN)
    	{
    		Toast.makeText(this, R.string.select_bt_dev, Toast.LENGTH_SHORT).show();
    		return;
    	}

    	// Show the please wait connecting dialog
    	connectDialog = ProgressDialog.show(SmartCamActivity.this, 
    			getString(R.string.please_wait), 
    			getString(R.string.connecting), true);

    	String btDevAddr = btDevAddrAndName.substring(0, BtDevPreference.ADDR_STR_LEN - 1);
    	commHandler = btHandler;
    	encodeHandler.setCommHandler(commHandler);
    	commHandler.connect(btDevAddr);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
    	String settingsCamResolutionKey = getString(R.string.settings_camera_resolution_key);
    	String settingsCamHidePreviewKey = getString(R.string.settings_cam_hide_preview_key);
    	if(key.equals(settingsCamResolutionKey) || key.equals(settingsCamHidePreviewKey))
    	{
    		closeCamera();
    	}
    }

    private void exitApp()
    {
    	encodeHandler.clearQ();
    	commHandler.clearQ();
    	commHandler.disconnect();
    	finish();
    }

    // Handler message types:
    public static final int MESSAGE_CONNECTED = 1;
    public static final int MESSAGE_DISCONNECTED = 2;
    public static final int MESSAGE_ENCODE_FINISHED = 3;
    public static final int MESSAGE_ENCODE_ERROR = 4;
    public static final int MESSAGE_SHOW_COULD_NOT_CONECT_MSG = 5;

    // The Handler that gets information back from the other threads
    private final Handler msgHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
            case MESSAGE_CONNECTED:
                Log.i(TAG, "CONNECTED");
                setTitle(R.string.title_connected);
                //Toast.makeText(SmartCamActivity.this, R.string.connected, Toast.LENGTH_LONG).show();
                if(connectDialog != null)
                {
                	connectDialog.dismiss();
                	connectDialog = null;
                }
                encodeHandler.start(frameBufferSize, frameWidth, frameHeight);
                break;

            case MESSAGE_DISCONNECTED:
                Log.i(TAG, "DISCONNECTED");
                setTitle(R.string.title_disconnected);
                //Toast.makeText(SmartCamActivity.this, R.string.disconnected, Toast.LENGTH_LONG).show();
                if(connectDialog != null)
                {
                	connectDialog.dismiss();
                	connectDialog = null;
                }

                encodeHandler.clearQ();
                commHandler.clearQ();
                encodeHandler.stop();
                break;

            case MESSAGE_ENCODE_FINISHED:
            	byte[] frame = (byte[])msg.obj;
            	if(camera != null)
            	{
            		camera.addCallbackBuffer(frame);
            	}
                break;

            case MESSAGE_ENCODE_ERROR:
                Log.e(TAG, "Encode error");
                Toast.makeText(SmartCamActivity.this, "Encode error", Toast.LENGTH_LONG).show();
                break;

            case MESSAGE_SHOW_COULD_NOT_CONECT_MSG:
                if(connectDialog != null)
                {
                	connectDialog.dismiss();
                	connectDialog = null;
                }
                Toast.makeText(SmartCamActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
                break;

            }
        }
    };
}
