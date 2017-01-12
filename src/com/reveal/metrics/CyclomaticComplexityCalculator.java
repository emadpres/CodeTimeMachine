package com.reveal.metrics;


public class CyclomaticComplexityCalculator implements MetricCalculatorBase
{
    private static CyclomaticComplexityCalculator instance = null;
    @Override
    public void calculate(String input, MetricCalculationResults results)
    {
        if(results.CC != MetricCalculationResults.INVALID) return;

        String[] lines = input.split("return|if|else|case|default|for|while|do|break|continue|/&/&|/|/||/?|:|catch|finally|throw|throws");
        results.CC = lines.length;
    }

    @Override
    public String toString()
    {
        return "Cyclomatic Complexity";
    }

    private CyclomaticComplexityCalculator() {/* to private constructor*/}

    public static MetricCalculatorBase getInstance()
    {
        if(instance==null)
            instance = new CyclomaticComplexityCalculator();
        return instance;
    }
}
