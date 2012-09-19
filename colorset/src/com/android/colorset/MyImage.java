package com.android.colorset;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MyImage extends View {
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Bitmap mBitmap;
	private float [] array=new float[20];

	private float mAngle;

	public MyImage(Context context,AttributeSet attrs) {
		super(context,attrs);
	
		mBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.test);
		invalidate();
	}


	public void setValues(float [] a){
		for(int i=0;i<20;i++){
		array[i]=a[i];
		}

	}

	@Override protected void onDraw(Canvas canvas) {
		Paint paint = mPaint;
		paint.setColorFilter(null);
		canvas.drawBitmap(mBitmap, 0, 0, paint);
		ColorMatrix cm = new ColorMatrix();
		//设置颜色矩阵
		cm.set(array);
		
		int w=mBitmap.getWidth();
		int h=mBitmap.getHeight();
		int [] pixels=new int[w*h];
		
		int[]rgb = new int[w*h*2];    
		IntBuffer bufferRGB = IntBuffer.wrap(rgb);//ByteBuffer.wrap(rgb);//将 byte 数组包装到Buffer缓冲区中
		
		Log.v("test","start w :"+w+"  h:"+h);
		Log.v("test","start 1 ");
		//mBitmap.getPixels(pixels, 0, w, 0, 0, w, h);
		/*mBitmap.copyPixelsToBuffer(bufferRGB);
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				//bufferRGB.get(i*j)=;
				bufferRGB.put((i*j), bufferRGB.get(i*j)&~(0xff));
			}
		}
		mBitmap.copyPixelsFromBuffer(bufferRGB);
		Log.v("test","start 2 ");*/
		int r,g,b;
		int rmsk=0xff<<16;
		int gmsk=0xff<<8;
		int bmsk=0xff;
		int rmax=0xff<<16;
		int rmin=0xc8<<16;		//200
		int gmax=80<<8;
		int gmin=0;
		int bmax=50;
		int bmin=0;
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				int argb=mBitmap.getPixel(i, j);
				r=(argb&rmsk)>>16;
				g=(argb&gmsk)>>8;
				b=argb&bmsk;
				int H=0;
				int max=getMaxRGBWhichIs(r,g,b);
				int min=getMinRGBWhichIs(r,g,b);
						if(max==min)
							continue;
						if (r == max)
							H = (g-b)/(max-min)  ;
						if (g == max)
							H = 2 + (b-r)/(max-min)  ;
						if (b == max)
							H = 4 + (r-g)/(max-min)  ;
						H = H * 60;
						if (H < 0) 
							H = H + 360;
						//if((H<240)&&(H>120))
						if(H<120){
							int V=max;  
							int S=(max-min)/max;
							//mBitmap.setPixel(i, j, (0x31a8E3));
							mBitmap.setPixel(i, j, (argb&0xFF000000)|r<<8|g<<16|b/*getRGBFromHSV(H+180,S,V)*/);
						}
						
				/*double hh;
				double pi=(double)3.1415926535;
				if(b<=g){
					hh=getHHWW((double)r,(double)g,(double)b);
				}else{
					hh=360-getHHWW((double)r,(double)g,(double)b);
				}
				Log.v("test",":"+hh);
				if((315<=hh)||(hh<23))
					mBitmap.setPixel(i, j, (0x31a8E3));*/
			}
		}
		Log.v("test","end");
		//颜色滤镜，将颜色矩阵应用于图片
	//	paint.setColorFilter(new ColorMatrixColorFilter(cm));
		//绘图
		canvas.drawBitmap(mBitmap, 0, 0, paint);
		
		Log.i("CMatrix", "--------->onDraw");


	} 
	double getHHWW(double R,double G,double B){
		double ddd=(2*R-G-B)/(2*Math.sqrt((R-G)*(R-G)+(R-B)*(G-B)));
		Log.v("test",""+ddd);
		return arccos(ddd);
	}
	/**
	   * 余弦反算函数，精度到0.03秒
	   * @param a double 余弦值
	   * @return double   角度(360)
	   */
	public double arccos(double a){
		
	    double b = 90.0, c0 = 0.0, c1 = 180.0;
	    if (a < 1 && a > -1){
	      do{
			if (Math.cos(b * Math.PI / 180) >= a){
			   c0 = b;
			   b = (c0 + c1) / 2;
			}
			if (Math.cos(b * Math.PI / 180) <= a){
			   c1 = b;
			   b = (c0 + c1) / 2;
			}
	      }
	      while (Math.abs(c0 - c1) > 0.00001);
	    }
	    return b;
	}
	///////////////////////////
	private int getMaxRGBWhichIs(int R,int G,int B){
		if(R>G){
			if(R>B)
				return R;
			else
				return B;
		}else{
			if(G>B)
				return G;
			else
				return B;
		}
 
	}
	private int getMinRGBWhichIs(int R,int G,int B){
		if(R<G){
			if(R<B)
				return R;
			else
				return B;
		}else{
			if(G<B)
				return G;
			else
				return B;
		}
	}
	/////////////////////////
	private int getRGBFromHSV(int H,int S,int V){
		int R,G,B;
		R=G=B=V;
		if (S == 0){ 
				return (R<<16)|(G<<8)|B;
		}else{
				int i= H/60;    
				//i = INTEGER(H)

				int f = H - i;   
				int a = V * ( 1 - S );    
				int b = V * ( 1 - S * f );
				int c = V * ( 1 - S * (1 - f ) );  

				switch(i){   
				case 0: R = V; G = c; B = a;      
				case 1: R = b; G = V; B = a;      
				case 2: R = a; G = V; B = c;      
				case 3: R = a; G = b; B = V;      
				case 4: R = c; G = a; B = V;      
				case 5: R = V; G = a; B = b;
				}
				return (R<<16)|(G<<8)|B;
		}
	}
}
