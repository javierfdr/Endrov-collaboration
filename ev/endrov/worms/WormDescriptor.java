package endrov.worms;

import endrov.util.Vector2i;
import endrov.util.curves.EvCardinalSpline;
import endrov.util.curves.WrongParameterSplineException;
import endrov.worms.skeleton.WormSkeleton;

import java.util.ArrayList;
import java.util.Iterator;
import com.graphbuilder.curve.Point;

public class WormDescriptor
	{

	WormPixelMatcher pixelMatcher;
	//EvCardinalSpline cs;

	/**
	 * Creates a new worm descriptor given a WormPixelMatcher object that
	 * represents the current worm as an Endrov image, and Worm skeleton from
	 * which the corresponding spline curve is calculated. This constructor uses
	 * the default values 1.0 for the percentage of control points used and 0.5 as
	 * alpha value.
	 * 
	 * @param wm
	 *          Representation of the worm as Endrov image
	 * @param ws
	 *          The skeleton of the worm
	 */
	public WormDescriptor(WormPixelMatcher wpm)
		{
		this.pixelMatcher = wpm;
	/*	try
			{
			//this.cs = getShapeSpline(wm, ws, 1.0, 0.5);
			}
		catch (WrongParameterSplineException e)
			{
			System.out
					.println("Wrong attributes on WormSkeleton object 'ws' to calculate Cardinal Spline ");
			e.printStackTrace();
			}*/
		}

	public WormDescriptor(WormPixelMatcher wpm,int numPointsPercentage, 
			double alpha)
		{
		this.pixelMatcher = wpm;
		/*try
			{
			this.cs = getShapeSpline(wm, ws, alpha, numPointsPercentage);
			}
		catch (WrongParameterSplineException e)
			{
			System.out
					.println("Wrong attributes on WormSkeleton object 'ws' to calculate Cardinal Spline ");
			e.printStackTrace();
			}*/
		}

	private EvCardinalSpline getSkeletonSpline(WormPixelMatcher wpm,
			WormSkeleton ws, double alpha, double numPointsPercentage)
			throws WrongParameterSplineException
		{
		return new EvCardinalSpline(wpm.baseToPoint(ws.getBasePoints()), wpm
				.pixelListToPoint(ws.getSkPoints()), alpha, numPointsPercentage);
		}
/*
	public void setWormDescriptor(WormPixelMatcher wpm, WormSkeleton ws,
			double alpha, double numPointsPercentage)
			throws WrongParameterSplineException
		{
		this.pixelMatcher = wpm;
		this.cs = new EvCardinalSpline(wpm.baseToPoint(ws.getBasePoints()), wpm
				.pixelListToPoint(ws.getSkPoints()), alpha, numPointsPercentage);
		}
*/
	}
