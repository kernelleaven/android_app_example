package com.android.dlna.server.services;

import java.lang.ref.WeakReference;

import com.android.dlna.server.DlnaEventListen;
import com.android.dlna.server.serverActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class DlnaMediaControlService extends Service {
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
		serverActivity.initMediaControl();
		registerReceiver(msgr, new IntentFilter(DlnaEventListen.RENDERER_TOCONTRPOINT_CMD));
		super.onCreate();
	}
	@Override
	public void onDestroy() {
		unregisterReceiver(msgr);
		serverActivity.stopMediaControl();
		if(DEBUG)Log.v(TAG,"DLNA C++ DMC service onDestroy");
		super.onDestroy();
	}
	private MSGReceiver msgr=new MSGReceiver();
	
	private class MSGReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(DlnaEventListen.RENDERER_TOCONTRPOINT_CMD.equals(action)){
				int cmd=intent.getIntExtra(DlnaEventListen.GET_RENDERER_TOCONTRPOINT_CMD, 0);
				switch(cmd){
				case 0:
					break;
				case DlnaEventListen.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_DURATION:
					String duration= intent.getStringExtra(DlnaEventListen.GET_PARAM_MEDIA_DURATION);
					serverActivity.setCmdToControlPointByRendererCall(DlnaEventListen.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_DURATION,
								duration,null);
					break;
				case DlnaEventListen.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_POSITION:
					String positionto= intent.getStringExtra(DlnaEventListen.GET_PARAM_MEDIA_POSITION);
					serverActivity.setCmdToControlPointByRendererCall(DlnaEventListen.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_POSITION,
							positionto,null);
					break;
				case DlnaEventListen.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_PLAYINGSTATE:
					String playingstate= intent.getStringExtra(DlnaEventListen.GET_PARAM_MEDIA_PLAYINGSTATE);
					serverActivity.setCmdToControlPointByRendererCall(DlnaEventListen.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_PLAYINGSTATE,
							playingstate,null);
					break;
				}
			}
		}
	}
	/////////////////
    public void setMediaVolume(int volume) {
        synchronized (this) {
            serverActivity.setMediaVolume(volume);
        }
    }
    public void tryGetPositionInfo() {
        synchronized (this) {
            serverActivity.tryGetPositionInfo();
        }
    }
    public void tryGetMediaInfo() {
        synchronized (this) {
            serverActivity.tryGetMediaInfo();
        }
    }
    public void tryGetVolume() {
        synchronized (this) {
            serverActivity.tryGetVolume();
        }
    }
    public String getAllDMRUUIDAndNameStrings() {
        synchronized (this) {
            return serverActivity.getAllDMRUUIDAndNameStrings();
        }
    }
    public void setCmdToControlPointByRendererCall(int cmd, String value,String data){
        synchronized (this) {
            serverActivity.setCmdToControlPointByRendererCall(cmd, value, data);
        }
    }
    public void setCmdToCtlPlayState(String state){
        synchronized (this) {
            serverActivity.setCmdToCtlPlayState(state);
        }
    }
    public void setCmdToCtlMuteState(String state){
        synchronized (this) {
            serverActivity.setCmdToCtlMuteState(state);
        }
    }
    public void setCmdToCtlSetSeekTime(String realtime){
        synchronized (this) {
            serverActivity.setCmdToCtlSetSeekTime(realtime);
        }
    }
    public int setCurrentPlayRenderer(String uuid){
        synchronized (this) {
            return serverActivity.setCurrentPlayRenderer(uuid);
        }
    }
    public String getDescriptionUrlByDMRUUID(String uuid){
        synchronized (this) {
            return serverActivity.getDescriptionUrlByDMRUUID(uuid);
        }
    }
    public String getDMRIpByUUID(String uuid){
        synchronized (this) {
            return serverActivity.getDMRIpByUUID(uuid);
        }
    }
    public String setDMRAVTransportURI(String uri,String didl){
        synchronized (this) {
        	return serverActivity.setDMRAVTransportURI(uri,didl);
        }
    }
	/////////////////
	
	/*
	 * 				be protect by services
	 */
    static class ServiceStub extends IDlnaMediaControlService.Stub{
    	WeakReference<DlnaMediaControlService> mService;
        
        ServiceStub(DlnaMediaControlService service) {
            mService = new WeakReference<DlnaMediaControlService>(service);
        }
		@Override
		public void setMediaVolume(int volume) throws RemoteException {
			mService.get().setMediaVolume(volume);
		}

		@Override
		public void tryGetPositionInfo() throws RemoteException {
			mService.get().tryGetPositionInfo();
		}

		@Override
		public void tryGetMediaInfo() throws RemoteException {
			mService.get().tryGetMediaInfo();
		}

		@Override
		public void tryGetVolume() throws RemoteException {
			mService.get().tryGetVolume();
		}

		@Override
		public String getAllDMRUUIDAndNameStrings() throws RemoteException {
			return mService.get().getAllDMRUUIDAndNameStrings(); //返回为null没有测试
		}

		@Override
		public void setCmdToControlPointByRendererCall(int cmd, String value,
				String data) throws RemoteException {
			mService.get().setCmdToControlPointByRendererCall(cmd,value,data);
		}

		@Override
		public void setCmdToCtlPlayState(String state) throws RemoteException {
			mService.get().setCmdToCtlPlayState(state);
		}

		@Override
		public void setCmdToCtlMuteState(String state) throws RemoteException {
			mService.get().setCmdToCtlMuteState(state);
		}

		@Override
		public void setCmdToCtlSetSeekTime(String reltime)
				throws RemoteException {
			mService.get().setCmdToCtlSetSeekTime(reltime);
		}

		@Override
		public int setCurrentPlayRenderer(String uuid) throws RemoteException {
			return mService.get().setCurrentPlayRenderer(uuid);
		}

		@Override
		public String getDescriptionUrlByDMRUUID(String uuid) throws RemoteException {
			return mService.get().getDescriptionUrlByDMRUUID(uuid);
		}

		@Override
		public String getDMRIpByUUID(String uuid) throws RemoteException {
			return mService.get().getDMRIpByUUID(uuid);
		}

		@Override
		public String setDMRAVTransportURI(String url, String didl) throws RemoteException {
			return mService.get().setDMRAVTransportURI(url,didl);
		}
    }
    private final IBinder mBinder = new ServiceStub(this);
}
