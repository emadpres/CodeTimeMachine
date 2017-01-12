package com.reveal.metrics;


import com.siyeh.ig.methodmetrics.CyclomaticComplexityInspection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MetricCalculationResults
{
    static final int INVALID = -1;

    String code = "", groupID = "";
    int loc = INVALID;
    static Map<String, Integer> MaxLOCPerGroup = new HashMap<String, Integer>();
    int CC = INVALID;
    static Map<String, Integer> MaxCCPerGroup = new HashMap<String, Integer>();



    public MetricCalculationResults(String code, String groupID)
    {
        this.code = code;
        this.groupID = groupID;
    }

    public int getMaxValue(Metrics.Types metricType)
    {
        switch (metricType)
        {
            case NONE:
                return INVALID;
            case LOC:
              return MaxLOCPerGroup.getOrDefault(groupID, 1).intValue();
            case CyclomaticComplexity:
                return MaxCCPerGroup.getOrDefault(groupID, 1).intValue();
        }
        return 1;
    }

    public int getMetricValue(Metrics.Types metricType)
    {
        switch (metricType)
        {
            case NONE:
                return INVALID;
            case LOC:
                if(loc==INVALID)
                {
                    LineOfCodeCalculator.getInstance().calculate(code, this);
                    if(loc > MaxLOCPerGroup.getOrDefault(groupID, -1).intValue())
                        MaxLOCPerGroup.put(groupID, loc);
                }
                return loc;
            case CyclomaticComplexity:
                if(CC==INVALID)
                {
                    CyclomaticComplexityCalculator.getInstance().calculate(code, this);
                    if(CC > MaxCCPerGroup.getOrDefault(groupID, -1).intValue())
                        MaxCCPerGroup.put(groupID, CC);
                }
                return CC;
        }
        return INVALID;
    }
}
