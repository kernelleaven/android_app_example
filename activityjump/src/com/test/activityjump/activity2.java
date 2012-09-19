package com.test.activityjump;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class activity2 extends Activity {
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("test","----new 2");
        setContentView(R.layout.main);
        TextView tmptv= (TextView) findViewById(R.id.maintvview);
        tmptv.setText("我是第二个activity");
        
        
        Button bt = (Button) findViewById(R.id.mainbt);
        bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//Log.v("test","------arg0 "+arg0);
				Intent in=new Intent();
				in.setClass(activity2.this,ActivityjumpActivity.class);
				//---------------------------------------------------------------
				//简单返回
				//startActivity(in);
				//---------------------------------------------------------------
				//返回结果和数据
				//Bundle bundle = new Bundle(); 
				//bundle.putString("phoneNO", "020-123"); 
				//setResult(10, in.putExtras(bundle)); 
				//activity2.this.finish();
				//---------------------------------------------------------------
				//setResult(int resultCode)//只返回结果不带数据
				setResult(60);
				activity2.this.finish();
				overridePendingTransition(R.anim.frombuttomtotop, R.anim.fromtoptobuttom);
			}
		});
    }
}
