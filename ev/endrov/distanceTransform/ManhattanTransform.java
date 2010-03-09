package endrov.distanceTransform;

import endrov.imageset.EvPixels;

public class ManhattanTransform extends TwoScanDiscreteTransform
	{
	
	public ManhattanTransform(EvPixels input){
		super(input);
	}
	
	@Override
	int forwardDistance(int x, int y,int w, int h)
		{
		// TODO Auto-generated method stub
		if (binaryArray[y*w+x] ==0) {return 0;}
		int fourNeighbors[] = new int[2];
		
		fourNeighbors[0] = forwardArray[y*w+ (x-1)] +1 ; //left
		fourNeighbors[1] = forwardArray[(y-1)*w +x ]+1;  //up
		
		
		//find minimum value and actualize forwardArray
		int min = fourNeighbors[0];
		min	= (min > fourNeighbors[1])? fourNeighbors[1]: min;
		
		return min;	
	}
	
	@Override
	int backwardDistance(int x, int y,int w, int h)
		{
		// TODO Auto-generated method stub
		int fourNeighbors[] = new int[2];
		
		fourNeighbors[0] = backwardArray[y*w+ (x+1)]+1; //left
		fourNeighbors[1] = backwardArray[(y+1)*w +x]+1;  //up
		
		//find minimum value and actualize forwardScanImage
		int min = fourNeighbors[0];
		min	= (min > fourNeighbors[1])? fourNeighbors[1]: min;
		
		int minBackward = (forwardArray[(y*w)+x] < min)? forwardArray[(y*w)+x] : min;		
		return minBackward;
		}
	}
