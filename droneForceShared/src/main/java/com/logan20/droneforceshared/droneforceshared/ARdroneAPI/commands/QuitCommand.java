package com.logan20.droneforceshared.droneforceshared.ARdroneAPI.commands;


import com.logan20.droneforceshared.droneforceshared.ARdroneAPI.DroneCommand;

public class QuitCommand extends DroneCommand
{
    @Override
    public int getPriority()
    {
        return MAX_PRIORITY;
    }
}
