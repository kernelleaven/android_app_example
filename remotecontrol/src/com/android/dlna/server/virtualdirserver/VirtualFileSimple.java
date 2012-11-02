package com.android.dlna.server.virtualdirserver;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class VirtualFileSimple {
	public final static String FILETYPE_AUDIOID="object.item.audioItem.musicTrack";
	public final static String FILETYPE_VIDEOID="object.item.videoItem";
	public final static String FILETYPE_IMAGEID="object.item.imageItem.photo";
	public final static String FILETYPE_FOLDERID="object.container.storageFolder";
	
	
	public final static int VIRTUALMAXNUM=10000;
	public final static String PATHSPLIT="/";
	private static boolean [] allvirtualids=null;
	
	private static void initVirtualIds(){
		allvirtualids=new boolean [VIRTUALMAXNUM];
		for(int i=0;i<VIRTUALMAXNUM;i++){
			allvirtualids[i]=false;			//����ط�Ӧ�ûָ������˳����״̬//???
		}
	}
	private static int allocMyVirtualIds(){
		if(null == allvirtualids)
			initVirtualIds();
		for(int i=0;i<VIRTUALMAXNUM;i++){
			if(!allvirtualids[i])
				return i;
		}
		Log.v("log","allocMyVirtualIds fail ");
		return -1;
	}
	
	private String meidapath="";
	private String mediatitle="";
	private String mediaartist="";
	private String mediaalbum="";
	private String mediaalbumiconpath="";
	private Drawable meidaalbumicondraw=null;
	
	public int virtualid=0;		//���id��������Ŀ¼����Ψһ�ġ�
	public String virtualapth="";	
	public String virtualtitle="";	//�ļ�����������Ŀ¼��ͬһ��Ŀ¼�¿����ظ�������Ŀ¼�����ظ�
	public String virtualparentpath="";
	public String virtualsize="";
	public String virtualtype="";
	public Drawable virtualicondraw=null;
	public String virtualiconpath="";
	
	public VirtualFileSimple(String path,String title,String artist,String album,String albumiconpath,String filetype) {
		virtualid=allocMyVirtualIds();
		meidapath=path;
		mediatitle=title;
		mediaartist=artist;
		mediaalbum=album;
		mediaalbumiconpath=albumiconpath;
		virtualtype=filetype;
	}
	
	public String getMediaPath(){
		return meidapath;
	}
	public String getMediaTitle(){
		return mediatitle;
	}
	public String getMediaArtist(){
		return mediaartist;
	}
	public String getMediaAlbum(){
		return mediaalbum;
	}
	public String getMediaAlbumIconPath(){
		return mediaalbumiconpath;
	}
	public String getMediaType(){
		return virtualtype;
	}
}
