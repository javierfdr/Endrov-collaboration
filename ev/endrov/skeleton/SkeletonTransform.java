package endrov.skeleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.hssf.record.formula.functions.Concatenate;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector2i;
import endrov.util.Vector3i;

public abstract class SkeletonTransform
	{
	abstract int[] getNeighbors(int pixelPosition, int w);

	abstract Vector2i getMaxDirectionalNeighbor(int[] imageArray, int w,
			int currentPixel, int neighborMovement);
	abstract ArrayList<Vector2i> getDirectionalNeighbors(int[] imageArray, int w,
			int currentPixel, int neighborMovement);
	abstract public boolean nonConnectedPixel(boolean[] skeleton, int w, int pixel);
	
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
		//ArrayList<Integer> skPoints = upDownHill(imageArray, w, baseApex.get(2));
		
		boolean[] isSkeleton = listToMatrix(w*h, baseApex.get(2));
		ArrayList<Integer> skPoints = upDownHill2(imageArray, w, baseApex.get(2),isSkeleton);
		//skPoints = upDownHill2(imageArray, w, skPoints,isSkeleton);
		refineBasePoints(isSkeleton,baseApex.get(0), w);
		//System.out.println("Start Redyce");
		//skPoints = reduceSkeleton(imageArray, isSkeleton, w, skPoints);
		//System.out.println("End Redyce");
		
		// System.out.println("SIZE: "+ skPoints.size());
		EvPixels skImage = buildImage(input, skPoints);
		//EvPixels skImage = buildImage(input, baseApex.get(2));
		return skImage;
		}
	
	/**
	 * Returns an array of length 'size' setting true every position found in list. 
	 */
	public boolean[] listToMatrix(int size, ArrayList<Integer> list){
	
		boolean[] matrix = new boolean[size];
		Iterator<Integer> it = list.iterator();
		while (it.hasNext())
		{
			int pixelPos = (int) it.next();
			matrix[pixelPos] = true;
		}
		return matrix;
	}

	public EvPixels getSkeleton2(EvPixels input)
		{
		int w = input.getWidth();
		int h = input.getHeight();
		int[] imageArray = input.getArrayInt();

		Vector<ArrayList<Integer>> baseApex = detectBaseApex(imageArray, w, h);
		// ArrayList<Integer> skeleton_points =
		//upDownHill(imageArray,base_apex.get(2));
		boolean[] isSkeleton = listToMatrix(w*h, baseApex.get(2));
		ArrayList<Integer> skPoints = upDownHill2(imageArray, w, baseApex.get(2),isSkeleton);
		refineBasePoints(isSkeleton,baseApex.get(0), w);
		
		
		// System.out.println("SIZE: "+ skPoints.size());
		EvPixels skImage = buildImage(input, baseApex.get(0));
		//EvPixels skImage = buildImage(input, skPoints);
		return skImage;
		}

	/**
	 * Returns the surrounding pixels. This is all the pixels in the 
	 * positions of the 3x3 matrix where position is the center.
	 * 
	 */
	public int[] getCircularNeighbors(int position, int w)
		{
		int neighbors[] = new int[8];
		neighbors[0] = position-w; // Up
		neighbors[1] = position+1; // Right
		neighbors[2] = position+w; // Down
		neighbors[3] = position-1; // Left
		neighbors[4] = neighbors[0]-1; //Up-left
		neighbors[5] = neighbors[0]+1; //Up-right
		neighbors[6] = neighbors[2]-1;//Down-left
		neighbors[7] = neighbors[2]+1; //down-right
		
		return neighbors;
		}
	/**
	 * True if neighbor is a circular neighbor of position in a image of width w
	 */
	public boolean isCircularNeighbor(int neighbor, int position,int w){
		int diff = neighbor-position;
		int[] neigh = {w,-w,1,-1,w-1,w+1,-w-1,-w+1};
		
		for (int i=0;i<8;i++){
			if (diff==neigh[i]) return true;
		}
		return false;
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
		boolean[] isBase = new boolean[w*h]; //useful for remove wrong base points
		boolean[] isApex = new boolean[w*h]; //useful for remove wrong apex points
		boolean[] isBaseApex = new boolean[w*h];
		
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
				int cNeighbors[] = getCircularNeighbors(count, w);
				// Count number of zeros 3x3 surrounding neighborhood
				for (int n = 0; n<cNeighbors.length; n++)
					{
					if (base&&imageArray[cNeighbors[n]]==0)
						zeroCount++;
					}
				if (base&&zeroCount>4)
					{//Base points
					basePoints.add(count);
					isBase[count] = true;
					isBaseApex[count] = true;
					}
				// Find if pixelValue is the maximum value in the
				// given neighborhood
				for (int n = 0; n<neighbors.length; n++)
					{
					if (imageArray[neighbors[n]]>pixelValue) apex = false;
					}

				if (apex)
					{
					apexPoints.add(count);
					isApex[count] = true;
					isBaseApex[count] = true;
					addCount++;
					}
				if (addCount>0)
					{
					//orderList.add(count);
					//addCount = 0;
					}
				}
			}

		//remove base points that do not have neighbors
		Iterator<Integer> baseIt = basePoints.iterator();
		int neigh[];
		boolean hasNeighbor;
		while(baseIt.hasNext()){
			Integer b = baseIt.next();
			neigh = getCircularNeighbors((int)b, w);
			
			hasNeighbor = false;
			for(int n =0; n<8; n++){
				if (isBaseApex[neigh[n]]){
					hasNeighbor = true;			
				}
			}
			if (!hasNeighbor){
	//			baseIt.remove();
				if (isApex[b]){
	//				apexPoints.remove(b);
				}
				continue;
			}
			orderList.add(b);
		}
		
		baseIt = apexPoints.iterator();
		while(baseIt.hasNext()){
			orderList.add(baseIt.next());
		}
		
		
		Vector<ArrayList<Integer>> baseApex = new Vector<ArrayList<Integer>>(3);
		baseApex.add(basePoints);
		baseApex.add(apexPoints);
		baseApex.add(orderList);

		return baseApex;
		}

	public void refineBasePoints(boolean skPoint[], ArrayList<Integer> basePoints,int w){
		Iterator<Integer> it = basePoints.iterator();
		ArrayList<Integer> wrongBase = new ArrayList<Integer>();
		int neighbors[];
		int b;
		int numSk;
		while(it.hasNext()){
			b = it.next();
			neighbors = getCircularNeighbors(b, w);

			numSk=0;
			for (int n=0;n<8;n++)
				{ //Count number of skeleton neighbors
					if (skPoint[neighbors[n]]){
						numSk++;
					}
				}
			if (numSk==0){ 
				wrongBase.add(((Integer)b));
				skPoint[b] = false;
			}
		}
		//Delete every wrong base point
		it = wrongBase.iterator();
		while(it.hasNext()){
			basePoints.remove(it.next());
		}
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
		it = markedPoints.iterator();
		while(it.hasNext()){
		int pixelPos = (int) it.next();
		generateDownHill(imageArray, width, pixelPos, pixelPos, -1, markedPoints,
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
						currentPixel, neighborMovement);
				generateUpHill(imageArray, w, maxDirectional.x, currentPixel,
						maxDirectional.y, markedPoints, evalPoints);
				}
			else
				{// Follow path for the best neighbor that has the same distance
				Iterator<Vector2i> it = maxPixels.iterator();
				Vector2i v, maxDirectional;
				int m = -1;
				int mIndex = -1;
				int maxMove = -1;
				int previousP = -1;

				while (it.hasNext())
					{
					// If the pixel has already been evaluated it will be in
					// generateUpHill
					v = (Vector2i) it.next();
					maxDirectional = getMaxDirectionalNeighbor(imageArray, w, v.x,v.y);
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

}

	private void generateDownHill(int[] imageArray, int w, int currentPixel,
			int previousPixel, int neighborMovement, ArrayList<Integer> markedPoints,
			boolean[] evalPoints){
			
			if (evalPoints[currentPixel])
				return; // Check for the recursive call

			int neighbors[] = getNeighbors(currentPixel, w);
			int numMax = 0;
			int currentPixelValue = imageArray[currentPixel];
			int max = currentPixelValue;
			ArrayList<Vector2i> maxPixels = new ArrayList<Vector2i>(2);

			// find max value in neighborhood and number of repetitions
			for (int i = 0; i<neighbors.length; i++)
				{
				if (max<imageArray[neighbors[i]])
					{
					max = imageArray[neighbors[i]];
					numMax = 1;
					maxPixels.clear();
					maxPixels.add(new Vector2i(neighbors[i], i));
					}
				else if (max==imageArray[neighbors[i]])
					{
					numMax++;
					maxPixels.add(new Vector2i(neighbors[i], i));
					}
				}
			evalPoints[currentPixel] = true; // Current pixel evaluated

				// Chose best following pixel in the 45 degrees directional pixels set
				if (currentPixel!=previousPixel)
					{
					Vector2i maxDirectional = getMaxDirectionalNeighbor(imageArray, w,
							currentPixel, neighborMovement);
					if (!evalPoints[maxDirectional.x]){
						generateDownHill(imageArray, w, maxDirectional.x, currentPixel,
							maxDirectional.y, markedPoints, evalPoints);
					}
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
						maxDirectional = getMaxDirectionalNeighbor(imageArray, w, v.x, v.y);
						if (m<imageArray[maxDirectional.x])
							{
							m = imageArray[maxDirectional.x];
							mIndex = maxDirectional.x;
							maxMove = maxDirectional.y;
							previousP = v.x;
							}
						}
					evalPoints[previousP] = true;
					if (!evalPoints[mIndex]){
						generateDownHill(imageArray, w, mIndex, previousP, maxMove, markedPoints,
							evalPoints);
					}
				}
			}

	/**
	 * Sets in skPoint the pixels that belong to the skeleton of the image
	 * represented on imageArray performing a two step hill processing over a
	 * given base or apex point. The algorithm calculates the best path to follow
	 * from the given skeleton point to find the next skeleton points. The search
	 * is done in two steps. Both steps are applied for every neighbor of the
	 * currentPixel. In the first step, the maximum directional neighbor is
	 * calculated. In the second step the three directional neighbor of the pixel
	 * obtained in the first step are calculated, and is taken then the one with
	 * the greatest distance that is also an skeleton pixel. This allows to
	 * connect the disconnected skeleton pixels with more than 4 pixels distance.
	 * The remaining skeleton has more than 1-pixel width and is almost totally
	 * connected. The remaining disconnections are no greater than 1 pixel long.
	 * 
	 * @param imageArray
	 *          The distance transform image array
	 * @param w
	 *          width of the image represented in imageArray
	 * @param currentPixel
	 *          The current analyzed pixel
	 * @param skPoint
	 *          Skeleton points are true for they given position
	 */
	private void TwoStepHillConnection(int[] imageArray, int w, int currentPixel,
			boolean[] skPoint)
		{
		int neighbors[] = getNeighbors(currentPixel, w);		
		ArrayList<Vector2i>  connectionN; //Neighbors for checking connections
		Iterator<Vector2i> cit;
		// Evaluate for every neighbor
		for (int i = 0; i<neighbors.length; i++)
			{
			int n1 = neighbors[i];
			if (skPoint[n1])
				continue; // if Neighbor is skeleton -> skip

			Vector2i firstDir = getMaxDirectionalNeighbor(imageArray, w, n1,i); // first step best neighbor
			if (skPoint[firstDir.x])
				{
				boolean connect = true;
				//Check if one of the directional neighbors from currenPixel in the direction i
				//already connects pixel with firstDir
				connectionN= getDirectionalNeighbors(imageArray, w, currentPixel, i);
				cit = connectionN.iterator();
				while(cit.hasNext()){
					Vector2i cNeighbor = cit.next();
					if (cNeighbor.x != n1 && isCircularNeighbor(cNeighbor.x,firstDir.x,w) && 
								skPoint[cNeighbor.x])
						{			
						connect = false;
						break;
					}
				}
				if (connect){					
					skPoint[n1] = true;
					continue;
				}
			}
			else
				{
				int maxDist = -1;
				Vector2i bestPair = new Vector2i(-1, -1);

				ArrayList<Vector2i> directionals = getDirectionalNeighbors(imageArray,
						w, firstDir.x, firstDir.y); // first step best neighbor
				Iterator<Vector2i> it = directionals.iterator();
				Vector2i secondDir;
				// Iterate over every directional neighbor of the first step pixel
				while (it.hasNext())
					{
					secondDir = it.next();
					if (skPoint[secondDir.x]&&imageArray[firstDir.x]>maxDist)
					// picks the greatest directional that reaches a skPoint (not the
					// maxDirectional)
						{
						maxDist = imageArray[firstDir.x];
						bestPair = new Vector2i(n1, firstDir.x);
						}
					if (maxDist>0)
						{
						if (nonConnectedPixel(skPoint, w, bestPair.x))
							{
							skPoint[bestPair.x] = true;
							}
						if (nonConnectedPixel(skPoint, w, bestPair.y))
							{
							skPoint[bestPair.y] = true;
							}
						}
					}
				}
			}
		}
	
	/**
	 * Generates the list of pixels representing the skeleton that is constructed
	 * connecting the base and apex pixels passed as parameter with non-yet
	 * skeleton pixels using two step hill connection. Also sets the boolean array
	 * isSkeleton with the skeleton points position as true
	 * 
	 * @param imageArray
	 *          The distance transform image array
	 * @param width
	 *          width of the image represented in imageArray
	 * @param baseApexList
	 *          List of base and apex points in imageArray
	 * @param boolean array of the size of imageArray that will be set up with the 
	 *        skeleton points
	 * @return List of skeleton pixels built with up and down hill processing over
	 *         the base and apex points list.
	 */
	private ArrayList<Integer> upDownHill2(int[] imageArray, int width,
			ArrayList<Integer> baseApexList,boolean[] isSkeleton)
		{		
		ArrayList<Integer> skeleton = new ArrayList<Integer>();
		Iterator<Integer> it = baseApexList.iterator();

		// Connect the separated skeleton points applying two step hill connection
		// on every base and apex pixel.
		while (it.hasNext())
			{
			int pixelPos = (int) it.next();
			TwoStepHillConnection(imageArray, width, pixelPos, isSkeleton);
		}
	
		//Add every point belonging to the skeleton to the skeleton pixel list
		for (int i = 0; i<imageArray.length; i++)
			{
			if (isSkeleton[i])
				skeleton.add(i);			
			}

		return skeleton;
		}
/**
 * Creates and EvPixels image setting to 1 the positions in the list points.
 */
	
	private static EvPixels buildImage(EvPixels input, ArrayList<Integer> points)
		{
		EvPixels skImage = new EvPixels(EvPixelsType.INT, input.getWidth(), input
				.getHeight());
		int[] skArray = skImage.getArrayInt();

		Iterator<Integer> it = points.iterator();
		while (it.hasNext())
			{
			int pos = (int) it.next();
			skArray[pos] = 1;
			}
		return skImage;
		}
	
	private ArrayList<Integer> reduceSkeleton(int[] imageArray, boolean[] isSkeleton, int width,
			ArrayList<Integer> skeletonPoints){
			boolean[] isThinSkeleton = new boolean[isSkeleton.length];
			boolean[] isBanned = new boolean[isSkeleton.length]; //faster than delete from ArrayList
			ArrayList<Integer> thinSkeletonPoints = new ArrayList<Integer>();
			ArrayList<Vector2i> neighbors;
			Iterator<Vector2i> neighIt;
			Vector2i n;
			
			int countScan = 0;
			int initPoints = 0;

			Iterator<Integer> it = skeletonPoints.iterator();
			while (it.hasNext())
			{
			int pixel = it.next();
			// The pixel has already been evaluated or the skeleton is already
			// connected
			if (isThinSkeleton[pixel]||isBanned[pixel])
				continue;
			// if (isThinSkeleton[pixel] || !nonConnectedPixel(isThinSkeleton, width,
			// pixel)) continue;
			isThinSkeleton[pixel] = true;
			thinSkeletonPoints.add(pixel);
			initPoints++;

			int firstN[] = getCircularNeighbors(pixel, width);
			int initPixel; //For circle checking
			for (int i=0; i<8; i++){			
				boolean scanSkeleton = true;
				int previous = pixel;
				int prevMove = i; 
				pixel = firstN[i];
				initPixel = pixel;
				//Start scanning skeleton from pixel
				while (scanSkeleton)
				{
					scanSkeleton = false;
					neighbors = getDirectionalNeighbors(imageArray, width, pixel,
							prevMove);
					neighIt = neighbors.iterator();
					while (neighIt.hasNext())
					{						
						n = neighIt.next();
						if (n.x==initPixel) {scanSkeleton=false;break;} //Breaking circles
						
						// set the new pixel and continue scanning
						if (!scanSkeleton && isSkeleton[n.x] && !isBanned[n.x])
						{
							thinSkeletonPoints.add(n.x);
							isThinSkeleton[n.x] = true;
							previous = pixel;
							pixel = n.x;
							prevMove = n.y;
							scanSkeleton = true; // keep scanning
							countScan++;
						}
						//Ban any other directional neighbor
						else if (scanSkeleton)
						{
							isBanned[n.x] = true;
						}
					}
				}
			}
			}
			return thinSkeletonPoints;
		}
	}
