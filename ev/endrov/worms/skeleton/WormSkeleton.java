package endrov.worms.skeleton;

import java.util.ArrayList;
import java.util.Iterator;

import endrov.worms.WormPixelMatcher;
import endrov.worms.skeleton.NotWormException;

/**
 * Class representing a 1 worm skeleton, conformed by its base points,
 * and skeleton points *
 */
public class WormSkeleton
	{
	int[] basePoints;
	ArrayList<Integer> skPoints;
	boolean[] isSkPoint;
	WormPixelMatcher wpm;

	public WormSkeleton(ArrayList<Integer> basePoints,
			ArrayList<Integer> skPoints, WormPixelMatcher wpm) throws NotWormException
		{
		if (basePoints.size()!=2)
			throw new NotWormException(
					"Wrong amount of base points. Must be exactly two");
		else {
			this.basePoints = new int[2];
			this.basePoints[0] = basePoints.get(0);
			this.basePoints[1] = basePoints.get(1);
		}
		this.skPoints = new ArrayList<Integer>(skPoints);		
		this.isSkPoint = new boolean[wpm.getH()*wpm.getW()];
		this.isSkPoint = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), skPoints);
		this.wpm = wpm;
		}

	public WormSkeleton(WormClusterSkeleton wcs, WormPixelMatcher wpm) throws NotWormException
		{
		if (wcs.getBasePoints().size()!=2)
			throw new NotWormException(
					"Wrong amount of base points. Must be exactly two");
		else{
			ArrayList<Integer> bp = wcs.getBasePoints();
			this.basePoints = new int[2];
			this.basePoints[0] = bp.get(0);
			this.basePoints[1] = bp.get(1);
		}
		this.skPoints = new ArrayList<Integer>(wcs.getSkPoints());		
		this.isSkPoint = new boolean[wpm.getH()*wpm.getW()];
		this.isSkPoint = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wcs.getSkPoints());
		this.wpm = wpm;
		}
	
	public int[] getBasePoints()
		{
		return basePoints;
		}

	public ArrayList<Integer> getSkPoints()
		{
		return skPoints;
		}
	
	public boolean[] getIsSkPoint(){
		return isSkPoint;
	}
	
	public WormPixelMatcher getPixelMatcher(){
		return wpm;
	}
	
	public void setSkPoints(ArrayList<Integer> skPoints){
		this.skPoints = new ArrayList<Integer>(skPoints);	
	}
	
	}
