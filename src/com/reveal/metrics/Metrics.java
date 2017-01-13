package com.reveal.metrics;


public class Metrics
{
    public static enum Types
    {NONE, LOC, CyclomaticComplexity, LineOfComment /*update getCalculatorForType(..) after addding new item here*/};

    static public MetricCalculatorBase getCalculatorForType(Types requestedType)
    {
        switch (requestedType)
        {
            case NONE:
                return null;
            case LOC:
                return LineOfCodeCalculator.getInstance();
            case CyclomaticComplexity:
                return ClassCyclomaticComplexityCalculator.getInstance();
            case LineOfComment:
                return LineOfCommentCalculator.getInstance();
        }
        return null;
    }
}
