package com.android.dlna.server.virtualdirserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class VirtualDir {

	
	////////////////////////////////////////////////////////////////////
	
	VirtualDir(String path){
		virtualapth=path;
	}

	public int virtualid=0;		//���id��������Ŀ¼����Ψһ�ġ�
	public String virtualapth="";	
	public String virtualtitle="";	//�ļ�����������Ŀ¼��ͬһ��Ŀ¼�¿����ظ�������Ŀ¼�����ظ�
	public String virtualparentpath="";
	public String virtualsize="";
	public String virtualtype="";
	public Drawable virtualicondraw=null;
	public String virtualiconpath="";
	
	public int parentvirtualid=0;
	public ArrayList<Integer> subvirtualids=null;
	
	boolean isExists(){
		//if
		return false;
	}
	
	boolean creatDir(){
		
		if(-1==virtualid)
			return false;
		return true;
	}
}

