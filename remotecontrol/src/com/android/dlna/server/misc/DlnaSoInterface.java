package com.android.dlna.server.misc;

import java.util.HashMap;

import com.android.dlna.server.services.DlnaRemouteControlerService;
import com.android.dlna.server.services.DlnaMediaControlService;
import com.android.dlna.server.services.DlnaRemouteControlService;
import com.android.dlna.server.services.IDlnaMediaControlService;
import com.android.dlna.server.services.IDlnaRemouteControlService;
import com.android.dlna.server.services.IDlnaRemouteControlerService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;


public class DlnaSoInterface {
	private final static String TAG="dlna";
	
	/* ---------------------------------------------------------
	 |		DMC server bind 相关接口
	 *  --------------------------------------------------------*/
    public static ServiceToken bindToService(Activity context) {
        return bindToService(context, null);
    }

    public static ServiceToken bindToService(Activity context, ServiceConnection callback) {
        Activity realActivity = context.getParent();
        if (realActivity == null) {
            realActivity = context;
        }
        ContextWrapper cw = new ContextWrapper(realActivity);
        cw.startService(new Intent(cw, DlnaMediaControlService.class));
        ServiceBinder sb = new ServiceBinder(callback);
        if (cw.bindService((new Intent()).setClass(cw, DlnaMediaControlService.class), sb, 0)) {
            sConnectionMap.put(cw, sb);
            return new ServiceToken(cw);
        }
        Log.e(TAG, "Failed to bind to service");
        return null;
    }

    public static void unbindFromService(ServiceToken token) {
        if (token == null) {
            Log.e(TAG, "Trying to unbind with null token");
            return;
        }
        ContextWrapper cw = token.mWrappedContext;
        ServiceBinder sb = sConnectionMap.remove(cw);
        if (sb == null) {
            Log.e(TAG, "Trying to unbind for unknown Context");
            return;
        }
        cw.unbindService(sb);
        if (sConnectionMap.isEmpty()) {
        	sService = null;
        }
    }
	/* ---------------------------------------------------------
	 |		DMC server interface 相关需要的定义
	 *  --------------------------------------------------------*/
	public static IDlnaMediaControlService sService = null;
	private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();
	public static class ServiceToken {
        ContextWrapper mWrappedContext;
        ServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }
    private static class ServiceBinder implements ServiceConnection {
        ServiceConnection mCallback;
        ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }
        
        public void onServiceConnected(ComponentName className, android.os.IBinder service) {
        	sService=IDlnaMediaControlService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }
        
        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            sService = null;
        }
    }
	/* ---------------------------------------------------------
	 |		可被直接调用的 DMC server interface 
	 *  --------------------------------------------------------*/
    public static void tryGetPositionInfo(){
    	if(null != sService)
			try {
				sService.tryGetPositionInfo();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    }
    public static void tryGetMediaInfo(){
    	if(null != sService)
			try {
				sService.tryGetMediaInfo();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    }
    public static void setMediaVolume(int volume){
    	if(null != sService)
			try {
				sService.setMediaVolume(volume);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    }
    public static void setCmdToCtlPlayState(String state){
    	if(null != sService)
			try {
				sService.setCmdToCtlPlayState(state);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    }
    public static void setCmdToCtlSetSeekTime(String reltime){
    	if(null != sService)
			try {
				sService.setCmdToCtlSetSeekTime(reltime);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    }
    public static String getAllDMRUUIDAndNameStrings(){
    	if(null != sService)
			try {
				return sService.getAllDMRUUIDAndNameStrings();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    	return null;
    }
    public static String setDMRAVTransportURI(String uri,String didl){
    	if(null != sService)
			try {
				return sService.setDMRAVTransportURI(uri,didl);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    	return null;
    }
    public static void setCurrentPlayRenderer(String uuid){
    	if(null != sService)
			try {
				sService.setCurrentPlayRenderer(uuid);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    }
    public static String getDMRIpByUUID(String uuid){
    	if(null != sService)
			try {
				return sService.getDMRIpByUUID(uuid);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    	return null;
    }
    
	/* ---------------------------------------------------------
	 |	||||||||||||||||||||||||||||||||||||||||||||||||||||||||	
	 *  --------------------------------------------------------*/
	/* ---------------------------------------------------------
	 |		RCS server bind 相关接口
	 *  --------------------------------------------------------*/
	public static RCSServiceToken bindToRCSService(Activity context) {
		return bindToRCSService(context, null);
	}

	public static RCSServiceToken bindToRCSService(Activity context, ServiceConnection callback) {
		Activity realActivity = context.getParent();
		if (realActivity == null) {
			realActivity = context;
		}
		ContextWrapper cw = new ContextWrapper(realActivity);
		cw.startService(new Intent(cw, DlnaRemouteControlService.class));
		RCSServiceBinder sb = new RCSServiceBinder(callback);
		if (cw.bindService((new Intent()).setClass(cw, DlnaRemouteControlService.class), sb, 0)) {
			sRCSConnectionMap.put(cw, sb);
			return new RCSServiceToken(cw);
		}
		Log.e(TAG, "Failed to bind to RCS service");
		return null;
	}

	public static void unbindFromRCSService(RCSServiceToken token) {
		if (token == null) {
			Log.e(TAG, "Trying to unbind with RCS null token");
			return;
		}
		ContextWrapper cw = token.mWrappedContext;
		RCSServiceBinder sb = sRCSConnectionMap.remove(cw);
		if (sb == null) {
			Log.e(TAG, "Trying to unbind for unknown Context");
			return;
		}
		cw.unbindService(sb);
		if (sRCSConnectionMap.isEmpty()) {
			sRCSService = null;
		}
	}
	/* ---------------------------------------------------------
	 |		RCS remoute control server interface 相关需要的定义
	 *  --------------------------------------------------------*/
	public static IDlnaRemouteControlService sRCSService = null;
	private static HashMap<Context, RCSServiceBinder> sRCSConnectionMap = new HashMap<Context, RCSServiceBinder>();
	public static class RCSServiceToken {
		ContextWrapper mWrappedContext;
		RCSServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }
	private static class RCSServiceBinder implements ServiceConnection {
		ServiceConnection mCallback;
		RCSServiceBinder(ServiceConnection callback) {
			mCallback = callback;
		}
       
       public void onServiceConnected(ComponentName className, android.os.IBinder service) {
    	   sRCSService=IDlnaRemouteControlService.Stub.asInterface(service);
           if (mCallback != null) {
               mCallback.onServiceConnected(className, service);
           }
       }
       
       public void onServiceDisconnected(ComponentName className) {
           if (mCallback != null) {
               mCallback.onServiceDisconnected(className);
           }
           sRCSService = null;
       }
	}
	
	
	/* ---------------------------------------------------------
	 |	||||||||||||||||||||||||||||||||||||||||||||||||||||||||	
	 *  --------------------------------------------------------*/
	/* ---------------------------------------------------------
	 |		RCC server bind 相关接口
	 *  --------------------------------------------------------*/
	public static RCCServiceToken bindToRCCService(Activity context) {
		return bindToRCCService(context, null);
	}

	public static RCCServiceToken bindToRCCService(Activity context, ServiceConnection callback) {
		Activity realActivity = context.getParent();
		if (realActivity == null) {
			realActivity = context;
		}
		ContextWrapper cw = new ContextWrapper(realActivity);
		cw.startService(new Intent(cw, DlnaRemouteControlerService.class));
		RCCServiceBinder sb = new RCCServiceBinder(callback);
		if (cw.bindService((new Intent()).setClass(cw, DlnaRemouteControlerService.class), sb, 0)) {
			sRCCConnectionMap.put(cw, sb);
			return new RCCServiceToken(cw);
		}
		Log.e(TAG, "Failed to bind to RCC service");
		return null;
	}

	public static void unbindFromRCCService(RCCServiceToken token) {
		if (token == null) {
			Log.e(TAG, "Trying to unbind with RCC null token");
			return;
		}
		ContextWrapper cw = token.mWrappedContext;
		RCCServiceBinder sb = sRCCConnectionMap.remove(cw);
		if (sb == null) {
			Log.e(TAG, "Trying to unbind for unknown Context");
			return;
		}
		cw.unbindService(sb);
		if (sRCCConnectionMap.isEmpty()) {
			sRCCService = null;
		}
	}
	/* ---------------------------------------------------------
	 |		RCC remoute control server interface 相关需要的定义
	 *  --------------------------------------------------------*/
	public static IDlnaRemouteControlerService sRCCService = null;
	private static HashMap<Context, RCCServiceBinder> sRCCConnectionMap = new HashMap<Context, RCCServiceBinder>();
	public static class RCCServiceToken {
		ContextWrapper mWrappedContext;
		RCCServiceToken(ContextWrapper context) {
           mWrappedContext = context;
       }
	}
	private static class RCCServiceBinder implements ServiceConnection {
		ServiceConnection mCallback;
		RCCServiceBinder(ServiceConnection callback) {
			mCallback = callback;
		}
      
      public void onServiceConnected(ComponentName className, android.os.IBinder service) {
   	   sRCCService=IDlnaRemouteControlerService.Stub.asInterface(service);
          if (mCallback != null) {
              mCallback.onServiceConnected(className, service);
          }
      }
      
      public void onServiceDisconnected(ComponentName className) {
          if (mCallback != null) {
              mCallback.onServiceDisconnected(className);
          }
          sRCCService = null;
      }
	}
}
