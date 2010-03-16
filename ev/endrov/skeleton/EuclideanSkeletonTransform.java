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
				int nList[] = new int[6];
								
		switch (neighborMovement)
			{
			case 0: //up
				nList[0] = currentPixel-w; nList[1] = 0; // up
				nList[2] = currentPixel-w+1;nList[3] =5; // up-right
				nList[4] = currentPixel-w-1;nList[5] = 4; // up-left
				break;
			case 1: //right
				nList[0] = currentPixel+1; nList[1] = 1; // right
				nList[2] = currentPixel-w+1; nList[3] = 5; // right-up
				nList[4] = currentPixel+w+1; nList[5]= 7;// right-down
				break;
			case 2: //down
				nList[0] = currentPixel+w;nList[1] = 2; // down
				nList[2] = currentPixel+w-1; nList[3] = 6;// down-left
				nList[4] = currentPixel+w+1; nList[5]= 7; // down-right
				break;
			case 3: //left
				nList[0] = currentPixel-1; nList[1] = 3; // left
				nList[2] = currentPixel-1+w; nList[3]= 6;// left-down
				nList[4] = currentPixel-1-w; nList[5] = 4;// left-up
				break;
			case 4: //up-left
				nList[0] = currentPixel-w-1; nList[1] = 4;//up-left
				nList[2] = currentPixel-1; nList[3] = 3;//left
				nList[4] = currentPixel-w; nList[5] = 0;//up
				break;
			case 5: //up-right
				nList[0] = currentPixel-w+1; nList[1] = 5;//up-right
				nList[2] = currentPixel+1; nList[3] = 1;//right
				nList[4] = currentPixel-w; nList[5] = 0;//up
				break;
			case 6: //down-left
				nList[0] = currentPixel+w-1; nList[1] = 6;//down-left
				nList[2] = currentPixel+w; nList[3] = 2;//down
				nList[4] = currentPixel-1; nList[5] = 3;//left
				break;
			case 7: //down-right
				nList[0] = currentPixel+w+1; nList[1] = 7;//down-right
				nList[2] = currentPixel+1; nList[3] = 1; //right
				nList[4] = currentPixel+w; nList[5] = 2;//down
				break;
			}
		
			//get the max directional neighbor and its direction
			int max=imageArray[nList[0]];
			int moveIndex=nList[1];
			int maxIndex=0;
			for (int i=2; i<6; i+=2){
				if (imageArray[nList[i]] > max) {
					max = imageArray[nList[i]];
					moveIndex = nList[i+1];
					maxIndex=i;
				}				
			}
			return new Vector2i(nList[maxIndex],moveIndex);				
		}
	}	

