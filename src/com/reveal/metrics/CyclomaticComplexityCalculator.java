package com.reveal.metrics;


public class CyclomaticComplexityCalculator implements MetricCalculatorBase
{
    private static CyclomaticComplexityCalculator instance = null;
    @Override
    public void calculate(String input, MetricCalculationResults results)
    {
        if(results.cyclomaticComplexity != MetricCalculationResults.INVALID) return;

        String[] lines = input.split("\r\n|\r|\n");
        results.cyclomaticComplexity = lines.length;
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
