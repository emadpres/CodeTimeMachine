package com.reveal.metrics;


public interface MetricCalculatorBase
{
    void calculate(MetricCalculationResults results);

    @Override
    String toString();
}
