package com.iflytek.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class Receive extends Service implements Runnable{
	private final String TAG="udp";
	
	InetAddress group;
	//InetSocketAddress group;
	MulticastSocket socket=null;
	Thread rethread;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		Log.v(TAG,"SERVER IS START");
		try {
			socket = new MulticastSocket(1990);
			group = InetAddress.getByName("230.0.0.0");//("239.255.255.250"); 
			socket.joinGroup(group);
			//Log.v(TAG,"InetAddress.getLocalHost() : "+InetAddress.getLocalHost().getHostAddress());
			//Log.v(TAG,"getLocalIP : "+getLocalIP());
			//socket.setNetworkInterface(NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.1.105")));
			//ssdpMultiIf = NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.1.101"));
			//group = new InetSocketAddress(InetAddress.getByName("239.255.255.250"), 1900);
			//socket.joinGroup(group, ssdpMultiIf);	
		} catch (IOException e) {
			Log.v(TAG,"INIT ERR :"+e.getMessage());
		}
		
		rethread = new Thread(this, "udpmurec");
		rethread.run();
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
    	if(null != socket){
			try {
				socket.leaveGroup(group);
				//socket.leaveGroup(group, ssdpMultiIf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			socket.close();
		}
		super.onDestroy();
	}

	@Override
	public void run() {
		while(true){
			DatagramPacket packet;
		    byte[] buf = new byte[1024];
		    Arrays.fill(buf,(byte)0);
		    packet = new DatagramPacket(buf, buf.length);
		    try {
		    	Receive.this.socket.receive(packet);
			} catch (IOException e) {
				Log.v(TAG,"rec err : "+e.getMessage());
				continue;
			}
		    String received = new String(packet.getData());
		    Log.v(TAG,"----------------------------------------------");
		    Log.v(TAG,"getHostName:" + packet.getAddress().getHostName()+" getHostAddress: "+packet.getAddress().getHostAddress());
		    Log.v(TAG,"getAddress().toString:" + packet.getAddress().toString());
		    Log.v(TAG,"getSocketAddress().toString:" + packet.getSocketAddress().toString());
		    Log.v(TAG,"Port:" + packet.getPort());
		    Log.v(TAG,"getSocketAddress:"+packet.getSocketAddress());
		    Log.v(TAG,"Quote of the Moment: " + received.substring(0, 100));
		}
		
	}
}
