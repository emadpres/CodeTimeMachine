package com.reveal.testtimemachine;

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
}
