package endrov.skeleton;

import endrov.util.Vector2i;

public final class ManhattanSkeletonTransform extends SkeletonTransform
	{
	@Override
	public int[] getNeighbors(int position, int w)
		{
		int neighbors[] = new int[4];
		neighbors[0] = position-w; // Up
		neighbors[1] = position+1; // Right
		neighbors[2] = position+w; // Down
		neighbors[3] = position-1; // Left

		return neighbors;
		}
	@Override
	Vector2i getMaxDirectionalNeighbor(int[] imageArray, int w, int currentPixel,
			int previousPixel, int neighborMovement)
		{
		// TODO Auto-generated method stub
		return null;
		}
	}
