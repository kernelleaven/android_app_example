package com.android.dlna.server.services;

import com.android.dlna.server.DlnaEventListen;
import com.android.dlna.server.serverActivity;
import com.android.dlna.server.misc.DlnaData;
import com.android.dlna.server.services.DlnaMediaControlService.ServiceStub;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;

public class DlnaRemouteControlService extends Service {
	private final String TAG="dlna";
	private final boolean DEBUG=true;
	private boolean mServiceInUse=false;
	@Override
	public IBinder onBind(Intent intent) {
		if(DEBUG)Log.v(TAG,"DlnaRemouteControlService onBind");
		mServiceInUse=true;
		return mBinder;
	}
	@Override
	public void onRebind(Intent intent) {
		if(DEBUG)Log.v(TAG,"DlnaRemouteControlService onRebind");
		mServiceInUse=true;
		super.onRebind(intent);
	}
	@Override
	public boolean onUnbind(Intent intent) {
		if(DEBUG)Log.v(TAG,"DlnaRemouteControlService onUnbind");
		mServiceInUse=false;
		return super.onUnbind(intent);
	}
	@Override
	public void onCreate() {
		if(DEBUG)Log.v(TAG,"DlnaRemouteControlService onCreate");
		String uuid=DlnaData.creatNewUUID_12Bit(this);
		if(DEBUG)Log.v(TAG,"DlnaRemouteControlService onCreate uuid:"+uuid);
		serverActivity.initRemouteControlServer("EXAMPLE rcs",uuid);
		super.onCreate();
	}
	@Override
	public void onDestroy() {
		serverActivity.stoprmoutecontrolserver();
		if(DEBUG)Log.v(TAG,"DLNA C++ RCS service onDestroy");
		super.onDestroy();
	}
	
	/*
	 * 				be protect by services
	 */
    static class ServiceStub extends IDlnaRemouteControlService.Stub{
    	WeakReference<DlnaRemouteControlService> mService;
        
        ServiceStub(DlnaRemouteControlService service) {
            mService = new WeakReference<DlnaRemouteControlService>(service);
        }
    }
    private final IBinder mBinder = new ServiceStub(this);
}
