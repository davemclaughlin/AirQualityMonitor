package com.axoninstruments.airqualitymonitor;

/**
 * Created by Dave on 23/06/2017.
 */

public class MovingAverage
{
    private double[] _buffer;
    private int _lastNdx = 0;
    public double LastAverage;

    public MovingAverage(int bufferSize, double initialValue)
    {
        _buffer = new double[bufferSize];
        for (int i = 0; i < bufferSize; i++)
        {
            _buffer[i] = initialValue;
        }
    }

    public double NextAverage(double value)
    {
        int oldestNdx = _lastNdx % _buffer.length;
        double oldestValue = _buffer[oldestNdx];
        _buffer[oldestNdx] = value;
        _lastNdx = ++oldestNdx;
        LastAverage = LastAverage + (value - oldestValue) / _buffer.length;
        return LastAverage;
    }

    public int getCount()
    {
        return _buffer.length;
    }
}
