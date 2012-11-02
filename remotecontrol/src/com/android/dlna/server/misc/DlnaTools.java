package com.android.dlna.server.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DlnaTools {
	private final static String TAG="dlna";
    //----------------------------------------------------------
	public static void saveStringToSysPre(String key,String name,Context mc){
		SharedPreferences set=mc.getSharedPreferences(key,0); 
    	set.edit().putString(key, name).commit();
	}
	public static String loadStringFromSysPre(String key,String defaultname,Context mc){
		SharedPreferences set=mc.getSharedPreferences(key,Context.MODE_PRIVATE );
		return set.getString(key,defaultname);
	}
	//-----------------------------------------------------------
	public static void saveIntToSysPre(String key,int data,Context mc){
		SharedPreferences set=mc.getSharedPreferences(key,0); 
		set.edit().putInt(key, data).commit();	
	}
	public static int loadIntFromSysPre(String key,int defaultdata,Context mc){
		SharedPreferences set=mc.getSharedPreferences(key,0); 
		return set.getInt(key, defaultdata);
	}
	//------------------------------------------------------------
	public static void saveBoolToSysPre(String key,boolean data,Context mc){
		SharedPreferences set=mc.getSharedPreferences(key,0); 
		set.edit().putBoolean(key,data).commit();	
	}
	public static boolean loadBoolFromSysPre(String key,boolean defaultdata,Context mc){
		SharedPreferences set=mc.getSharedPreferences(key,0); 
		return set.getBoolean(key, defaultdata);
	}
	/**********************************************************/
	
	/*
	 * 			toast 信息显示
	 * 
	 * */
	/*private static Toast toast=null;
	public static void infoToastShow(String info,Context c){
		if(null!=toast){
			toast.setText(info);
			toast.setDuration(Toast.LENGTH_LONG);
		}else{
			toast = Toast.makeText(c.getApplicationContext(),
					info, Toast.LENGTH_LONG);
		}
		toast.show();
	}
	public static void cancelInfoToastShow(){
		if(null!=toast)
			toast.cancel();
		toast=null;
	}*/
	/**********************************************************/
	/*
	 * 		时间的格式转换
	 * 
	 * */
	public static String formatTimeFromMSInt(int time){
		String hour="00";
		String min="00";
		String sec="00";
		String split=":";
		int tmptime=time;
		int tmp=0;
		if(tmptime>=3600000){
			tmp=tmptime/3600000;
			hour=formatHunToStr(tmp);
			tmptime-=tmp*3600000;
		}
		if(tmptime>=60000){
			tmp=tmptime/60000;
			min=formatHunToStr(tmp);
			tmptime-=tmp*60000;
		}
		if(tmptime>=1000){
			tmp=tmptime/1000;
			sec=formatHunToStr(tmp);
			tmptime-=tmp*1000;
		}
		
		String ret=hour+split+min+split+sec;
		return ret;
	}
	private static String formatHunToStr(int hun){
		hun=hun%100;
		if(hun>9)
			return (""+hun);
		else
			return ("0"+hun);
	}
	/**********************************************************/
	//将获取到的seek时间转换成ms
	public static int convertSeekRelTimeToMs(String reltime){
		int sec=0;
		int ms=0;
		String[] times=reltime.split(":");
		if(3!=times.length)
			return 0;
		if(!isNumeric(times[0]))
			return 0;
		int hour=Integer.parseInt(times[0]);
		if(!isNumeric(times[1]))
			return 0;
		int min=Integer.parseInt(times[1]);
		String[] times2=times[2].split("\\.");
		if(2==times2.length){//00:00:00.000
			if(!isNumeric(times2[0]))
				return 0;
			sec=Integer.parseInt(times2[0]);
			if(!isNumeric(times2[1]))
				return 0;
			ms=Integer.parseInt(times2[1]);
		}else if(1==times2.length){//00:00:00
			if(!isNumeric(times2[0]))
				return 0;
			sec=Integer.parseInt(times2[0]);
		}
		return (hour*3600000+min*60000+sec*1000+ms);
	}
	/**********************************************************/
	//判断字符串是否是数字
	public static boolean isNumeric(String str){
		if("".equals(str))
			return false;
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ){
			return false;
		}
		return true;
	} 
	
	/*
	 * 		查看网络状态
	 *
	 * */
	public static boolean getNetState(Context context){
		ConnectivityManager connectivity = 
				(ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
		if (null == connectivity) {
			return false;
		} else {//获取所有网络连接信息
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {//逐一查找状态为已连接的网络
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	/*
	 * 		设置音量
	 * 
	 * */
	public static void setCurrentVolume(int percent,Context mc){
		AudioManager am=(AudioManager)mc.getSystemService(Context.AUDIO_SERVICE);
		int maxvolume=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, (maxvolume*percent)/100, //STREAM_MUSIC:媒体音量 
				AudioManager.FLAG_PLAY_SOUND|AudioManager.FLAG_SHOW_UI);//FLAG_PLAY_SOUND:调整音量时播放声音
		am.setMode(AudioManager.MODE_INVALID);
	}
	public static void setVolumeMute(Context mc){
		AudioManager am=(AudioManager)mc.getSystemService(Context.AUDIO_SERVICE);
		am.setMode(AudioManager.MODE_INVALID);
	}
	public static void setVolumeUnmute(Context mc){
		AudioManager am=(AudioManager)mc.getSystemService(Context.AUDIO_SERVICE);
		am.setMode(AudioManager.MODE_NORMAL);
	}
	
}
