package com.logan20.droneforceshared.droneforceshared.ARdroneAPI.commands;

public class KeepAliveCommand extends ATCommand
{
    
    @Override
    protected String getID()
    {
        return "COMWDG";
    }

    @Override
    protected Object[] getParameters()
    {
        return new Object[] {};
    }
    
    @Override
    public int getPriority()
    {
        return VERY_HIGH_PRIORITY;
    }
}
