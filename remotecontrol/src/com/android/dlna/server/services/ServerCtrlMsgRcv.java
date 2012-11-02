package com.android.dlna.server.services;

import com.android.dlna.server.misc.DlnaTools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ServerCtrlMsgRcv extends BroadcastReceiver {
	private static boolean dlnasharestart=false;
	private static boolean dlnarendererstart=false;
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			//Toast.makeText(context, "tryStartRendererService ", Toast.LENGTH_LONG).show();
			tryStartDlnaShareService(context);
			tryStartRendererService(context);
		}else if(action.equals(DlnaShareFileServer.STARTSHARESERVICE)){
			tryStartDlnaShareService(context);
			tryStartRendererService(context);
		}else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
			//tryStartDlnaShareService(context);
			//tryStartRendererService(context);
		}else if(action.equals("android.intent.action.MEDIA_UNMOUNTED")){
			Intent startclean;
	    	startclean = new Intent(DlnaShareFileServer.CLEANEMPTYSHAREDFILE);
	    	context.sendBroadcast(startclean);
		}
	}
	
	private void tryStartDlnaShareService(Context context){
		boolean netstate=DlnaTools.getNetState(context);
		if(netstate&&!dlnasharestart){	
			context.startService(new Intent(context,DlnaShareFileServer.class));
			dlnasharestart=true;
		}else{
		}
	}
	
	private void tryStartRendererService(Context context){
		boolean netstate=DlnaTools.getNetState(context);
		if(netstate&&!dlnarendererstart){	
			context.startService(new Intent(context,DlnaRendererServer.class));
			dlnarendererstart=true;
		}else{
		}
	}

}
