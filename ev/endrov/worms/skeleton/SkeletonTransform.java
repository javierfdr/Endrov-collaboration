package endrov.worms.skeleton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector2i;
import endrov.worms.WormPixelMatcher;

public abstract class SkeletonTransform
	{
	final static int[] diagDic =
		{ -1, 0, -1, 4, -1, 4, -1, 0 };// complete cross

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
	private static ArrayList<Integer> detectBasePoints(boolean[] isShape, int w,
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
	 * Expands the skeleton extremes (which are not the worm extreme points) to
	 * match the worm extremes
	 */
	private static void expandToWormBase(int[] dtArray, int w,
			boolean[] isSkPoint, ArrayList<Integer> skPoints,
			ArrayList<Integer> basePoints)
		{
		int current;
		int previous;
		int move;
		Vector2i next;
		int len;
		int[] neigh;
		int pIndex = -1;
		ArrayList<Integer> newBases = new ArrayList<Integer>(basePoints.size());
		int[] dic =
			{ 4, 6, 0, 2 };// opposite direction
		int extra;

		// Procedure: Follow Max directional neighbor until a
		// 1-value pixel is found or until the neighbor is out of the
		// shape (picking the last one)

		Iterator<Integer> bIt = basePoints.iterator();
		previous = -1;
		while (bIt.hasNext())
			{
			current = bIt.next();
			// Already border pixel
			if (dtArray[current]==1)
				{
				newBases.add(current);
				continue;
				}
			// Find previous skeleton pixel
			neigh = SkeletonUtils.getCrossNeighbors(current, w);
			for (int i = 0; i<4; i++)
				{
				if (isSkPoint[neigh[i]])
					{
					previous = neigh[i];
					pIndex = i;
					}
				}
			if (previous==-1)
				{
				newBases.add(current);
				continue;
				}
			move = dic[pIndex];

			// Follow best neighbor until background or border pixel is found
			while (true)
				{
				next = SkeletonUtils.getMaxDirectionalNeighbor(dtArray, w, current,
						move);
				previous = current;
				current = next.x;
				move = next.y;

				if (dtArray[current]==1)
					{
					newBases.add(current);
					skPoints.add(current);
					isSkPoint[current] = true;

					if (diagDic[move]!=-1)
						{
						extra = SkeletonUtils.getNeighbor(previous, diagDic[move], w);
						skPoints.add(extra);
						isSkPoint[extra] = true;
						}
					break;
					}
				else if (dtArray[current]==0)
					{
					newBases.add(previous);
					skPoints.add(previous);
					isSkPoint[previous] = true;
					break;
					}
				// Add Path
				skPoints.add(current);
				isSkPoint[current] = true;

				if (diagDic[move]!=-1)
					{
					extra = SkeletonUtils.getNeighbor(previous, diagDic[move], w);
					skPoints.add(extra);
					isSkPoint[extra] = true;
					}
				}
			}
		// Set new bases points
		int b = 0;
		int base;
		bIt = newBases.iterator();
		while (bIt.hasNext())
			{
			base = bIt.next();
			isSkPoint[base] = true;
			basePoints.set(b, base);
			b++;
			}
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
	 * skeletons that appear clustered or overlapped. This function calls the
	 * getWormClusterSkeletons(EvPixels,int[],int) given the input parameters and
	 * setting minPixels as 60
	 * 
	 * @param image
	 *          The initial image from which the distance transformation is taken
	 * @param dt
	 *          The distance transformation of the initial image
	 */

	public ArrayList<WormClusterSkeleton> getWormClusterSkeletons(EvPixels image,
			int[] dtArray, WormPixelMatcher wpm)
		{
		return getWormClusterSkeletons(image, dtArray, wpm, 60);
		}

	/**
	 * Calculates the general skeleton associated with distance transform image
	 * (dt) taken from image and returns a list containing the isolated Worm
	 * skeletons that appear clustered or overlapped. Any skeleton that contains
	 * less than minPixels number of pixels will be discarded.
	 * 
	 * @param image
	 *          The initial image from which the distance transformation is taken
	 * @param dt
	 *          The distance transformation of the initial image
	 * @param minPixels
	 *          The minimum number of pixels to be considered worm skeleton
	 */

	public ArrayList<WormClusterSkeleton> getWormClusterSkeletons(EvPixels image,
			int[] dtArray, WormPixelMatcher wpm, int minPixels)
		{
		int w = wpm.getW();
		int h = wpm.getH();
		// int[] dtArray = dt.getArrayInt();
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
		expandToWormBase(dtArray, w, isSkeleton, skPoints, basePoints);
		isolate(basePoints, isSkeleton, w, h, isolatedPoints, isolatedBases);

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
			if (currentPointList.size()+currentBaseList.size()<minPixels)
				continue;
			WormClusterSkeleton wcs = new WormClusterSkeleton(image, dtArray, w, h,
					currentBaseList, currentPointList, wpm);
			wcList.add(wcs);
			}

		return wcList;
		}

	/**
	 * Calculates the shape of the worm skeleton if the number of skeleton points
	 * are equal or bigger than minLength
	 * 
	 * @param minLength
	 *          minimum number of points for the worm skeleton if equals 0 then
	 *          the skeleton can have any length
	 */
	public static ArrayList<Integer> getShapeContour(WormClusterSkeleton wc,
			int minLength)
		{
		if ((minLength>0&&minLength<=wc.skPoints.size())||minLength==0)
			{
			return getShapeContour(wc);
			}
		return null;
		}

	/**
	 * Calculates the contour of a worm given its skeleton. The method finds the
	 * closest contour pixel and follows it until no more contour pixel exist
	 */
	private static ArrayList<Integer> getShapeContour(WormClusterSkeleton wc)
		{
		// Just for fixed Skeletons
		if (wc.basePoints.size()!=2)
			return null;
		System.out.println("Tracing contour of isolated worm");
		ArrayList<Integer> contour = new ArrayList<Integer>();
		int init = wc.basePoints.get(0);
		int firstContour = -1;
		int[] neigh;
		int min = Integer.MAX_VALUE;
		int minI = -1;

		if (wc.dt[init]==1)
			firstContour = init;
		else
			{
			neigh = SkeletonUtils.getCircularNeighbors(init, wc.w);
			for (int i = 0; i<neigh.length; i++)
				{
				if (wc.dt[neigh[i]]==1)
					{
					firstContour = neigh[i];
					break;
					}
				else if (wc.dt[neigh[i]]<min)
					{
					min = wc.dt[neigh[i]];
					minI = i;
					}
				}
			}
		// find the closest contour (Should always work)
		if (firstContour==-1)
			{
			ArrayList<Vector2i> dir = SkeletonUtils.getDirectionalNeighbors(null,
					wc.w, min, minI);
			Iterator<Vector2i> it = dir.iterator();
			Vector2i v;
			while (it.hasNext())
				{
				v = it.next();
				if (wc.dt[v.x]==1)
					{
					firstContour = v.x;
					break;
					}
				}
			}

		// Follow contour
		boolean[] isContour = new boolean[wc.w*wc.h];
		followContourPath(wc, firstContour, isContour, contour);

		return contour;
		}

	/**
	 * Follows recursively the contour of the given worm skeleton 'wc'. First
	 * contour is supposed as contour element. The next non-contour neighbor is
	 * found until no more contour pixel exists
	 */
	private static void followContourPath(WormClusterSkeleton wc,
			int firstContour, boolean[] isContour, ArrayList<Integer> contour)
		{
		int neigh[];
		contour.add(firstContour);
		isContour[firstContour] = true;

		neigh = SkeletonUtils.getCircularNeighbors(firstContour, wc.w);
		for (int i = 0; i<neigh.length; i++)
			{
			if (wc.dt[neigh[i]]==1&&!isContour[neigh[i]])
				{
				firstContour = neigh[i];
				followContourPath(wc, firstContour, isContour, contour);
				}
			}
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

	public static ArrayList<ArrayList<Integer>> getAllPaths(WormClusterSkeleton wc){
		ArrayList<ArrayList<Integer>> allPaths = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> newPath = new ArrayList<Integer>();
		allPaths.add(newPath);
		
		int[] matching  = new int[wc.h*wc.w];
		int base = wc.basePoints.get(0);
		newPath.add(base);
						
		pathBranching(matching,wc.isBasePoint,wc.isSkPoint,wc.w,base,allPaths,newPath);		
		return allPaths;
	}
	
	private static void pathBranching(int[] matching, boolean[] isBase,
			boolean[] isSkeleton, int w, int pixel,
			ArrayList<ArrayList<Integer>> paths, ArrayList<Integer> currentSkPoints)
		{
		int[] neighbors;
		if (matching[pixel]!=0) // the pixel has already been checked
			return;
		matching[pixel] = 1;
		currentSkPoints.add(pixel);
		// if (isBase[pixel])
		// currentBasePoints.add(pixel);

		boolean createBranch = false;
		//neighbors = SkeletonUtils.getCircularNeighbors(pixel, w);
		neighbors = SkeletonUtils.getCrossNeighbors(pixel, w);
		for (int i = 0; i<4; i++)
			{
			if (isSkeleton[neighbors[i]] && matching[neighbors[i]]==0)
				{// Follow every path recursively
				if (createBranch)
					{
					ArrayList<Integer> newPath = new ArrayList<Integer>(currentSkPoints);
					paths.add(newPath);
					int[] newMatching = matching.clone();
					pathBranching(newMatching, isBase, isSkeleton, w, neighbors[i],
							paths, newPath);
					}
				else
					{
					pathBranching(matching, isBase, isSkeleton, w, neighbors[i], paths,
							currentSkPoints);
					createBranch = true;
					}
				}
			}
		}
	
	
	
	public static ArrayList<WormSkeleton> wormsFromPaths(EvPixels image,
			int[] dt, WormPixelMatcher wpm, ArrayList<ArrayList<Integer>> guessPaths)
		{
		ArrayList<WormSkeleton> wlist = new ArrayList<WormSkeleton>();
		Iterator<ArrayList<Integer>> wPaths = guessPaths.iterator();
		while (wPaths.hasNext())
			{
			ArrayList<Integer> wormPath = wPaths.next();
			ArrayList<Integer> baseP = new ArrayList<Integer>(2);
			baseP.add(wormPath.get(0));
			baseP.add(wormPath.get(wormPath.size()-1));

			WormSkeleton ws = null;
			try
				{
				ws = new WormSkeleton(image, dt, wpm.getW(), wpm.getH(), baseP,
						wormPath, wpm);
				}
			catch (NotWormException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			wlist.add(ws);
			}
		return wlist;
		}

	/**
	 * Calculates the best path to follow starting from extreme or base points on
	 * the worm cluster wc and reaching another extreme or base point
	 * 
	 * @param wc
	 *          Worm cluster skeleton
	 * @param nSteps
	 *          Number of last steps stored from previous path
	 */

	public static ArrayList<ArrayList<Integer>> guessWormPaths(
			WormClusterSkeleton wc, int nSteps,WormPixelMatcher wpm, Vector2i minMaxWormLength)
		{
		ArrayList<ArrayList<Integer>> wormPaths = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> checkedBase = new ArrayList<Integer>();
		Iterator<Integer> it = wc.basePoints.iterator();
		System.out.println("Number of bases"+ wc.basePoints.size());
		int base;
		System.out.print("BASES: ");
		for(int i=0;i<wc.basePoints.size();i++){
			System.out.print(wpm.getPixelPos(wc.basePoints.get(i))+" ");
		}
		HashSet<String> pathPairs = new HashSet<String>();
		while (it.hasNext())
			{
			base = it.next();
			if (!checkedBase.contains((Integer) base))
				{
				checkedBase.add(base);
				traceBestPath(wc, base, nSteps, wormPaths, checkedBase,wc.basePoints,wpm,pathPairs);
				}
			else{
				System.out.println("Skip base");
			}
		}

		//Discard to short and to long paths
		Iterator<ArrayList<Integer>> wit = wormPaths.iterator();
		int wormLength;
		while(wit.hasNext()){
			wormLength = wit.next().size();
			System.out.println("WL: "+wormLength+" MIN-MAX: "+minMaxWormLength);
			if(wormLength< minMaxWormLength.x || wormLength > minMaxWormLength.y){
				wit.remove();
				System.out.println("Remove");
			}
		}

		return wormPaths;
		}

	/**
	 * Calculates the best path starting from an extreme pixel (base) of the worm
	 * cluster wc according to the next pixel heuristic implemented on
	 * getBestNeighbor.
	 * 
	 * @param wc
	 *          Worm cluster skeleton
	 * @param base
	 *          Base or extreme point of the worm cluster wc
	 * @param nSteps
	 *          Number of last steps stored from previous path
	 * @param wormPaths
	 * @param checkedBase
	 */

	public static void traceBestPath(WormClusterSkeleton wc, int base,
			int nSteps, ArrayList<ArrayList<Integer>> wormPaths,
			ArrayList<Integer> checkedBase, ArrayList<Integer> basePoints,
			WormPixelMatcher wpm, HashSet<String> pathPairs)
		{
		System.out.println("Starting tracking: "+wpm.getPixelPos(base));
		ArrayList<Integer> currentPath = new ArrayList<Integer>();
		int[] directions = new int[4];
		int[] diagonalDirections = new int[4];
		int neigh[];
		int previous;
		int lastNCounter = nSteps-1;
		int[] lastMove =
			{ -1 };

		Queue<Vector2i> lastNSteps = new LinkedList<Vector2i>();
		Vector2i bestN = new Vector2i(base, -1);
		Vector2i aux;
		boolean reachedBase = false;
		boolean[] isPath = new boolean[wc.h*wc.w];

		// wormPaths.add(currentPath);
		currentPath.add(base);
		isPath[base] = true;
		previous = -1;

		while (bestN.x!=-1 && !reachedBase)
			{
			neigh = SkeletonUtils.getCrossNeighbors(bestN.x, wc.w);
			// aux =
			// bestHeuristicNeighbor(wc,neigh,directions,diagonalDirections,previous);
			aux = getBestNeighbor(wc, neigh, directions, previous, nSteps);
			previous = bestN.x;
			bestN = aux;

			if (bestN.x==-1||isPath[bestN.x])
				break;

			currentPath.add(bestN.x);
			isPath[bestN.x] = true;
			lastNCounter = updateDirections(directions, diagonalDirections,
					lastNSteps, lastMove, bestN, lastNCounter);
			reachedBase = wc.isBasePoint[bestN.x];
			}
		// If a base was reached then is marked and added to the path
		if (reachedBase = true)
			{
			//System.out.println("STOP AT: "+wpm.getPixelPos(bestN.x));
			//if(!checkedBase.contains((Integer)bestN.x)){
				//checkedBase.add(bestN.x);
				
				//Check member to avoid duplicate paths
				String pPair = pathToString(currentPath);
				if(!pathPairs.contains(pPair)){
					wormPaths.add(currentPath);
					pathPairs.add(pPair);
					System.out.println("Added");
					}
			//}
		}
		// If a base is not reached then the closest base (euclidean distance) to
		// the
		// last pixel is marked
		else
			{
			System.out.println("Not base reached");
			int closest = -1;
			double bestD = Integer.MAX_VALUE;
			double d;
			int pbase;
			Iterator<Integer> bit = basePoints.iterator();
			while (bit.hasNext())
				{
				pbase = bit.next();
				//if (checkedBase.contains((Integer) pbase))
					//continue;
				d = WormPixelMatcher.calculatePixelDistance(previous, pbase, wpm);
				if (d<bestD)
					{
					bestD = d;
					closest = pbase;
					}
				}
			if (closest!=-1)
				{
				String pPair = pathToString(currentPath);
				if(!pathPairs.contains(pPair)){
				//checkedBase.add(closest);
				pathPairs.add(pPair);
				wormPaths.add(currentPath);
				}
				}
			}

		}
	

	public static String pathToString(ArrayList<Integer> wormPath){
		int base1= wormPath.get(0);
		int base2= wormPath.get(wormPath.size()-1);
		int aux;
		if(base1>base2){
			aux = base1;
			base1 = base2;
			base2 = aux;
		}
		return base1+"-"+base2;
	}
	
	public static Vector2i bestHeuristicNeighbor(WormClusterSkeleton wc,
			int[] neigh, int[] directions, int[] diagonal, int previous, int nSteps)
		{
		int[] neigh2;
		ArrayList<Vector2i> neighDirections = new ArrayList<Vector2i>();
		ArrayList<Vector2i> bestNeighbors = new ArrayList<Vector2i>();

		for (int n = 0; n<neigh.length; n++)
			{
			if (!wc.isSkPoint[neigh[n]]||neigh[n]==previous)
				continue;

			neigh2 = SkeletonUtils.getCrossNeighbors(neigh[n], wc.w);
			Vector2i secondBest = getBestNeighbor(wc, neigh2, directions, neigh[n],
					nSteps);
			if (secondBest.x==-1)
				continue;

			neigh2 = SkeletonUtils.getCrossNeighbors(secondBest.x, wc.w);
			Vector2i thirdBest = getBestNeighbor(wc, neigh2, directions,
					secondBest.x, nSteps);

			switch (secondBest.y)
				{
				case 0:
					switch (thirdBest.y)
						{
						case 1:
							neighDirections.add(new Vector2i(0, -1));
							break;
						case 3:
							neighDirections.add(new Vector2i(3, -1));
							break;
						// case 0: neighDirections.add(new Vector2i(3,0));break;
						default:
							neighDirections.add(new Vector2i(-1, -1));
							break;
						}
					break;
				case 1:
					switch (thirdBest.y)
						{
						case 0:
							neighDirections.add(new Vector2i(0, -1));
							break;
						case 2:
							neighDirections.add(new Vector2i(1, -1));
							break;
						// case 1: neighDirections.add(new Vector2i(0,1));break;
						default:
							neighDirections.add(new Vector2i(-1, -1));
							break;
						}
					break;
				case 2:
					switch (thirdBest.y)
						{
						case 1:
							neighDirections.add(new Vector2i(1, -1));
							break;
						case 3:
							neighDirections.add(new Vector2i(2, -1));
							break;
						// case 2: neighDirections.add(new Vector2i(2,1));break;
						default:
							neighDirections.add(new Vector2i(-1, -1));
							break;
						}
					break;
				case 3:
					switch (thirdBest.y)
						{
						case 0:
							neighDirections.add(new Vector2i(3, -1));
							break;
						case 2:
							neighDirections.add(new Vector2i(2, -1));
							break;
						// case 3: neighDirections.add(new Vector2i(3,2));break;
						default:
							neighDirections.add(new Vector2i(-1, -1));
							break;
						}
					break;
				default:
					neighDirections.add(new Vector2i(-1, -1));
					break;
				}
			bestNeighbors.add(new Vector2i(neigh[n], n));
			}

		// Get max diagonal
		int maxD = Integer.MIN_VALUE;
		int maxI = -1;
		for (int d = 0; d<diagonal.length; d++)
			{
			if (maxD<diagonal[d])
				{
				maxD = diagonal[d];
				maxI = d;
				}
			}

		ArrayList<Integer> matchingIndex = new ArrayList<Integer>(3);
		ArrayList<Vector2i> matchingDirections = new ArrayList<Vector2i>(3);

		// Find neighbor which matches diagonal direction
		Iterator<Vector2i> it = neighDirections.iterator();
		int index = 0;
		Vector2i nextN;
		while (it.hasNext())
			{
			nextN = it.next();
			if (nextN.x==maxI)
				{

				if (directions[bestNeighbors.get(index).y]<maxD)
					{
					// matchingIndex.add(index);
					// matchingDirections.add(nextN);
					// }
					System.out.println("Diagonal Way");
					return bestNeighbors.get(index);
					}

				}
			else if (nextN.y==maxI)
				{
				System.out.println("Diagonal Way");
				if (directions[bestNeighbors.get(index).y]<maxD)
					{
					// matchingIndex.add(index);
					// matchingDirections.add(nextN);
					// }
					return bestNeighbors.get(index);
					}
				}
			index++;
			}/*
				 * //Pick neighbor with the best accumulated directional combination
				 * Iterator<Vector2i> itM = matchingDirections.iterator(); int maxComb =
				 * -1; int maxCombIndex =-1; int currentComb =0; Vector2i currentN;
				 * while(itM.hasNext()){ currentN = itM.next(); if (currentN.y ==-1)
				 * currentComb = diagonal diagonal[(direction+3)%4]+=value;
				 * diagonal[direction]+=value; }
				 */

		// default value
		if (bestNeighbors.size()>0)
			{
			/*
			 * Iterator<Vector2i> it2 = bestNeighbors.iterator(); Vector2i bn = new
			 * Vector2i(-1,-1); Vector2i nb; while(it2.hasNext()){ nb = it2.next(); if
			 * (nb.x > bn.x){ bn = nb; } } return bn;
			 */

			}
		return getBestNeighbor(wc, neigh, directions, previous, nSteps);
		// return new Vector2i(-1,-1);
		}

	/**
	 * Calculates the next path pixel, finding the neighbor of the current pixel,
	 * preceded by previous, that maximizes the cost function
	 * directions[p]+dt[p]*heuristic, where directions contains the amount of
	 * times that any of the cross points have been chosen in the last nSteps
	 * steps in the currently build path. The cost function consist of the
	 * direction reliability value and the strength of the distance value for the
	 * next pixel. This will make the path tend to reach crossing points before
	 * turning.
	 * 
	 * @param wc
	 *          Worm cluster skeleton
	 * @param neigh
	 *          Neighbors of the current pixel
	 * @param directions
	 *          Accumulated directions in last nSteps steps
	 * @param previous
	 *          Previous pixel
	 * @param nSteps
	 *          Number of last steps stored from previous path
	 * @return best following neighbor
	 */

	public static Vector2i getBestNeighbor(WormClusterSkeleton wc, int[] neigh,
			int[] directions, int previous, int nSteps)
		{
		Vector2i best = new Vector2i(-1, -1);
		double max = Double.MIN_VALUE;
		double heuristic = 2;
		for (int i = 0; i<neigh.length; i++)
			{
			if (!wc.isSkPoint[neigh[i]]||neigh[i]==previous)
				continue;
			if (max<(directions[i]+(wc.dt[neigh[i]])*heuristic))
				{
				max = directions[i]+(wc.dt[neigh[i]]*heuristic);
				best.x = neigh[i];
				best.y = i;
				}
			}
		return best;
		}

	/**
	 * Calculates and updates the values for the directions vector adding the
	 * lastMove direction and removing the first of the path if lastNSteps is
	 * filled, to maintain the recently walked path size
	 */

	public static int updateDirections(int[] directions, int[] diagonal,
			Queue<Vector2i> lastNSteps, int[] lastMove, Vector2i bestNeighbor,
			int lastNCounter)
		{

		if (lastNCounter>=0)
			{
			if (!lastNSteps.isEmpty())
				{
				updateDiagonal(diagonal, bestNeighbor.y, 1, lastMove[0]);
				lastMove[0] = bestNeighbor.y;
				}

			directions[bestNeighbor.y] += 1;
			lastNSteps.add(bestNeighbor);
			lastNCounter--;
			}
		else
			{
			directions[bestNeighbor.y] += 1;
			directions[lastNSteps.peek().y] -= 1;
			lastNSteps.poll(); // Remove Nsteps'th element

			updateDiagonal(diagonal, bestNeighbor.y, 1, lastMove[0]);
			lastMove[0] = bestNeighbor.y;

			lastNSteps.add(bestNeighbor);
			}
		return lastNCounter;
		}

	public static void updateDiagonal(int[] diagonal, int direction, int value,
			int lastStep)
		{
		switch (lastStep)
			{
			case 0:
				switch (direction)
					{
					case 1:
						diagonal[0] += value;
						break;
					case 3:
						diagonal[3] += value;
						break;
					}
				break;
			case 1:
				switch (direction)
					{
					case 0:
						diagonal[0] += value;
						break;
					case 2:
						diagonal[1] += value;
						break;
					}
				break;
			case 2:
				switch (direction)
					{
					case 1:
						diagonal[1] += value;
						break;
					case 3:
						diagonal[2] += value;
						break;
					}
				break;
			case 3:
				switch (direction)
					{
					case 0:
						diagonal[3] += value;
						break;
					case 2:
						diagonal[2] += value;
						break;
					}
				break;
			}
		}

	}
