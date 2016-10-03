
package com.logan20.droneforceshared.droneforceshared.ARdroneAPI;

public interface DroneVideoListener
{
    void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize);
}
