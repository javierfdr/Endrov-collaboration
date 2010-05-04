package endrov.worms;

import java.util.ArrayList;
import java.util.Iterator;

import com.graphbuilder.curve.Point;

import endrov.util.Vector2i;

/**
 * Class that represents a matching matrix that contains the corresponding (x,y)
 * coordinates of an EvPixels array point.
 */
public class WormPixelMatcher
	{
	private Vector2i[] matchMatrix;
	int w;
	int h;

	public WormPixelMatcher(int w, int h)
		{
		this.w = w;
		this.h = h;
		matchMatrix = new Vector2i[w*h];
		int count = 0;
		for (int j = 0; j<h; j++)
			{
			for (int i = 0; i<w; i++)
				{
				matchMatrix[count] = new Vector2i(i, j);
				count++;
				}
			}
		}

	/**
	 * Returns 2-dimensional position corresponding to pixel position
	 */
	public Vector2i getPixelPos(int pixel)
		{
		return matchMatrix[pixel];
		}

	public int pointToPixel(Point p)
		{
		double[] xy = p.getLocation();
		return (((int) xy[0])+((int) (xy[1]))*w);
		}

	/**
	 * Returns a Point representation corresponding to the position pixel,
	 * corresponding to the worm image
	 * 
	 * @param pixel
	 * @return
	 */
	public Point pixelToPoint(int pixel)
		{
		Vector2i pixel2D = getPixelPos(pixel);
		return (new PointFactory()).createPoint(pixel2D.x, pixel2D.y);
		}

	/**
	 * Returns a list of Point transforming each integer point in the given
	 * integer list points
	 * 
	 * @param points
	 *          List of integer points
	 */
	public ArrayList<Point> pixelListToPoint(ArrayList<Integer> points)
		{
		ArrayList<Point> pointList = new ArrayList<Point>(points.size());
		Iterator<Integer> pIt = points.iterator();
		while (pIt.hasNext())
			{
			pointList.add(pixelToPoint(pIt.next()));
			}
		return pointList;
		}

	/**
	 * Returns an array of int transforming each Point from points list to the
	 * corresponding integer matrix value
	 */

	public int[] pointListToPixel(ArrayList<Point> points)
		{
		int[] pixels = new int[points.size()];
		Iterator<Point> it = points.iterator();
		Point p;
		int count = 0;
		while (it.hasNext())
			{
			pixels[count] = pointToPixel(it.next());
			count++;
			}
		return pixels;
		}

	/**
	 * Transform a int array containing the two base points
	 * into a Point array
	 * 
	 */
	
	public ArrayList<Point> baseToPoint(int[] basePoints)
		{
		if (basePoints.length!=2)
			return null;
		ArrayList<Point> pl = new ArrayList<Point>(2);
		pl.add(pixelToPoint(basePoints[0]));
		pl.add(pixelToPoint(basePoints[1]));

		return pl;
		}

	}
