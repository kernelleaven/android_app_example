package com.iflytek.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
 
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.util.Log;

public class AndroidUDPClient extends Activity {
	private final String TAG="udp";
	
	MulticastLock multicastLock; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        allowMulticast();
        Log.v(TAG,"allow 3 ");
        try {
			NetUtil.findServerIpAddress();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        stopMulticast();
        Log.v(TAG,"stop ");
      //  Intent in=new Intent(this, Receive.class);
      //  startService(in);
    }
    

    
    @Override
    protected void onPause() {
    	
    	super.onPause();
    }
    
    private void allowMulticast(){
        WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        multicastLock=wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();
    } 
    
    private void stopMulticast(){
    	multicastLock.release();
    }



}