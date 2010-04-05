package endrov.skeleton;

import java.util.ArrayList;

import endrov.imageset.EvPixels;

public final class WormClusterSkeleton extends Skeleton
	{
	ArrayList<Integer> basePoints;
	ArrayList<Integer> skPoints;
	boolean[] isBasePoint; // added for efficient check
	boolean[] isSkPoint; // added for efficient check
	int numWorms;

	/**
	 * Creates a instance of worm cluster skeleton, that is a skeleton of an image
	 * that could contain 1 or more overlapping worms. The number of worms are
	 * calculated as the half of the number of base points.
	 * 
	 * @param image
	 *          the image from which the skeleton is taken
	 * @param dt
	 *          a distance transformation of image
	 * @param w
	 *          the width of image
	 * @param h
	 *          the height of image
	 * @param basePoints
	 *          list of the base (extreme) points of the worms skeleton
	 * @param skPoints
	 *          list of the skeleton points
	 * @param isBasePoint
	 *          boolean matrix-like array that checks if a point is base
	 * @param isSkPoint
	 *          boolean matrix-like array that checks if a point is skeleton
	 */
	public WormClusterSkeleton(EvPixels image, int[] dt, int w, int h,
			ArrayList<Integer> basePoints, ArrayList<Integer> skPoints,
			boolean[] isBasePoint, boolean[] isSkPoint)
		{

		super(image, dt, w, h);
		this.basePoints = basePoints;
		this.skPoints = skPoints;
		this.isBasePoint = isBasePoint; // could be unnecessary
		this.isSkPoint = isSkPoint;
		numWorms = basePoints.size()/2;
		}

	/**
	 * Creates a instance of worm cluster skeleton, that is a skeleton of an image
	 * that could contain 1 or more overlapping worms. The number of worms are
	 * calculated as the half of the number of base points.
	 * 
	 * @param image
	 *          the image from which the skeleton is taken
	 * @param dt
	 *          a distance transformation of image
	 * @param w
	 *          the width of image
	 * @param h
	 *          the height of image
	 * @param basePoints
	 *          list of the base (extreme) points of the worms skeleton
	 * @param skPoints
	 *          list of the skeleton points
	 */

	public WormClusterSkeleton(EvPixels image, int[] dt, int w, int h,
			ArrayList<Integer> basePoints, ArrayList<Integer> skPoints)
		{

		super(image, dt, w, h);
		this.basePoints = basePoints;
		this.skPoints = skPoints;
		this.isBasePoint = SkeletonUtils.listToMatrix(w*h, basePoints); // could be
																																		// unnecessary
		this.isSkPoint = SkeletonUtils.listToMatrix(w*h, skPoints);
		numWorms = basePoints.size()/2;
		}

	}
