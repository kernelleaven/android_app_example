package com.android.dlna.server.virtualdirserver;

import android.graphics.drawable.Drawable;

public class VirtualFile extends VirtualDir{
	VirtualFile(String path) {
		super(path);
		// TODO Auto-generated constructor stub
	}
	public final static String FILETYPE_AUDIOID="object.item.audioItem.musicTrack";
	public final static String FILETYPE_VIDEOID="object.item.videoItem";
	public final static String FILETYPE_IMAGEID="object.item.imageItem.photo";
		
	private String mediatitle="";
	private String mediaartist="";
	private String mediaalbum="";
	private String mediaalbumiconpath="";
	private Drawable meidaalbumicondraw=null;
	private String meidapath="";
}
