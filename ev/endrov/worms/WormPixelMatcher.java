package endrov.worms;

import com.graphbuilder.curve.Point;

import endrov.util.Vector2i;
/**
 * Class that represents a matching matrix that contains
 * the corresponding (x,y) coordinates of an EvPixels array
 * point.
 *
 */
public class WormPixelMatcher
	{
	private Vector2i[] matchMatrix;
	int w;
	int h;
	
	public WormPixelMatcher(int w,int h){
		this.w = w;
		this.h = h;
		matchMatrix = new Vector2i[w*h];
		int count = 0;
		for(int j=0;j<h;j++){
			for(int i=0;i<w;i++){
				matchMatrix[count] = new Vector2i(i,j);
				count++;
			}
		}			
	}
	/**
	 * Returns 2-dimensional position corresponding to pixel
	 * position
	 * 
	 */
	public Vector2i getPixelPos(int pixel){
		return matchMatrix[pixel];
	}
	
	public int pointToPixel(Point p){
		double[] xy = p.getLocation();
		return (((int)xy[0])+((int)(xy[1]))*w);
	}
	
	}
