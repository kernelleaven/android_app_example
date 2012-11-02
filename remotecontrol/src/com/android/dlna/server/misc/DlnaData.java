package com.android.dlna.server.misc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class DlnaData {
	public static final String DLNAJNIRETSUC="true"; 
	public static final String DLNAJNIRETFAIL="false"; 
	
	public static boolean isstartshare;
	
	public static final String ROOTSHARE_FOLDERNAME="dlnashareroot";
	public static final String AUDIOSHARE_FOLDERNAME="Audio";
	public static final String PHOTOSHARE_FOLDERNAME="Photos";
	public static final String VIDEOSHARE_FOLDERNAME="Video";
	public static final String DIRECTTVSHARE_FOLDERNAME="DirectTV";
	public static final String PVRSHARE_FOLDERNAME="Pvrrec";
	
	public static final String PRE_SHARESTATE="pre_sharestate";
	public static final String PRE_DEVICESCONTROLSTATE="pre_devicescontrolstate";
	
	public static final String DEFAULTSERVERNAME="创维数字家庭共享文件服务器PAD";//"ICAN DLNA FILE SHARE";
	public static final String DEFAULTDMRNAME="创维数字家庭共享播放器";
	
	public static final String loadFileServerName(Context mc){
		String name=DlnaTools.loadStringFromSysPre("dlna_file_server_name", DEFAULTSERVERNAME, mc);
		if((null == name)||(name.equals("")))
			return DEFAULTSERVERNAME;
		return name;
	}
	public static final void saveFileServerName(String name,Context mc){
		DlnaTools.saveStringToSysPre("dlna_file_server_name", name, mc);
	}
	public static final String loadFileServerUUID(Context mc){
		String name=DlnaTools.loadStringFromSysPre("dlna_file_server_uuid", "000000000", mc);
		if((null == name)||(name.equals("")))
			return "000000000";
		return name;
	}
	private static void saveLocalFileServerUUID(String uuid,Context mc){
		DlnaTools.saveStringToSysPre("dlna_file_server_uuid", uuid, mc);
	}
	public static final String loadDMRName(Context mc){
		return loadFileServerName(mc);
		/*String name=Tools.loadStringFromSysPre("dlna_dmr_name", DEFAULTDMRNAME, mc);
		if((null == name)||(name.equals("")))
			return DEFAULTDMRNAME;
		return name;*/
	}
	public static final void saveDMRName(String name,Context mc){
		saveFileServerName(name,mc);
		//Tools.saveStringToSysPre("dlna_dmr_name", name, mc);
	}
	
	public static boolean isTrue(int value){
		if(0==value)
			return false;
		else 
			return true;
	}
	public static boolean isTrue(String value){
		if((null == value)||("".equals(value)))
			return false;
		try {
			int valueint=Integer.parseInt(value);
			return isTrue(valueint);
		} catch (Exception e) {
			return false;
		}
	}
	public static int getTrue(){return 1;}
	public static int getFalse(){return 0;}
	
	/*
	 * 		
	 * 
	 * */
	public final static String DLNA_OBJECTCLASS_MUSICID="object.item.audioItem.musicTrack";
	public final static String DLNA_OBJECTCLASS_VIDEOID="object.item.videoItem";
	public final static String DLNA_OBJECTCLASS_MOVIEID="object.item.videoItem.movie";
	public final static String DLNA_OBJECTCLASS_PHOTOID="object.item.imageItem.photo";
	public final static String DLNA_OBJECTCLASS_PHOTOID2="object.item.imageItem";
	
	/*
	 * 
	 * 
	 * */
    //
	private static Map<String,String> hashmapfiletabe=null;
	public static void initMapLnAndPath(){
		hashmapfiletabe=new HashMap<String,String>();
	}
	public static void mapIntoHashTable(String lnpathkey,String path){
		if(hashmapfiletabe.containsKey(lnpathkey))
			umapHashTable(lnpathkey);
		hashmapfiletabe.put(lnpathkey, path);
	}
	public static String getMapHashFilePath(String lnpathkey){
		if(hashmapfiletabe.containsKey(lnpathkey))
			return hashmapfiletabe.get(lnpathkey);
		return null;
	}
	public static void umapHashTable(String lnpathkey){
		if(hashmapfiletabe.containsKey(lnpathkey))
			hashmapfiletabe.remove(lnpathkey);
	}
	public static void cleanMapLnFile(File file){
		if(null != file){
			umapHashTable(file.getPath());
			file.delete();
		}
	}
	public static class DlnaMediaInfo{
		public String uri=null;
		public String title="";				
		public String artist="";
		public String album="";
		public String albumiconuri="";
		public String objectclass="";
	}
	public static DlnaMediaInfo getDlnaMediaInfoFromMetaData(String metadata){
		DlnaMediaInfo mediainfo=new DlnaMediaInfo();
		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = dfactory.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(metadata.getBytes("UTF-8"));//("GB2312"));//("UTF-8"));
			Document doc = documentBuilder.parse(is);
			mediainfo.objectclass=getElementValue(doc,"upnp:class");
			mediainfo.title=getElementValue(doc,"dc:title");
			mediainfo.album=getElementValue(doc,"upnp:album");
			mediainfo.artist=getElementValue(doc,"upnp:actor"/*"dc:creator"*/);
			mediainfo.albumiconuri=getElementValue(doc,"upnp:albumArtURI");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return mediainfo;
	}
	private static String getElementValue(Document doc , String element){
		NodeList containers = doc.getElementsByTagName(element);
		for (int j = 0; j < containers.getLength(); ++j) {//文件夹类型
			Node container = containers.item(j);
			NodeList childNodes = container.getChildNodes();
			if(childNodes.getLength()!=0){
				Node childNode = childNodes.item(0);
				return childNode.getNodeValue();
			}
		}
		return "";
	}
	//////////
	public static String creatNewUUID_12Bit(Context mc){
		String mac=DlnaData.getLocalMacAddress(mc);
		mac=mac.replace(":","");
		String localip=getLocalIpAddress();
		localip= localip.replace(".", "");
		
		String ret=localip.subSequence(localip.length()-4,localip.length())+""+mac.subSequence(4, mac.length());
		Log.v("testdlna","dlnadata uuid: "+ret);
		saveLocalFileServerUUID(ret,mc); //TODO:----创建了不一定代表使用了
		return ret;
	}
	public static String getLocalIpAddress() {  
		String ip="0.0.0.0";
        try {  
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {  
                NetworkInterface intf = en.nextElement();  
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {  
                    InetAddress inetAddress = enumIpAddr.nextElement();  
                    if (!inetAddress.isLoopbackAddress()) {  
                    	ip=inetAddress.getHostAddress().toString();  
                    	Log.v("testdlna","dlnadata ip :"+ip);
                        return ip;
                    }  
                }  
            }  
        } catch (SocketException ex) {  
        }  
        return ip;  
    } 
	public static String getWifiMacAddress(Context mc) {   
        WifiManager wifi = (WifiManager) mc.getSystemService(Context.WIFI_SERVICE);   
        WifiInfo info = wifi.getConnectionInfo();   
        return info.getMacAddress();   
    }  
	public static String getLocalMacAddress(Context mc){
		String defmac="00:00:00:00:00:00";
		InputStream   input   =   null;
		String wifimac=DlnaData.getWifiMacAddress(mc);
		if(null != wifimac){
			if(!wifimac.equals(defmac))
				return wifimac;
		}
		try{
			//执行命令
			ProcessBuilder builder = new ProcessBuilder( "busybox","ifconfig");
			Process process = builder.start();
			input = process.getInputStream();
			
			//Process process = Runtime.getRuntime().exec("busybox ifconfig");
			//int status = process.waitFor();
			//if(0 != status)
			//	return defmac;
			//input = process.getInputStream();

			//把得到的流得到
			byte[] b = new byte[1024];
			StringBuffer buffer = new StringBuffer();
			while(input.read(b)>0){
				buffer.append(new String(b));
			}
			String value = buffer.substring(0);
			String systemFlag ="HWaddr ";
			int index = value.indexOf(systemFlag);
			//List <String> address   = new ArrayList <String> ();
			if(0<index){
				value = buffer.substring(index+systemFlag.length());
				//address.add(value.substring(0,18));
				defmac=value.substring(0,17);
			}
		}catch (Exception e) {
		}
		return defmac;
	}
	
	/*
	 * 		
	 * */
	private static Random randomnum = null;//随机数
	//min 和 max 都是正数
	public static int getRandomBetween(int min,int max){
		int tmp;
		if(null == randomnum){
			randomnum=new Random();
		}
		if(min==max)
			return min;
		tmp = randomnum.nextInt();
		if(tmp<0)
			tmp=-tmp;
		return (tmp%(max-min)+min);
	}
	/*
	 * 		url 范例：http://192.168.1.101:56318/
	 * */
	public static String getIPFromUrl(String url){
		if((null == url)||(url.length()<"http://0.0.0.0:".length()))
			return null;
		String retip=url.substring("http://".length());
		int ipend=retip.indexOf(":");
		retip=retip.substring(0,ipend);
		return retip;
	}
	/*
	 * 		从字符串中获取键值DMR对
	 * */
	 public static Map<String,String> getAllDMRUUIDAndNameMapFromStrings(String dmrsstr){
		 if(null == dmrsstr)
			 return null;
	   	 String SPLIT="\n\r";
	   	 Map<String,String> dmrmap=new HashMap<String,String>();
	   	 if(dmrsstr.length()>1){
	   		 String[] dmr=dmrsstr.split(SPLIT);
	   		 if(dmr.length%2==0){
	   			 int nums=dmr.length/2;
	   			 for(int i=0;i<nums;i++){
	   				 dmrmap.put(dmr[i*2],dmr[i*2+1]);
	   			 }
	   			 return dmrmap;
	   		 }else
	   			 return null;
	   	 }else{
	   		 return null;
	   	 }
	 }
}
