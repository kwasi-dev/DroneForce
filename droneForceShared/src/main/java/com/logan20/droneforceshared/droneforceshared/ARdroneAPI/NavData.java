package com.logan20.droneforceshared.droneforceshared.ARdroneAPI;


import com.logan20.droneforceshared.droneforceshared.ARdroneAPI.data.navdata.ControlAlgorithm;
import com.logan20.droneforceshared.droneforceshared.ARdroneAPI.data.navdata.CtrlState;
import com.logan20.droneforceshared.droneforceshared.ARdroneAPI.data.navdata.FlyingState;
import com.logan20.droneforceshared.droneforceshared.ARdroneAPI.data.navdata.Mode;
import com.logan20.droneforceshared.droneforceshared.ARdroneAPI.data.navdata.vision.VisionTag;

import java.util.List;

public interface NavData {

    /**
     * 
     * @return value in meters
     */
    public abstract float getAltitude();

    public abstract int getBattery();

    public abstract ControlAlgorithm getControlAlgorithm();

    public abstract CtrlState getControlState();

    public abstract float getLongitude();

    public abstract Mode getMode();

    /**
     * 
     * @return value in degrees
     */
    public abstract float getPitch();

    /**
     * 
     * @return value in degrees
     */
    public abstract float getRoll();

    public abstract int getSequence();

    public abstract float getVx();

    public abstract float getVz();

    /**
     * 
     * @return value in degrees
     */
    public abstract float getYaw();

    public abstract boolean isAcquisitionThreadOn();

    public abstract boolean isADCWatchdogDelayed();

    public abstract boolean isAltitudeControlActive();

    public abstract boolean isAngelsOutOufRange();

    public abstract boolean isATCodedThreadOn();

    public abstract boolean isBatteryTooHigh();

    public abstract boolean isBatteryTooLow();

    public abstract boolean isCommunicationProblemOccurred();

    public abstract boolean isControlReceived();

    public abstract boolean isControlWatchdogDelayed();

    public abstract boolean isCutoutSystemDetected();

    public abstract boolean isEmergency();

    public abstract boolean isFlying();

    public abstract boolean isGyrometersDown();

    public abstract boolean isMotorsDown();

    public abstract boolean isNavDataBootstrap();

    public abstract boolean isNavDataDemoOnly();

    public abstract boolean isNavDataThreadOn();

    public abstract boolean isNotEnoughPower();

    public abstract boolean isPICVersionNumberOK();

    public abstract boolean isTimerElapsed();

    public abstract boolean isTooMuchWind();

    public abstract boolean isTrimReceived();

    public abstract boolean isTrimRunning();

    public abstract boolean isTrimSucceeded();

    public abstract boolean isUltrasonicSensorDeaf();

    public abstract boolean isUserFeedbackOn();

    public abstract boolean isVideoEnabled();

    public abstract boolean isVideoThreadOn();

    public abstract boolean isVisionEnabled();

    public abstract FlyingState getFlyingState();

    public abstract List<VisionTag> getVisionTags();

}