package com.android.haizitopingying;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HanzitopingyingActivity extends Activity {
	TextView tView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tView=(TextView)findViewById(R.id.tv);
        tView.setTextSize(24);
        tView.setTextColor(Color.WHITE);
       /* String hanziString="ºº";
        //String hanziString="C";
        //String pinyinString= Hanzitopinyin.toPinYin(hanziString.charAt(0));
        String pinyinString= Hanzitopinyin.converterToSpell(hanziString);
        tView.setText("Æ´Òô£º"+pinyinString+"\n"+"ºº×Ö£º"+hanziString);
        // setContentView(tView);
         * 
         */
        Button bt=(Button)findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBtClick();
			}
		});
    }
    
    public void onBtClick(){
    	String hanziString="ºº";
        String pinyinString= converterToSpell(hanziString);
        tView.setText("Æ´Òô£º"+pinyinString+"\n"+"ºº×Ö£º"+hanziString);
    }
    
    public String converterToSpell(String chinese){
        String pinyinName = "";
        char[] nameChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
                if (nameChar[i] > 128) {
                        try {
                                pinyinName += PinyinHelper.toHanyuPinyinStringArray(
                                                nameChar[i], defaultFormat)[0];
                        } catch (BadHanyuPinyinOutputFormatCombination e) {
                                e.printStackTrace();
                        }
                } else {
                        pinyinName += nameChar[i];
                }
        }
        return pinyinName;
    }
}