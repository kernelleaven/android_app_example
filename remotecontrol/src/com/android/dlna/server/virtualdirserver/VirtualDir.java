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

	public int virtualid=0;		//这个id号在虚拟目录中是唯一的。
	public String virtualapth="";	
	public String virtualtitle="";	//文件名，在虚拟目录中同一个目录下可以重复。但是目录不能重复
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

