package com.android.apkstore;


import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.http.util.EncodingUtils;

public class ApkLocation extends Activity
{
  private String FileContent;
  private String FilePath = Environment.getExternalStorageDirectory() + "/ApkInstall/ApkLocation.txt";
  private byte[] buffer = new byte[102400];
  int byteCount;
  private String encoding;
  private FileInputStream fin;

  public String ReadFile(){
	  try{
		  File file=new File(FilePath);
		  if(!file.exists()){
			  String filepath=FilePath.substring(0, FilePath.lastIndexOf("/"));
			  File filep=new File(filepath);
			  filep.mkdirs();
			  file.createNewFile();
			  }
		  
		  this.fin = new FileInputStream(this.FilePath);
		  this.fin.read(this.buffer);
		  this.FileContent = EncodingUtils.getString(this.buffer, "gb2312");
		  this.fin.close();
		  return this.FileContent;
		  }catch (Exception localException){
			  this.FileContent = new String("NULL");
			  //Log.d("ApkInstall", "ReadFile Exception");
			  }
	  return null;
	  }

  public String getCharSet()throws IOException{
	  String str = "gb2312";
	  try{
		  FileInputStream localFileInputStream = new FileInputStream(this.FilePath);
		  byte[] arrayOfByte = new byte[3];
		  localFileInputStream.read(arrayOfByte);
		  if ((arrayOfByte[0] == -1) && (arrayOfByte[1] == -2))
			  str = "UTF-16";
		  if ((arrayOfByte[0] == -2) && (arrayOfByte[1] == -1))
			  str = "Unicode";
		  if ((arrayOfByte[0] == -17) && (arrayOfByte[1] == -69) && (arrayOfByte[2] == -65))
			  str = "UTF-8";
		  //Log.d("ApkInstall", "code is " + str);
		  localFileInputStream.reset();
		  }catch (FileNotFoundException localFileNotFoundException){
			  localFileNotFoundException.printStackTrace();
			  }
	  return str;
	  }
}