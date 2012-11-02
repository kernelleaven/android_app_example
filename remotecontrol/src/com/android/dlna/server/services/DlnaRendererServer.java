package com.android.dlna.server.services;

import java.io.UnsupportedEncodingException;

import com.android.dlna.server.DlnaEventListen;
import com.android.dlna.server.serverActivity;
import com.android.dlna.server.misc.DlnaData;
import com.android.dlna.server.misc.DlnaTools;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class DlnaRendererServer extends Service {
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void starteDMRServer(){
		String uuid=DlnaData.creatNewUUID_12Bit(this);
		serverActivity.initRenderer(DlnaData.loadDMRName(this),uuid);
	}
	private void stopDMRServer(){
		serverActivity.stoprenderer();
	}
	public void onCreate() {
		starteDMRServer();
		DlnaEventListen.setcontext(this);
		registerReceiver(msgr, new IntentFilter(DlnaEventListen.DLNA_RENDERER_CMD));
		registerReceiver(msgr, new IntentFilter(SETNEWNAMEDMRSERVICECMD));
		super.onCreate();
	}
	
	public void onDestroy() {
		stopDMRServer();
		DlnaEventListen.releasecontext();
		unregisterReceiver(msgr);
		super.onDestroy();
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}	
	
	private class MSGReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(DlnaEventListen.DLNA_RENDERER_CMD.equals(action)){
				int cmd=intent.getIntExtra(DlnaEventListen.GET_DLNA_RENDERER_CMD, 0);
				switch(cmd){
				case 0:
					break;
				case DlnaEventListen.MEDIA_RENDER_CTL_MSG_SET_AV_URL:
					DlnaData.DlnaMediaInfo mediainfo=null;
					String name=intent.getStringExtra(DlnaEventListen.GET_PARAM_PLAYING_URI);
					String metadata=intent.getStringExtra(DlnaEventListen.GET_PARAM_METADATA);
					if(null != metadata){
						//Toast.makeText(context, "dlna server set uri play : "+name, Toast.LENGTH_LONG).show();
						try {
							mediainfo = DlnaData.getDlnaMediaInfoFromMetaData(metadata);
							mediainfo.uri=name;
							if(null != mediainfo.objectclass){
								if(mediainfo.objectclass.equals(DlnaData.DLNA_OBJECTCLASS_VIDEOID)||mediainfo.objectclass.equals(DlnaData.DLNA_OBJECTCLASS_MOVIEID))
									setPlayingMediaUri(mediainfo,DlnaData.DLNA_OBJECTCLASS_VIDEOID);
								else if(mediainfo.objectclass.equals(DlnaData.DLNA_OBJECTCLASS_PHOTOID)||mediainfo.objectclass.equals(DlnaData.DLNA_OBJECTCLASS_PHOTOID2)){
									setPlayingMediaUri(mediainfo,DlnaData.DLNA_OBJECTCLASS_PHOTOID);
									//Toast.makeText(context, " DLNA_OBJECTCLASS_PHOTOID ", Toast.LENGTH_LONG).show();
								}else if(mediainfo.objectclass.equals(DlnaData.DLNA_OBJECTCLASS_MUSICID)){
									setPlayingMediaUri(mediainfo,DlnaData.DLNA_OBJECTCLASS_MUSICID);
								}
							}
						}catch (Exception e) {
							setPlayingMediaUri(mediainfo,DlnaData.DLNA_OBJECTCLASS_VIDEOID);//手机上装的软件，不全面，发送的metadata为空。
						}
					}
					break;
				case DlnaEventListen.MEDIA_RENDER_CTL_MSG_PLAY:
					//Toast.makeText(context, "play the video", Toast.LENGTH_LONG).show();
					if((null != mediacurrentinfo)&&(null != mediacurrentinfo.uri)){
						if(mediaurichaged)
							startPlayingMedia();
						else
							fromePauseToPlaying();
					}
					break;
				case DlnaEventListen.MEDIA_RENDER_CTL_MSG_STOP:
					//Toast.makeText(context, "stop the video", Toast.LENGTH_LONG).show();
					//stopPlaying();
					delaySendStopCmd();
					break;
				case DlnaEventListen.MEDIA_RENDER_CTL_MSG_PAUSE:
					pausePlaying();
					//Toast.makeText(context, "pause the video", Toast.LENGTH_LONG).show();
					break;
				case DlnaEventListen.MEDIA_RENDER_CTL_MSG_SETMUTE:
					Toast.makeText(context, "set mute ", Toast.LENGTH_LONG).show();
					//setCurrentVolume(0);
					break;
				case DlnaEventListen.MEDIA_RENDER_CTL_MSG_SETVOLUME:
					int volume = intent.getIntExtra(DlnaEventListen.GET_VIDEO_VOLUME, 70);//系统音量70%作为默认值
					if(volume < 101){//the volume value is low than 100
						DlnaTools.setCurrentVolume(volume,DlnaRendererServer.this);
					}
					break;
				case DlnaEventListen.MEDIA_RENDER_CTL_MSG_SEEK:
					String timetype=intent.getStringExtra(DlnaEventListen.GET_SEEK_TIMETYPE);
					String position=intent.getStringExtra(DlnaEventListen.GET_SEEK_POSITION);
					if(DlnaEventListen.MEDIA_SEEK_TIME_TYPE_REL_TIME.equals(timetype)){
						int seekps=DlnaTools.convertSeekRelTimeToMs(position);
						setPlayingSeek(seekps);
					}else{
						Toast.makeText(context, "seek : "+timetype+"="+position, Toast.LENGTH_LONG).show();
					}
					break;
				}
			}else if(SETNEWNAMEDMRSERVICECMD.equals(action)){
				boolean netstate=DlnaTools.getNetState(context);
				if(netstate){
					stopDMRServer();
					new Thread(new Runnable() { 
						public void run() { 
							starteDMRServer();
						}
					}).start();
				}
			}
		}
	}

	/*
	 * 		全局变量
	 * 
	 * */
	public final static String SETNEWNAMEDMRSERVICECMD="com.android.dlna_server.services.SETNEWNAME";
	public static final String RENDERER_MEDIA_CMD_STOP="com.android.dlna_server.services.RENDERER_MEDIA_CMD_STOP"; 
	public static final String RENDERER_MEDIA_CMD_PAUSE="com.android.dlna_server.services.RENDERER_MEDIA_CMD_PAUSE"; 
	public static final String RENDERER_MEDIA_CMD_PLAY="com.android.dlna_server.services.RENDERER_MEDIA_CMD_PLAY"; 
	public static final String RENDERER_MEDIA_CMD_SEEKPS="com.android.dlna_server.services.RENDERER_MEDIA_CMD_SEEKPS"; 
	
	public static final String GET_PARAM_CMD_SEEKPS="get_param_cmd_seekps";
	public static final String GET_METADATA_MEDIAINFO_ALBUM="get_metadata_mediainfo_album";
	public static final String GET_METADATA_MEDIAINFO_ALBUM_URI="get_metadata_mediainfo_album_uri";
	public static final String GET_METADATA_MEDIAINFO_ARTIST="get_metadata_mediainfo_artist";
	public static final String GET_METADATA_MEDIAINFO_TITLE="get_metadata_mediainfo_title";
	private DlnaData.DlnaMediaInfo mediacurrentinfo=null;	//控制点当前传给renderer的媒体信息
	private boolean mediaurichaged=false;			//用来决定是由暂停转播放还是启动新的播放
	private String urimediatype="";
	private String dlnahead="FROMDLNARENDERER";	//用于告诉外部media源
	private String headsplit="/";				//头部分隔符
	private MSGReceiver msgr=new MSGReceiver();
	/*******************************************************************/
	
	/*
	 * 		启动activity对让当前的uri进行视频播放
	 * 
	 * */
	public void startPlayingMedia() { 
		if((null==mediacurrentinfo)||(null == mediacurrentinfo.uri))
			return;
		if(urimediatype.equals(DlnaData.DLNA_OBJECTCLASS_VIDEOID)){
			delayStartVidoPlay();
		}else if(urimediatype.equals(DlnaData.DLNA_OBJECTCLASS_MUSICID)){
			delayStartMusicPlay();
		}else if(urimediatype.equals(DlnaData.DLNA_OBJECTCLASS_PHOTOID)){//对于Gif需要特殊处理
			delayStartPicturePlay();
		}
		mediaurichaged=false;
	}  
	
	private void setPlayingMediaUri(DlnaData.DlnaMediaInfo mediainfo,String mediatype){
		urimediatype=mediatype;
		mediacurrentinfo = mediainfo;
		mediaurichaged=true;
	}
	/*
	 * 		延时启动，防止控制点发送过于密集
	 * */
	private static final int STARTMUSICPLAY = 0;
	private static final int STARTPICPLAY=1;
	private static final int STARTVIDOPLAY=2;
	private static final int SENDPAUSECMD=3;
	private static final int SENDSTOPCMD=4;
	private static final int DELAYTIME=500;
	public DelayHandler mHandler =new DelayHandler();
	private void delayStartMusicPlay(){
		Log.v("timedelay","delay start play");
		clearMsgToPlay();
		mHandler.sendMessageDelayed(mHandler.obtainMessage(STARTMUSICPLAY), DELAYTIME);
	}
	private void delayStartPicturePlay(){
		clearMsgToPlay();
		mHandler.sendMessageDelayed(mHandler.obtainMessage(STARTPICPLAY), DELAYTIME);
	}
	private void delayStartVidoPlay(){
		clearMsgToPlay();
		mHandler.sendMessageDelayed(mHandler.obtainMessage(STARTVIDOPLAY), DELAYTIME);
	}
	private void delaySendStopCmd(){
		clearMsgToPlay();
		mHandler.sendMessageDelayed(mHandler.obtainMessage(SENDSTOPCMD), DELAYTIME);
	}
	private void clearMsgToPlay(){
		clearDelayMsg(SENDSTOPCMD);
		clearDelayMsg(STARTMUSICPLAY);
		clearDelayMsg(STARTPICPLAY);
		clearDelayMsg(STARTVIDOPLAY);
	}
	private void clearDelayMsg(int num){
		mHandler.removeMessages(num);
	}
	class DelayHandler extends Handler { // handler类的实现
		public void handleMessage(Message msg) { // 事件处理函数
			switch (msg.what) {
				case STARTMUSICPLAY:
					Log.v("timedelay","start play");
					startMusicPlay();
					break;
				case STARTPICPLAY:
					startPicturePlay();
					break;
				case STARTVIDOPLAY:
					startVideoPlay();
					break;
				case SENDPAUSECMD:
					pausePlaying();
					break;
				case SENDSTOPCMD:
					stopPlaying();
					break;
			}
		}
	}
	private void startMusicPlay(){
		try{
            Intent intent = new Intent(Intent.ACTION_VIEW);  
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setClassName("com.android.skmusicplayer",
    				"com.android.skmusicplayer.activitys.ExternAudioPlayRequest");
            intent.setDataAndType(Uri.parse(dlnahead+headsplit+mediacurrentinfo.uri), "audio/*");
            intent.putExtra(GET_METADATA_MEDIAINFO_ALBUM, mediacurrentinfo.album);
            intent.putExtra(GET_METADATA_MEDIAINFO_ALBUM_URI, mediacurrentinfo.albumiconuri);
            intent.putExtra(GET_METADATA_MEDIAINFO_ARTIST, mediacurrentinfo.artist);
            intent.putExtra(GET_METADATA_MEDIAINFO_TITLE, mediacurrentinfo.title);
            this.startActivity(intent);  
            }catch (Exception e) {
    		}
	}
	private void startPicturePlay(){
		try {
			Intent intent;
			intent = new Intent();  
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
	        intent.setAction(Intent.ACTION_VIEW);  
	        intent.setDataAndType(Uri.parse(mediacurrentinfo.uri),"image/*");
	        startActivity(intent);	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	private void startVideoPlay(){
//		Intent in=new Intent();
//		in.setClass(DlnaRendererServer.this,RendererPlay.class);
//		Bundle bd=new Bundle();
//		bd.putString("uripath",mediacurrentinfo.uri);
//		in.putExtras(bd);
//		in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(in);
	}
	
	/*
	 * 		暂停播放
	 * 
	 * */
	private void pausePlaying(){
		Intent stopmadia=new Intent(RENDERER_MEDIA_CMD_PAUSE);
        sendBroadcast(stopmadia);
	}
	/*
	 * 		停止播放
	 * 
	 * */
	private void stopPlaying(){
		Intent stopmadia=new Intent(RENDERER_MEDIA_CMD_STOP);
        sendBroadcast(stopmadia);
	}
	/*
	 * 		由暂停转到播放
	 * 
	 * */
	private void fromePauseToPlaying(){
		Intent stopmadia=new Intent(RENDERER_MEDIA_CMD_PLAY);
        sendBroadcast(stopmadia);
	}
	/*
	 * 		指定播放位置
	 * 
	 * */
	private void setPlayingSeek(int seekps){
		Intent seeki=new Intent(RENDERER_MEDIA_CMD_SEEKPS);
		seeki.putExtra(GET_PARAM_CMD_SEEKPS, seekps);
        sendBroadcast(seeki);
	}
}
