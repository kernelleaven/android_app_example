package com.android.dlna.server;

import com.android.dlna.server.misc.DlnaData;
import com.android.dlna.server.misc.DlnaTools;
import com.android.dlna.server.services.DlnaRendererServer;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class DlnaEventListen {
	private static Context mc=null;
	private static final String TAG="dlna";
	private static final boolean DEBUG=true;
	
	/////////////////////////////////////
    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDSTOP = "stop";
	/////////////////////////////////////	
	private static final int MEDIA_RENDER_CTL_MSG_BASE = 0x100;
	private static final int MEDIA_RENDER_CTL_MSG_SIZE = 50;
	private static final int MEDIA_SHARE_CTL_MSG_BASE = (MEDIA_RENDER_CTL_MSG_BASE+MEDIA_RENDER_CTL_MSG_SIZE);
	private static final int MEDIA_SHARE_CTL_MSG_SIZE = 10;
	private static final int MEDIA_CONTROLLER_CTL_MSG_BASE = (MEDIA_SHARE_CTL_MSG_BASE+MEDIA_SHARE_CTL_MSG_SIZE);
	private static final int MEDIA_CONTROLLER_CTL_MSG_SIZE = 30;
	/*----------------------------------------------------------------*/
	public static final int MEDIA_RENDER_CTL_MSG_SET_AV_URL = (MEDIA_RENDER_CTL_MSG_BASE+0);
	public static final int MEDIA_RENDER_CTL_MSG_STOP = (MEDIA_RENDER_CTL_MSG_BASE+1);
	public static final int MEDIA_RENDER_CTL_MSG_PLAY = (MEDIA_RENDER_CTL_MSG_BASE+2);
	public static final int MEDIA_RENDER_CTL_MSG_PAUSE = (MEDIA_RENDER_CTL_MSG_BASE+3);
	public static final int MEDIA_RENDER_CTL_MSG_SEEK = (MEDIA_RENDER_CTL_MSG_BASE+4);
	public static final int MEDIA_RENDER_CTL_MSG_SETVOLUME = (MEDIA_RENDER_CTL_MSG_BASE+5);
	public static final int MEDIA_RENDER_CTL_MSG_SETMUTE = (MEDIA_RENDER_CTL_MSG_BASE+6);
	private static final int MEDIA_RENDER_CTL_MSG_SETPLAYMODE = (MEDIA_RENDER_CTL_MSG_BASE+7);
	private static final int MEDIA_RENDER_CTL_MSG_PRE = (MEDIA_RENDER_CTL_MSG_BASE+8);
	private static final int MEDIA_RENDER_CTL_MSG_NEXT = (MEDIA_RENDER_CTL_MSG_BASE+9);
	/*----------------------------------------------------------------*/
	public static final int MEDIA_SHARE_CTL_MSG_ONECONTROLATTACHED = (MEDIA_SHARE_CTL_MSG_BASE+0);
	public static final int MEDIA_SHARE_CTL_MSG_ONPROCESSFILEREQUEST = (MEDIA_SHARE_CTL_MSG_BASE+1);
	/*----------------------------------------------------------------*/
	public static final int  MEDIA_CONTROLLER_EVENT_CTL_MSG_DURATION = (MEDIA_CONTROLLER_CTL_MSG_BASE+0);
	public static final int  MEDIA_CONTROLLER_EVENT_CTL_MSG_TRANPLAYSTATE = (MEDIA_CONTROLLER_CTL_MSG_BASE+1);
	public static final int  MEDIA_CONTROLLER_EVENT_CTL_MSG_VOLUME = (MEDIA_CONTROLLER_CTL_MSG_BASE+2);
	public static final int  MEDIA_CONTROLLER_EVENT_CTL_MSG_MUTE = (MEDIA_CONTROLLER_CTL_MSG_BASE+3);
	public static final int  MEDIA_CONTROLLER_REQRSLT_CTL_MSG_DURATION = (MEDIA_CONTROLLER_CTL_MSG_BASE+10);
	public static final int  MEDIA_CONTROLLER_REQRSLT_CTL_MSG_TRANPLAYSTATE = (MEDIA_CONTROLLER_CTL_MSG_BASE+11);
	public static final int  MEDIA_CONTROLLER_REQRSLT_CTL_MSG_VOLUME = (MEDIA_CONTROLLER_CTL_MSG_BASE+12);
	public static final int  MEDIA_CONTROLLER_REQRSLT_CTL_MSG_MUTE = (MEDIA_CONTROLLER_CTL_MSG_BASE+13);
	public static final int  MEDIA_CONTROLLER_REQRSLT_CTL_MSG_POSITION = (MEDIA_CONTROLLER_CTL_MSG_BASE+14);
	public static final int MEDIA_CONTROLLER_OTHER_MSG_ONMSADD=(MEDIA_CONTROLLER_CTL_MSG_BASE+20);
	public static final int MEDIA_CONTROLLER_OTHER_MSG_ONMSDEL=(MEDIA_CONTROLLER_CTL_MSG_BASE+21);
	public static final int MEDIA_CONTROLLER_OTHER_MSG_ONMRADD=(MEDIA_CONTROLLER_CTL_MSG_BASE+22);
	public static final int MEDIA_CONTROLLER_OTHER_MSG_ONMRDEL=(MEDIA_CONTROLLER_CTL_MSG_BASE+23);
	/*----------------------------------------------------------------*/
	
	public static final String DLNA_RENDERER_CMD="com.android.dlna.renderer.cmd";
	public static final String GET_DLNA_RENDERER_CMD="get_dlna_renderer_cmd";
	public static final String GET_PARAM_PLAYING_URI="get_param_playing_uri";
	public static final String GET_PARAM_METADATA="get_param_metadata";
	public static final String GET_VIDEO_VOLUME="get_video_volume";
	public static final String GET_SEEK_TIMETYPE="get_seek_timetype";//REL_TIME;TRACK_NR
	public static final String GET_SEEK_POSITION="get_seek_position";
	/*----------------------------------------------------------------*/
	public static final String DLNA_SHARE_CMD="com.android.dlna.share.cmd";
	public static final String GET_DLNA_SHARE_CMD="get_dlna_share_cmd";
	public static final String GET_ATTACHEDIP="get_attachedip";
	/*----------------------------------------------------------------*/
	
	/*
	 * 		往JNI设置的命令
	 * 
	 * */
	public static final int MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_DURATION = (MEDIA_RENDER_CTL_MSG_BASE+0);
	public static final int MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_POSITION = (MEDIA_RENDER_CTL_MSG_BASE+1);
	public static final int MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_PLAYINGSTATE = (MEDIA_RENDER_CTL_MSG_BASE+2);
	/*----------------------------------------------------------------*/
	public static final String RENDERER_TOCONTRPOINT_CMD="com.android.dlna.renderer.tocontrolpointer.cmd";
	public static final String GET_RENDERER_TOCONTRPOINT_CMD="get_dlna_renderer_tocontrolpointer.cmd";
	public static final String GET_PARAM_MEDIA_DURATION="get_param_media_duration";
	public static final String GET_PARAM_MEDIA_POSITION="get_param_media_position";
	public static final String GET_PARAM_MEDIA_PLAYINGSTATE="get_param_media_playingstate";
	/*----------------------------------------------------------------*/
	//播放状态的宏字符串来自dlna协议，不可随意修改
    public static final String MEDIA_PLAYINGSTATE_STOP="STOPPED";
    public static final String MEDIA_PLAYINGSTATE_PAUSE="PAUSED_PLAYBACK";
    public static final String MEDIA_PLAYINGSTATE_PLAYING="PLAYING";
    public static final String MEDIA_PLAYINGSTATE_TRANSTION="TRANSITIONING";
    public static final String MEDIA_PLAYINGSTATE_NOMEDIA="NO_MEDIA_PRESENT";
    
    public static final String MEDIA_MUTESTATE_MUTE="MUTE";
    public static final String MEDIA_MUTESTATE_UNMUTE="UNMUTE";
    
    /*<allowedValue>STOPPED</allowedValue>
    <allowedValue>PAUSED_PLAYBACK</allowedValue>
    <allowedValue>PAUSED_RECORDING</allowedValue>
    <allowedValue>PLAYING</allowedValue>
    <allowedValue>RECORDING</allowedValue>
    <allowedValue>TRANSITIONING</allowedValue>
    <allowedValue>NO_MEDIA_PRESENT</allowedValue>*/
    /*----------------------------------------------------------------*/
    //控制点过来的seek时间类型有如下两种，字符串不可随意修改
    public static final String MEDIA_SEEK_TIME_TYPE_REL_TIME="REL_TIME";	//时间格式为：0:0:0.000
    public static final String MEDIA_SEEK_TIME_TYPE_TRACK_NR="TRACK_NR";
    /*----------------------------------------------------------------*/
    
	public static void sendRendererMsgToJavaInThread(int cmd,String value,String data){
		/*if(null == mc)
			return;
		Intent i;
		if(cmd>=MEDIA_SHARE_CTL_MSG_ONECONTROLATTACHED){
			i= new Intent(DLNA_SHARE_CMD);
			i.putExtra(GET_DLNA_SHARE_CMD, cmd);
		}else{
			i = new Intent(DLNA_RENDERER_CMD);
			i.putExtra(GET_DLNA_RENDERER_CMD, cmd);
		}*/
		switch(cmd){
			/*case MEDIA_RENDER_CTL_MSG_SET_AV_URL://设置播放地址
		    	i.putExtra(GET_PARAM_PLAYING_URI, value);
		    	i.putExtra(GET_PARAM_METADATA, data);
		    	mc.sendBroadcast(i);
				break;
			case MEDIA_RENDER_CTL_MSG_PLAY://启动播放
		    	mc.sendBroadcast(i);
				break;
			case MEDIA_RENDER_CTL_MSG_PAUSE:
		    	mc.sendBroadcast(i);
				break;
			case MEDIA_RENDER_CTL_MSG_STOP:
		    	mc.sendBroadcast(i);
				break;
			case MEDIA_RENDER_CTL_MSG_SEEK:
				if(null == value)
					break;
				String[] seektime=value.split("=");
				if(2==seektime.length){
					i.putExtra(GET_SEEK_TIMETYPE, seektime[0]);
					i.putExtra(GET_SEEK_POSITION, seektime[1]);
					mc.sendBroadcast(i);
				}
				break;
			case MEDIA_RENDER_CTL_MSG_SETMUTE:
		    	mc.sendBroadcast(i);
				break;
			case MEDIA_RENDER_CTL_MSG_SETVOLUME:
				try {
					if(null == value)
						break;
					int volume=Integer.valueOf(value);
					i.putExtra(GET_VIDEO_VOLUME, volume);
			    	mc.sendBroadcast(i);
				} catch (Exception e) {
					// TODO: handle exception
				}
				break;
			case MEDIA_RENDER_CTL_MSG_SETPLAYMODE:
				break;
			case MEDIA_RENDER_CTL_MSG_PRE:
				break;
			case MEDIA_RENDER_CTL_MSG_NEXT:
				break;
				*/
			/*--------- SHARE --------*/	
			/*case MEDIA_SHARE_CTL_MSG_ONECONTROLATTACHED:
				if(null == value)
					break;
				i.putExtra(GET_ATTACHEDIP, value);
				mc.sendBroadcast(i);
				break;*/
			case MEDIA_SHARE_CTL_MSG_ONPROCESSFILEREQUEST:
				/*Intent sendIntent = new Intent("com.skyworthdigital.action.START_DVB");  //或者new Intent(MY_ACTION)
	 		 	sendIntent.putExtra("order", Data.DATA_TYPE_CHANGE_CHN);
				sendIntent.putExtra("android.stb.EXTRA_PLAY_CHANNEL_ID", data1.chid);
				sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	 		 	if(null != mc)
	 		 		mc.startActivity(sendIntent);*/
				onDlnaMediaServerCmdParser(cmd,value);
				Log.v("DLNA ","直播 ： "+value);
				break;
			/*--------- CONTROLLER --------*/	
			case MEDIA_CONTROLLER_EVENT_CTL_MSG_DURATION:
			case MEDIA_CONTROLLER_EVENT_CTL_MSG_TRANPLAYSTATE:
			case MEDIA_CONTROLLER_EVENT_CTL_MSG_VOLUME:
			case MEDIA_CONTROLLER_EVENT_CTL_MSG_MUTE:
			case MEDIA_CONTROLLER_REQRSLT_CTL_MSG_DURATION:
			case MEDIA_CONTROLLER_REQRSLT_CTL_MSG_TRANPLAYSTATE:
			case MEDIA_CONTROLLER_REQRSLT_CTL_MSG_VOLUME:
			case MEDIA_CONTROLLER_REQRSLT_CTL_MSG_MUTE:
			case MEDIA_CONTROLLER_REQRSLT_CTL_MSG_POSITION:
				if(DEBUG) Log.v(TAG,"onDlnaMediaControllerCtlParser");
				onDlnaMediaControllerCtlParser(cmd,value);
				break;
			default:
				break;
		}
		
	}
	
	public void sendRendererMsgToJava(int cmd,String value,String data){
		sendRendererMsgToJavaInThread(cmd,value,data);
	}
	
	/*
	 * 		jni获取媒体信息的接口
	 * */
	public MediaInfo getMediaInfoFromJavaInThread(String mediaPath){
		if(DEBUG) Log.v(TAG,"getMediaInfoFromJavaInThread");
		MediaInfo media=new MediaInfo();
		media.mediatitle="hello get object";
		return media;
	}
	public MediaInfo getMediaInfoFromJava(String mediaPath){
		return getMediaInfoFromJavaInThread( mediaPath);
	}
	////////////////////////////////////////////////////////////////////////////////
	public static void setcontext(Context c){
		mc=c;
	}
	public static void releasecontext(){
		mc=null;
	}
	
	
	/*
	 * 		thread for jni call
	 * */
	private static Handler JNICALLHANDLE = new Handler(){
       	public void handleMessage(Message msg) {  
       		if(DEBUG) Log.v(TAG,"handleMessage");
       		String value=(String)msg.obj;
       		switch(msg.what){
       		case MEDIA_CONTROLLER_EVENT_CTL_MSG_DURATION:
       			if(DEBUG) Log.v(TAG,"MEDIA_CONTROLLER_EVENT_CTL_MSG_DURATION");
       			mctlerlisten.OnMediaControlEventDuration(value);
				break;
			case MEDIA_CONTROLLER_EVENT_CTL_MSG_TRANPLAYSTATE:
				if(DEBUG) Log.v(TAG,"MEDIA_CONTROLLER_EVENT_CTL_MSG_TRANPLAYSTATE");
				mctlerlisten.OnMediaControlEventTranPlayState(value);
				break;
			case MEDIA_CONTROLLER_EVENT_CTL_MSG_VOLUME:
				if(DEBUG) Log.v(TAG,"MEDIA_CONTROLLER_EVENT_CTL_MSG_VOLUME");
				if(!DlnaTools.isNumeric(value))
					break;
				//if(null != mc)
				//	Tools.setCurrentVolume(Integer.parseInt(value),mc);
				mctlerlisten.OnMediaControlEventVolume(value);
				break;
			case MEDIA_CONTROLLER_EVENT_CTL_MSG_MUTE:
				if(DEBUG) Log.v(TAG,"MEDIA_CONTROLLER_EVENT_CTL_MSG_MUTE");
				//if(value.equals(MEDIA_MUTESTATE_MUTE))
				//	Tools.setVolumeMute(mc);
				//else
				//	Tools.setVolumeUnmute(mc);
				mctlerlisten.OnMediaControlEventMute(value);
				break;
			case MEDIA_CONTROLLER_REQRSLT_CTL_MSG_DURATION:
				if(DEBUG) Log.v(TAG,"MEDIA_CONTROLLER_REQRSLT_CTL_MSG_DURATION");
				mctlerlisten.OnMediaControlReqRsltDuration(value);
				break;
			case MEDIA_CONTROLLER_REQRSLT_CTL_MSG_TRANPLAYSTATE:
				if(DEBUG) Log.v(TAG,"MEDIA_CONTROLLER_REQRSLT_CTL_MSG_TRANPLAYSTATE");
				mctlerlisten.OnMediaControlReqRsltTranPlayState(value);
				break;
			case MEDIA_CONTROLLER_REQRSLT_CTL_MSG_VOLUME:
				if(DEBUG) Log.v(TAG,"MEDIA_CONTROLLER_REQRSLT_CTL_MSG_VOLUME");
				if(!DlnaTools.isNumeric(value))
					break;
				//if(null != mc)
				//	Tools.setCurrentVolume(Integer.parseInt(value),mc);
				mctlerlisten.OnMediaControlReqRsltVolume(value);
				break;
			case MEDIA_CONTROLLER_REQRSLT_CTL_MSG_MUTE:
				if(DEBUG) Log.v(TAG,"MEDIA_CONTROLLER_REQRSLT_CTL_MSG_MUTE");
				//if(value.equals(MEDIA_MUTESTATE_MUTE))
				//	Tools.setVolumeMute(mc);
				//else
				//	Tools.setVolumeUnmute(mc);
				mctlerlisten.OnMediaControlReqRsltMute(value);
				break;
			case MEDIA_CONTROLLER_REQRSLT_CTL_MSG_POSITION:
				if(DEBUG) Log.v(TAG,"MEDIA_CONTROLLER_REQRSLT_CTL_MSG_POSITION");
				mctlerlisten.OnMediaControlReqRsltPosition(value);
				break;
       		}
       	}   
       };
    private static void ceateThreadToProCall(int cmd,String msgvalue){
    	Message message = new Message();   
		message.what=cmd;
		message.obj=msgvalue;
		JNICALLHANDLE.sendMessage(message);	
    }
	/////////////////////////////////////////////
	/*
	 * 		控制点事件信息接口
	 * */
	public interface DlnaMediaControllerListen {
		public void OnMediaControlEventDuration(String duration);
		public void OnMediaControlEventVolume(String volume);
		public void OnMediaControlEventTranPlayState(String playstate);
		public void OnMediaControlEventMute(String mutestate);
		public void OnMediaControlReqRsltDuration(String duration);
		public void OnMediaControlReqRsltVolume(String volume);
		public void OnMediaControlReqRsltTranPlayState(String playstate);
		public void OnMediaControlReqRsltMute(String mutestate);
		public void OnMediaControlReqRsltPosition(String position);
	}
	
	private static DlnaMediaControllerListen mctlerlisten=null;
	public static void setOnDlnaMediaControllerListen(DlnaMediaControllerListen l){
		mctlerlisten=l;
	}
	private static void onDlnaMediaControllerCtlParser(int cmd , String value){
		if(null != mctlerlisten){
			ceateThreadToProCall(cmd,value);
		}
	}
	
	/*
	 * 		DMS事件信息接口
	 * */
	public interface DlnaMediaServerListen {
		public void OnMediaServerProcessFileRespone(String filepath);
	}
	private static DlnaMediaServerListen mserverlisten=null;
	public static void setOnDlnaMediaServerListen(DlnaMediaServerListen l){
		mserverlisten=l;
	}
	private static void onDlnaMediaServerCmdParser(int cmd , String value){
		if(null != mserverlisten){
			switch(cmd){
			case MEDIA_SHARE_CTL_MSG_ONPROCESSFILEREQUEST:
				mserverlisten.OnMediaServerProcessFileRespone(value);
				break;
			}
		}
	}
	
    /******************************************************/
    //					
    /******************************************************/
	/*
	 * 
	 * */
	public static void sendMsgToJavaInThread(int cmd,String value,String data){
		switch(cmd){
		case MEDIA_CONTROLLER_OTHER_MSG_ONMSADD:
		case MEDIA_CONTROLLER_OTHER_MSG_ONMSDEL:
			onDlnaMediaServerConnectChange(cmd,value,data);
			break;
		case MEDIA_CONTROLLER_OTHER_MSG_ONMRADD:
			break;
		case MEDIA_CONTROLLER_OTHER_MSG_ONMRDEL:
			break;
		}
	}
	public void sendMsgToJava(int cmd,String value,String data){
		sendMsgToJavaInThread(cmd,value,data);
	}
	
	/*
	 * 		DMS添加删除接口
	 * */
	public interface DlnaMediaServerConnectListen {
		public void OnMediaServerAdd(MediaServerInfo msinst);
		public void OnMediaServerRemove(String uuid);
	}
	private static DlnaMediaServerConnectListen msconlisten=null;
	public static void setOnDlnaMediaServerConnectListen(DlnaMediaServerConnectListen l){
		msconlisten=l;
	}
	private static void onDlnaMediaServerConnectChange(int cmd , String value,String data){
		if(null != msconlisten){
			String SPLIT="\n\r";
			switch(cmd){
			case MEDIA_CONTROLLER_OTHER_MSG_ONMSADD:
				if(null == value)
					return ;
				String[] infos=value.split(SPLIT);
				if(4 != infos.length){
					Log.v(TAG,"ms info err num :"+infos.length);
					return ;
				}
				MediaServerInfo msadd=new MediaServerInfo();
				msadd.friendlyname=infos[0];
				msadd.uuid=infos[1];
				msadd.servertype=infos[2];
				msadd.descriptionurl=infos[3];
				msadd.ip=DlnaData.getIPFromUrl(infos[3]);
				msconlisten.OnMediaServerAdd(msadd);
				break;
			case MEDIA_CONTROLLER_OTHER_MSG_ONMSDEL:
				msconlisten.OnMediaServerRemove(value);
				break;
			}
		}
	}
}
