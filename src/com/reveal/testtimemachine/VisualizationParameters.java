package com.reveal.testtimemachine;


import java.awt.*;

public class VisualizationParameters
{
    public static final boolean IS_UI_IN_DEBUGGING_MODE = false;
    public static final boolean COLORFUL = false;
    public static final Color MOUSE_HOVERED_COLOR = Color.ORANGE;
    public static final int TOP_BAR_HEIGHT = 25;
    public static final int VIRTUAL_WINDOW_BORDER_TICKNESS = 1;

    public static final float LAYER_DISTANCE = 0.2f;
    public static final float LAYERS_DEPTH_ERROR_THRESHOLD = LAYER_DISTANCE/10;
    public static final float MIN_VISIBLE_DEPTH = -LAYER_DISTANCE;
    public static final float MAX_VISIBLE_DEPTH_CHANGE_VALUE = 0.3f;

    /////////////////


    public static float maxVisibleDepth = 2f;
}
