package endrov.distanceTransform;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * A class the defines the general representation for a discrete distance
 * transformation implementing the two scan distance transformation developed
 * by Frank Y.Shih and Yi-Ta Wu and presented in 'Fast Euclidean distance 
 * transformation in two scans using a 3x3 neighborhood'. 
 */

public abstract class TwoScanDiscreteTransform extends DistanceTransform
	{
	public int[] forwardArray;
	public EvPixels backwardScanImage;
	int[] backwardArray;
	abstract int forwardDistance(int x, int y,int w, int h);
	abstract int backwardDistance(int x, int y, int w, int h);
	
	public TwoScanDiscreteTransform(EvPixels input){
		super(input);
	
		int w = binaryImage.getWidth();
		int h = binaryImage.getHeight();
		
		System.out.println("W: "+w+"H "+h);
		forwardArray = new int[w*h];
		backwardScanImage = new EvPixels(EvPixelsType.INT,w,h);
		backwardArray = backwardScanImage.getArrayInt();
	}
	
	/**
	 * Perform a two scan distance transform method. First a forward scan is performed, left
	 * to right and top to bottom, updating the values in forwardScanImage. 
	 * 
	 */
	@Override
	public EvPixels transform()
		{
			int w = binaryImage.getWidth();
			int h = binaryImage.getHeight();
	
			//Forward Scan (left to right, top to bottom)			
			for (int py = 1; py< h-1; py++){//The borders are supposed as background
				for (int px = 1; px< w-1; px++){
					forwardArray[py*w+px] = forwardDistance(px,py,w,h);					
				}
			}
			
			//backward Scan (right to left, bottom to top)
			for (int py = h-2; py>0; py--){//The borders are supposed as background
				for	(int px = w-2; px>0; px--){
				backwardArray[py*w+px] = backwardDistance(px,py,w,h);
				}
			}					
		return backwardScanImage;
		}
	
	}
