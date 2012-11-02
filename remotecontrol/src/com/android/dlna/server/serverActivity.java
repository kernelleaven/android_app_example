package com.android.dlna.server;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.android.dlna.server.misc.DlnaData;


public class serverActivity {
    static {
        System.loadLibrary("PlatinumJNI");
     }
    private static byte[] getBytesFromStr(String str){
    	if(null == str)
    		str="";
    	try {
    		return str.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
    }
    /******************************************************/
    //					DMS
    /******************************************************/
    private static native void initserver(byte[] path,byte[] name,int port,byte[] uid);
    public static void initServer(String path,String name,int port,String uid){
    	byte[] parampath=getBytesFromStr(path);
    	byte[] paramname=getBytesFromStr(name);
    	byte[] paramuid=getBytesFromStr(uid);
    	if((null != parampath)&&(null != paramname)&&(null != paramuid))
    		initserver(parampath,paramname,port,paramuid);
    }
    public static native void stopserver();
    
    /******************************************************/
    //					DMR
    /******************************************************/
    private static native void initrenderer(byte[] name ,byte[] uid);
    public static void initRenderer(String name ,String uid){
    	byte[] paramname=getBytesFromStr(name);
    	byte[] paramuid=getBytesFromStr(uid);
    	if((null != paramname)&&(null != paramuid))
    		initrenderer(paramname,paramuid);
    }
    public static native void stoprenderer();
    
    /******************************************************/
    //					DMC
    /******************************************************/
    public static native void initMediaControl();
    public static native void stopMediaControl();
    public static native void setMediaVolume(int volume);
    public static native void tryGetPositionInfo();
    public static native void tryGetMediaInfo();
    public static native void tryGetVolume();
    
    /*
     *		Map<String,String> = Map<UUID,FrendlyName>
     */
    private static native String getAllMediaRendererr();
    public static Map<String,String> getAllDMRUUIDAndNameMap(){
    	 String SPLIT="\n\r";
    	 Map<String,String> dmrmap=new HashMap<String,String>();
         String dmrs=getAllMediaRendererr();
		if(dmrs.length()>1){
			String[] dmr=dmrs.split(SPLIT);
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
    public static String getAllDMRUUIDAndNameStrings(){
    	String dmrs=getAllMediaRendererr();
    	return dmrs;
	}
     
    //
    private static native void setCmdToControlPointByRenderer(int cmd,byte[] value ,byte[] data);
    public static void setCmdToControlPointByRendererCall(int cmd ,String value,String data){
    	if(null == value){
    		value="";
    	}
    	if(null == data){
    		data="";
    	}
    	byte[] valueparam=getBytesFromStr(value);
    	byte[] dataparam=getBytesFromStr(data);
    	if((null != valueparam)&&(null != dataparam))
    		setCmdToControlPointByRenderer(cmd,valueparam,dataparam);
    }
    
    //
    private static native void setPlayState(byte[] state);
    public static void setCmdToCtlPlayState(String state){
    	byte[] stateparam=getBytesFromStr(state);
    	if(null != stateparam)
			setPlayState(stateparam);
    }
    //
    private static native void setMuteState(byte[] state);
    public static void setCmdToCtlMuteState(String state){
    	byte[] stateparam=getBytesFromStr(state);
    	if(null != stateparam)
    		setMuteState(stateparam);
    }
    //
    private static native void setPlaySeek(byte[] state);
    public static void setCmdToCtlSetSeekTime(String reltime){
    	byte[] param=getBytesFromStr(reltime);
    	if(null != param){
    		setPlaySeek(param);
    	}
    }
    //
  //public static native String setPlayingRenderer(byte[] uuid);
    private static native String setPlayingRenderer(byte[] uuid,int displaynum);
    public static int setCurrentPlayRenderer(String uuid){
    	byte[] paramuuid=getBytesFromStr(uuid);
    	if(null == paramuuid)
    		return 0;
    	
    	int checknum=DlnaData.getRandomBetween(200, 500);
		if(setPlayingRenderer(paramuuid,checknum).equals(DlnaData.DLNAJNIRETFAIL))
				return 0;
		return checknum;
    }
    //
    private static native String tryGetDescriptionUrl(byte[] uuid);
    public static String getDescriptionUrlByDMRUUID(String uuid){
    	byte[] param=getBytesFromStr(uuid);
    	if(null != param){
    		return tryGetDescriptionUrl(param);
    	}else{
    		return null;
    	}
    }
    public static String getDMRIpByUUID(String uuid){
    	return DlnaData.getIPFromUrl(getDescriptionUrlByDMRUUID(uuid));
    }
    //
    private static native String setAVTransportURI(byte[] url,byte[] didl); 
    public static String setDMRAVTransportURI(String url,String didl){
    	String ret=DlnaData.DLNAJNIRETFAIL;
    	byte[] paramurl=getBytesFromStr(url);
    	byte[] paramdidl=getBytesFromStr(didl);
    	if((null != paramurl)&&(null != paramdidl)){
    		ret = setAVTransportURI(paramurl,paramdidl);
    	}
    	return ret;
    }
    /******************************************************/
    //					OTHER
    /******************************************************/
    /**/
    private static native void pushTsStreamToPad(byte[] srcts,byte[] outfifo);
    public static void callServerDlnaTsStream(String srcts,String outfifo){
    	byte[] paramsrcts=getBytesFromStr(srcts);
    	byte[] paramoutfifo=getBytesFromStr(outfifo);
    	if((null != paramsrcts)&&(null != paramsrcts))
    		pushTsStreamToPad(paramsrcts,paramoutfifo);
    }
    /**/
    private static native void createFIFOFile(byte[] path);
    public static void callSystemCreateFIFOFile(String path){
    	byte[] param=getBytesFromStr(path);
    	if(null != param)
    		createFIFOFile(param);
    }
    
    /******************************************************/
    //					Be Control Client
    /******************************************************/
    public static native void initRemouteControler();
    public static native void stopRemouteControler();
    public static native String getAllRCS();
    
    
    private static native String setCurrentRCSByUUID(byte[] uuid,int num);
    public static void setCurrentRCSByUUID(String uuid){
    	if(null == uuid)
    		return;
    	try {
    		setCurrentRCSByUUID(uuid.getBytes("utf-8"),DlnaData.getRandomBetween(200, 500));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    
    /******************************************************/
    //				Remoute Control Server
    /******************************************************/
    private static native void initrmoutecontrolserver(byte[] name ,byte[] uid);
    public static void initRemouteControlServer(String name,String uuid){
    	if((null == name)||(null == uuid))
    		return ;
    	try {
    		serverActivity.initrmoutecontrolserver(name.getBytes("utf-8"),uuid.getBytes("utf-8"));
		} catch (Exception e) {
		}
    }
    public static native void stoprmoutecontrolserver();
}
