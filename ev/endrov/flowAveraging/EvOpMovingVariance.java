package endrov.flowAveraging;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.CumSumArea;

/**
 * Moving variance
 * 
 * TODO should maybe output floating point due to division
 * @author Johan Henriksson
 */
public class EvOpMovingVariance extends EvOpSlice1
	{
	private final int pw, ph;

	public EvOpMovingVariance(Number pw, Number ph)
		{
		this.pw = pw.intValue();
		this.ph = ph.intValue();
		}

	public EvPixels exec1(EvPixels... p)
		{
		return localVariance(p[0], pw, ph);
		}
	
	public static EvPixels localVariance(EvPixels in, int pw, int ph)
		{
		if(in.getType()==EvPixels.TYPE_INT)
			{
			in=in.convertTo(EvPixels.TYPE_INT, true);
			int w=in.getWidth();
			int h=in.getHeight();
			EvPixels out=new EvPixels(EvPixels.TYPE_DOUBLE,w,h);
			double[] outPixels=out.getArrayDouble();
			
			CumSumArea cumsum=new CumSumArea(in);
			CumSumArea cumsum2=CumSumArea.cumsum2(in);
			
			for(int ay=0;ay<h;ay++)
				{
				for(int ax=0;ax<w;ax++)
					{
					int fromx=Math.max(0,ax-pw);
					int tox=Math.min(w,ax+pw+1);
					int fromy=Math.max(0,ay-ph);
					int toy=Math.min(h,ay+ph+1);
					int area=(tox-fromx)*(toy-fromy);
					
					//Var(x)=E(x^2)-(E(x))^2
					int v1=cumsum2.integralFromCumSumInteger(fromx, tox, fromy, toy);
					int v2=cumsum.integralFromCumSumInteger(fromx, tox, fromy, toy);
					
					outPixels[out.getPixelIndex(ax, ay)]=(v1 - v2*v2/(double)area)/area;
					}
				}
			return out;
			}
		else
			{
			in=in.convertTo(EvPixels.TYPE_DOUBLE, true);
			int w=in.getWidth();
			int h=in.getHeight();
			EvPixels out=new EvPixels(EvPixels.TYPE_DOUBLE,w,h);
			double[] outPixels=out.getArrayDouble();
			
			CumSumArea cumsum=new CumSumArea(in);
			CumSumArea cumsum2=CumSumArea.cumsum2(in);
			
			for(int ay=0;ay<h;ay++)
				{
				for(int ax=0;ax<w;ax++)
					{
					int fromx=Math.max(0,ax-pw);
					int tox=Math.min(w,ax+pw+1);
					int fromy=Math.max(0,ay-ph);
					int toy=Math.min(h,ay+ph+1);
					int area=(tox-fromx)*(toy-fromy);
					
					//Var(x)=E(x^2)-(E(x))^2
					double v1=cumsum2.integralFromCumSumDouble(fromx, tox, fromy, toy);
					double v2=cumsum.integralFromCumSumDouble(fromx, tox, fromy, toy);
					
					outPixels[out.getPixelIndex(ax, ay)]=(v1 - v2*v2/(double)area)/area;
					}
				}
			return out;

			}
		}
	
	}