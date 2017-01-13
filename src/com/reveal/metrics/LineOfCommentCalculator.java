package com.reveal.metrics;


import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class LineOfCommentCalculator implements MetricCalculatorBase
{
    private static LineOfCommentCalculator instance = null;
    private static Metrics.Types myMetricType = Metrics.Types.LineOfComment;

    ArrayList<String> targetKeywords = new ArrayList<String>(Arrays.asList("//","/*"));

    @Override
    public void calculate(MetricCalculationResults results)
    {
        if(results.lineOfComment != MetricCalculationResults.INVALID) return;

        int sum =0 ;
        for( String eachKeyword: targetKeywords)
        {
            sum += StringUtils.countMatches(results.code, eachKeyword);
        }

        results.lineOfComment = sum;

        if(results.lineOfComment > results.getMetricMaxValue(myMetricType))
            results.setMetricMaxValue(myMetricType, results.lineOfComment);
    }

    @Override
    public String toString()
    {
        return "Cyclomatic Complexity";
    }

    private LineOfCommentCalculator() {/* to private constructor*/}

    public static LineOfCommentCalculator getInstance()
    {
        if(instance==null)
            instance = new LineOfCommentCalculator();
        return instance;
    }
}
