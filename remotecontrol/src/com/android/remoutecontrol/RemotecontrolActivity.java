package com.android.remoutecontrol;

import com.android.dlna.server.misc.DlnaSoInterface;
import com.android.dlna.server.misc.DlnaSoInterface.RCCServiceToken;
import com.android.dlna.server.misc.DlnaSoInterface.RCSServiceToken;
import com.android.dlna.server.misc.DlnaSoInterface.ServiceToken;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class RemotecontrolActivity extends Activity {
	private final boolean DEBUG=true;
	private final String TAG="dlna";
	private RCCServiceToken rcctoken;
	private RCSServiceToken rcstoken;
	private ServiceToken dmctoken;
	
	private boolean PAD=true;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //PAD=false;
        if(PAD)
        	rcctoken = DlnaSoInterface.bindToRCCService(this);
        else
        	rcstoken = DlnaSoInterface.bindToRCSService(this);
        // dmctoken = DlnaSoInterface.bindToService(this);
        if(DEBUG)Log.v(TAG,"oncreate");
    }
    
    @Override
    protected void onPause() {
    	if(DEBUG)Log.v(TAG,"onPause");
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	if(DEBUG)Log.v(TAG,"onDestroy");
    	if(PAD)
    		DlnaSoInterface.unbindFromRCCService(rcctoken);
    	else
    		DlnaSoInterface.unbindFromRCSService(rcstoken);
    	//DlnaSoInterface.unbindFromService(dmctoken);
    	super.onDestroy();
    }
}