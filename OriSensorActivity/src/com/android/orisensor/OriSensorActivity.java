package com.android.orisensor;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class OriSensorActivity extends Activity implements SensorEventListener{
    /** Called when the activity is first created. */
	
	private boolean mRegisteredSensor; 
	public int xx=2;
	//����SensorManager   
    private SensorManager  mSensorManager; 
    Context mc;
	TextView tvaccx;
	TextView tvaccy;
	TextView tvaccz;
	TextView tvgrox;
	TextView tvgroy;
	TextView tvgroz;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ����Ϊ����   
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  
        //����Ϊ�ޱ�����           
        requestWindowFeature( Window.FEATURE_NO_TITLE ); 
        setContentView(R.layout.main);
        
    	tvaccx=(TextView)findViewById(R.id.txv);
    	tvaccy=(TextView)findViewById(R.id.tyv);
    	tvaccz=(TextView)findViewById(R.id.tzv);
    	tvgrox=(TextView)findViewById(R.id.txvg);
    	tvgroy=(TextView)findViewById(R.id.tyvg);
    	tvgroz=(TextView)findViewById(R.id.tzvg);
        //����Ϊȫ��ģʽ   
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                              WindowManager.LayoutParams.FLAG_FULLSCREEN );
        //ȡ��SensorManagerʵ��   
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); 
        
        
        mc=this;
    }
    
    @Override
    protected void onResume() {
    	initSensorRecever();
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	uninitSensorRecever();
    	super.onPause();
    }
    
    private void initSensorRecever(){
    		//����SensorManager��һ���б�(Listener)   
            //��������ָ������ΪTYPE_ORIENTATION(�����Ӧ��)   
    		List<Sensor> sensors=mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER); 
      
            if (sensors.size() > 0){  
            	Log.v("test","have TYPE_ACCELEROMETER num :"+sensors.size());
                Sensor sensor = sensors.get(0);  
                //ע��SensorManager   
                mRegisteredSensor = mSensorManager.registerListener(this, 
                		sensor,SensorManager.SENSOR_DELAY_UI);
            }else{
            	Log.v("test","no TYPE_ACCELEROMETER");
            }
            
            sensors=mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
            if(sensors.size() > 0){
            	Log.v("test","have TYPE_GYROSCOPE num :"+sensors.size());
                Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);//sensors.get(1);
                Log.v("test","default TYPE_GYROSCOPE :"+sensor.getName());
                
                //ע��SensorManager   
                mSensorManager.registerListener(this, 
                		sensor,SensorManager.SENSOR_DELAY_UI);
            }else{
            	Log.v("test","no TYPE_GYROSCOPE");
            }
            
            sensors=mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
            if(sensors.size() > 0){
            	Log.v("test","have TYPE_ORIENTATION");
            }else{
            	Log.v("test","no TYPE_ORIENTATION");
            }
            
            sensors=mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if(sensors.size() > 0){
            	Log.v("test","have TYPE_MAGNETIC_FIELD");
            }else{
            	Log.v("test","no TYPE_MAGNETIC_FIELD");
            }
            
            sensors=mSensorManager.getSensorList(Sensor.TYPE_ALL);
            if(sensors.size() > 0){
            	for(int i=0;i<sensors.size();i++){
            		Sensor sensor = sensors.get(i);  
            		Log.v("test"," \n"+i+" :"+sensor.getName()+" \n"+sensor.getType()+" \n"+sensor.getVendor()+" \n"+sensor.getVersion());
            	}
            }
    }
    
    private void uninitSensorRecever(){
        mSensorManager.unregisterListener(this);
        Log.v("test","un000000000000000");
    }
    
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		//Log.v("test","onAccuracyChanged");////����ȷ�ȷ����ı�ʱ  
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//Log.v("test","onSensorChanged");
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			//if(2==xx){
			//	Log.v("test"," "+event.sensor.getName()+" :"+event.values[SensorManager.DATA_Y]);
			//}
			float [] datas=event.values;
			//Log.v("test","chaged");
			String tvinfo;
			tvinfo="x : "+datas[0];
			tvaccx.setText(tvinfo);
			tvinfo="y : "+datas[1];
			tvaccy.setText(tvinfo);
			tvinfo="z : "+datas[2];//event.values[SensorManager.DATA_Z];
			tvaccz.setText(tvinfo);
			   // �������ǿ��Եõ����ݣ�Ȼ�������Ҫ������   
           // float y = event.values[SensorManager.DATA_Y]; 
		}else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
			float [] datas=event.values;
			//Log.v("test","chaged");
			String tvinfo;
			tvinfo="gx : "+datas[0];
			tvgrox.setText(tvinfo);
			tvinfo="gy : "+datas[1];
			tvgroy.setText(tvinfo);
			tvinfo="gz : "+datas[2];//event.values[SensorManager.DATA_Z];
			tvgroz.setText(tvinfo);
			Log.v("test","gx :"+datas[0]);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//xx=3;
		//xx+=xx;
		//Log.v("test","mRegisteredSensor: "+xx);
		if(mRegisteredSensor){
			mSensorManager.unregisterListener((OriSensorActivity)mc);
			mRegisteredSensor=false;
		}
		return super.onTouchEvent(event);
	}
}