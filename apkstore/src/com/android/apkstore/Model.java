package com.android.apkstore;


import android.graphics.drawable.Drawable;

public class Model
{
  private char InstallLocation = '\000';
  private Drawable icon;
  private String name;
  private boolean selected;

  public Model(String paramString)
  {
    this.name = paramString;
    this.selected = false;
  }

  public void SetInstallLocation(char paramChar)
  {
    this.InstallLocation = paramChar;
  }

  public Drawable getIcon()
  {
    return this.icon;
  }

  public char getInstallLocation()
  {
    return this.InstallLocation;
  }

  public String getName()
  {
    return this.name;
  }

  public boolean isSelected()
  {
    return this.selected;
  }

  public void setIcon(Drawable paramDrawable)
  {
    this.icon = paramDrawable;
  }

  public void setName(String paramString)
  {
    this.name = paramString;
  }

  public void setSelected(boolean paramBoolean)
  {
    this.selected = paramBoolean;
  }
}