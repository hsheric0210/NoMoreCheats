package com.eric0210.nomorecheats.api.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class AverageCollector
{
	private ArrayList<Double> averageNumbers;

	public AverageCollector()
	{
		this.averageNumbers = new ArrayList<>();
	}

	public void add(final double d)
	{
		if (Double.isNaN(d) || Double.isInfinite(d))
			return;
		this.averageNumbers.add(d);
	}

	public double getAverage()
	{
		double total = 0.0;
		try
		{
			for (final double d : this.averageNumbers)
			{
				if (d > 0)
					total += d;
			}
		}
		catch (ConcurrentModificationException e)
		{
			return -1.0;
		}
		try
		{
			total /= this.averageNumbers.size();
		}
		catch (ArithmeticException ex)
		{
			ex.printStackTrace();
			return -2D;
		}
		return total;
	}

	public double getMax()
	{
		double max = 0.0;
		for (final double d : this.averageNumbers)
		{
			if (d > max)
			{
				max = d;
			}
		}
		return max;
	}

	public double getMin()
	{
		double min = 9.9999999E7;
		for (final double d : this.averageNumbers)
		{
			if (d < min)
			{
				min = d;
			}
		}
		if (min != Double.MAX_VALUE)
		{
			return min;
		}
		return 0.0;
	}

	public ArrayList<Double> getNumbers()
	{
		return this.averageNumbers;
	}

	public int getCount()
	{
		return this.averageNumbers.size();
	}

	public void reset()
	{
//		this.averageNumbers.clear();
		this.averageNumbers = new ArrayList<>();
	}
}
