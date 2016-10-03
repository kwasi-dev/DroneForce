package com.logan20.droneforceshared.droneforceshared.ARdroneAPI.commands;

public class FlatTrimCommand extends ATCommand
{
    @Override
    protected String getID()
    {
        return "FTRIM";
    }

    @Override
    protected Object[] getParameters()
    {
        return new Object[] {};
    }
}
