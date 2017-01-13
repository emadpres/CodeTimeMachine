package com.reveal.metrics;


import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class ClassCyclomaticComplexityCalculator implements MetricCalculatorBase
{
    private static ClassCyclomaticComplexityCalculator instance = null;
    private static Metrics.Types myMetricType = Metrics.Types.CyclomaticComplexity;

    ArrayList<String> targetKeywords = new ArrayList<String>(Arrays.asList("return","if","else","case","default","for","while","do","break","continue","&&","||","?",":","catch","finally","throw","throws"));

    @Override
    public void calculate(MetricCalculationResults results)
    {
        if(results.classCyclomaticComplexity != MetricCalculationResults.INVALID) return;

        int sum =0 ;
        for( String eachKeyword: targetKeywords)
        {
            sum += StringUtils.countMatches(results.code, eachKeyword);
        }
        //String[] lines = results.code.split("return|if|else|case|default|for|while|do|break|continue|/&/&|/|/||/?|:|catch|finally|throw|throws");
        results.classCyclomaticComplexity = sum;

        if(results.classCyclomaticComplexity > results.getMetricMaxValue(myMetricType))
            results.setMetricMaxValue(myMetricType, results.classCyclomaticComplexity);
    }

    @Override
    public String toString()
    {
        return "Cyclomatic Complexity";
    }

    private ClassCyclomaticComplexityCalculator() {/* to private constructor*/}

    public static MetricCalculatorBase getInstance()
    {
        if(instance==null)
            instance = new ClassCyclomaticComplexityCalculator();
        return instance;
    }
}
