package com.android.dlna.server.services;

import com.android.dlna.server.serverActivity;
import com.android.dlna.server.misc.DlnaData;
import com.android.dlna.server.services.DlnaRemouteControlService.ServiceStub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.lang.ref.WeakReference;

public class DlnaRemouteControlerService extends Service {

	private final String TAG="dlna";
	private final boolean DEBUG=true;
	private boolean mServiceInUse=false;
	@Override
	public IBinder onBind(Intent intent) {
		mServiceInUse=true;
		return mBinder;
	}
	@Override
	public void onRebind(Intent intent) {
		mServiceInUse=true;
		super.onRebind(intent);
	}
	@Override
	public boolean onUnbind(Intent intent) {
		mServiceInUse=false;
		return super.onUnbind(intent);
	}
	
	@Override
	public void onCreate() {
		serverActivity.initRemouteControler();
		super.onCreate();
	}
	@Override
	public void onDestroy() {
		serverActivity.stopRemouteControler();
		if(DEBUG)Log.v(TAG,"DLNA C++ RCC service onDestroy");
		super.onDestroy();
	}
	
	/*
	 * 				be protect by services
	 */
    static class ServiceStub extends IDlnaRemouteControlerService.Stub{
    	WeakReference<DlnaRemouteControlerService> mService;
        
        ServiceStub(DlnaRemouteControlerService service) {
            mService = new WeakReference<DlnaRemouteControlerService>(service);
        }
    }
    private final IBinder mBinder = new ServiceStub(this);
}
