package com.reveal.metrics;


public class LineOfCodeCalculator implements MetricCalculatorBase
{
    private static LineOfCodeCalculator instance = null;

    @Override
    public void calculate(String input, MetricCalculationResults results)
    {
        if(results.loc != MetricCalculationResults.INVALID) return;

        String[] lines = input.split("\r\n|\r|\n");
        results.loc = lines.length;
    }

    @Override
    public String toString()
    {
        return "Line of Code";
    }

    private LineOfCodeCalculator() {/* to private constructor*/}


    public static MetricCalculatorBase getInstance()
    {
        if(instance==null)
            instance = new LineOfCodeCalculator();
        return instance;
    }
}
