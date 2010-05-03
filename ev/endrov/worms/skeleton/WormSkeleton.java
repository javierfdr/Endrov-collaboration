package endrov.worms.skeleton;

import java.util.ArrayList;
import java.util.Iterator;

import endrov.worms.skeleton.NotWormException;

public class WormSkeleton
	{
		int[] basePoints;
		ArrayList<Integer> skPoints;
		boolean[] isSkPoint;
		
		public WormSkeleton(ArrayList<Integer> basePoints,ArrayList<Integer> skPoints,int imageSize) 
			throws NotWormException{
				if (basePoints.size()!=2) throw new NotWormException("Wrong amount of base points. Must be exactly two");
				this.skPoints = new ArrayList<Integer>(skPoints);
				
				this.isSkPoint = new boolean[imageSize];
				Iterator<Integer> it = skPoints.iterator();
				while(it.hasNext()){
					this.isSkPoint[it.next()] = true;					
				}				
		}	
	}
