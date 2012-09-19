package com.iflytek.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;

public class AndroidUDPServer extends Activity {
    /** Called when the activity is first created. */
	
	private Button button;

	  private NetworkInfo mWifiInfo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        button = (Button) findViewById(R.id.button1);
        
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//new Discoverer((WifiManager) getSystemService(Context.WIFI_SERVICE))
		        //.start();
				startUPDServer();
			}
		});
       
    }
    private void startUPDServer(){
    	
	      DatagramSocket socket = null;
	      String destAddressStr = "233.0.0.0";//"239.255.255.250"; 
		  try {
			  	/*InetAddress address = InetAddress.getByName("192.168.1.105");			  
			    socket = new DatagramSocket(1900, address);
	            byte[] buf = new byte[256];
	            // don't wait for request...just send a quote

	            String dString = new Date().toString();
	            buf = dString.getBytes();
	            InetAddress group = InetAddress.getByName("239.255.255.250");
	            DatagramPacket packet;
	            packet = new DatagramPacket(buf, buf.length, group, 1900);
				socket.send(packet);
				socket.close();   */
              InetAddress destAddress = InetAddress.getByName(destAddressStr);  
              if(!destAddress.isMulticastAddress()){//检测该地址是否是多播地址  
            	  Log.v("udp",destAddressStr+":is not multicast addr");  
              }else{
            	  Log.v("udp",destAddressStr+":is multicast addr");  
              }
              int destPort = 1990;  
              int TTL = 1;  
              MulticastSocket multiSocket =new MulticastSocket();  
              multiSocket.setTimeToLive(TTL);  
              byte[] sendMSG = "---------------fsdfsdfsdf".getBytes();  
              DatagramPacket dp = new DatagramPacket(sendMSG, sendMSG.length, destAddress  , destPort);  
              multiSocket.send(dp);  
              multiSocket.close();  
	        } catch (IOException e) {
	            e.printStackTrace();
	        } 	
    }
}