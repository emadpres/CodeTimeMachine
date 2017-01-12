package com.reveal.metrics;


import com.siyeh.ig.methodmetrics.CyclomaticComplexityInspection;

public class MetricCalculationResults
{
    static final int INVALID = -1;

    String code = "";
    int loc = INVALID;
    int cyclomaticComplexity = INVALID;

    public MetricCalculationResults(String code)
    {
        this.code = code;
    }

    public int getMetricValue(Metrics.Types metricType)
    {
        switch (metricType)
        {
            case NONE:
                return INVALID;
            case LOC:
                if(loc==INVALID)
                    LineOfCodeCalculator.getInstance().calculate(code, this);
                return loc;
            case CyclomaticComplexity:
                if(cyclomaticComplexity==INVALID)
                    CyclomaticComplexityCalculator.getInstance().calculate(code, this);
                return cyclomaticComplexity;
        }
        return INVALID;
    }
}
