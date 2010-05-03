package endrov.worms.skeleton;

import java.util.ArrayList;
import java.util.Iterator;

import endrov.imageset.EvPixels;
import endrov.util.Vector2i;

public final class WormClusterSkeleton extends Skeleton
	{
	public ArrayList<Integer> basePoints;
	public ArrayList<Integer> skPoints;
	public boolean[] isBasePoint; // added for efficient check
	public boolean[] isSkPoint; // added for efficient check
	public int numWorms;

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
		this.basePoints = new ArrayList<Integer>(basePoints);
		this.skPoints = new ArrayList<Integer>(skPoints);			
		
		this.isBasePoint = new boolean[isBasePoint.length]; // could be unnecessary
		for(int i=0;i<this.isBasePoint.length;i++) this.isBasePoint[i] = isBasePoint[i];
		
		this.isSkPoint = new boolean[isSkPoint.length];
		for(int i=0;i<this.isSkPoint.length;i++) this.isSkPoint[i] = isSkPoint[i];
		
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
		this.basePoints = new ArrayList<Integer>(basePoints);
		this.skPoints = new ArrayList<Integer>(skPoints);
		this.isBasePoint = SkeletonUtils.listToMatrix(w*h, basePoints); // could be
																																		// unnecessary
		this.isSkPoint = SkeletonUtils.listToMatrix(w*h, skPoints);
		numWorms = basePoints.size()/2;
		}
	
	
	
	
	/**
	 * Returns the paths that most likely describe the worms of the calling
	 * worm cluster following the directional neighbors starting from
	 * the given base points.
	 */
	public ArrayList<ArrayList<Integer>> getAppWormPaths(){
		ArrayList<Integer> baseCopy = new ArrayList<Integer>(basePoints);
		Iterator<Integer> bIt = baseCopy.iterator();
		ArrayList<ArrayList<Integer>> wormPaths = new ArrayList<ArrayList<Integer>>(0);
		ArrayList<Integer> markedBases = new ArrayList<Integer>();
		int[] imageArray= image.getArrayInt();
		
		int base;
		int next=-1;
		int move=-1;
		int[] neigh;
		int prev;
		int auxPrev;
		int crossPixel;
		Vector2i[] crossN;
		Vector2i max;
		
		while(bIt.hasNext()){
			base = bIt.next();
			if (markedBases.contains((Integer)base)) continue;
			
			ArrayList<Integer> newPath = new ArrayList<Integer>();
			newPath.add(base);
			neigh = SkeletonUtils.getCircularNeighbors(base, w); //Thinning gives cross path only
			for(int n=0;n<8;n++){
				if(isSkPoint[neigh[n]]) {
				next = neigh[n]; 
				move = n;
				break;
				}
			}
			//Start walking to base point
			prev=base;
			while(next!=-1 && !isBasePoint[next]){
			//System.out.println("Looping");
				newPath.add(next);

				max = SkeletonUtils.getMaxDirectionalNeighbor(imageArray, isSkPoint, w, next, move);
				auxPrev = next;
				
				//REFINATE THIS STEP								
				if (max.x==-1) {//Find best cross Neighbor, not in return way
					crossN = SkeletonUtils.getCrossNeighborsDir(next, w);
					int maxCross=-1;					
					for(int i=0;i<4;i++){
					crossPixel = crossN[i].x;
						if(isSkPoint[crossPixel] && crossPixel!=prev 
								&& (imageArray[crossPixel]) > maxCross){
							maxCross = imageArray[crossPixel];							
							next = crossPixel; move = crossN[i].y;
						}
					}			
					prev=auxPrev;
					if(maxCross==-1) next=-1;
				}
				else{
					prev=next;
					next = max.x; move = max.y;
				}
			}			
			if(next!=-1){
				newPath.add(next);
				markedBases.add(next);
				wormPaths.add(newPath);
			}
		}		
		return wormPaths;
	}
	

	}
