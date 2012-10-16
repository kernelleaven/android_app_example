package com.android.simple.video;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;

public class SimpleVideoPlayActivity extends Activity implements OnBufferingUpdateListener,
		OnCompletionListener,MediaPlayer.OnPreparedListener,SurfaceHolder.Callback{
	private final String TAG="SimpleVideoPlayActivity======================";
	private SurfaceView mSurfaceView = null; 
	private SurfaceHolder holder = null;  
	private MediaPlayer mMediaPlayer = null; 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
	    holder = mSurfaceView.getHolder();  
	    holder.addCallback(this);  
	    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    mSurfaceView.setVisibility(View.VISIBLE);
	    //AttributeSet xx;
	    //xx.get
    }
    @Override
    protected void onPause() {
    	if(null != mMediaPlayer){
    		mMediaPlayer.stop();
    		mMediaPlayer.release();
    		mMediaPlayer=null;
    	}
    	super.onPause();
    }
	public void playVedio() {  
	    try {  
	        mMediaPlayer = new MediaPlayer(); 
	        //这是变形精钢三的视频，宽度1280左右不能充满。将SurfaceView的宽度在xml中设置成1281就可以充满
	        mMediaPlayer.setDataSource(this, Uri.parse("http://v.youku.com/player/getRealM3U8/vid/XMzgyMDQ2NTQ4/type/hd2/ts/1350369886/video.m3u8"));
	        //另外一个视频宽度也是1280但是没有问题
	        //mMediaPlayer.setDataSource(this, Uri.parse("http://v.youku.com/player/getRealM3U8/vid/XMzUzMzI5Nzc2/type/hd2/ts/1350281901/video.m3u8"));
	        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  
	        mMediaPlayer.setDisplay(holder);
	        //mMediaPlayer.prepare();		//这个地方在网络状况不好的时候会被卡死很久
	        mMediaPlayer.prepareAsync();
	        mMediaPlayer.setOnBufferingUpdateListener(this);  
	        mMediaPlayer.setOnCompletionListener(this);  
	        mMediaPlayer.setOnPreparedListener(this);  
	    } catch (Exception ex) {  
	    	Log.v(TAG,"playVedio Exception"+ex.getMessage());
	    }  
	} 
    ///////////////////////////////////////////////////////////////////////
	//SurfaceHolder.Callback
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.v(TAG,"surfaceChanged  : w*h=["+width+"*"+height+"]");
		
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		playVedio();		
		Log.v(TAG,"surfaceCreated end");
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(TAG,"surfaceDestroyed");
		
	}
	////////////////////////////////////////////////////////////////////////
	//
	@Override
	public void onPrepared(MediaPlayer mp) {
	    int width = mMediaPlayer.getVideoWidth();  
	    int height = mMediaPlayer.getVideoHeight();  
	    if(width !=0 && height !=0){  
	    	Log.v(TAG,"onPrepared  suc w*h=["+width+"*"+height+"]");
	        holder.setFixedSize(1281, height); 
	    	//holder.setFixedSize(1280, 534); 
	        mMediaPlayer.start();  
	        int d=mMediaPlayer.getDuration();
	    }else{//???错误处理
	    	Log.v(TAG,"onPrepared faile");
	    }
	}
	////////////////////////////////////////////////////////////////////////
	//
	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.v(TAG,"onCompletion");
		int ps=mMediaPlayer.getDuration();
	}

	//////////////////////////////////////////////////////////////////////
	//OnBufferingUpdateListener
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		//Log.i(TAG, "onBufferingUpdate :"+percent+"|"+mMediaPlayer.getCurrentPosition());  
	}
}