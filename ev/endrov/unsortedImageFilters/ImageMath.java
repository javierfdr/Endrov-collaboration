package endrov.unsortedImageFilters;

import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.newcore.SliceOp;

/**
 * Math ops on images. with EvImage, use convenience functions to make a common size and position
 * 
 * Assumes same pixel and position
 * 
 * @author Johan Henriksson
 *
 */
public class ImageMath
	{

	
	
	public static EvPixels plus(EvPixels a, EvPixels b)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		b=b.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] bPixels=b.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]+bPixels[i];
		
		return out;
		}
	
	public static EvPixels plus(EvPixels a, int b)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]+b;
		
		return out;
		}
	
	
	
	public static EvPixels minus(EvPixels a, EvPixels b)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		b=b.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] bPixels=b.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]-bPixels[i];
		
		return out;
		}
	
	//This one replaces the invert operation. it is more general
	//and assumes no range
	
	public static EvPixels minus(int a, EvPixels b)
		{
		//Should use the common higher type here
		b=b.convertTo(EvPixels.TYPE_INT, true);
		
		int w=b.getWidth();
		int h=b.getHeight();
		EvPixels out=new EvPixels(b.getType(),w,h);
		int[] bPixels=b.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<bPixels.length;i++)
			outPixels[i]=a-bPixels[i];
		
		return out;
		}
	

	/**
	 * Add images. Assumes same size and position
	 */
	public static EvPixels times(EvPixels a, EvPixels b)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		b=b.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] bPixels=b.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]*bPixels[i];
		
		return out;
		}
	
	public static EvPixels times(EvPixels a, int b)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]*b;
		
		return out;
		}
	
	public static EvPixels times(EvPixels a, double b)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]*b;
		
		return out;
		}
	
	public static EvPixels div(EvPixels a, EvPixels b)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		b=b.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] bPixels=b.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]/bPixels[i];
		
		return out;
		}
	
	public static EvPixels div(EvPixels a, int b)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]/b;
		
		return out;
		}
	
	/**
	 * log(a)
	 */
	public static EvPixels log(EvPixels a)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=Math.log(aPixels[i]);
		
		return out;
		}
	
	
	/**
	 * a*b+c
	 */
	public static EvPixels saxpy(EvPixels a, int b, int c)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_INT, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]*b+c;
		
		return out;
		}
	
	
	
	
	/**
	 * a*b+c
	 * Can be used to implement contrast-brightness the old style
	 */
	public static EvPixels axpy(EvPixels a, double b, double c)
		{
		//Should use the common higher type here
		a=a.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		double[] aPixels=a.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]*b+c;
		
		return out;
		}
	
	
	/**
	 * Sum up the signal in an image
	 */
	//could always be double if we wanted
	public static double sum(EvPixels a)
		{
		//support all types
		a=a.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		double[] aPixels=a.getArrayDouble();
		double sum=0;
		for(int i=0;i<aPixels.length;i++)
			sum+=aPixels[i];
		return sum;
		}

	public static class MulScalarOp extends SliceOp
		{
		int b;
		public MulScalarOp(int b)
			{
			this.b = b;
			}
		public EvPixels exec(EvPixels... p)
			{
			return times(p[0], b);
			}
		}
	
	
	public static class MulImageOp extends SliceOp
		{
		public EvPixels exec(EvPixels... p)
			{
			return times(p[0], p[1]);
			}
		}

	
	
	//OLD!!!!!
	public static EvChannel times(EvChannel ch, final int b)
		{
		return new SliceOp(){
			public EvPixels exec(EvPixels... p)
				{
				return times(p[0], b);
				}
		}.exec(ch);
		}
	
	}
