package com.reveal.testtimemachine;


import java.awt.*;

public class MyRenderer
{
    float Y_OFFSET_FACTOR = 250;
    final float BASE_DEPTH = 2.0f; // Min:1.0 // ?? maybe 0
    final float TIME_LINE_GAP = 0.1f;
    private static final MyRenderer instance = new MyRenderer();

    private MyRenderer() {
        if (instance != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static MyRenderer getInstance() {
        return instance;
    }



    public int render3DTo2D(int dis, float renderingDepth)
    {
        return ((int) (dis / renderingDepth));
    }


    public Point render3DTo2D(int x, int y, float renderingDepth)
    {
        Point p = new Point();
        p.x = x;
        p.y = y - (int) (Math.log(renderingDepth-BASE_DEPTH + Math.exp(0)) * Y_OFFSET_FACTOR);
        return p;
    }

    public Point calculateTimeLinePoint(int xCenterDefault, int yCenterDefault, int wDefault, int hDefault, float renderingDepth)
    {
        Point p = MyRenderer.getInstance().render3DTo2D(xCenterDefault, yCenterDefault, renderingDepth);
        int w = MyRenderer.getInstance().render3DTo2D(wDefault, renderingDepth);
        int h = MyRenderer.getInstance().render3DTo2D(hDefault, renderingDepth);
        p.x = p.x - w/2 - (int)(TIME_LINE_GAP*w);
        p.y = p.y - h/2;
        return p;
    }

    public Point calculateChartTimeLinePoint(int xCenterDefault, int yCenterDefault, int wDefault, int hDefault, float renderingDepth)
    {
        Point p = MyRenderer.getInstance().render3DTo2D(xCenterDefault, yCenterDefault, renderingDepth);
        int w = MyRenderer.getInstance().render3DTo2D(wDefault, renderingDepth);
        int h = MyRenderer.getInstance().render3DTo2D(hDefault, renderingDepth);
        p.x = p.x + w/2 + (int)(TIME_LINE_GAP*w);
        p.y = p.y - h/2;
        return p;
    }

}
