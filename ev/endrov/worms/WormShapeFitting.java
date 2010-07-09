package endrov.worms;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector2d;

import com.graphbuilder.curve.CardinalSpline;
import com.graphbuilder.curve.Point;

import endrov.imageset.EvPixels;
import endrov.tesselation.PolygonRasterizer;
import endrov.util.Vector2i;
import endrov.util.Vector3i;
import endrov.util.curves.EvCardinalSpline;
import endrov.worms.skeleton.NotWormException;
import endrov.worms.skeleton.SkeletonTransform;
import endrov.worms.skeleton.SkeletonUtils;
import endrov.worms.skeleton.WormClusterSkeleton;
import endrov.worms.skeleton.WormSkeleton;

public class WormShapeFitting
	{

	/**
	 * Calculates the cost of the objective function for a given worm profile
	 * rasterization. Objective Function is: The sum of the rasterized pixels that
	 * are background
	 */
	public static double objFunction(ArrayList<Integer> rastShape, int[] dtArray)
		{
		double count = 0;
		Iterator<Integer> it = rastShape.iterator();
		while (it.hasNext())
			{
			if (dtArray[it.next()]==0)
				count++;
			}
		return count*1.0;
		}

	public static void wormClusterOptimization(WormClusterSkeleton wc,WormPixelMatcher wpm,int wormLength)
		{
		SkeletonTransform.guessWormPaths(wc, 15,wpm,WormSkeleton.getMinMaxLength(wormLength));
		}

	public static WormDescriptor bestNeighborOptimization(WormDescriptor wd)
		{
		int[] wormAngles = new int[wd.wprof.thickness.length];
		// Initialize neighbor array
		Vector2i[] auxNeighArray = new Vector2i[(wd.wprof.thickness.length*4)-8];
		for (int i = 0; i<auxNeighArray.length; i++)
			{
			auxNeighArray[i] = new Vector2i();
			}
		double best = Integer.MAX_VALUE;
		boolean rast = true;
		ArrayList<Integer> rastShape = new ArrayList<Integer>();
		try
			{
			rastShape = wd.rasterizeWorm();
			}
		catch (RuntimeException e)
			{
			rast = false;
			}
		if (rast)
			{
			best = objFunction(rastShape, wd.dtArray);
			}
		double initBest = best;
		boolean newBend = true;
		double currentValue = -1;
		Vector2i bestPert = new Vector2i();
		int cp;
		int iter = 1;
		// Start minimization
		while (newBend)
			{
			// System.out.println("ITERATION: "+iter);
			iter++;
			newBend = false;
			int dist = -1;
			int newpos = -1;
			int oldpos = -1;
			int bestPos = -1;

			getNeighborhood(wormAngles, wd, auxNeighArray);

			for (int i = 0; i<auxNeighArray.length; i++)
				{
				cp = auxNeighArray[i].x;
				// System.out.println("Trying: "+auxNeighArray[i]);

				if (auxNeighArray[i].y<0)
					{
					dist = -auxNeighArray[i].y;
					newpos = wd.angleSouthLine[cp][dist];
					wd.updateCP(cp, newpos);
					}
				else
					{
					dist = auxNeighArray[i].y;
					newpos = wd.angleNorthLine[cp][dist];
					wd.updateCP(cp, newpos);
					}
				rast = true;
				try
					{
					rastShape = wd.rasterizeWorm();
					}
				catch (RuntimeException e)
					{
					rast = false;
					currentValue = Integer.MAX_VALUE;
					// System.out.println("FAILURE: "+cp+" "+dist+" "+auxNeighArray[i]);
					}
				if (rast)
					{
					currentValue = objFunction(rastShape, wd.dtArray);
					}
				if (currentValue<best)
					{
					best = currentValue;
					bestPert = new Vector2i(auxNeighArray[i].x, auxNeighArray[i].y);
					bestPos = newpos;
					newBend = true;
					}
				// return change
				if (wormAngles[cp]<0)
					{
					dist = -wormAngles[cp];
					oldpos = wd.angleSouthLine[cp][dist];
					}
				else
					{
					dist = wormAngles[cp];
					oldpos = wd.angleNorthLine[cp][dist];
					}
				wd.updateCP(cp, oldpos);
				}
			if (newBend)
				{
				wormAngles[bestPert.x] = bestPert.y;

				wd.updateCP(bestPert.x, bestPos);
				// System.out.println("Got better "+bestPert);

				for (int i = 0; i<wormAngles.length; i++)
					{
					// System.out.print(wormAngles[i]+" ");
					}
				// System.out.println();
				}
			// System.out.println("\n");
			}

		System.out.println("INIT-BEST: "+initBest+" "+best);
		return wd;
		}

	public static void getNeighborhood(int[] wormAngles, WormDescriptor wd,
			Vector2i[] auxNeighArray)
		{
		/*
		 * Change +1 / -1 in the given direction generates thickness.length
		 * neighbors Neighbors are a value indicating the displacement. Positive
		 * corresponds to North and Negative to South
		 */
		// Avoid endpoints and calculate all possible +1 neighborhoods
		int nCount = 0;
		for (int i = 1; i<wormAngles.length-1; i++, nCount++)
			{
			if ((wormAngles[i]+1)<=wd.angleNorthLine[i].length-1)
				{
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i]+1;
				}
			else
				{
				// dont perturbate
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i];
				}
			}

		for (int i = 1; i<wormAngles.length-1; i++, nCount++)
			{
			if ((wormAngles[i]-1)>=-1*(wd.angleSouthLine[i].length-1))
				{
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i]-1;
				}
			else
				{
				// dont perturbate
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i];
				}
			}

		// int nCount=0;
		for (int i = 1; i<wormAngles.length-1; i++, nCount++)
			{
			if ((wormAngles[i]+1)<=wd.angleNorthLine[i].length-1)
				{
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i]+2;
				}
			else
				{
				// dont perturbate
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i];
				}
			}

		for (int i = 1; i<wormAngles.length-1; i++, nCount++)
			{
			if ((wormAngles[i]-1)>=-1*(wd.angleSouthLine[i].length-1))
				{
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i]-2;
				}
			else
				{
				// dont perturbate
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i];
				}
			}
		}
	
	public static ArrayList<Integer> fitIsolatedWorm(WormClusterSkeleton wc,WormPixelMatcher wpm, int minSkeletonLength){
		//TAKE LATER INTO ACCOUNT, TOo BIG SKELETONS
	
		ArrayList<Integer> wormContour = SkeletonTransform.getShapeContour(wc,minSkeletonLength);
	//	ArrayList<Point> points = wpm.pixelListToPoint(wormContour);
	//	ArrayList<Point> base = new ArrayList<Point>();
	//	base.add(wpm.pixelToPoint(wormContour.get(0)));
	//	base.add(wpm.pixelToPoint(wormContour.get(wormContour.size()-1)));
		
	//	CardinalSpline skSpline = EvCardinalSpline.getShapeSpline(base,points,0.5,0.2);
	//	wormContour = wpm.pointListToPixelList(EvCardinalSpline.getCardinalPoints(skSpline, 0));
		return wormContour;

	}
	
	public static ArrayList<ArrayList<Integer>> fitWormCluster(WormClusterSkeleton wc,WormProfile wprof, 
			int[] dtArray,EvPixels inputImage,int wormLength){
	
	ArrayList<ArrayList<Integer>> shapeList = new ArrayList<ArrayList<Integer>>();
	WormPixelMatcher wpm = wprof.wpm;
	System.out.println("Guessing Worm Path");
	ArrayList<WormSkeleton> skList = SkeletonTransform.wormsFromPaths(
			inputImage, dtArray, wpm, SkeletonTransform.guessWormPaths(wc, 15,wpm,WormSkeleton.getMinMaxLength(wormLength)));
	System.out.println("Finished Guessing");
	
	Iterator<WormSkeleton> wit = skList.iterator();
	int count=0;
	
	while (wit.hasNext())
			{
			System.out.println();
			System.out.println("Matching Worm in WormCLuster: "+count);	
			count+=1;
			
			CardinalSpline cs;
			WormSkeleton ws = wit.next();
			SkeletonUtils.makeConsecutive(ws);
			cs = EvCardinalSpline.getShapeSpline(ws, 0.5, 0.09);
			if (cs!=null)
				{
				int[] profPts = wpm.pointListToPixel(EvCardinalSpline
						.getCardinalPoints(cs, wprof.thickness.length));
				}
			else{
				System.out.println("  ---> Output: Path is not a skeleton. Skeleton Spline not created");
				continue;
				}

			System.out.print("  --> Building Descriptor");
			WormDescriptor wd = new WormDescriptor(wprof, ws, dtArray,
					wprof.thickness.length, 8.0);
			System.out.println(": Succesfully built");
			System.out.print("  --> Matching shape: Best Neighbor Optimization");
			wd = WormShapeFitting.bestNeighborOptimization(wd);
			System.out.println(": Match succesfully found");
			
			boolean rastOk = true;
			ArrayList<Integer> rastShape = new ArrayList<Integer>();
			try
				{			
				rastShape = wd.fitAndRasterizeWorm();
				}
			catch (RuntimeException e)
				{
				rastOk = false;		
				}
			if (!rastOk)
				continue;
			
			System.out.println("  -->Worm succesfully rasterized");
			shapeList.add(rastShape);
		}
		return shapeList;
	};
	
	}
