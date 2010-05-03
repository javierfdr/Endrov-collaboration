package endrov.util.curves;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import endrov.util.Vector2i;
import endrov.util.curves.WrongParameterSplineException;
import endrov.worms.PointFactory;

import com.graphbuilder.curve.CardinalSpline;
import com.graphbuilder.curve.ControlPath;
import com.graphbuilder.curve.GroupIterator;
import com.graphbuilder.curve.Point;
import com.graphbuilder.curve.ShapeMultiPath;

public class EvCardinalSpline
	{
	CardinalSpline cs;
	ArrayList<Point> extremePoints;
	ArrayList<Point> controlPoints;

	/**
	 * Constructs a cardinal spline that starts in one base point and ends in the
	 * other one, passing through the points defined at points,storing the
	 * transformation values
	 * 
	 * @param basePoints
	 *          List of size 2 that contains the starting and ending point of the
	 *          curve
	 * @param points
	 *          the tentative cardinal spline control points
	 * @param alpha
	 *          slack value
	 * @param numPointsPercentage
	 *          Number of points from 'points' that want to be consider as control
	 *          points
	 */
	public EvCardinalSpline(ArrayList<Point> base,
			ArrayList<Point> controlPoints, double alpha, double numPointsPercentage)
			throws WrongParameterSplineException
		{

		if (base.size()!=2)
			throw new WrongParameterSplineException("Base size must be exactly 2");
		if (numPointsPercentage>1.0||numPointsPercentage<0.0)
			throw new WrongParameterSplineException(
					"Percentage must be between 0.0 and 1.0");

		this.cs = getShapeSpline(base, controlPoints, alpha, numPointsPercentage);
		this.extremePoints = new ArrayList<Point>(base);
		this.controlPoints = new ArrayList<Point>(controlPoints);

		}

	/**
	 * Calculates a cardinal spline that starts in one base point and ends in the
	 * other one, passing through the points defined at points, and constructs
	 * 
	 * @param basePoints
	 *          List of size 2 that contains the starting and ending point of the
	 *          curve
	 * @param points
	 *          the tentative cardinal spline control points
	 * @param alpha
	 *          slack value
	 * @param numPointsPercentage
	 *          Number of points from 'points' that want to be consider as control
	 *          points
	 * @return
	 */
	public static CardinalSpline getShapeSpline(ArrayList<Point> basePoints,
			ArrayList<Point> points, double alpha, double numPointsPercentage)
		{

		int length = points.size();
		int numPoints = (int) (((double)length)*numPointsPercentage);
		if (numPoints<2)
			return null;
		int step = length/(numPoints-1);
		int stepCount;
		Iterator<Point> it = points.iterator();
		ControlPath cp = new ControlPath();

		// Adding skeleton points to ControlPath. Note that
		// the base points are added twice, manually and belonging
		// to points. This to make them count in spline curve
		cp.addPoint(basePoints.get(0));
		while (it.hasNext()&&numPoints>0)
			{
			cp.addPoint(it.next());
			stepCount = 0;
			while (stepCount<step-1 && it.hasNext())
				{
				it.next();
				stepCount++;
				}
			numPoints--;
			}
		cp.addPoint(basePoints.get(1));
		CardinalSpline cs = new CardinalSpline(cp, new GroupIterator("0:n-1", cp
				.numPoints()));
		cs.setAlpha(alpha);

		return cs;
		}
/**
 * Returns a list containing numPoints points evenly separated that belong to
 * the cs cardinal spline path. If numPoints is bigger than the number of 
 * points generated or if equals 0, then all the points are returned 
 */
	public ArrayList<Point> getCardinalPoints(int numPoints){
		return getCardinalPoints(this.cs,numPoints);
	}
	
	/**
	 * Returns a list containing numPoints points evenly separated that belong to
	 * the cs cardinal spline path. If numPoints is bigger than the number of 
	 * points generated or if equals 0, all the points are returned 
	 * 
	 * @param cs
	 *          A cardinal spline object
	 * @param numPoints
	 *          the number of points evenly separated to return
	 */
	public static ArrayList<Point> getCardinalPoints(CardinalSpline cs, int numPoints)
		{
		ShapeMultiPath mp = new ShapeMultiPath();
		cs.appendTo(mp); // Computing and adding to multipath
		
		Vector2i[] points = new Vector2i[mp.getCapacity()];
		PathIterator pi = mp.getPathIterator(null);
		float coords[] = new float[2];
		int index=0;
		while (!pi.isDone())
			{
			pi.currentSegment(coords);
			points[index]=new Vector2i((int) coords[0],(int) coords[1]);
			pi.next();
			index++;
			}
		System.out.println("Number of spline points: "+index);
		if (numPoints>index || numPoints==0) numPoints=index;
		if (numPoints<0) return null;
		
		int count = 0;
		int step = index/(numPoints-1);
		Vector2i temp;
		ArrayList<Point> cardinalPoints = new ArrayList<Point>(index);
		while(count<index){
			temp = points[count];
			cardinalPoints.add(new PointFactory().createPoint(temp.x,temp.y));
			count+=step;
		}
		return cardinalPoints;
	}

	/**
	 * Sets imageArray to true in every spline points position contained
	 * in points
	 * @param points
	 * @param imageArray
	 */
	public static void drawCardinalSpline(int[] points, int[] imageArray)
		{
			int count=0;
			while(count<points.length){
				imageArray[points[count]] = 1;
				count++;
			}
		}
	
	}
