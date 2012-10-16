package com.android.simple.video;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

//import com.android.internal.R;

public class MySurfaceView extends SurfaceView {
	private final static String TAG="MySurfaceView";
	public MySurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ResetView(context, attrs,0);
	}
	public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ResetView(context, attrs,defStyle);
	}
	
    public void ResetView(Context context, AttributeSet attrs, int defStyle) {

       /* TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.View,
                defStyle, 0);
        int mMinWidth;
        int mMinHeight;
        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.View_minWidth:
                    mMinWidth = a.getDimensionPixelSize(attr, 0);
                    Log.v(TAG,TAG+" W: "+mMinWidth);
                    break;
                case R.styleable.View_minHeight:
                    mMinHeight = a.getDimensionPixelSize(attr, 0);
                    Log.v(TAG,TAG+" H: "+mMinHeight);
                    break;
            }
        }

        a.recycle();*/
    }
    
    @Override
    public void setLayoutParams(LayoutParams params) {
    	//-1:match_parent/fill_parent
    	//-2:wrap_content
    	//if((-1==params.width)||(-2==params.width)||(1280==params.width)){
    	//	params.width=1281;
    	//}
    	//Log.v(TAG,TAG+" setLayoutParams "+params.width);
    	super.setLayoutParams(params);
    }
}
