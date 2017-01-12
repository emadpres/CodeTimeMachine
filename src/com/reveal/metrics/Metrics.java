package com.reveal.metrics;


public class Metrics
{
    public static enum Types
    {NONE, LOC, CyclomaticComplexity /*update getCalculatorForType(..) after addding new item here*/};

    /*static public MetricCalculatorBase getCalculatorForType(Types requestedType)
    {
        switch (requestedType)
        {
            case NONE:
                return null;
            case LOC:
                return new LineOfCodeCalculator();
            case CyclomaticComplexity:
                return new CyclomaticComplexityCalculator();
        }
        return null;
    }*/
}
