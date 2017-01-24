package com.reveal.codetimemachine;


import java.awt.*;
import java.util.Random;

public class DistinctColorsProvider
{
    private static int PRESET_SIZE = 11;
    private static Random rand = new Random();
    private static Color[] colors = {
            Color.RED,
            new Color(17,198,56),
            Color.CYAN,
            new Color(254,1,255),
            new Color(74,111,227),
            new Color(142,6,59),
            new Color(239,151,8),
            new Color(211,80,106),
            new Color(15,207,192),
            new Color(247,156,212),
            new Color(2,63,165)
            /*in case you modified this list, update PRESET_SIZE*/
    };

    public static Color GetDistinctColor(int i /*0-based*/)
    {
        if(i<PRESET_SIZE)
            return colors[i];
        else
        {
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            return new Color(r,g,b);
        }
    }
}
/*
        {2,63,165},{125,135,185},{190,193,212},{214,188,192},{187,119,132},{142,6,59},
        {74,111,227},{133,149,225},{181,187,227},{230,175,185},{224,123,145},{211,63,106},
        {17,198,56},{141,213,147},{198,222,199},{234,211,198},{240,185,141},{239,151,8},
        {15,207,192},{156,222,214},{213,234,231},{243,225,235},{246,196,225},{247,156,212}.

              */
