package com.reveal.metrics;


public interface MetricCalculatorBase
{
    void calculate(String input, MetricCalculationResults results);

    @Override
    String toString();
}
