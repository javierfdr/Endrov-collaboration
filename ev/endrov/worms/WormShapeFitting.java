package endrov.worms;

import java.awt.image.RasterOp;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Hashtable;

import javax.vecmath.Vector2d;

import com.graphbuilder.curve.CardinalSpline;
import com.graphbuilder.curve.Point;

import endrov.imageset.EvPixels;
import endrov.quickhull3d.Vector3d;
import endrov.tesselation.PolygonRasterizer;
import endrov.util.Vector2i;
import endrov.util.Vector3i;
import endrov.util.curves.EvCardinalSpline;
import endrov.worms.skeleton.NotWormException;
import endrov.worms.skeleton.SkeletonTransform;
import endrov.worms.skeleton.SkeletonUtils;
import endrov.worms.skeleton.WormClusterSkeleton;
import endrov.worms.skeleton.WormSkeleton;
import endrov.worms.utils.greedyNonBipartiteAssignment;


public class WormShapeFitting
	{

	/**
	 * Calculates the cost of the objective function for a given worm profile
	 * rasterization. Objective Function is: The sum of the rasterized pixels that
	 * are background
	 */
	public static double objFunction(ArrayList<Integer> rastShape, int[] dtArray)
		{
		if(rastShape ==null) return Double.MAX_VALUE;
		double background = 0;
		double foreground = 0;		
		Iterator<Integer> it = rastShape.iterator();
		while (it.hasNext())
			{
			if (dtArray[it.next()]==0)
				background++;
			else{
				foreground++;
			}
			}
		//return background;
		return (background/(foreground+background))*1.4;
		}

	public static void wormClusterOptimization(WormClusterSkeleton wc,WormPixelMatcher wpm,int wormLength)
		{
		SkeletonTransform.guessWormPaths(wc, 15,wpm,WormSkeleton.getMinMaxLength(wormLength));
		}

	public static double bestNeighborOptimization(WormDescriptor wd)
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
		return best;
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
	
	public static ArrayList<Integer> fitIsolatedWorm(WormClusterSkeleton wc,WormPixelMatcher wpm, WormProfile wprof,
			int[] dtArray, int minSkeletonLength,double splineRate){
		//TAKE LATER INTO ACCOUNT, TOo BIG SKELETONS
	
		ArrayList<Integer> wormContour = SkeletonTransform.getShapeContour(wc,minSkeletonLength);
		WormSkeleton ws = null;
		try
			{
			ws = new WormSkeleton(wc, wpm);
			}
		catch (NotWormException e)
			{
			System.out.println("EXCEPTION ");
			e.printStackTrace();
			}
		
		wormContour = WormShape.ensureCounterClockwise(wormContour, wprof, ws, dtArray);		
		return wormContour;
	}

	
	//public static void addToBase(Hashtable<Integer,> pathDic, ){
	
	//}
	
	/**
	 * Filter the table of possible matchings finding the lowest cost assignment
	 * the maximizes the number of base points taken. Its implemented following the
	 * Hungarian Algorithm for the assignment problem in a bipartite graph.
	 * 
	 */
	
	public static ArrayList<WormShape> filterFittingDictionary(Hashtable<Integer,ArrayList<Vector3d>> fitDic, 
			ArrayList<WormShape> matchedShapes,WormClusterSkeleton wc){
									
		HashSet<WormShape> clusterBestMatch = new HashSet<WormShape>();
		ArrayList<Integer> basePoints = wc.getBasePoints();

		int shapeIndex;
		double min;
		int bestShapeIndex;
		int base;
		HashSet<Integer> checkBases = new HashSet<Integer>();
		

		HashSet<Integer> nonConflictingShapes = new HashSet<Integer>();
		ArrayList<Integer> conflictingBases = detectConflictingBases(fitDic, basePoints,nonConflictingShapes);		
		ArrayList<Integer> shapeList = resolveConflict(conflictingBases,fitDic,basePoints);
				
		//Add conflict resolved shapes to the final match list
		Iterator<Integer> shapeIt = shapeList.iterator();
		while(shapeIt.hasNext()){
			clusterBestMatch.add(matchedShapes.get(shapeIt.next()));
		}
		
		//Add non conflicting shapes to the final match list
		shapeIt = nonConflictingShapes.iterator();
		while(shapeIt.hasNext()){
			clusterBestMatch.add(matchedShapes.get(shapeIt.next()));
		}
		
		return new ArrayList<WormShape>(clusterBestMatch);
		}

	private static ArrayList<Integer> resolveConflict(ArrayList<Integer> conflictingBases, 
			Hashtable<Integer, ArrayList<Vector3d>> fitDic ,ArrayList<Integer> basePoints){
				
			ArrayList<Integer> shapeList = new ArrayList<Integer>();
			
			//create base point index array
			Hashtable<Integer,Integer> baseHash = new Hashtable<Integer, Integer>();
			Hashtable<Integer,Integer> baseReverse = new Hashtable<Integer, Integer>();
			for(int bi=0;bi<conflictingBases.size();bi++){
				baseHash.put(conflictingBases.get(bi),bi);
				baseReverse.put(bi,conflictingBases.get(bi));
			}
			//Create matching matrix
			double[][] matchMatrix = new double[conflictingBases.size()][conflictingBases.size()];
			for(int i=0;i<matchMatrix.length;i++){
			for(int j=0;j<matchMatrix.length;j++){
					matchMatrix[i][j] = -1;
			}
		}
			
			ArrayList<Vector3d> matches;
			Iterator<Vector3d> it3;
			Vector3d match;
			Integer secondBase;
			int base;
			int shapeIndex;
			for(int i =0;i<conflictingBases.size(); i++){
				base = conflictingBases.get(i);
				matches = fitDic.get(base);
				if(matches==null){
					continue;
				} 
				int firstBaseIndex = baseHash.get(base);
				it3 = matches.iterator();
				while (it3.hasNext())
					{
					match = it3.next();				
					shapeIndex = (int) match.z;					
					secondBase = baseHash.get((int)match.x);
					//Discard paths that reach non conflicting bases
					if(secondBase == null) continue;
					
					
					System.out.println("BASE AND SECOND "+firstBaseIndex+" "+secondBase);
					
					matchMatrix[firstBaseIndex][secondBase] = match.y;
					matchMatrix[secondBase][firstBaseIndex] = match.y;
					}
			}
			
			System.out.println("PRINTING MATCH MATRIX");
			greedyNonBipartiteAssignment.printMatchMatrix(matchMatrix);
						
			ArrayList<Vector2i> bestAssignment = greedyNonBipartiteAssignment.findBestAssignment(matchMatrix);
			Iterator<Vector2i> bit = bestAssignment.iterator();
			Vector2i assignment;
			while(bit.hasNext()){
				assignment = bit.next();
				System.out.println("Assignment Pair: "+assignment);
				matches = fitDic.get(baseReverse.get(assignment.x));
				int otherBase;
				if(matches==null){
					System.out.println("FIRST NULL");
					matches = fitDic.get(baseReverse.get(assignment.y));
					if(matches==null) {
						System.out.println("NULL AGAIN");
						continue;}
					otherBase = baseReverse.get(assignment.x);
				}
				else{
					otherBase = baseReverse.get(assignment.y);
				}
					it3 = matches.iterator();
					while(it3.hasNext()){
						match = it3.next();
						if(((int)match.x) == otherBase){
							System.out.println("Adding Shape: "+baseReverse.get(assignment.x)+" "+baseReverse.get(assignment.y));
							shapeList.add((int)match.z);
							break;
						}
					}
				System.out.println("I N     V  I    T  R   O   ->>>>>>>>>");
				}										
			return shapeList;
	}
	
	/**
	 * Calculates the bases whose best match conflicts with another best match
	 * base. 
	 */
	private static ArrayList<Integer> detectConflictingBases(
			Hashtable<Integer, ArrayList<Vector3d>> fitDic, ArrayList<Integer> bases,
			HashSet<Integer> nonConflictingShapes)
		{

		Iterator<Integer> bit = bases.iterator();
		int base;
		ArrayList<Vector3d> matches;
		Iterator<Vector3d> it3;
		Vector3d match;
		Vector3d bestMatch = null;
		double min;
		//int bestMatchIndex;
		//int bestShapeIndex;
		HashSet<Integer> conflictingBases = new HashSet<Integer>();
		Hashtable<Integer, HashSet<Integer>> pathsPerBase= new Hashtable< Integer, HashSet<Integer>>();
		
		//Indexes of shape per base. Just an integer required not list
		Hashtable<Integer, Integer> indexPerBase = new Hashtable<Integer, Integer>();
		
		while (bit.hasNext())
			{
			base = bit.next();			
			matches = fitDic.get(base);
			boolean isNew = true;			
			if (matches!=null)
				{
				min = Double.MAX_VALUE;
				//bestMatchIndex = -1;
				//bestShapeIndex = -1;
				it3 = matches.iterator();
				while (it3.hasNext())
					{
					match = it3.next();
					if (min>match.y)
						{
						//bestMatchIndex = (int) match.x;
						min = match.y;
						//bestShapeIndex = (int)match.z;
						bestMatch = match;
						}
					}
				HashSet<Integer> basePaths = pathsPerBase.get(base);
				if(basePaths==null){
					basePaths = new HashSet<Integer>();
					pathsPerBase.put(base, basePaths);
				}
				basePaths.add((int)bestMatch.x);
				indexPerBase.put(base,(int)bestMatch.z);
					
				basePaths = pathsPerBase.get((int)bestMatch.x);
				if(basePaths==null){
					basePaths = new HashSet<Integer>();
					pathsPerBase.put((int)bestMatch.x, basePaths);
				}
				basePaths.add(base);
				indexPerBase.put((int) bestMatch.x, (int) bestMatch.z);
				}
			}

		bit = bases.iterator();
		HashSet basePaths = new HashSet<Integer>();
		while (bit.hasNext())
			{
			base = bit.next();
			basePaths = pathsPerBase.get(base);
			if (basePaths==null)
				continue;
			System.out.println("BP SIZE: "+basePaths.size());
			// If there are more than 1 path for a base point add all the involved
			// bases
			if (basePaths.size()>1)
				{

				Iterator<Integer> pit = basePaths.iterator();
				while (pit.hasNext())
					{
					conflictingBases.add(pit.next());
					}
				conflictingBases.add(base);
				}
			}
		bit = bases.iterator();
		while (bit.hasNext())
			{
			base = bit.next();
			if (!conflictingBases.contains(base))
				{
				Integer shapeIndex = indexPerBase.get(base);
				if (shapeIndex!=null)
					{
					nonConflictingShapes.add(shapeIndex);
					}
				}
			}

		return new ArrayList<Integer>(conflictingBases);

	}
	
	public static ArrayList<WormShape> filterFittingDictionary2(Hashtable<Integer,ArrayList<Vector3d>> fitDic, 
			ArrayList<WormShape> matchedShapes,WormClusterSkeleton wc){
		
		HashSet<WormShape> clusterBestMatch = new HashSet<WormShape>();
		ArrayList<Integer> basePoints = wc.getBasePoints();
		ArrayList<Vector3d> matches;
		Iterator<Vector3d> it3;
		Vector3d match;
		int bestShapeIndex;
		double min;
		int base;
		HashSet<Integer> checkBases = new HashSet<Integer>();
		for (int i = 0; i<basePoints.size(); i++)
			{			
			base = basePoints.get(i);
			if (checkBases.contains((Integer)base)){
				continue;
			}			
			checkBases.add(base);
			matches = fitDic.get(base);
			if (matches!=null)
				{
				if(matches.size()==1){
					match = matches.get(0);
					System.out.println("Adding best match. COST "+match.y);
					clusterBestMatch.add(matchedShapes.get((int)match.z));
					checkBases.add((int)match.x);
					continue;
				}
				min = Double.MAX_VALUE;
				it3 = matches.iterator();
				bestShapeIndex = -1;
				while (it3.hasNext())
					{
					match = it3.next();
					if (min>match.y)
						{
						min = match.y;
						bestShapeIndex = (int) match.z;
						}
					}
				System.out.println("Adding best match. COST "+ min);
				clusterBestMatch.add(matchedShapes.get(bestShapeIndex));
				}
			}
	
		return new ArrayList<WormShape>(clusterBestMatch);
	}
	
	public static Hashtable<Integer,ArrayList<Vector3d>> fitWormCluster(WormClusterSkeleton wc,WormProfile wprof, 
			int[] dtArray,EvPixels inputImage,int wormLength,ArrayList<WormShape> matchedShapes){
	
	ArrayList<ArrayList<Integer>> shapeList = new ArrayList<ArrayList<Integer>>();
	WormPixelMatcher wpm = wprof.wpm;
	System.out.println("Guessing Worm Path");
	
	ArrayList<WormSkeleton> skList = SkeletonTransform.wormsFromPaths(
			inputImage, dtArray, wpm, SkeletonTransform.guessWormPaths(wc, 15,wpm,WormSkeleton.getMinMaxLength(wormLength)));
	
	ArrayList<WormSkeleton> allList = SkeletonTransform.wormsFromPaths(
			 inputImage, dtArray, wpm, SkeletonTransform.getAllPaths(wc,wormLength));
	Hashtable<Integer,ArrayList<WormSkeleton>> allPathDic = WormShapeFitting.buildPathDictionary(allList);
	
	System.out.println("Finished Guessing");
	
	Iterator<WormSkeleton> wit = skList.iterator();
	int count=0;
	int index =0;
	//For every base a list containing recieving base, optimization value and 
	//matched array position is stored
	Hashtable<Integer,ArrayList<Vector3d>> matchDic = new Hashtable<Integer, ArrayList<Vector3d>>();	
	
	while (wit.hasNext())
			{
			//Optimize WormSkeleton shape
			System.out.println();
			System.out.println("Matching Worm in WormCLuster: "+count);	
			count+=1;
			WormSkeleton ws = wit.next();
			skeletonMatchingOpt(ws,wprof,dtArray,wpm,matchedShapes,matchDic,index,true);					
			index++;
			}
	//Find the base points that have no paths and all the possible paths
	ArrayList<Integer> basePoints = wc.getBasePoints();
	Iterator<Integer> bit = basePoints.iterator();
	int base;
	ArrayList<WormSkeleton> forgottenPaths = new ArrayList<WormSkeleton>();
	while(bit.hasNext()){
		base = bit.next();
		if(matchDic.get(base)==null){
		System.out.println("LA BASE "+base+ " NO TIENE CAMINOS");
			ArrayList<WormSkeleton> allBasePath = allPathDic.get(base);
			if(allBasePath!=null){
				forgottenPaths.addAll(allBasePath);
			}
		}
	}
	//Match the shape for the paths of forgotten base points
	wit=forgottenPaths.iterator();
	count = 0;
	System.out.println("FORGOTTEN SIZEL: "+forgottenPaths.size());
	while (wit.hasNext())
		{
		//Optimize WormSkeleton shape
		System.out.println();
		System.out.println("Matching Forgotten: "+count);	
		count+=1;
		WormSkeleton ws = wit.next();
		skeletonMatchingOpt(ws,wprof,dtArray,wpm,matchedShapes,matchDic,index,false);					
		index++;
		}
	
	/*String pPair = pathToString(currentPath);
	if(!pathPairs.contains(pPair)){
		wormPaths.add(currentPath);
		pathPairs.add(pPair);
		System.out.println("Added");
		}
	*/
	
		return matchDic;
		//return matchedShapes;
	};
	
	private static void skeletonMatchingOpt(WormSkeleton ws,WormProfile wprof,int[] dtArray,
			WormPixelMatcher wpm, ArrayList<WormShape> matchedShapes,
			Hashtable<Integer,ArrayList<Vector3d>> matchDic,int index,boolean isGuessedPath){

			//Optimize WormSkeleton shape

			double[] optValue = {-1};
			System.out.println("Starting bending");
			ArrayList<Integer> rastShape = bendingOptimization(ws, wprof, dtArray,optValue);

			//Preference is given to guessed paths to improve their matching value
			if(isGuessedPath){			
				optValue[0] = optValue[0]*0.30;
			}
			
			WormShape worm = new WormShape(rastShape,wpm,false);
			matchedShapes.add(worm);			
			System.out.println("Finish bending");
	    
			int base1 = ws.getSkPoints().get(0);
			int base2 = ws.getSkPoints().get(ws.getSkPoints().size()-1);
			
			Vector3d newMatch = new Vector3d((double)base2, optValue[0], (double)index);
			ArrayList<Vector3d> list= (ArrayList<Vector3d>)matchDic.get(base1);
	     if (list != null) {
	        	list.add(newMatch);
	     }
	     else{
	     	ArrayList<Vector3d> newList = new ArrayList<Vector3d>();
	     	newList.add(newMatch);
	     	matchDic.put(base1, newList);
	     }
	     
				newMatch = new Vector3d((double)base1, optValue[0], (double)index);
				list= (ArrayList<Vector3d>)matchDic.get(base2);
		     if (list != null) {
		        	list.add(newMatch);
		     }
		     else{
		     	ArrayList<Vector3d> newList = new ArrayList<Vector3d>();
		     	newList.add(newMatch);
		     	matchDic.put(base2, newList);
		     }
	}
	
	public static void printFitDic(Hashtable<Integer,ArrayList<Vector3d> > fittingDic, WormClusterSkeleton wc){
			Iterator<Integer> bit = wc.getBasePoints().iterator();
			Iterator<Vector3d> lit;
			ArrayList<Vector3d> mlist;
			Vector3d rec;
			int base;
			System.out.println("PRINTING MATCH DICTIONARY");
			while(bit.hasNext()){
				base = bit.next();
				System.out.println("For Base: "+base);
				mlist= fittingDic.get(base);
				if(mlist==null) continue;
				
				lit = mlist.iterator();				
				while(lit.hasNext()){
					rec = lit.next();
					System.out.println("       --> "+rec);					
				}		
			}
			System.out.println();
	}
	
	private static ArrayList<Integer> bendingOptimization(WormSkeleton ws,WormProfile wprof,int[] dtArray,double[] optValue){
	
	ArrayList<Integer> rastShape = new ArrayList<Integer>();
	CardinalSpline cs;
	System.out.println("Making Consecutive");
	//SkeletonUtils.makeConsecutive(ws);
	System.out.println("Is consecutive");
	cs = EvCardinalSpline.getShapeSpline(ws, 0.5, 0.09);
	if (cs!=null)
		{
		int[] profPts = ws.getPixelMatcher().pointListToPixel(EvCardinalSpline
				.getCardinalPoints(cs, wprof.thickness.length));
		}
	else{
		System.out.println("  ---> Output: Path is not a skeleton. Skeleton Spline not created");
		return null;
		}

	System.out.print("  --> Building Descriptor");
	WormDescriptor wd = new WormDescriptor(wprof, ws, dtArray,
			wprof.thickness.length, 8.0);
	System.out.println(": Succesfully built");
	System.out.print("  --> Matching shape: Best Neighbor Optimization");
	optValue[0] = WormShapeFitting.bestNeighborOptimization(wd);
	System.out.println(": Match succesfully found");
	
	boolean rastOk = true;
	//ArrayList<Integer> rastShape = new ArrayList<Integer>();
	try
		{			
		rastShape = wd.fitAndRasterizeWorm();
		}
	catch (RuntimeException e)
		{
		rastOk = false;		
		}
	if (!rastOk){
	return null;
	}
	
	System.out.println("  -->Worm succesfully rasterized");
	return rastShape;
	
	}
	
	public static Hashtable<Integer,ArrayList<WormSkeleton>> buildPathDictionary(ArrayList<WormSkeleton> wormSkeletons){
		Hashtable<Integer,ArrayList<WormSkeleton>> pathDic = new Hashtable<Integer, ArrayList<WormSkeleton>>();
		Iterator<WormSkeleton> wit = wormSkeletons.iterator();
		WormSkeleton ws;
		int base1;
		int base2;
		while(wit.hasNext()){
			ws = wit.next();
			base1 = ws.getSkPoints().get(0);
			base2 = ws.getSkPoints().get(ws.getSkPoints().size()-1);
			
	    ArrayList<WormSkeleton> list1= (ArrayList<WormSkeleton>)pathDic.get(base1);
	     if (list1 != null) {
	        	list1.add(ws);
	     }
	     else{
	     	ArrayList<WormSkeleton> newList = new ArrayList<WormSkeleton>();
	     	newList.add(ws);
	     	pathDic.put(base1, newList);
	     }
			
		    ArrayList<WormSkeleton> list2= (ArrayList<WormSkeleton>)pathDic.get(base2);
		     if (list2 != null) {
		        	list2.add(ws);
		     }
		     else{
		     	ArrayList<WormSkeleton> newList = new ArrayList<WormSkeleton>();
		     	newList.add(ws);
		     	pathDic.put(base2, newList);
		     }
		}
		
		return pathDic;
	}

	
	}
