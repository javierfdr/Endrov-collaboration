package endrov.skeleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector2i;
import endrov.util.Vector3i;

public abstract class SkeletonTransform
	{
	abstract int[] getNeighbors(int pixelPosition, int w);

	abstract Vector2i getMaxDirectionalNeighbor(int[] imageArray, int w,
			int currentPixel, int previousPixel, int neighborMovement);
	abstract ArrayList<Vector2i> getDirectionalNeighbors(int[] imageArray, int w,
			int currentPixel, int previousPixel, int neighborMovement);
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
		ArrayList<Integer> skPoints = upDownHill2(imageArray, w, baseApex.get(2));
		// System.out.println("SIZE: "+ skPoints.size());
		EvPixels skImage = buildImage(input, skPoints);
		//EvPixels skImage = buildImage(input, baseApex.get(2));
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
		//ArrayList<Integer> skPoints = upDownHill(imageArray, w, baseApex.get(2));

		// System.out.println("SIZE: "+ skPoints.size());
		EvPixels skImage = buildImage(input, baseApex.get(0));
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
				int cNeighbors[] = getCircularNeighbors(count, w);
				// Count number of zeros 3x3 surrounding neighborhood
				for (int n = 0; n<cNeighbors.length; n++)
					{
					if (base&&imageArray[cNeighbors[n]]==0)
						zeroCount++;
					}
				if (base&&zeroCount>4)
					{
					basePoints.add(count);
					addCount++;
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
                  currentPixel, previousPixel, neighborMovement);
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

				// Chose best following pixel in the 45 degrees directional pixels set
				if (currentPixel!=previousPixel)
					{
					Vector2i maxDirectional = getMaxDirectionalNeighbor(imageArray, w,
							currentPixel, previousPixel, neighborMovement);
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
		
		// Evaluate for every neighbor
		for (int i = 0; i< neighbors.length; i++)
			{
			int n1 = neighbors[i];
			if (skPoint[n1]) continue; //if Neighbor is skeleton -> skip
			
			Vector2i firstDir = getMaxDirectionalNeighbor(imageArray, w, n1,
					currentPixel, i); // first step best neighbor
			if (skPoint[firstDir.x]) 	{
				if (nonConnectedPixel(skPoint,w,n1)){ 
					skPoint[n1] = true;
					continue;
				}
			}
			else
				{
				int maxDist= -1;
				Vector2i bestPair = new Vector2i(-1,-1);
				
				ArrayList<Vector2i> directionals = getDirectionalNeighbors(imageArray, w,
						firstDir.x, n1, firstDir.y); // first step best neighbor
				Iterator<Vector2i> it = directionals.iterator();
				Vector2i secondDir;
				//Iterate over every directional neighbor of the first step pixel
				while(it.hasNext()){
					secondDir = it.next();
					if (skPoint[secondDir.x] && imageArray[firstDir.x]>maxDist) 
						//picks the greatest directional that reaches a skPoint (not the maxDirectional)
						{						
						maxDist = imageArray[firstDir.x];
						bestPair = new Vector2i(n1,firstDir.x);		
						}					
					if (maxDist>0){
						if (nonConnectedPixel(skPoint,w,bestPair.x)) 
							skPoint[bestPair.x] = true;
						if (nonConnectedPixel(skPoint,w,bestPair.y)) 
							skPoint[bestPair.y] = true;
					}
				}
			}
		}
		}
	
	/**
	 * Generates the list of pixels representing the skeleton that is constructed
	 * connecting the base and apex pixels passed as parameter with non-yet
	 * skeleton pixels using two step hill connection
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
	private ArrayList<Integer> upDownHill2(int[] imageArray, int width,
			ArrayList<Integer> baseApexList)
		{
		boolean skPoint[] = new boolean[imageArray.length];
		ArrayList<Integer> skeleton = new ArrayList<Integer>();
		Iterator<Integer> it = baseApexList.iterator();
		
		while (it.hasNext())
			{ // Fill current skeleton points
			int pixelPos = (int) it.next();
			skPoint[pixelPos] = true;
			}
		
		// Connect the separated skeleton points applying two step hill connection
		// on every base and apex pixel.
		it = baseApexList.iterator();
		while (it.hasNext())
			{
			int pixelPos = (int) it.next();
			TwoStepHillConnection(imageArray, width, pixelPos, skPoint);
		}
		
		//Add every point belonging to the skeleton to the skeleton pixel list
		for (int i = 0; i<imageArray.length; i++)
			{
			if (skPoint[i])
				skeleton.add(i);			
			}
		return skeleton;
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
