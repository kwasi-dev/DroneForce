package com.logan20.droneforceshared.droneforceshared.ARdroneAPI.commands;

public class EmergencyCommand extends RefCommand
{
    public EmergencyCommand()
    {
        value |= (1<<8);
    }
}
