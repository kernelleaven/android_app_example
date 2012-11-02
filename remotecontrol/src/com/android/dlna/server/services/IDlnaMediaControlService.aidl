package com.android.dlna.server.services;

interface IDlnaMediaControlService
{
    void setMediaVolume(int volume);
    void tryGetPositionInfo();
    void tryGetMediaInfo();
    void tryGetVolume();
    
    /*
     *		Map<String,String> = Map<UUID,FrendlyName>
     */
    String getAllDMRUUIDAndNameStrings();
    void setCmdToControlPointByRendererCall(int cmd ,String value,String data);
    void setCmdToCtlPlayState(String state);
    void setCmdToCtlMuteState(String state);
    void setCmdToCtlSetSeekTime(String reltime);
    int setCurrentPlayRenderer(String uuid);
    String getDescriptionUrlByDMRUUID(String uuid);
    String getDMRIpByUUID(String uuid);
    String setDMRAVTransportURI(String url,String didl);
}