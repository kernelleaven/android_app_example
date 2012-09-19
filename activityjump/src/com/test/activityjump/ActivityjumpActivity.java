package com.test.activityjump;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActivityjumpActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.v("test","-----------oncreat1");
        setContentView(R.layout.main);
        //Log.v("test","-----------oncreat2");
        TextView tmptv= (TextView) findViewById(R.id.maintvview);
        //Log.v("test","-----------oncreat3");
        tmptv.setText("我是第一个activity");
        //Log.v("test","-----------oncreat4");
        Button bt = (Button) findViewById(R.id.mainbt);
        //Log.v("test","-----------oncreat6");
        bt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub			
				Intent in=new Intent();
				in.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				in.setClass(ActivityjumpActivity.this,activity2.class);
				//---------------------------------------------------------------
				//简单启动
				startActivity(in);
				//---------------------------------------------------------------
				//简洁模式
				//startActivity(new Intent(ActivityjumpActivity.this,activity2.class));
				//---------------------------------------------------------------
				//需要返回数据或结果的，则使用startActivityForResult (Intent intent, int requestCode)
				//startActivityForResult(in, 90);//requestCode系统保存并返回给onActivityResult	
				//---------------------------------------------------------------
				//跳转的动画实现
				overridePendingTransition(R.anim.frombuttomtotop, R.anim.fromtoptobuttom);
			}
		});
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	Log.v("test","------get resultCose :"+resultCode+" requestCode:"+requestCode);
    	if(null != data){
	    	Bundle bund = data.getExtras();
	    	if(null != bund){
	    		String getrst = bund.getString("phoneNO");
	    		Log.v("test","------getrst : "+getrst);
	    	}
    	}
    }
}