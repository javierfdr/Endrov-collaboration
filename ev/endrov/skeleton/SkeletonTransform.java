package endrov.skeleton;

import java.util.ArrayList;
import java.util.Iterator;
import endrov.imageset.EvPixels;
import endrov.util.Vector2i;

public abstract class SkeletonTransform
	{
	abstract int[] getNeighbors(int pixelPosition, int w);

	/**
	 * Returns the neighbor that corresponds to the maximum directional movement
	 * from previousPixel to currentPixel, performing the movement
	 * neighborMovement.
	 */
	abstract Vector2i getMaxDirectionalNeighbor(int[] imageArray, int w,
			int currentPixel, int neighborMovement);

	/**
	 * Returns all the neighbors obtained performing the movement neighborMovement
	 * from previousPixel to currentPixel
	 */
	abstract ArrayList<Vector2i> getDirectionalNeighbors(int[] imageArray, int w,
			int currentPixel, int neighborMovement);

	/**
	 * Checks whether pixel is a connected pixel in skeleton.
	 */
	abstract public boolean nonConnectedPixel(boolean[] skeleton, int w, int pixel);

	/**
	 * Finds all the base or extreme points given the binary representation
	 * isShape. A base point is such that, in a 1-pixel width skeleton, has only
	 * one neighbor or has two neighbors in the same direction
	 * 
	 * @param isShape
	 *          Matrix-like array, true for every position that belongs to the
	 *          shape
	 * @param w
	 *          Width of the image matrix represented in isSkeleton
	 * @param shapePoints
	 *          List of the pixels that belong to the shape represented in isShape
	 * @return
	 */
	private ArrayList<Integer> detectBasePoints(boolean[] isShape, int w,
			ArrayList<Integer> shapePoints)
		{
		ArrayList<Integer> basePoints = new ArrayList<Integer>();
		Iterator<Integer> it = shapePoints.iterator();
		int pixel;
		int neigh[];
		int totalNeigh;
		int totalHitAreas;

		while (it.hasNext())
			{
			pixel = it.next();
			neigh = SkeletonUtils.getCircularNeighbors(pixel, w);
			totalNeigh = 0;
			totalHitAreas = 0;

			for (int i = 0; i<7; i++)
				{
				if (isShape[neigh[i]]&&isShape[neigh[i+1]])
					totalHitAreas++;
				if (isShape[neigh[i]])
					totalNeigh++;
				}
			if (isShape[neigh[7]]&&isShape[neigh[0]])
				totalHitAreas++;
			if (isShape[neigh[7]])
				totalNeigh++;

			if (totalNeigh==1||(totalHitAreas==1&&totalNeigh==2))
				{
				basePoints.add(pixel);
				}
			}
		return basePoints;
		}

	/**
	 * Calculates the skeleton associated with the input distance transform image
	 * and returns the skeleton image
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
		ArrayList<Integer> skPoints = new ArrayList<Integer>();
		boolean isSkeleton[] = new boolean[w*h];

		for (int i = 0; i<imageArray.length; i++)
			{
			if (imageArray[i]>0)
				{
				skPoints.add(i);
				isSkeleton[i] = true;
				}
			else
				isSkeleton[i] = false;
			}

		Thinning.thinToOnePixel(imageArray, isSkeleton, w, h, skPoints);
		EvPixels skImage = SkeletonUtils.buildImage(w, h, isSkeleton);

		return skImage;
		}

	/**
	 * Calculates the general skeleton associated with distance transform image
	 * (dt) taken from image and returns a list containing the isolated Worm
	 * skeletons that appear clustered or overlapped.
	 * 
	 * @param image
	 *          The initial image from which the distance transformation is taken
	 * @param dt
	 *          The distance transformation of the initial image
	 */

	public ArrayList<WormClusterSkeleton> getWormClusterSkeletons(EvPixels image,
			EvPixels dt)
		{
		int w = dt.getWidth();
		int h = dt.getHeight();
		int[] dtArray = dt.getArrayInt();
		ArrayList<Integer> skPoints = new ArrayList<Integer>();
		ArrayList<Integer> basePoints;
		ArrayList<ArrayList<Integer>> isolatedPoints = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> isolatedBases = new ArrayList<ArrayList<Integer>>();
		ArrayList<WormClusterSkeleton> wcList = new ArrayList<WormClusterSkeleton>();
		boolean isSkeleton[] = new boolean[w*h];

		for (int i = 0; i<dtArray.length; i++)
			{
			if (dtArray[i]>0)
				{
				skPoints.add(i);
				isSkeleton[i] = true;
				}
			else
				isSkeleton[i] = false;
			}

		// reduce, detect base points and isolate
		Thinning.thinToOnePixel(dtArray, isSkeleton, w, h, skPoints);
		basePoints = detectBasePoints(isSkeleton, w, skPoints);
		isolate(basePoints, isSkeleton, w, h, isolatedPoints, isolatedBases);

		boolean[] isBase = SkeletonUtils.listToMatrix(w*h, basePoints);

		// Create a worm cluster skeleton using the obtained isolated points and
		// their corresponding bases
		Iterator<ArrayList<Integer>> ip = isolatedPoints.iterator();
		Iterator<ArrayList<Integer>> ib = isolatedBases.iterator();
		ArrayList<Integer> currentPointList;
		ArrayList<Integer> currentBaseList;
		while (ip.hasNext())
			{
			currentPointList = ip.next();
			currentBaseList = ib.next();
			WormClusterSkeleton wcs = new WormClusterSkeleton(image, dtArray, w, h,
					currentBaseList, currentPointList, isBase, isSkeleton);
			wcList.add(wcs);
			}

		return wcList;
		}

	/**
	 * Finds all the connected paths starting and ending in base points and
	 * indexes them to isolate them. Returns then a matrix-like array of width h
	 * and height h that contains the isolation index for each pixel, so pixels
	 * with same number belong to the same connected skeleton. Every shape-pixel
	 * receives and isolation index.
	 * 
	 * @param basePoints
	 *          List of base (or extreme) points of the shape figure
	 * @param isSkeleton
	 *          Matrix-like array, true for every position that belongs to the
	 *          skeleton
	 * @param w
	 *          Width of the image matrix represented in isSkeleton
	 * @param h
	 *          Height of the image matrix represented in isSkeleton
	 */
	private int[] isolate(ArrayList<Integer> basePoints, boolean[] isSkeleton,
			int w, int h, ArrayList<ArrayList<Integer>> isolatedPoints,
			ArrayList<ArrayList<Integer>> isolatedBases)
		{
		int[] matching = new int[w*h];
		boolean[] isBase = SkeletonUtils.listToMatrix(w*h, basePoints);
		int isoCount = 1;
		int base;
		Iterator<Integer> bIt = basePoints.iterator();
		while (bIt.hasNext())
			{
			base = bIt.next();
			if (matching[base]==0)
				{
				ArrayList<Integer> currentSkPoints = new ArrayList<Integer>();
				ArrayList<Integer> currentBasePoints = new ArrayList<Integer>();
				isolatedPoints.add(currentSkPoints);
				isolatedBases.add(currentBasePoints);
				currentBasePoints.add(base);

				pathToBase(matching, isBase, isSkeleton, w, base, isoCount,
						currentSkPoints, currentBasePoints);
				isoCount += 1;
				}
			}
		return matching;
		}

	/**
	 * Explores the connected path starting from pixel and following every
	 * neighbor indexing the matching with isoCount.
	 * 
	 * @param matching
	 *          matrix-like array that contains the isolation index for each
	 *          pixel. The current pixel will be modified once in each call
	 * @param isBase
	 *          Matrix-like array, true for every pixel that is base point
	 * @param isSkeleton
	 *          Matrix-like array, true for every pixel that belongs to the
	 *          skeleton
	 * @param w
	 *          Width of the image matrix represented in isSkeleton
	 * @param pixel
	 *          current expanding pixel
	 * @param isoCount
	 *          isolation index corresponding to pixel
	 */
	private void pathToBase(int[] matching, boolean[] isBase,
			boolean[] isSkeleton, int w, int pixel, int isoCount,
			ArrayList<Integer> currentSkPoints, ArrayList<Integer> currentBasePoints)
		{
		int[] neighbors;
		if (matching[pixel]!=0) // the pixel has already been checked
			return;
		matching[pixel] = isoCount;
		currentSkPoints.add(pixel);
		if (isBase[pixel])
			currentBasePoints.add(pixel);

		neighbors = SkeletonUtils.getCircularNeighbors(pixel, w);
		for (int i = 0; i<8; i++)
			{
			if (isSkeleton[neighbors[i]])
				{// Follow every path recursively
				pathToBase(matching, isBase, isSkeleton, w, neighbors[i], isoCount,
						currentSkPoints, currentBasePoints);
				}
			}
		}
	}
