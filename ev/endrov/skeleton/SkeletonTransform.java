package endrov.skeleton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector2i;

public abstract class SkeletonTransform
	{
	abstract int[] getNeighbors(int pixelPosition, int w);

	abstract Vector2i getMaxDirectionalNeighbor(int[] imageArray, int w,
			int currentPixel, int previousPixel, int neighborMovement);

	/**
	 * Calculates the skeleton associated to the input distance transform image
	 * 
	 * @param input
	 *          Distance transformed image where background pixels are 0's
	 * @return Skeleton calculated from the input distance transform image
	 */
	public EvPixels getSkeleton(EvPixels input)
		{
		int w = input.getWidth();
		int h = input.getHeight();
		int[] imageArray = input.getArrayInt();

		Vector<ArrayList<Integer>> baseApex = detectBaseApex(imageArray, w, h);
		// ArrayList<Integer> skeleton_points =
		// upDownHill(imageArray,base_apex.get(2));
		ArrayList<Integer> skPoints = upDownHill(imageArray, w, baseApex.get(2));

		// System.out.println("SIZE: "+ skPoints.size());
		EvPixels skImage = buildImage(input, skPoints);
		return skImage;
		}

	public EvPixels getSkeleton2(EvPixels input)
		{
		int w = input.getWidth();
		int h = input.getHeight();
		int[] imageArray = input.getArrayInt();

		Vector<ArrayList<Integer>> baseApex = detectBaseApex(imageArray, w, h);
		// ArrayList<Integer> skeleton_points =
		// upDownHill(imageArray,base_apex.get(2));
		// ArrayList<Integer> skPoints = upDownHill(imageArray, w, baseApex.get(2));

		// System.out.println("SIZE: "+ skPoints.size());
		EvPixels skImage = buildImage(input, baseApex.get(2));
		return skImage;
		}

	/**
	 * Finds the base points and the apex points from the given distance image
	 * array. The base points are those who are in the extremes or corners of the
	 * object. The apex points are interior elements or base points which have the
	 * highest altitude locally. They are consider as elementary cells of the
	 * skeleton, which is further built starting from this points.
	 * 
	 * @return A 3 elements Vector containing the base points position list, the
	 *         apex points position list, and the base and apex list in the order
	 *         they are found, respectively.
	 */

	private Vector<ArrayList<Integer>> detectBaseApex(int[] imageArray, int w,
			int h)
		{

		ArrayList<Integer> basePoints = new ArrayList<Integer>();
		ArrayList<Integer> apexPoints = new ArrayList<Integer>();
		ArrayList<Integer> orderList = new ArrayList<Integer>();

		// Setting loop variables
		int count = w+1;
		int zeroCount = 0;
		int addCount = 0;
		boolean base;
		boolean apex;
		for (int py = 1; py<h-1; py++, count += 2)
			{// The borders are supposed as background
			for (int px = 1; px<w-1; px++, count++)
				{
				apex = true;
				base = false;

				int pixelValue = imageArray[count];
				if (pixelValue==0)
					continue;
				else if (pixelValue==1)
					{
					base = true;
					}

				zeroCount = 0;
				int neighbors[] = getNeighbors(count, w);
				// Count number of zeros in neighborhood and find if pixelValue is the
				// higher value
				for (int n = 0; n<neighbors.length; n++)
					{
					if (base&&imageArray[neighbors[n]]==0)
						zeroCount++;
					if (imageArray[neighbors[n]]>pixelValue)
						apex = false;
					}

				if (base&&zeroCount>4)
					{
					basePoints.add(count);
					addCount++;
					}

				/*
				 * else if(base && zeroCount ==4){ //MARGIN BUG:: If pixel is object
				 * pixel is next to background //Check up-right-left-down neighbors for
				 * object pixel followed by background if (imageArray[neighbors[0]] != 0
				 * & imageArray[neighbors[0]-w] ==0){ basePoints.add(count); addCount++;
				 * } else if(imageArray[neighbors[1]] != 0 & imageArray[neighbors[1]+1]
				 * ==0){ basePoints.add(count); addCount++; } else
				 * if(imageArray[neighbors[2]] != 0 & imageArray[neighbors[2]-1] ==0){
				 * basePoints.add(count); addCount++; } else if(imageArray[neighbors[3]]
				 * != 0 & imageArray[neighbors[3]+w] ==0){ basePoints.add(count);
				 * addCount++; } }
				 */
				if (apex)
					{
					apexPoints.add(count);
					addCount++;
					}
				if (addCount>0)
					{
					orderList.add(count);
					addCount = 0;
					}
				}
			}
		Vector<ArrayList<Integer>> baseApex = new Vector<ArrayList<Integer>>(3);
		baseApex.add(basePoints);
		baseApex.add(apexPoints);
		baseApex.add(orderList);

		return baseApex;
		}

	/**
	 * Generates the list of pixels representing the skeleton that is constructed
	 * connecting the base and apex pixels passed as parameter with non-yet
	 * skeleton pixels through and up hill and down hill processing
	 * 
	 * @param imageArray
	 *          The distance transform image array
	 * @param width
	 *          width of the image represented in imageArray
	 * @param baseApexList
	 *          List of base and apex points in imageArray
	 * @return List of skeleton pixels built with up and down hill processing over
	 *         the base and apex points list.
	 */
	private ArrayList<Integer> upDownHill(int[] imageArray, int width,
			ArrayList<Integer> baseApexList)
		{
		boolean[] evalPoints = new boolean[imageArray.length];// Evaluated skeleton
																													// points
		ArrayList<Integer> markedPoints = new ArrayList<Integer>();
		ArrayList<Integer> skPoints = new ArrayList<Integer>(40);

		Iterator<Integer> it = baseApexList.iterator();
		while (it.hasNext())
			{
			int pixelPos = (int) it.next();
			if (evalPoints[pixelPos])
				continue; // The skeleton point has already been checked
			generateUpHill(imageArray, width, pixelPos, pixelPos, -1, markedPoints,
					evalPoints);
			}
		for (int i = 0; i<imageArray.length; i++)
			{
			if (evalPoints[i])
				skPoints.add(i);
			}
		return skPoints;
		}

	/**
	 * Sets in evalPoints the pixels that belong to the skeleton of the image
	 * represented on imageArray performing a upHill processing over a given base
	 * or apex point. The algorithm calculates the best path to follow from the
	 * given skeleton point to find the next skeleton points. The maximum value
	 * among the neighbors is calculated. If there is only one neighbor then is
	 * followed. If there are more than 1 neighbor then the neighbor with the
	 * higher valued directional (45 degrees) pixel is marked as skeleton and the
	 * process continues from this point until the path is no longer. If there are
	 * no neighbors with higher or equal distance value this are marked (added to
	 * markedPoints) to be proccessed further on generateDownHill method.
	 * 
	 * @param imageArray
	 *          The distance transform image array
	 * @param w
	 *          width of the image represented in imageArray
	 * @param currentPixel
	 *          The current analyzed pixel
	 * @param previousPixel
	 *          The pixel that precedes currentPixel
	 * @param neighborMovement
	 *          The performed movement to go from previousPixel to currentPixel
	 * @param markedPoints
	 *          Pixels marked for post processing
	 * @param evalPoints
	 *          Skeleton points are true for they given position
	 */
	private void generateUpHill(int[] imageArray, int w, int currentPixel,
			int previousPixel, int neighborMovement, ArrayList<Integer> markedPoints,
			boolean[] evalPoints)
		{
		if (evalPoints[currentPixel])
			return; // Check for the recursive call

		int neighbors[] = getNeighbors(currentPixel, w);
		int numMax = 0;
		int currentPixelValue = imageArray[currentPixel];
		int max = currentPixelValue;
		ArrayList<Vector2i> maxPixels = new ArrayList<Vector2i>(2);
		int maxNeighborPos = -1;

		// find max value in neighborhood and number of repetitions
		for (int i = 0; i<neighbors.length; i++)
			{
			if (max<imageArray[neighbors[i]])
				{
				max = imageArray[neighbors[i]];
				numMax = 1;
				maxNeighborPos = i;
				maxPixels.clear();
				maxPixels.add(new Vector2i(neighbors[i], i));
				}
			else if (max==imageArray[neighbors[i]])
				{
				numMax++;
				maxNeighborPos = i;
				maxPixels.add(new Vector2i(neighbors[i], i));
				}
			}
		evalPoints[currentPixel] = true; // Current pixel evaluated
		if (numMax==1)
			{ // There is just one skeleton successor pixel
			generateUpHill(imageArray, w, neighbors[maxNeighborPos], currentPixel,
					maxNeighborPos, markedPoints, evalPoints);
			}
		else if (numMax>1)
			{
			// Chose best following pixel in the 45 degrees directional pixels set
			if (currentPixel!=previousPixel)
				{
				Vector2i maxDirectional = getMaxDirectionalNeighbor(imageArray, w,
						currentPixel, previousPixel, neighborMovement);
				generateUpHill(imageArray, w, maxDirectional.x, currentPixel,
						maxDirectional.y, markedPoints, evalPoints);
				}
			else
				{// Follow path for the best neighbor that has the same distance value
				Iterator<Vector2i> it = maxPixels.iterator();
				Vector2i v, maxDirectional;
				int m = -1;
				int mIndex = -1;
				int maxMove = -1;
				int previousP = -1;

				while (it.hasNext())
					{
					// If the pixel has already been evaluated it will be ignored by
					// generateUpHill
					v = (Vector2i) it.next();
					maxDirectional = getMaxDirectionalNeighbor(imageArray, w, v.x,
							currentPixel, v.y);
					if (m<imageArray[maxDirectional.x])
						{
						m = imageArray[maxDirectional.x];
						mIndex = maxDirectional.x;
						maxMove = maxDirectional.y;
						previousP = v.x;
						}
					}
				evalPoints[previousP] = true;
				generateUpHill(imageArray, w, mIndex, previousP, maxMove, markedPoints,
						evalPoints);
				}
			}
		else
			{
			markedPoints.add(currentPixel);
			}

		/*
		 * if (currentPixel = previousPixel & max = currentPixelValue){
		 * Iterator<Integer> it = maxPixels.iterator(); while (it.hasNext()){ int
		 * neighbor = (int) it.next(); } }
		 */
		}

	private static EvPixels buildImage(EvPixels input, ArrayList<Integer> points)
		{
		EvPixels skImage = new EvPixels(EvPixelsType.INT, input.getWidth(), input
				.getHeight());
		int[] skArray = skImage.getArrayInt();

		int i = 0;
		Iterator<Integer> it = points.iterator();
		while (it.hasNext())
			{
			int pos = (int) it.next();
			skArray[pos] = 1;
			}
		return skImage;
		}
	}
