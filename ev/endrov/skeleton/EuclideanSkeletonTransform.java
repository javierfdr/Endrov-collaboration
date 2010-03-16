package endrov.skeleton;

import endrov.util.Vector2i;

public final class EuclideanSkeletonTransform extends SkeletonTransform
	{
		@Override
		int[] getNeighbors(int position, int w)
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
		 * Returns the neighbor that corresponds to the maximum directional movement from
		 * previousPixel to currentPixel, performing the movement neighborMovement.
		 */
		public Vector2i getMaxDirectionalNeighbor(int[] imageArray, int w,
				int currentPixel, int previousPixel, int neighborMovement)
			{
				int nList[] = new int[3];
								
		switch (neighborMovement)
			{
			case 0:
				nList[0] = currentPixel-w; // up
				nList[1] = currentPixel-w+1; // up-right
				nList[2] = currentPixel-w-1; // up-left
				break;
			case 1:
				nList[0] = currentPixel+1; // right
				nList[1] = currentPixel-w+1; // right-up
				nList[2] = currentPixel+w+1; // right-down
				break;
			case 2:
				nList[0] = currentPixel+w; // down
				nList[1] = currentPixel+w-1; // down-left
				nList[2] = currentPixel+w+1; // down-right
				break;
			case 3:
				nList[0] = currentPixel-1; // left
				nList[1] = currentPixel-1-w; // left-down
				nList[2] = currentPixel-1+w; // left-up
				break;
			case 4:
				nList[0] = currentPixel-w-1; //up-left
				nList[1] = currentPixel-1; //left
				nList[2] = currentPixel-w; //up
				break;
			case 5:
				nList[0] = currentPixel-w+1; //up-right
				nList[1] = currentPixel+1; //right
				nList[2] = currentPixel-w; //up
				break;
			case 6:
				nList[0] = currentPixel+w-1; //down-left
				nList[1] = currentPixel+w; //down
				nList[2] = currentPixel-1; //left
				break;
			case 7:
				nList[0] = currentPixel+w+1; //down-right
				nList[1] = currentPixel+1; //right
				nList[2] = currentPixel+w; //down
				break;
			}
		
			//get the max directional neighbor and its direction
			int max=imageArray[nList[0]];
			int maxIndex=0;
			for (int i=1; i<3; i++){
				if (imageArray[nList[i]] > max) {
					max = imageArray[nList[i]];
					maxIndex = i;
				}				
			}
			return new Vector2i(max,maxIndex);				
		}
	}	

