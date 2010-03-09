
package endrov.distanceTransform;

import endrov.imageset.EvPixels;

public class chessboardTransform extends TwoScanDiscreteTransform
	{
	
	public chessboardTransform(EvPixels input){
		super(input);
	}
	
	@Override
	int forwardDistance(int x, int y,int w, int h)
		{
		// TODO Auto-generated method stub
		if (binaryArray[y*w+x] ==0) {return 0;}
		int fourNeighbors[] = new int[4];
		
		fourNeighbors[0] = forwardArray[y*w+ (x-1)] +1; //left
		fourNeighbors[1] = forwardArray[(y-1)*w +x ]+1; //up
		fourNeighbors[2] = forwardArray[(y-1)*w+(x-1)]+1; //left-up
		fourNeighbors[3] = forwardArray[(y-1)*w+(x+1)]+1; //right-up
		
		//find minimum value and actualize forwardScanImage
		int min = fourNeighbors[0];
		for (int it=1;it<4;it++){
			if(fourNeighbors[it]<min) min=fourNeighbors[it];
		}
		
		return min;	
	}
	
	@Override
	int backwardDistance(int x, int y,int w, int h)
		{
		// TODO Auto-generated method stub
		int fourNeighbors[] = new int[4];
		
		fourNeighbors[0] = backwardArray[y*w+ (x+1)]+1; //left
		fourNeighbors[1] = backwardArray[(y+1)*w +x]+1;  //up
		fourNeighbors[2] = forwardArray[(y+1)*w+(x+1)]+1; //left-up
		fourNeighbors[3] = forwardArray[(y+1)*w+(x-1)]+1; //right-up
		
		//find minimum value and actualize forwardScanImage
		int min = fourNeighbors[0];
		for (int it=1;it<4;it++){
			if(fourNeighbors[it]<min) min=fourNeighbors[it];
		}
		
		int minBackward = (forwardArray[(y*w)+x] < min)? forwardArray[(y*w)+x] : min;		
		return minBackward;
		}
	}
