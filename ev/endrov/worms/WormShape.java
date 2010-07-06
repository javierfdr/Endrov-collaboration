package endrov.worms;

import java.util.ArrayList;
import java.util.Iterator;

import endrov.worms.skeleton.SkeletonUtils;

public class WormShape
	{
	ArrayList<Integer> wormContour;
	ArrayList<Integer> wormArea;
	WormPixelMatcher wpm;
	boolean[] isContourPoint;
	boolean[] isWormArea;
	
	public WormShape(ArrayList<Integer> wormContour,ArrayList<Integer> wormArea, WormPixelMatcher wpm){
		wormContour = new ArrayList<Integer>(wormContour);
		wormArea = new ArrayList<Integer>(wormArea);
		this.wpm = wpm;
		isContourPoint = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wormContour);
		isWormArea = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wormArea);		
	}
	
	/**
	 * Constructs a worm shape based on the type of worm information given. If contourGiven is true
	 * then wormPoints must be the list of points belonging to the worm contour. Otherwise the list
	 * must be the list of points belonging to the worm area, including the worm contour. The missing
	 * points (either contour or area) are calculated based on the input
	 * 
	 */
	public WormShape(ArrayList<Integer> wormPoints, WormPixelMatcher wpm, boolean contourGiven){
		this.wpm = wpm;
		if(contourGiven){
			wormContour = new ArrayList<Integer>(wormPoints);
			isContourPoint = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wormContour);			
		}
		else{
			wormArea = new ArrayList<Integer>(wormPoints);
			isWormArea = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wormArea);
			wormContour = contourFromArea(wormArea,isWormArea,wpm);
			isContourPoint =  SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wormContour);	
		}
	}

	public ArrayList<Integer> getWormContour()
		{
		return wormContour;
		}

	public ArrayList<Integer> getWormArea()
		{
		return wormArea;
		}

	public WormPixelMatcher getWpm()
		{
		return wpm;
		}

	public boolean[] getIsContourPoint()
		{
		return isContourPoint;
		}

	public boolean[] getIsWormArea()
		{
		return isWormArea;
		}

	private static ArrayList<Integer> contourFromArea(ArrayList<Integer> area, boolean isArea[],WormPixelMatcher wpm){
		Iterator<Integer> areaIt = area.iterator();
		ArrayList<Integer> contour = new ArrayList<Integer>();
		int[] neigh;
		int pixel;
		while(areaIt.hasNext()){
			pixel = areaIt.next();
			neigh = SkeletonUtils.getCrossNeighbors(pixel, wpm.w);
			//If the area point has a non-area pixel then is a contour point
			for(int i =0; i<neigh.length;i++){
				if(!isArea[neigh[i]]){
					contour.add(pixel);
					break;
				}
			}			
		}		
		return contour;
	}	
}


