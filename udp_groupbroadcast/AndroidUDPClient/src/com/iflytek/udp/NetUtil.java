package com.iflytek.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.util.Log;

public class NetUtil {        
        private static final String TAG="udp";   
        private static final int MULTICAST_PORT=1990;//5111;   
        private static final String GROUP_IP="233.0.0.0";//"224.5.0.7";   
        private static byte[] sendData;   
          
        static{   
            sendData=new byte[4];   
            // 0xEE78F1FB   
            sendData[3] = (byte) 0xEE;   
            sendData[2] = (byte) 0x78;   
            sendData[1] = (byte) 0xF1;   
            sendData[0] = (byte) 0xFB;   
        }   
          
        public static String findServerIpAddress() throws IOException{   
            String ip=null;  
      
            MulticastSocket multicastSocket=new MulticastSocket(MULTICAST_PORT);   
            multicastSocket.setLoopbackMode(true);   
            InetAddress group = InetAddress.getByName(GROUP_IP);   
            multicastSocket.joinGroup(group);   
              
            DatagramPacket packet=new DatagramPacket(sendData, sendData.length,group,MULTICAST_PORT);   
              
            for(;;){   
                multicastSocket.send(packet);   
                Log.d(TAG,">>>send packet ok");   
                  
                byte[] receiveData=new byte[1024];   
                packet=new DatagramPacket(receiveData, receiveData.length,group,MULTICAST_PORT);  
                Log.v(TAG,"GO TOrec");
                multicastSocket.receive(packet);   
                Log.v(TAG,"HAVE rec");
                  
                String packetIpAddress=packet.getAddress().toString();   
                packetIpAddress=packetIpAddress.substring(1, packetIpAddress.length());   
                Log.d(TAG,"packet ip address: "+packetIpAddress);   
                  
                StringBuilder packetContent=new StringBuilder();   
                for(int i=0;i<receiveData.length;i++){   
                    if(receiveData[i]==0){   
                        break;   
                    }   
                    packetContent.append((char)receiveData[i]);   
                }   
                ip=packetContent.toString();   
                Log.d(TAG,"packet content ip is: "+ip);   
                  
                if(ip.equals(packetIpAddress)){   
                    Log.d(TAG,"find server ip address: "+ip);   
                    break;   
                }else{   
                    Log.d(TAG,"not find server ip address, continue бн");   
                    try {   
                        Thread.sleep(1000);   
                    } catch (InterruptedException e) {   
                    }   
                }   
            }   
              
            return ip;   
        }   
}
