package com.reveal.metrics;


import java.util.HashMap;
import java.util.Map;

public class MetricCalculationResults
{
    static final int INVALID = -1;

    String code = "", groupID = "";

    int loc = INVALID;
    static Map<String, Integer> MaxLOCPerGroup = new HashMap<String, Integer>();

    int classCyclomaticComplexity = INVALID;
    static Map<String, Integer> MaxCCPerGroup = new HashMap<String, Integer>();

    int lineOfComment = INVALID;
    static Map<String, Integer> MaxLineOfCommentPerGroup = new HashMap<String, Integer>();


    public MetricCalculationResults(String code, String groupID)
    {
        this.code = code;
        this.groupID = groupID;
    }

    public void setMetricMaxValue(Metrics.Types metricType, int newMaxValue)
    {
        switch (metricType)
        {
            case NONE:
                return;
            case LOC:
                MaxLOCPerGroup.put(groupID, newMaxValue);
                break;
            case CyclomaticComplexity:
                MaxCCPerGroup.put(groupID, newMaxValue);
                break;
            case LineOfComment:
                MaxLineOfCommentPerGroup.put(groupID, newMaxValue);
                break;
        }
    }


    public int getMetricMaxValue(Metrics.Types metricType)
    {
        int max = INVALID;
        switch (metricType)
        {
            case NONE:
                break;
            case LOC:
                max = MaxLOCPerGroup.getOrDefault(groupID, INVALID).intValue();
                break;
            case CyclomaticComplexity:
                max = MaxCCPerGroup.getOrDefault(groupID, INVALID).intValue();
                break;
            case LineOfComment:
                max = MaxLineOfCommentPerGroup.getOrDefault(groupID, INVALID).intValue();
                break;
        }

        return max;
    }

    public int getMetricValue(Metrics.Types metricType)
    {
        int v = INVALID;
        switch (metricType)
        {
            case NONE:
                break;
            case LOC:
                v = loc;
                break;
            case CyclomaticComplexity:
                v = classCyclomaticComplexity;
                break;
            case LineOfComment:
                v = lineOfComment;
                break;
        }
        return v;
    }
}
