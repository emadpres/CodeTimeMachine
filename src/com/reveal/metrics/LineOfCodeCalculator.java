package com.reveal.metrics;


public class LineOfCodeCalculator implements MetricCalculatorBase
{
    private static LineOfCodeCalculator instance = null;
    private static Metrics.Types myMetricType = Metrics.Types.LOC;

    @Override
    public void calculate(MetricCalculationResults results)
    {
        if(results.loc != MetricCalculationResults.INVALID) return;

        String[] lines = results.code.split("\r\n|\r|\n");
        results.loc = lines.length;

        if(results.loc > results.getMetricMaxValue(myMetricType))
            results.setMetricMaxValue(myMetricType, results.loc);
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
