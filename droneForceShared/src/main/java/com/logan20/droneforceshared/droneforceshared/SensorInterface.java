package com.logan20.droneforceshared.droneforceshared;

/**
 * Created by kwasi on 9/13/2016.
 *
 */
public interface SensorInterface {
    void onDataChanged(SensorClass.Direction direction, float speed);
}
