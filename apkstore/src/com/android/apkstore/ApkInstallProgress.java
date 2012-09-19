package com.android.apkstore;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class ApkInstallProgress extends Activity{
  private int progress;
  private ProgressDialog progressDialog;

  public void CloseProgress(){
	  this.progress = 0;
	  this.progressDialog.dismiss();
	  }

  public void IncProgress(){
	  this.progressDialog.incrementProgressBy(1);
	  }

  public void SetProgress(int paramInt){
	  this.progress = paramInt;
	  this.progressDialog.setProgress(this.progress);
	  }

  public void showProgressDialog(int paramInt1, Context paramContext, int paramInt2){
	  Log.d("ApkInstall", "showProgressDialog");
	  this.progressDialog = new ProgressDialog(paramContext);
	  this.progressDialog.setTitle(paramContext.getResources().getString(R.string.installapk));
	  this.progressDialog.setMessage(paramContext.getResources().getString(R.string.nogoback));
	  this.progressDialog.setProgressStyle(paramInt1);
	  this.progressDialog.setMax(paramInt2);
	  //this.progress = 0;
	  //this.progressDialog.setProgress(this.progress);
	  this.progressDialog.show();
	  }
}