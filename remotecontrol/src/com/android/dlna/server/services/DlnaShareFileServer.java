package com.android.dlna.server.services;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import com.android.dlna.server.serverActivity;
import com.android.dlna.server.misc.DlnaData;
import com.android.dlna.server.misc.DlnaTools;
import com.android.dlna.server.virtualdirserver.VirtualFileSimple;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.MediaStore;

public class DlnaShareFileServer extends Service {
	public final static String SETNEWNAMESHAREDFILE="dlna_server.DlnaShareFileServer.SETNEWNAME";
	public final static String CLEANEMPTYSHAREDFILE="dlna_server.DlnaShareFileServer.CLEANEMPTYSHAREDFILE";
	public final static String STARTSHARESERVICE="dlna_server.DlnaShareFileServer.STARTSHARESERVICE";
	public final static String SETSHARESTATE="dlna_server.DlnaShareFileServer.SETSHARESTATE";
	public final static String SHARESTATE_PARAM="sharestate_param";
	public final static String SHAREFILES_FRESH="dlna_server.DlnaShareFileServer.SHAREFILES_FRESH";
	//public final static String GETNEWNAME="getnewname";
	
	String sharerootpath=null,audiopath=null,photopath=null,videopath=null;
	ArrayList<VirtualFileSimple> audiovirfloder=null,photovirfloder=null,videovirfloder=null;
	private boolean sharestate;
	
	private MsgRcv rcv=new MsgRcv();
	private HashMap<String, String> pathmap=new HashMap<String, String>();
	private static boolean hasstarted=false;
	
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		DlnaData.initMapLnAndPath();
		initSharePath();
		sharestate=DlnaTools.loadBoolFromSysPre(DlnaData.PRE_SHARESTATE, true, this);
		setShareAction();
		registerReceiver(rcv, new IntentFilter(SETSHARESTATE));
		registerReceiver(rcv, new IntentFilter(CLEANEMPTYSHAREDFILE));
		registerReceiver(rcv, new IntentFilter(SETNEWNAMESHAREDFILE));
		MediaInformLoaderThread.run();
		super.onCreate();
	}
	
	public void onDestroy() {
		unregisterReceiver(rcv);
		super.onDestroy();
	}
	
    private void initSharePath(){
    	//sharerootpath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Data.ROOTSHARE_FOLDERNAME;
    	sharerootpath=getFilesDir().getAbsolutePath()+"/"+DlnaData.ROOTSHARE_FOLDERNAME;
    	File rootfile=new File(sharerootpath);
    	if(!rootfile.exists()){
    		if(!rootfile.mkdir()){
    			sharerootpath=null;
    			return;
    		}
    	}
    	audiopath=sharerootpath+"/"+DlnaData.AUDIOSHARE_FOLDERNAME;
    	photopath=sharerootpath+"/"+DlnaData.PHOTOSHARE_FOLDERNAME;
    	videopath=sharerootpath+"/"+DlnaData.VIDEOSHARE_FOLDERNAME;
    	String[] paths={audiopath,photopath,videopath};
    	for(int i=0;i<paths.length;i++){
    		File dir=new File(paths[i]);
        	if(!dir.exists()){
        		dir.mkdir();
        	}else{
        		cleanDirsLnFileAndDir(paths[i]);
        		dir.delete();
        		dir.mkdir();
        	}
    	}
    	audiovirfloder=new ArrayList<VirtualFileSimple>();
    	photovirfloder=new ArrayList<VirtualFileSimple>();
    	videovirfloder=new ArrayList<VirtualFileSimple>();
    }
    
    private void mapDigitMedias(){
    	if(null == sharerootpath)
    		return;
    	int i;
    	//映射音乐
     	Cursor c = getContentResolver().query(
 	            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,  
 	            MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
     	if(null != c){
     		int _name_index = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
     		int _dir_index = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
     		int _artist_index= c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
     		int _album_index=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
     		int _albumid_index=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
     		if (c.moveToFirst()) {  
         		do {  // 通过Cursor 获取路径
         			String srcpath=c.getString(_dir_index);
         			String name=c.getString(_name_index);
         			String artist=c.getString(_artist_index);
         			String album=c.getString(_album_index);
         			String albumid=c.getString(_albumid_index);
         			if(!pathmap.containsKey(srcpath)){
         				mapAudio(srcpath,name);
         			}
         			VirtualFileSimple tmpfile=new VirtualFileSimple(srcpath, name, artist, album, albumid, VirtualFileSimple.FILETYPE_AUDIOID);
         			audiovirfloder.add(tmpfile);
         		} while (c.moveToNext());  
         	}  
     		c.close();
     	}
     	//映射视频
     	c = getContentResolver().query(
 	            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,  
 	            MediaStore.Video.Media.DEFAULT_SORT_ORDER);
     	if(null !=c){
     		int _name_index = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
     		int _dir_index = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
     		int _artist_index= c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
     		int _album_index=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
     		if (c.moveToFirst()) {  
         		do {  // 通过Cursor 获取路径
         			String srcpath=c.getString(_dir_index);
         			String name=c.getString(_name_index);
         			String artist=c.getString(_artist_index);
         			String album=c.getString(_album_index);
         			if(!pathmap.containsKey(srcpath)){
         				mapVideo(srcpath,name);
         			}
         			VirtualFileSimple tmpfile=new VirtualFileSimple(srcpath, name, artist, album, "", VirtualFileSimple.FILETYPE_VIDEOID);
         			videovirfloder.add(tmpfile);
         		} while (c.moveToNext());  
         	}  
     		c.close();
     	}
     	//映射图片
     	c = getContentResolver().query(
 	            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,  
 	            MediaStore.Images.Media.DEFAULT_SORT_ORDER);
     	if(null !=c){
     		int _name_index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
     		int _dir_index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
     		if (c.moveToFirst()) {  
         		do {  // 通过Cursor 获取路径
         			String srcpath=c.getString(_dir_index);
         			String name=c.getString(_name_index);
         			if(!pathmap.containsKey(srcpath)){
         				mapPhoto(srcpath,name);
         			}
         			VirtualFileSimple tmpfile=new VirtualFileSimple(srcpath, name,"","","", VirtualFileSimple.FILETYPE_IMAGEID);
         			photovirfloder.add(tmpfile);
         		} while (c.moveToNext());  
         	}  
     		c.close();
     	}
    }
    
    class MsgRcv extends BroadcastReceiver{

		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(SETSHARESTATE.equals(action)){
				boolean setsharestate=intent.getBooleanExtra(SHARESTATE_PARAM, sharestate);
				if(sharestate!=setsharestate){
					sharestate=setsharestate;
					setShareAction();
				}
			}else if(CLEANEMPTYSHAREDFILE.equals(action)){
				cleanEmptyFile();
			}else if(SETNEWNAMESHAREDFILE.equals(action)){
				if(sharestate){
					sharestate=false;
					setShareAction();
					new Thread(new Runnable() { 
						public void run() { 
							DlnaShareFileServer.this.sharestate=true;
							setShareAction();
						}
					}).start();
				}
			}
		}
    }
    
    Thread MediaInformLoaderThread=new Thread(new Runnable() {
        public void run() {
        	mapDigitMedias();
        }
    });
    
    private void cleanEmptyFile(){//当u盘被拔出后要清理无用的连接
    	String[] shareroots=new String[3];
        String sharerootpath=getFilesDir().getAbsolutePath()+"/"+DlnaData.ROOTSHARE_FOLDERNAME;
        shareroots[0]=sharerootpath+"/"+DlnaData.AUDIOSHARE_FOLDERNAME;
        shareroots[1]=sharerootpath+"/"+DlnaData.PHOTOSHARE_FOLDERNAME;
        shareroots[2]=sharerootpath+"/"+DlnaData.VIDEOSHARE_FOLDERNAME;
        for(int i=0 ;i<3;i++){
        	cleanDirsSizeIsZeroFile(shareroots[i]);
        }
        Intent fresh=new Intent(SHAREFILES_FRESH);
        sendBroadcast(fresh);
    }
    private void cleanDirsSizeIsZeroFile(String dir){
    	if(null == dir)
    		return;
    	File dirfile=new File(dir);
    	if((null == dirfile)||(!dirfile.exists())){
    		return;
    	}
    	File[] files=dirfile.listFiles();
    	if(null == files)
    		return ;
    	for(int i =0;i<files.length;i++){
    		if(!files[i].isDirectory()){
    			if(files[i].length()==0){
    				DlnaData.cleanMapLnFile(files[i]);
    			}
    		}
    	}
    }
    private void cleanDirsLnFileAndDir(String dir){
    	if(null == dir)
    		return;
    	File dirfile=new File(dir);
    	if((null == dirfile)||(!dirfile.exists())){
    		return;
    	}
    	File[] files=dirfile.listFiles();
    	if(null == files)
    		return ;
    	for(int i =0;i<files.length;i++){
    		DlnaData.cleanMapLnFile(files[i]);
    	}
    }
    
    private void mapVideo(String src,String name){
    	mapMediaFromPathToSharePath(src,videopath+"/",name);
    }
    private void mapAudio(String src,String name){
    	mapMediaFromPathToSharePath(src,audiopath+"/",name);
    }
    private void mapPhoto(String src,String name){
    	mapMediaFromPathToSharePath(src,photopath+"/",name);
    }
    
    private boolean mapMediaFromPathToSharePath(String src,String tar){
    	return softLinkMode(src, tar);
    }
    private void mapMediaFromPathToSharePath(String src,String tar,String tarname){
    	DlnaData.mapIntoHashTable(tar+tarname, src);
    	if(softLinkMode(src, tar+tarname))
    		return;
    	/*for(int i=0;i<10;i++){
    		if(softLinkMode(src, tar+i+"_"+tarname))
        		return;
    	}*/
    }
	private boolean softLinkMode(String srcpath,String tarpath){
		Process p;
		int status;
		try {
			p = Runtime.getRuntime().exec("ln -s " +srcpath+" "+ tarpath );
			status = p.waitFor();
			if (status == 0) {
				return true;//success
			} else {
				return false;
			}
		}catch (Exception e) {
			return false;
		}
	}
    
    private void setShareAction(){
    	if(null==sharerootpath)
			return;
    	if(sharestate){
    		if(hasstarted)
    			return;
    		hasstarted=true;
			String servername=DlnaData.loadFileServerName(this);
			String uuid=DlnaData.creatNewUUID_12Bit(this);
			serverActivity.initServer(sharerootpath,servername,0,uuid);
			
		}else{
			if(!hasstarted)
				return;
			hasstarted=false;
			serverActivity.stopserver();
		}
    }
}
