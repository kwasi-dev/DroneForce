package com.logan20.droneforceshared.droneforceshared.ARdroneAPI.data.logger;

public interface DataLogger {
    
    void log(ChannelDataChunk data);
    
    void logStreamContent(int data);
    
    public void finish();
}
