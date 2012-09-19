package com.android.apkstore;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver.Stub;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.android.apkstore.R.drawable;
import com.android.apkstore.R.layout;
import com.android.apkstore.R.string;

public class ApkInstallActivity extends ListActivity
  implements AdapterView.OnItemClickListener
{
  static final int FAILED = 0;
  static final int SUCCEEDED = 1;
  static int iIndex = 0;
  private String[] APK_CLASS_INFO = new String[500];
  private String[] APK_NAME = new String[500];
  private final String APK_PATH = new String("/sdcard/ApkInstall/");
  public ApkLocation ApkLocationFile = new ApkLocation();
  private ApkInstallProgress CustomProgress;
  private String FileLocation = new String();
  private final int GONE = 8;
  private final int INSTALL_COMPLETE = 1;
  private final int INSTALL_NEXT = -100;
  private final int INSTALL_NOMEMORY = -4;
  private final int INVISIBLE = 4;

  private final int VISIBLE = 0;
  public ArrayAdapter<Model> adapter;
  private boolean bProcessing = false;
  public Drawable gbadIcon = null;
  private int iInstalled = 0;
  private final int info_finish = 100;
  private final int info_noApk = -101;
  private final int info_nomemory = -1;
  private List<Model> list;
  
  private Handler UpdateHandler = new Handler(){
	  public void handleMessage(Message paramMessage){
		  super.handleMessage(paramMessage);
		  if (ApkInstallActivity.iIndex == 0)
			  ApkInstallActivity.this.NotifyInfo(info_noApk);
		  ApkInstallActivity.this.UpdateListView();
		  }
	  };
	  
  private Handler mHandler = new Handler(){
	  public void handleMessage(Message paramMessage){
		  switch (paramMessage.arg1){
		  case INSTALL_COMPLETE:
		  case INSTALL_NEXT:
			  if(paramMessage.arg1 == SUCCEEDED){
				  ApkInstallActivity.this.CustomProgress.IncProgress();
				  if(ApkInstallActivity.this.iInstalled < ApkInstallActivity.iIndex){
					  ApkInstallActivity.this.intallApk();
				  }else{
					  ApkInstallActivity.this.CustomProgress.CloseProgress();
					  ApkInstallActivity.this.RefreshScreen();
					  ApkInstallActivity.this.NotifyInfo(info_finish);
					  Log.d("ApkInstall", "apk is INSTALL_COMPLETE");
				  }
			  }
			  break;
		  case INSTALL_NOMEMORY:
			  ApkInstallActivity.this.CustomProgress.CloseProgress();
		      ApkInstallActivity.this.RefreshScreen();
		      ApkInstallActivity.this.NotifyInfo(info_nomemory);
			  break;
			  default:
				  break;
		  }		  
     }
 };
	  /*
	  private Handler mHandler = new Handler(){
		  public void handleMessage(Message paramMessage){
			  switch (paramMessage.what){
			  case INSTALL_COMPLETE:
			  //case INSTALL_NEXT:
				  ApkInstallActivity.this.CustomProgress.IncProgress();
				  if(paramMessage.arg1 == INSTALL_NEXT){
					  //ApkInstallActivity.this.CustomProgress.IncProgress();
					  if(ApkInstallActivity.this.iInstalled < ApkInstallActivity.iIndex){
						  ApkInstallActivity.this.intallApk();
					  }
				  }
				  else if (paramMessage.arg1 == SUCCEEDED){
					  ApkInstallActivity.this.CustomProgress.CloseProgress();
					  ApkInstallActivity.this.RefreshScreen();
					  ApkInstallActivity.this.NotifyInfo(info_finish);
					  Log.d("ApkInstall", "apk is INSTALL_COMPLETE");
				  }
				  break;
			  case INSTALL_NOMEMORY:
				  ApkInstallActivity.this.CustomProgress.CloseProgress();
			      ApkInstallActivity.this.RefreshScreen();
			      ApkInstallActivity.this.NotifyInfo(info_nomemory);
				  break;
				  default:
					  break;
			  }		  
	     }
	 };
	 */
  public ProgressDialog waitpop;

  private Model get(String paramString){
	  return new Model(paramString);
	  }

  private void intallApk(){
	  this.bProcessing = true;
	  int i = ((Model)this.list.get(this.iInstalled)).getInstallLocation();
	  PackageManager localPackageManager = getPackageManager();
	  if (((Model)this.list.get(this.iInstalled)).isSelected()){
		  new StringBuilder().append(this.APK_PATH).append(this.APK_NAME[this.iInstalled]).toString();    	
		  localPackageManager.installPackage(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getName()+ "/ApkInstall/" + this.APK_NAME[this.iInstalled])),new PackageInstallObserver(), i, this.APK_CLASS_INFO[this.iInstalled]);
  	  }
	  this.iInstalled = (1 + this.iInstalled);
	  Message localMessage = this.mHandler.obtainMessage(SUCCEEDED);
	  localMessage.arg1 = INSTALL_NEXT;
	  this.mHandler.sendMessage(localMessage);  
	  Log.d("ApkInstall", "Send msg!!");
	}

  
  private void printDirectory(File paramFile)
  {
    if (!paramFile.isDirectory()){
      String str;
      PackageManager localPackageManager;
      PackageParser.Package localPackage;
      if (IsApk(paramFile.getName())){
        this.APK_NAME[iIndex] = " ";
        this.APK_CLASS_INFO[iIndex] = " ";
        this.APK_NAME[iIndex] = paramFile.getName();
        str = this.APK_PATH + this.APK_NAME[iIndex];
        if (IsEmptyApk(str)){
          localPackageManager = getPackageManager();
          PackageParser localPackageParser = new PackageParser(str);
          DisplayMetrics localDisplayMetrics = new DisplayMetrics();
          localDisplayMetrics.setToDefaults();
          File localFile = new File(str);
          localPackage = localPackageParser.parsePackage(localFile, str, localDisplayMetrics, 0);
          try
          {
            ApplicationInfo localApplicationInfo = localPackage.applicationInfo;
            this.APK_CLASS_INFO[iIndex] = localApplicationInfo.packageName;
            try
            {
              if (localPackageManager.getPackageInfo(this.APK_CLASS_INFO[iIndex], PackageManager.GET_UNINSTALLED_PACKAGES) != null)
                Log.d("ApkInstall", "pi != null");
              else{
                //localException = localException;
                Log.d("ApkInstall", str + " parser error");
              }
            }
            catch (PackageManager.NameNotFoundException localNameNotFoundException)
            {
            	new ApkInfo();
            	ApkInfo localApkInfo = getApkInfo(str);
            	int k = this.APK_NAME[iIndex].length();
            	localApkInfo.name = this.APK_NAME[iIndex].substring(0, k - 4);
            	this.list.add(get(localApkInfo.name));
            	((Model)this.list.get(iIndex)).setSelected(true);
            	((Model)this.list.get(iIndex)).setIcon(localApkInfo.icon);
            	if (this.FileLocation.indexOf(this.APK_NAME[iIndex]) != -1){
                    Log.d("ApkInstall", "INSTALL_EXTERNAL");
                    ((Model)this.list.get(iIndex)).SetInstallLocation('\b');
            	}
            	iIndex = 1 + iIndex;
            }
          }
          catch (Exception localException)
          {
            
          }
        }
      }
    }else{
	    File[] arrayOfFile = paramFile.listFiles();
	    int j = arrayOfFile.length;
	    for (int i = 0;i<j;i++){
	      printDirectory(arrayOfFile[i]);
	    }
    }
  }

  
  public long GetFileSize(String paramString)
    throws IOException
  {
    Integer localInteger = Integer.valueOf(new FileInputStream(new File(paramString)).available());
    return localInteger.intValue();
  }

///判断是否是.APK文件
  public boolean IsApk(String paramString){
	  int i = paramString.length();
	  String str = paramString.substring(i - 4, i);    
    
	  if (str.equalsIgnoreCase(".apk")){
		  return true;
		  }else{
			  return false;
			  }
  }

//判断是否是一个空的APK文件
  public boolean IsEmptyApk(String paramString){ 	  
	  try{
		  long l = GetFileSize(paramString);
		  Log.d("ApkInstall", "IsEmptyApk is l=" + l);
		  if (l > 0L){
			  Log.d("ApkInstall", "IsEmptyApk is true" );
			  return true;
			  }
		  }catch (Exception localException){
			  Log.d("ApkInstall", "IsEmptyApk is false" );
			  return false;
		  }
	  return false;
  }

  public void NotifyInfo(int paramInt){
	  switch (paramInt){
	  case info_nomemory:
		  Log.d("ApkInstall", "install failure - no enough memory");
		  Toast.makeText(this, getResources().getString(string.noenoughmemory), 1).show();
		  break;
	  case info_noApk:
		  Log.d("ApkInstall", "not found APK in file");
		  Toast.makeText(this, getResources().getString(string.noApk), 1).show();
		  break;
	  case info_finish:
		  Log.d("ApkInstall", "finish install APK");
		  Toast.makeText(this, getResources().getString(string.alreadyinstall), 1).show();
		  break;
	  default:
		  break;
		  }
	  }

  //刷新列表
  public void RefreshScreen(){  
	  Log.d("ApkInstall", "RefreshScreen");
	  iIndex = 0;
	  for (int i = 0; i < APK_CLASS_INFO.length; i++){
		  this.APK_NAME[i] = " ";
		  this.APK_CLASS_INFO[i] = " ";
	  }
	  this.list = new ArrayList();
	  openwaitpop();
	  new ApkSearchThread().start();
	  }

  //查找APK文件
  public void SearchApkFile(){
	  this.FileLocation = this.ApkLocationFile.ReadFile();
	  Log.d("ApkInstall", "FileLocation" + this.FileLocation+"\n");
	  printDirectory(new File(this.APK_PATH));
	  closewaitpot();
	  this.UpdateHandler.sendEmptyMessage(1);
	  }

  //全选APK
  public void SeleckAll(){
	  if(getSelectNum() >= iIndex){
		  for (int k = 0; k < iIndex; k++){
			  ((Model)this.list.get(k)).setSelected(false);
		  }
	  }else{
		  for (int j = 0; j < iIndex; j++){
			  if(!((Model)this.list.get(j)).isSelected()){
				  ((Model)this.list.get(j)).setSelected(true);
			  }
		  }
	  }
	  this.adapter.notifyDataSetChanged();
  }

  
  public void UpdateListView(){
	  this.adapter = new InteractiveArrayAdapter(this, this.list);
	  setListAdapter(this.adapter);
	  }

  
  public void clickHandler(View paramView){
	  switch (paramView.getId()){
	  	case R.id.btn:
	    if (iIndex == 0){
	      startMarket();
	      System.exit(0);
	    }
	    Log.d("ApkInstall", "start install");
	    this.iInstalled = 0;
	    this.CustomProgress = new ApkInstallProgress();
	    this.CustomProgress.showProgressDialog(1, this, getSelectNum());
	    intallApk();
	    break;	  	
	  	case R.id.btn2:
		    startMarket();                                                                                                                                                                                                                                                                                                                                            
		    System.exit(0);
		    break;
	  	case R.id.btAll:
		    SeleckAll();
		    break;
	  	case R.id.progress:
	  	case R.id.progresstext:
	  		break;
	  	default:
	  		break;
	    }
	 	}  
  
  
  public void closewaitpot(){
	  this.waitpop.dismiss();
	  this.waitpop = null;
	  }

  //获取安装APK信息
  public ApkInfo getApkInfo(String paramString)
  {
    ApkInfo localApkInfo = new ApkInfo();

    String PATH_PackageParser = "android.content.pm.PackageParser";  
	String PATH_AssetManager = "android.content.res.AssetManager";

    if(paramString != null){
    	try{
    		Class pkgParserCls = Class.forName(PATH_PackageParser);
    		Class[] typeArgs = new Class[1];
    		typeArgs[0] = String.class;
    		Constructor pkgParserCt  = pkgParserCls.getConstructor(typeArgs);
    		Object[] valueArgs  = new Object[1];
    		valueArgs[0] = paramString;
    		Object pkgParser = pkgParserCt.newInstance(valueArgs);
	      
    		Log.d("ANDROID_LAB", "pkgParser:" + pkgParser.toString());      
    		// 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况            
    		DisplayMetrics metrics = new DisplayMetrics();
    		metrics.setToDefaults();
	      
    		typeArgs = new Class[4];
    		typeArgs[0] = File.class;
    		typeArgs[1] = String.class;
    		typeArgs[2] = DisplayMetrics.class;
    		typeArgs[3] = Integer.TYPE;
    		Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
	      
    		valueArgs  = new Object[4];
    		File localFile = new File(paramString);
    		valueArgs[0] = localFile;
    		valueArgs[1] = paramString;
    		valueArgs[2] = metrics;
    		valueArgs[3] = Integer.valueOf(0);
    		Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
    		// 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
    		ApplicationInfo appInfoFld = (ApplicationInfo)pkgParserPkg.getClass().getDeclaredField("applicationInfo").get(pkgParserPkg);
    		Log.d("ANDROID_LAB", "pkg:" + appInfoFld.packageName + " uid=" + appInfoFld.uid);
	
    		Class assetMagCls = Class.forName(PATH_AssetManager);
	      
    		Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);// 
    		//Object assetMag = assetMagCt.newInstance();
    		Object assetMag = assetMagCt.newInstance((Object[]) null);
	
    		typeArgs = new Class[1];
    		typeArgs[0] = String.class;
    		Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
    		valueArgs = new Object[1];
    		valueArgs[0] = paramString;
    		assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
	      
    		Resources res = getResources();
	      
    		typeArgs = new Class[3];
    		typeArgs[0] = assetMag.getClass();
    		typeArgs[1] = res.getDisplayMetrics().getClass();
    		typeArgs[2] = res.getConfiguration().getClass();
    		Constructor resCt = Resources.class.getConstructor(typeArgs);
	      
    		valueArgs = new Object[3];
    		valueArgs[0] = assetMag;
    		valueArgs[1] = res.getDisplayMetrics();
    		valueArgs[2] = res.getConfiguration();
    		res = (Resources)resCt.newInstance(valueArgs);

			if (appInfoFld.labelRes != 0){
				localApkInfo.name = ((String)res.getText(appInfoFld.labelRes));
			}   
			if(appInfoFld.icon != 0){
				Drawable icon = res.getDrawable(appInfoFld.icon);
				localApkInfo.icon = icon;//res.getDrawable(appInfoFld.icon);
			}else{
				localApkInfo.icon = this.gbadIcon;
			}

	    	}catch (Exception localException){
	    		Log.d("ApkInstall", "getApkInfo Error");
	    		localApkInfo.name = "";
	    		localApkInfo.icon = this.gbadIcon;
	    	}
    }
    return localApkInfo;
  }

  //获取选择APK的个数
  public int getSelectNum(){
	  int i = 0;
	  for (int j = 0; j < iIndex; j++){
		  if (((Model)this.list.get(j)).isSelected()){
			  i++;
			  }
		  }
	  return i;
	  }

  /** TODO 判断内存卡是否已插入*/  
 public boolean hasStorage(){
	 String state = android.os.Environment.getExternalStorageState();
	 if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
		 return true;
		 }
	 return false;
	 } 

  /*
   通过查阅Android API可以得知android:onConfigurationChanged实际对应的是Activity里的onConfigurationChanged()方法。
   在AndroidManifest.xml中添加上诉代码的含义是表示在改变屏幕方向、弹出软件盘和隐藏软键盘时，
   不再去执行onCreate()方法，而是直接执行onConfigurationChanged()。如果不申明此段代码，
   按照Activity的生命周期，都会去执行一次onCreate()方法，而onCreate（）方法通常会在显示之前做一些初始化工作。
   所以如果改变屏幕方向这样的操作都去执行onCreate()方法，就有可能造成重复的初始化，降低程序效率是必然的了，
   而且更有可能因为重复的初始化而导致数据的丢失
   */
  @Override
  public void onConfigurationChanged(Configuration paramConfiguration){
	  super.onConfigurationChanged(paramConfiguration);
	  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    iIndex = 0;    
    
    for (int i = 0; i < 500; i++){
    	this.APK_NAME[i] = " ";
    	this.APK_CLASS_INFO[i] = " ";
    	}
    
    Log.d("ApkInstall", "Start");
    
    this.list = new ArrayList();
    setContentView(layout.main);
    this.gbadIcon = getResources().getDrawable(drawable.icon);
    
    if (!hasStorage()){
    	Toast.makeText(this, getResources().getString(string.nosdcard), SUCCEEDED).show();
    	}else{
    		if (!new File(this.APK_PATH).exists()){
    			Toast.makeText(this, getResources().getString(string.nofile), SUCCEEDED).show();
    			}
    		openwaitpop();
    		new ApkSearchThread().start();
    		getListView().setOnItemClickListener(this);
    		}
    }

  public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong){
    if (((Model)this.list.get(paramInt)).isSelected()){
      ((Model)this.list.get(paramInt)).setSelected(false);
    }else{
      ((Model)this.list.get(paramInt)).setSelected(true);
    }
    this.adapter.notifyDataSetChanged();
  } 
  
/*打开下载地址时pop框*/
  public void openwaitpop(){
	  this.waitpop = ProgressDialog.show(this, null, getResources().getString(string.wait), true);
	  }

  /*点击更多精彩button进入网页下载地址*/
  public void startMarket(){
	  try{
		  startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://market.dewav.com:8080/market/market.do")));
		  }catch (Exception localException){
			  Log.d("ApkInstall", "StartMarket Error");
		  }
	  }

  /*查找SD卡中的APK程序*/
  public class ApkSearchThread extends Thread{
	  public ApkSearchThread(){
    }

    public void run(){
    	ApkInstallActivity.this.SearchApkFile();
    	}
    }

  class PackageInstallObserver extends IPackageInstallObserver.Stub{
	  PackageInstallObserver(){
		  
	  }

	  public void packageInstalled(String paramString, int paramInt){
		  Log.d("ApkInstall", "packageInstalled.returnCode=" + paramInt);
		  Message localMessage = ApkInstallActivity.this.mHandler.obtainMessage(SUCCEEDED);
		  localMessage.arg1 = paramInt;
		  ApkInstallActivity.this.mHandler.sendMessage(localMessage);
		  }
   }

  static class ViewHolder{
	  protected CheckBox checkbox;
	  protected ImageView pic;
	  protected TextView text;
  	}

  static class ApkInfo{
	  protected Drawable icon;
	  protected String name;
  	}
}