package com.android.sreencut;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class SreencutActivity extends Activity {
    private Button shotButton;  
    private ImageView imageView; 
    
    private String savePath=null;
    private String saveDir="/screens";
    
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.main);  
        init();
        String sdPath=getSDPath();
        if(null != sdPath){
        	savePath=sdPath+saveDir;
        	File csp=new File(savePath);
        	if(!csp.exists()){
	        	if(!csp.mkdir())
	        		savePath=null;
        	}
        }
        shotButton=(Button)findViewById(R.id.shotButton);  
        imageView=(ImageView)findViewById(R.id.imageView);  
        shotButton.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
                //BitmapDrawable bd=new BitmapDrawable(getScreamBitmap());  
                //imageView.setBackgroundDrawable(bd);  
//              imageView.setImageBitmap(shot());
            	Log.v("screen","start");
            	FileOutputStream fileOut=getFileAsOutStream(savePath+"/"+getFiveBitLowerRandomNumber()+".png");
            	for(int i=0;i<100;i++){
            	if(null != savePath)
            		//saveScreamAsFile(savePath+"/"+getFiveBitLowerRandomNumber()+".png");
            		
            		writeTheScreenDataToStream(fileOut);
            	}
            	Log.v("screen","end");
            	try {
					fileOut.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	try {
					fileOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	Log.v("screen","file close");
            }  
        });  
    }  
  
    /** 
     * 截屏方法 
     * @return 
     */  
    private Bitmap getScreamBitmap() {  
        View view = getWindow().getDecorView();  
        Display display = this.getWindowManager().getDefaultDisplay();  
        view.layout(0, 0, display.getWidth(), display.getHeight());  
        view.setDrawingCacheEnabled(true);//允许当前窗口保存缓存信息，这样getDrawingCache()方法才会返回一个Bitmap  
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());  
        return bmp;  
    }  
    
    private void saveScreamAsFile(String file){
    	FileOutputStream outFile=getFileAsOutStream(file);
    	if(null == outFile)
    		return;
    	View view = getWindow().getDecorView();  
        Display display = this.getWindowManager().getDefaultDisplay();  
        view.layout(0, 0, display.getWidth(), display.getHeight());  
        view.setDrawingCacheEnabled(true);
        view.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, outFile);
        try {
			outFile.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			outFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private FileOutputStream getFileAsOutStream(String file){
    	File f = new File(file);
    	if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
    	FileOutputStream fOut = null;
    	try {
    		fOut = new FileOutputStream(f);
    		return fOut;
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		return null;
    	}
    }
    
    View view ;
    private void init(){
	    view = getWindow().getDecorView();  
	    Display display = this.getWindowManager().getDefaultDisplay();  
	    view.layout(0, 0, display.getWidth(), display.getHeight());  
	    view.setDrawingCacheEnabled(true);
    }
    private void writeTheScreenDataToStream(FileOutputStream outFile){
    	if(null == outFile)
    		return;
    	
        view.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, outFile);
    }
    
	public String getSDPath(){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
		if (sdCardExist){
			sdDir = Environment.getExternalStorageDirectory();//获取跟目录
			return sdDir.toString();
		}
		return null;
	}
	
	private static Random randomnum = null;//随机数
	//min 和 max 都是正数
	public int getRandomBetween(int min,int max){
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
	private int getFiveBitLowerRandomNumber(){
		return getRandomBetween(0,99999);
	} 
}
