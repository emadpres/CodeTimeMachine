package com.reveal.codetimemachine;

import java.awt.*;

/**
 * Created by emadpres on 12/30/16.
 */
public class DrawingHelper
{
    static public void drawStringCenter(Graphics g2d, String text, int x, int y)
    {
        int textLengthInPixel= g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, x-textLengthInPixel/2, y);
    }

    static public int howManyCharFitsInWidth(Graphics g2d, String text, int maxWidth)
    {
        for(int i=1; i<text.length(); i++)
        {
            int textLengthInPixel= g2d.getFontMetrics().stringWidth(text.substring(0,i));
            if(textLengthInPixel>maxWidth)
                return i-1;
        }
        return text.length();
    }
}
