package com.logan20.droneforceshared.dronePiloter;

/**
 * Created by kwasi on 4/12/2016.
 */
public interface DronePilot {
    void setYaw(byte val);
    void setPitch(byte val);
    void setRoll(byte val);
    void setFlag(byte val);
    void takeOff();
    void land();
    void emergencyLand();
}
