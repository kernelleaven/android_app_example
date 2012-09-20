package com.android.voiceserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class VoiceserverActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */    
    Button btnRecord, btnStop, btnExit;    
    SeekBar skbVolume;//调节音量    
    boolean isRecording = false;//是否录放的标记    
    static final int frequency =44100;    //16000;// 
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;//单声道
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;    	//只有8位 或者 16位
    int recBufSize,playBufSize;    
    AudioRecord audioRecord;    
    AudioTrack audioTrack;  
    
    dataBuffer bufstub;
    //------
    TextToSpeech tts;
    //------
    private File path=null;
    private String temp = "recaudio_";// 临时文件前缀  
    private MediaRecorder mMediaRecorder = null;  
    //------
    Context mc;    
    
    @Override    
    public void onCreate(Bundle savedInstanceState) {    
        super.onCreate(savedInstanceState);    
        setContentView(R.layout.main);    
        mc=this;
        
        setTitle("语音录放");    
        recBufSize = AudioRecord.getMinBufferSize(frequency,channelConfiguration, audioEncoding);    
        playBufSize=AudioTrack.getMinBufferSize(frequency,channelConfiguration, audioEncoding);    
        bufstub=new dataBuffer();
        // -----------------------------------------    
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,    
                channelConfiguration, audioEncoding, recBufSize);  
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,    
                channelConfiguration, audioEncoding,    
                playBufSize, AudioTrack.MODE_STREAM);
        //------------------------------------------    
        btnRecord = (Button) this.findViewById(R.id.btnRecord);    
        btnRecord.setOnClickListener(this);    
        btnStop = (Button) this.findViewById(R.id.btnStop);    
        btnStop.setOnClickListener(this);    
        btnExit = (Button) this.findViewById(R.id.btnExit);    
        btnExit.setOnClickListener(this);    
        skbVolume=(SeekBar)this.findViewById(R.id.skbVolume);    
        skbVolume.setMax(100);//音量调节的极限    
        skbVolume.setProgress(10);//设置seekbar的位置值    
        audioTrack.setStereoVolume(0.1f, 0.1f);//设置当前音量大小    
        skbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {    
            @Override    
            public void onStopTrackingTouch(SeekBar seekBar) {    
                float vol=(float)(seekBar.getProgress())/(float)(seekBar.getMax());    
                audioTrack.setStereoVolume(vol, vol);//设置音量    
            }    
            @Override    
            public void onStartTrackingTouch(SeekBar seekBar) {    
            }    
            @Override    
            public void onProgressChanged(SeekBar seekBar, int progress,    
                    boolean fromUser) {      
            }    
        });    
        //---------------------------------------------
        tts=new TextToSpeech(this,null);
       
        
        Button bttts=(Button) findViewById(R.id.btnttse);
        bttts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tts.speak("reproduction by means of a cell or organism dividing into two or more new cells or organisms", 
						TextToSpeech.QUEUE_FLUSH, null);
			}
		});
        bttts=(Button) findViewById(R.id.btnttsc);
        bttts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tts.speak("你好我会读中文,分享35个高质量的  CLICK HELLO  ",
						TextToSpeech.QUEUE_FLUSH, null);
			}
		});
        EditText xx=new EditText(this);
        xx.setInputType(InputType.TYPE_NULL);
        /////////////////////////////////////////////////////
    }    
    
    @Override    
    protected void onDestroy() {    
        super.onDestroy();    
        android.os.Process.killProcess(android.os.Process.myPid());    
    }    
      
    class dataBuffer{
    	byte[] inbuf;
    	byte[] buftmp;
    	int wps;
    	int rps;
    	byte[] outbuf;
    	int outlen;
    	dataBuffer(){
    		inbuf=new byte[recBufSize];
    		buftmp=new byte[recBufSize*10];
    		outbuf=new byte[recBufSize*10];
    		wps=0;
    		rps=0;
    		outlen=0;
    	}
    	void synReadData(){
	    	synchronized(buftmp){
	    		if(wps==rps){
	    			return;
	    		}
	    		if(wps>=rps){
	    			outlen=wps-rps;
	    			rps=wps;
	    			System.arraycopy(buftmp, 0, outbuf, 0, outlen);    
	    		}else if(wps<rps){
	    			outlen=wps+recBufSize*10-rps;
	    			System.arraycopy(buftmp, 0, outbuf, rps, recBufSize*10-rps);    
	    			System.arraycopy(buftmp, recBufSize*10-rps, outbuf, 0,wps); 
	    			rps=wps;
	    		}
	    	}    	
    	}
    	void synWriteData(int len){
    		synchronized(buftmp){
    			if(len < (recBufSize*10-wps)){
    				System.arraycopy(inbuf, 0, buftmp, wps, len);  
    				wps+=len;
    			}
    			else{
    				System.arraycopy(inbuf, 0, buftmp, wps, recBufSize*10-wps);  
    				System.arraycopy(inbuf, recBufSize*10-wps, buftmp, 0,len-(recBufSize*10-wps));  
    				wps=len-(recBufSize*10-wps);
    			}
    		}
    	}
    }
    class RecordThread extends Thread{
    	public void run(){
    		try{
    			 audioRecord.startRecording();//开始录制    
                 while (isRecording) {    
                	 //byte[] buffer = new byte[recBufSize];    
                	 int len = audioRecord.read(bufstub.inbuf, 0,recBufSize);  
                	 bufstub.synWriteData(len);
                 }   
                 audioRecord.stop();   
    		}catch (Exception e) {
    			Toast.makeText(VoiceserverActivity.this, e.getMessage(), 1000);    
			}
    	}
    }
    class TrackThread extends Thread{
    	public void run(){
    		try{
    			 audioTrack.play();//开始播放   
                 while (isRecording) {    
                	 bufstub.synReadData();
                	 if(bufstub.outlen>0)
                		 audioTrack.write(bufstub.outbuf, 0, bufstub.outlen);  
                	 else
                		 Thread.sleep(1);
                 }   
                 audioTrack.stop();//开始播放     
    		}catch (Exception e) {
    			Toast.makeText(VoiceserverActivity.this, e.getMessage(), 1000);    
			}
    	}
    }
    class RecordPlayThread extends Thread {    
        public void run() {    
            try {    
                byte[] buffer = new byte[recBufSize];    
                audioRecord.startRecording();//开始录制    
                audioTrack.play();//开始播放    
                    
                while (isRecording) {    
                    //从MIC保存数据到缓冲区    
                    int bufferReadResult = audioRecord.read(buffer, 0,recBufSize);    
                    byte[] tmpBuf = new byte[bufferReadResult];    
                    System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);    
                    //写入数据即播放    
                    audioTrack.write(tmpBuf, 0, tmpBuf.length);    
                }    
                audioTrack.stop();    
                audioRecord.stop();    
            } catch (Throwable t) {    
                Toast.makeText(VoiceserverActivity.this, t.getMessage(), 1000);    
            }    
        }    
    }
    
    void startRecord(){
    	try {
    		path=File.createTempFile(temp, ".amr", Environment.getExternalStorageDirectory());
    		//String pathstring=getFilesDir().getAbsolutePath()+"/"+temp+".amr";
    		/*File tmppath=new File(pathstring);
    		if(tmppath.exists()){
    			tmppath.delete();
    		}
    		path=new File(pathstring);*/
			setTitle(""+path.getAbsolutePath());  
			mMediaRecorder = new MediaRecorder();  
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置数据来源，麦克风  
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);// 设置格式  
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 设置编码  
			mMediaRecorder.setOutputFile(path.getAbsolutePath());// 设置输出文件路径  
			mMediaRecorder.prepare();  
			mMediaRecorder.start();  
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    void releaseRecord(){
    	if(null!=mMediaRecorder){
	    	mMediaRecorder.stop();  
	        mMediaRecorder.release();  
	        mMediaRecorder = null;  
    	}
    }
    
    //////////////////////////////////////////////
    class TrackThreadPlay extends Thread{
    	public void run(){
    		try{
    			if(audioTrack.getPlayState()!=AudioTrack.PLAYSTATE_STOPPED)
    				audioTrack.stop();//开始播放 
    			audioTrack.play();//开始播放   
    			try{ 
    				FileInputStream fin = new FileInputStream(path.getAbsolutePath());
    				//FileInputStream fin = openFileInput(fileName);  
    				//用这个就不行了，必须用FileInputStream
    				int length = fin.available(); 
    				byte [] buffer = new byte[length]; 
    				fin.read(buffer);     
    				audioTrack.write(buffer, 0, length);  
    				fin.close();     
    			}catch(Exception e){ 
    				e.printStackTrace(); 
    			} 
                 
                 //Thread.sleep(1);
                 //audioTrack.stop();//开始播放     
    		}catch (Exception e) {
    			Toast.makeText(VoiceserverActivity.this, e.getMessage(), 1000);    
			}
    	}
    }
    
    /* 播放录音文件 */
    private void playMusic(File file){
	    Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    /* 设置文件类型 */
	    intent.setDataAndType(Uri.fromFile(file), "audio");
	    startActivity(intent);
    }
    //////////////////////////////////////////////
    void startPlay(){
    	releaseRecord();
    	//File f = new File(/*path.getAbsolutePath()*/"/mnt/sdb/sdb4/11.mp3");  
    	//TrackThreadPlay play=new TrackThreadPlay();
    	//play.start();
    	playMusic(path);
    }

	@Override
	public void onClick(View v) {
        if (v == btnRecord) {    
        	if(isRecording){
        		releaseRecord();
        	}
            isRecording = true;    
           // new RecordPlayThread().start();// 开一条线程边录边放
          //  new RecordThread().start();
          //  new TrackThread().start();
            
            startRecord();
            
        } else if (v == btnStop) {    
            isRecording = false;
        	startPlay();        	
        } else if (v == btnExit) {    
            isRecording = false;    
            finish();    
        }  
		
	};    
	
	////////////////////////////////////////////////////////////////////////////////////////
	// cmd test
	private void cmdMkdirTest(){
		String pathg=Environment.getExternalStorageDirectory().getAbsolutePath()+"hello";
        String[] cmd = new String[]{"/bin/mkdir ",pathg};
        try {
			Process ps=Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			String xx=e.toString();
			e.printStackTrace();
		}
	}
	
	private void cmdChangeModeAndSoftLinkTest(){
		String route=mc.getFilesDir().getAbsolutePath();
    	File destDir = new File(route+"/temp");
    	if (!destDir.exists()) {
    		destDir.mkdirs();
    	}
    	changeMode(destDir.getAbsolutePath());
    	
    	softLinkMode(route+"/lndir");
	}
	
	private void changeMode(String path){
		Process p;
		int status;
		try {
			p = Runtime.getRuntime().exec("chmod 777 " + path );
			status = p.waitFor();
			if (status == 0) {
				Toast.makeText(this, "chmod succeed", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "chmod failed", Toast.LENGTH_LONG).show();
			}
		}catch (Exception e) {
			Toast.makeText(this, "chmod exception", Toast.LENGTH_LONG).show();
			return;
		}
	}
	private void softLinkMode(String path){
		Process p;
		int status;
		try {
			p = Runtime.getRuntime().exec("ln -s " +"/data"+" "+ path );
			status = p.waitFor();
			if (status == 0) {
				Toast.makeText(this, "ln succeed", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "ln failed", Toast.LENGTH_LONG).show();
			}
		}catch (Exception e) {
			Toast.makeText(this, "ln exception", Toast.LENGTH_LONG).show();
			return;
		}
	}
	/////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		TextView tv=(TextView)findViewById(R.id.textcode);
		String key=""+keyCode;
		tv.setText(key);
		Log.v("TEST","KEY : "+key);
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		TextView tv=(TextView)findViewById(R.id.textcode);
		String key="long click :"+keyCode;
		tv.setText(key);
		return super.onKeyLongPress(keyCode, event);
	}
}