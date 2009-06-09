package endrov.unsortedImageFilters.avfilter;

import endrov.flow.OpSlice;
import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.CumSumArea;

/**
 * Moving average. Average is taken over an area of size (2pw+1)x(2ph+1). r=0 hence corresponds
 * to the identity operation.
 * 
 * Complexity O(w*h)
 */
public class OpMovingAverage extends OpSlice
	{
	Number pw, ph;
	
	public OpMovingAverage(Number pw, Number ph)
		{
		this.pw = pw;
		this.ph = ph;
		}

	public EvPixels exec(EvPixels... p)
		{
		return movingAverage(p[0],pw.intValue(), ph.intValue());
		}
	
	public static EvPixels movingAverage(EvPixels in, int pw, int ph)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] outPixels=out.getArrayInt();
		
		EvPixels cumsum=CumSumArea.cumsum(in);
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				int area=(tox-fromx)*(toy-fromy);
				outPixels[out.getPixelIndex(ax, ay)]=CumSumArea.integralFromCumSum(cumsum, fromx, tox, fromy, toy)/(int)area;
				}
			}
		return out;
		}
	}