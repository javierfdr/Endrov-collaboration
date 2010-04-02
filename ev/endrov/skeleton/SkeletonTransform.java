package endrov.skeleton;

import java.util.ArrayList;
import java.util.Iterator;
import endrov.imageset.EvPixels;
import endrov.util.Vector2i;

public abstract class SkeletonTransform
	{
	abstract int[] getNeighbors(int pixelPosition, int w);
	/**
	* Returns the neighbor that corresponds to the maximum directional movement from
	* previousPixel to currentPixel, performing the movement neighborMovement.
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

		// Vector<ArrayList<Integer>> baseApex = detectBaseApex(imageArray, w, h);
		// ArrayList<Integer> skeleton_points =
		// upDownHill(imageArray,base_apex.get(2));
		// ArrayList<Integer> skPoints = upDownHill(imageArray, w, baseApex.get(2));
		ArrayList<Integer> skPoints = new ArrayList<Integer>();
		for (int i = 0; i<imageArray.length; i++)
			{
			if (imageArray[i]>0)
				skPoints.add(i);
			}
		boolean[] isSkeleton = listToMatrix(w*h, skPoints);

		// boolean[] isSkeleton = listToMatrix(w*h, baseApex.get(2));
		// ArrayList<Integer> skPoints = upDownHill2(imageArray, w,
		// baseApex.get(2),isSkeleton);
		// skPoints = upDownHill2(imageArray, w, skPoints,isSkeleton);
		// refineBasePoints(isSkeleton,baseApex, w);
		// System.out.println("Start Redyce");
		// skPoints = reduceSkeleton(imageArray, isSkeleton, w, skPoints);
		// skPoints = reduceSkeleton(imageArray, isSkeleton, w, baseApex.get(0));
		// System.out.println("End Redyce");

		Thinning.thinToOnePixel(imageArray, isSkeleton, w, h, skPoints); // EYE
		// skPoints
		// are not
		// right
		// here
		ArrayList<Integer> basePoints = detectBasePoints(isSkeleton, w, skPoints);
		// Vector<ArrayList<Integer>> baseApex = detectBaseApex(imageArray, w, h);
		int[] matching = isolate(basePoints, isSkeleton, w, h);

		// System.out.println("SIZE: "+ skPoints.size());
		EvPixels skImage = SkeletonUtils.buildImage(input, matching);
		// EvPixels skImage = buildImage(input, baseApex.get(0));
		return skImage;
		}

	/**
	 * Returns an array of length 'size' setting true every position found in
	 * list.
	 */
	public static boolean[] listToMatrix(int size, ArrayList<Integer> list)
		{

		boolean[] matrix = new boolean[size];
		Iterator<Integer> it = list.iterator();
		while (it.hasNext())
			{
			int pixelPos = (int) it.next();
			matrix[pixelPos] = true;
			}
		return matrix;
		}

	public static ArrayList<Integer> matrixToList(int[] matrix)
		{
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i<matrix.length; i++)
			{
			if (matrix[i]>0)
				list.add(i);
			}
		return list;
		}

	public EvPixels getSkeleton2(EvPixels input)
		{
		int w = input.getWidth();
		int h = input.getHeight();
		int[] imageArray = input.getArrayInt();

		// ArrayList<Integer> skeleton_points =
		// upDownHill(imageArray,base_apex.get(2));
		// ArrayList<Integer> skPoints = upDownHill(imageArray, w, baseApex.get(2));
		ArrayList<Integer> skPoints = new ArrayList<Integer>();
		for (int i = 0; i<imageArray.length; i++)
			{
			if (imageArray[i]>0)
				skPoints.add(i);
			}
		boolean[] isSkeleton = listToMatrix(w*h, skPoints);

		// boolean[] isSkeleton = listToMatrix(w*h, baseApex.get(2));
		// ArrayList<Integer> skPoints = upDownHill2(imageArray, w,
		// baseApex.get(2),isSkeleton);
		// skPoints = upDownHill2(imageArray, w, skPoints,isSkeleton);
		// refineBasePoints(isSkeleton,baseApex, w);
		// System.out.println("Start Redyce");
		// skPoints = reduceSkeleton(imageArray, isSkeleton, w, skPoints);
		// skPoints = reduceSkeleton(imageArray, isSkeleton, w, baseApex.get(0));
		// System.out.println("End Redyce");

		Thinning.thinToOnePixel(imageArray, isSkeleton, w, h, skPoints); // EYE
		// skPoints
		// are not
		// right
		// here
		ArrayList<Integer> basePoints = detectBasePoints(isSkeleton, w, skPoints);
		// Vector<ArrayList<Integer>> baseApex = detectBaseApex(imageArray, w, h);
		// int[] matching = isolate(basePoints, isSkeleton,w,h);
		// System.out.println("SIZE: "+ skPoints.size());
		// EvPixels skImage = SkeletonUtils.buildImage(input, matching);
		EvPixels skImage = SkeletonUtils.buildImage(input, basePoints);
		return skImage;

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
	 * @return
	 */
	public int[] isolate(ArrayList<Integer> basePoints, boolean[] isSkeleton,
			int w, int h)
		{
		int[] matching = new int[w*h];
		boolean[] isBase = listToMatrix(w*h, basePoints);
		int isoCount = 20;
		int base;
		Iterator<Integer> bIt = basePoints.iterator();
		while (bIt.hasNext())
			{
			base = bIt.next();
			if (matching[base]==0)
				{
				pathToBase(matching, isBase, isSkeleton, w, base, isoCount);
				isoCount = (int) (Math.random()*245)+10;
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
			boolean[] isSkeleton, int w, int pixel, int isoCount)
		{
		int[] neighbors;
		if (matching[pixel]!=0) // the pixel has already been checked
			return;
		matching[pixel] = isoCount;
		neighbors = SkeletonUtils.getCircularNeighbors(pixel, w);
		for (int i = 0; i<8; i++)
			{
			if (isSkeleton[neighbors[i]])
				{// Follow every path recursively
				pathToBase(matching, isBase, isSkeleton, w, neighbors[i], isoCount);
				}
			}
		}
	}
