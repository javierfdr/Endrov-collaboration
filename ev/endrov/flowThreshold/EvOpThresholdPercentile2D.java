/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowThreshold;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.EvListUtil;

/**
 * Threshold given as percentile
 * 
 * Complexity O(w*h)
 */
public class EvOpThresholdPercentile2D extends Threshold2D
	{
	private final double perc;
	public EvOpThresholdPercentile2D(int mode, double perc)
		{
		super(mode);
		this.perc = perc;
		}
	
	public double[] getThreshold(EvPixels in)
		{
		return new double[]{findThreshold(in, perc)};
		}
	public static double findThreshold(EvPixels in, double perc)
		{
		double[] arr=in.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		return EvListUtil.findPercentileDouble(arr, perc);
		}
	}
