package endrov.skeleton;

import java.util.ArrayList;
import java.util.Iterator;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

public class SkeletonUtils
	{
	
	/**
	 * Returns the surrounding pixels. This is all the pixels in the 
	 * positions of the 3x3 matrix where position is the center.
	 * 
	 */
	public static int[] getCircularNeighbors(int position, int w)
		{
		int neighbors[] = new int[8];
		neighbors[0] = position-w; // Up
		neighbors[1] = neighbors[0]+1; //up-right
		neighbors[2] = position+1; //right;
		neighbors[3] = position+w+1; //down-right
		neighbors[4] = neighbors[3]-1; //down
		neighbors[5] = neighbors[4]-1; //down-left
		neighbors[6] = position-1; //left
		neighbors[7] = neighbors[0]-1; //up-left
		
		return neighbors;
		}
	/**
	 * True if neighbor is a circular neighbor of position in a image of width w
	 */
	public static boolean isCircularNeighbor(int neighbor, int position,int w){
		int diff = neighbor-position;
		int[] neigh = {w,-w,1,-1,w-1,w+1,-w-1,-w+1};
		
		for (int i=0;i<8;i++){
			if (diff==neigh[i]) return true;
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
	 * Creates and EvPixels image setting to 1 the positions in the list points.
	 */
	public static EvPixels buildImage(EvPixels input, int[] matrix)
		{
		EvPixels skImage = new EvPixels(EvPixelsType.INT, input.getWidth(), input
				.getHeight());
		int[] skArray = skImage.getArrayInt();

		for (int i =0; i<matrix.length;i++)
		{
			if (matrix[i]>0){
			skArray[i] = matrix[i];
			}
		}
		return skImage;
		}
		
	}
