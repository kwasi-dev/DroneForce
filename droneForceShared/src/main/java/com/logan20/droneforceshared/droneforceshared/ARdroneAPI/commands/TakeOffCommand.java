package com.logan20.droneforceshared.droneforceshared.ARdroneAPI.commands;

public class TakeOffCommand extends RefCommand
{
    public TakeOffCommand()
    {
        value |= (1<<9);
    }
    
    public boolean isSticky()
    {
        return true;
    }
    
    public String getCategory()
    {
        return LAND_TAKEOFF_CATEGORY;
    }
}
