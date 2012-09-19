package com.android.webcam.wifi;

import java.io.IOException;
import java.util.List;

import com.android.webcam.comm.BluetoothHandler;
import com.android.webcam.comm.CommHandler;
import com.android.webcam.comm.InetHandler;
import com.smartcam.webcam.video.EncodeHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class SmartCamActivity extends Activity implements SurfaceHolder.Callback,
												PreviewCallback,OnSharedPreferenceChangeListener,Camera.ErrorCallback{
    // Handler message types:
    public static final int MESSAGE_CONNECTED = 1;
    public static final int MESSAGE_DISCONNECTED = 2;
    public static final int MESSAGE_ENCODE_FINISHED = 3;
    public static final int MESSAGE_ENCODE_ERROR = 4;
    public static final int MESSAGE_SHOW_COULD_NOT_CONECT_MSG = 5;
    
    private static final int FRAME_BUFFER_Q_SIZE = 5;
    
    //
	private static final int IDEAL_FRAME_WIDTH = 320;
	private static final int IDEAL_FRAME_HEIGHT = 240;
    //
    private final String TAG="webcam";
    
    //
    private static SmartCamActivity instance = null;
    private BluetoothHandler btHandler = null;
    private BluetoothAdapter btAdapter = null;
    private InetHandler inetHandler=null;
    private ProgressDialog connectDialog = null;
    private EncodeHandler encodeHandler = null;
    private int frameWidth = IDEAL_FRAME_WIDTH;
	private int frameHeight = IDEAL_FRAME_HEIGHT;
	private byte[][] frameBuffer = null;
	private int frameBufferSize = 0;
	private CommHandler commHandler = null;
	private Camera camera=null;
	private Camera.Parameters camParams;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
                
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
        if(btAdapter == null){
            Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_LONG).show();
        }else{
        	btHandler = new BluetoothHandler(this, msgHandler, btAdapter);
        }
        inetHandler = new InetHandler(this, msgHandler);
		String btConnType = getString(R.string.settings_connection_type_bt);
		String connType = prefs.getString(getString(R.string.settings_connection_type_key), btConnType);
		if(connType.equals(btConnType)&&(null != btHandler)){
			commHandler = btHandler;
		}else{
			commHandler = inetHandler;
		}
        encodeHandler = new EncodeHandler(msgHandler, commHandler);
    }

    @Override
    protected void onDestroy() {
    	closeCamera();
    	super.onDestroy();
    }
    
    private void exitApp(){
    	encodeHandler.clearQ();
    	commHandler.clearQ();
    	commHandler.disconnect();
    	finish();
    }
    private void getCameraResolutionFromSettings(List<Camera.Size> pvResolutions){
    	int camResIdx = -1;
    	String camResSettingsKey = getString(R.string.settings_camera_resolution_key);
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String camResIdxStr = prefs.getString(camResSettingsKey, "0");
    	try{
    		camResIdx = Integer.parseInt(camResIdxStr.trim());
    	}catch(NumberFormatException nfe){
    		Log.e(TAG, "Could not parse camera resolution index");
    		frameWidth = IDEAL_FRAME_WIDTH;
    		frameHeight = IDEAL_FRAME_HEIGHT;
    		return;
    	}
    	if(camResIdx < 0 || camResIdx >= pvResolutions.size()){
    		Log.e(TAG, "Out of bounds camera resolution index");
    		frameWidth = IDEAL_FRAME_WIDTH;
    		frameHeight = IDEAL_FRAME_HEIGHT;
    		return;
    	}
    	Camera.Size camRes = pvResolutions.get(camResIdx);
		frameWidth = camRes.width;
		frameHeight = camRes.height;
    }
    private void closeCamera(){
    	if(camera != null){
    		camera.setPreviewCallbackWithBuffer(null);
    		camera.stopPreview();
    		camera.release();
    		camera = null;
    	}
    }
    private void openCamera(SurfaceHolder holder){
    	if(camera != null)
    		return;
    	int num=Camera.getNumberOfCameras();  
    	if(num<=0)
    		return;
    	camera = Camera.open();
    	if(null == camera)
    		camera = Camera.open(0);
       	camParams = camera.getParameters();
        // check if the camera supports the YCbCr_420_SP format
        List<Integer> pvFmts = camParams.getSupportedPreviewFormats();
        if(pvFmts == null || !pvFmts.contains(PixelFormat.YCbCr_420_SP)){
        	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Camera error");
            alertDialog.setMessage("The camera of your device is not currently supported...\nSmartCam will now exit.");
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setButton(0,"OK", new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int which){
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
        for(int i = 0; i < FRAME_BUFFER_Q_SIZE; i++){
        	frameBuffer[i] = new byte[frameBufferSize];
        	camera.addCallbackBuffer(frameBuffer[i]);
        }

        camera.setParameters(camParams);

    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String hideCamPreviewSettingsKey = getString(R.string.settings_cam_hide_preview_key);
    	boolean hideCamPreview = prefs.getBoolean(hideCamPreviewSettingsKey, false);
    	if(!hideCamPreview){// show preview, hide adds
	        try{
	        	camera.setPreviewDisplay(holder);
	        }catch(IOException ioe){
	        	Log.e(TAG, "openCamera(): can not set the preview display");
	        	ioe.printStackTrace();
	        }
    	}else{
    	}
    	camera.startPreview();
    }
    //---------------------------------------------------------------------------------------------------
    /*
     * 			SurfaceHolder.Callback
     * 		1.surfaceChanged	
     * 		2.surfaceCreated
     * 		3.surfaceDestroyed
     * */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.v(TAG,"surfaceChanged");
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(TAG,"surfaceCreated");
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	String settingsKeepScreenOnKey = getString(R.string.settings_keep_screen_on_key);
    	boolean keepScreenOn = prefs.getBoolean(settingsKeepScreenOnKey, false);
    	if(keepScreenOn)
    		holder.setKeepScreenOn(true);

    	if(camera != null){
    		camera.stopPreview();
    		try{
    			camera.setPreviewDisplay(holder);
    		}catch(IOException ioe){
    			ioe.printStackTrace();
    		}
    		camera.startPreview();
    	}else{
    		openCamera(holder);
    	}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(TAG,"surfaceDestroyed");
		
	}

	/*
	 * 			PreviewCallback		//Camera
	 * 		1.onPreviewFrame
	 * */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Log.v(TAG,"onPreviewFrame");
		
	}

	/*
	 * 			OnSharedPreferenceChangeListener
	 * 		1.onSharedPreferenceChanged
	 * */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.v(TAG,"onSharedPreferenceChanged");
		
	}
	
	/*
	 * 
	 * */
    private final Handler msgHandler = new Handler()    {
        @Override
        public void handleMessage(Message msg)        {
            switch(msg.what)            {
            case MESSAGE_CONNECTED:
                Log.i(TAG, "CONNECTED");
                setTitle(R.string.title_connected);
                //Toast.makeText(SmartCamActivity.this, R.string.connected, Toast.LENGTH_LONG).show();
                if(connectDialog != null)                {
                	connectDialog.dismiss();
                	connectDialog = null;
                }
                encodeHandler.start(frameBufferSize, frameWidth, frameHeight);
                break;
            case MESSAGE_DISCONNECTED:
                Log.i(TAG, "DISCONNECTED");
                setTitle(R.string.title_disconnected);
                //Toast.makeText(SmartCamActivity.this, R.string.disconnected, Toast.LENGTH_LONG).show();
                if(connectDialog != null)                {
                	connectDialog.dismiss();
                	connectDialog = null;
                }
                encodeHandler.clearQ();
                commHandler.clearQ();
                encodeHandler.stop();
                break;
            case MESSAGE_ENCODE_FINISHED:
            	byte[] frame = (byte[])msg.obj;
            	if(camera != null)            	{
            		camera.addCallbackBuffer(frame);
            	}
                break;
            case MESSAGE_ENCODE_ERROR:
                Log.e(TAG, "Encode error");
                Toast.makeText(SmartCamActivity.this, "Encode error", Toast.LENGTH_LONG).show();
                break;
            case MESSAGE_SHOW_COULD_NOT_CONECT_MSG:
                if(connectDialog != null)                {
                	connectDialog.dismiss();
                	connectDialog = null;
                }
                Toast.makeText(SmartCamActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
                break;
            }
        }
    };
    
    /*
     * 			Camera.ErrorCallback
     * 		1.onError
     * */
	@Override
	public void onError(int error, Camera camera) {
		Log.v(TAG,"Camera.ErrorCallback");
	}
}
