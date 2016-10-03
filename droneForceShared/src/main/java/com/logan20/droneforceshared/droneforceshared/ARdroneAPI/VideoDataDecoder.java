package com.logan20.droneforceshared.droneforceshared.ARdroneAPI;


import com.logan20.droneforceshared.droneforceshared.ARdroneAPI.data.DataDecoder;

public abstract class VideoDataDecoder extends DataDecoder {
    
    private ARDrone drone;

    public VideoDataDecoder(ARDrone drone) {
        this.drone = drone;
    }

    public void notifyDroneWithDecodedFrame(int startX, int startY, int width, int height, int[] rgbArray, int offset, int scansize) {
        drone.videoFrameReceived(startX, startY, width, height, rgbArray, offset, scansize);
    }
}
