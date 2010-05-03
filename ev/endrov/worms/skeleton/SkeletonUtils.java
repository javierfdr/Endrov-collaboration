package endrov.worms.skeleton;

import java.util.ArrayList;
import java.util.Iterator;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector2i;

public class SkeletonUtils
	{

	/**
	 * Returns the surrounding pixels. This is all the pixels in the positions of
	 * the 3x3 matrix where position is the center.
	 */
	public static int[] getCircularNeighbors(int position, int w)
		{
		int neighbors[] = new int[8];
		neighbors[0] = position-w; // Up
		neighbors[1] = neighbors[0]+1; // up-right
		neighbors[2] = position+1; // right;
		neighbors[3] = position+w+1; // down-right
		neighbors[4] = neighbors[3]-1; // down
		neighbors[5] = neighbors[4]-1; // down-left
		neighbors[6] = position-1; // left
		neighbors[7] = neighbors[0]-1; // up-left

		return neighbors;
		}

	/**
	 * Returns the surrounding pixels. This is all the pixels in the positions of
	 * the 3x3 matrix where position is the center.
	 */
	public static int[] getCrossNeighbors(int position, int w)
		{
		int neighbors[] = new int[8];
		neighbors[0] = position-w; // Up
		neighbors[1] = position+1; // right;
		neighbors[2] = position+w; // down
		neighbors[3] = position-1; // left

		return neighbors;
		}

	/**
	 * Returns the surrounding pixels. This is all the pixels in the positions of
	 * the 3x3 matrix where position is the center.
	 */
	public static Vector2i[] getCrossNeighborsDir(int position, int w)
		{
		Vector2i neighbors[] = new Vector2i[4];
		neighbors[0] = new Vector2i(position-w,0); // Up
		neighbors[1] = new Vector2i(position+1,2); // right;
		neighbors[2] = new Vector2i(position+w,4); // down
		neighbors[3] = new Vector2i(position-1,6); // left

		return neighbors;
		}
	
	/**
	 * True if neighbor is a circular neighbor of position in a image of width w
	 */
	public static boolean isCircularNeighbor(int neighbor, int position, int w)
		{
		int diff = neighbor-position;
		int[] neigh =
			{ w, -w, 1, -1, w-1, w+1, -w-1, -w+1 };

		for (int i = 0; i<8; i++)
			{
			if (diff==neigh[i])
				return true;
			}
		return false;
		}

	/**
	 * Creates and EvPixels image setting to 1 the positions in the list points.
	 */
	public static EvPixels buildImage(EvPixels input, ArrayList<Integer> points)
		{
		EvPixels skImage = new EvPixels(EvPixelsType.INT, input.getWidth(), input
				.getHeight());
		int[] skArray = skImage.getArrayInt();

		Iterator<Integer> it = points.iterator();
		while (it.hasNext())
			{
			int pos = (int) it.next();
			skArray[pos] = 1;
			}
		return skImage;
		}

	/**
	 * Creates and EvPixels image setting the positions in the list points.
	 */
	public static EvPixels buildImage(int w, int h,int[] matrix)
		{
		EvPixels skImage = new EvPixels(EvPixelsType.INT, w, h);
		int[] skArray = skImage.getArrayInt();

		for (int i = 0; i<matrix.length; i++)
			{
			if (matrix[i]>0)
				{
				skArray[i] = matrix[i];
				}
			}
		return skImage;
		}

	/**
	 * Creates and EvPixels image setting to 1 the positions in the list points.
	 */
	public static EvPixels buildImage(int w, int h, boolean[] matrix)
		{
		EvPixels skImage = new EvPixels(EvPixelsType.INT, w, h);
		int[] skArray = skImage.getArrayInt();

		for (int i = 0; i<matrix.length; i++)
			{
			if (matrix[i])
				{
				skArray[i] = 1;
				}
			}
		return skImage;
		}
	
	/**
	 * Returns an array of length 'size' setting true every position found in
	 * list.
	 */
	public static boolean[] listToMatrix(int size, ArrayList<Integer> list)
		{

		boolean[] matrix = new boolean[size];
		Iterator<Integer> it = list.iterator();
		while (it.hasNext())
			{
			int pixelPos = (int) it.next();
			matrix[pixelPos] = true;
			}
		return matrix;
		}
/**
 * Returns the position of the points that are higher than 0 in the given matrix
 */
	public static ArrayList<Integer> matrixToList(int[] matrix)
		{
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i<matrix.length; i++)
			{
			if (matrix[i]>0)
				list.add(i);
			}
		return list;
		}
	
	/**
	 * Returns the position of the points that are true in the given matrix
	 */
		public static ArrayList<Integer> matrixToList(boolean[] matrix)
			{
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i<matrix.length; i++)
				{
				if (matrix[i])
					list.add(i);
				}
			return list;
			}


	public static ArrayList<Vector2i> getDirectionalNeighbors(int[] imageArray, int w,
			int currentPixel,int neighborMovement)
		{
			int nList[] = new int[6];
							
	switch (neighborMovement)
		{
		case 0: //up
			nList[0] = currentPixel-w; nList[1] = 0; // up
			nList[2] = currentPixel-w+1;nList[3] =1; // up-right
			nList[4] = currentPixel-w-1;nList[5] =7; // up-left
			break;
		case 1: //up-right
			nList[0] = currentPixel-w+1; nList[1] = 1;//up-right
			nList[2] = currentPixel+1; nList[3] = 2;//right
			nList[4] = currentPixel-w; nList[5] = 0;//up
			break;
		case 2: //right
			nList[0] = currentPixel+1; nList[1] = 2; // right
			nList[2] = currentPixel-w+1; nList[3] = 1; // right-up
			nList[4] = currentPixel+w+1; nList[5]= 3;// right-down
			break;
		case 3: //down-right
			nList[0] = currentPixel+w+1; nList[1] = 3;//down-right
			nList[2] = currentPixel+1; nList[3] = 2; //right
			nList[4] = currentPixel+w; nList[5] = 4;//down
			break;
		case 4: //down
			nList[0] = currentPixel+w;nList[1] = 4; // down
			nList[2] = currentPixel+w-1; nList[3] = 5;// down-left
			nList[4] = currentPixel+w+1; nList[5]= 3; // down-right
			break;

		case 5: //down-left
			nList[0] = currentPixel+w-1; nList[1] = 5;//down-left
			nList[2] = currentPixel+w; nList[3] = 4;//down
			nList[4] = currentPixel-1; nList[5] = 6;//left
			break;
		case 6: //left
			nList[0] = currentPixel-1; nList[1] = 6; // left
			nList[2] = currentPixel-1+w; nList[3]= 5;// left-down
			nList[4] = currentPixel-1-w; nList[5] = 7;// left-up
			break;
		case 7: //up-left
			nList[0] = currentPixel-w-1; nList[1] = 7;//up-left
			nList[2] = currentPixel-1; nList[3] = 6;//left
			nList[4] = currentPixel-w; nList[5] = 0;//up
			break;

		}
	ArrayList<Vector2i> neighbors= new ArrayList<Vector2i>(3);
	for (int i=0; i<6; i+=2){
		Vector2i n = new Vector2i(nList[i],nList[i+1]);
		neighbors.add(n);
	}
	return neighbors;			
	}
	

	/**
	 * Returns the neighbor that corresponds to the maximum directional movement from
	 * previousPixel to currentPixel, performing the movement neighborMovement.
	 */
	public static Vector2i getMaxDirectionalNeighbor(int[] imageArray,boolean[] isSkPoint, int w,
			int currentPixel,int neighborMovement)
		{
		ArrayList<Vector2i> neighbors= SkeletonUtils.getDirectionalNeighbors(imageArray, w, currentPixel, neighborMovement);
		//get the max directional neighbor and its direction

		Vector2i maxVector= new Vector2i(-1,-1);
		int max = Integer.MAX_VALUE;
		
		//Get first that is skPoint
		for(int i=0;i<neighbors.size();i++){
			if(isSkPoint[neighbors.get(i).x]){
				maxVector = neighbors.get(i);
				max = maxVector.x;
				break;
			}
		}
				
		Iterator<Vector2i> it = neighbors.iterator(); it.next();
		Vector2i n;
		while (it.hasNext()){
			n= it.next();
			if (imageArray[n.x] > max && isSkPoint[n.x]) {
				max = imageArray[n.x];
				maxVector = n;
			}				
		}
		return maxVector;	
		}	
	}
