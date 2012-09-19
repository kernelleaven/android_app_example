package com.android.colorset;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//http://www.cnblogs.com/leon19870907/articles/1978065.html
//http://www.dewen.org/q/2024/android%E5%9B%BE%E7%89%87%E5%A6%82%E4%BD%95%E6%8C%89%E8%87%AA%E5%B7%B1%E6%8C%87%E5%AE%9A%E7%9A%84%E9%A2%9C%E8%89%B2%E6%98%BE%E7%A4%BA
//http://blog.csdn.net/shijun_zhang/article/details/6525388
public class CMatrix extends Activity {
	private Button change;
	private EditText [] et=new EditText[20];
	private float []carray=new float[20];
	private MyImage sv;
	
	private float []carrayinit={(float) 1.0,(float) 0.0,(float) 0.0,(float) 0.0,(float)0.0,
			(float) 0.0,(float) 1.0,(float) 0.0,(float) 0.0,(float)0.0,
			(float) 0.0,(float) 0.0,(float) 1.0,(float) 0.0,(float)0.0,
			(float) 0.0,(float) 0.0,(float) 0.0,(float) 1.0,(float)0.0,};
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	change=(Button)findViewById(R.id.set);
	sv=(MyImage)findViewById(R.id.MyImage);

	for(int i=0;i<20;i++){

	et[i]=(EditText)findViewById(R.id.indexa+i);
	et[i].setText(""+carrayinit[i]);
	carray[i]=Float.valueOf(et[i].getText().toString());
	}

	change.setOnClickListener(l);
	}

	private Button.OnClickListener l=new Button.OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			getValues();
			sv.setValues(carray);
			sv.invalidate();
		}

	};
	public void getValues(){
	for(int i=0;i<20;i++){
		carray[i]=Float.valueOf(et[i].getText().toString());
	}

	}
}