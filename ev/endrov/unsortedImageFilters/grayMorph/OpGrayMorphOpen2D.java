package endrov.unsortedImageFilters.grayMorph;

import endrov.flow.OpSlice1;
import endrov.imageset.EvPixels;

/**
 * Open: dilate, then erode
 * <br/>
 * in (o) kernel
 * <br/>
 * in ⊚ kernel
 * @author Johan Henriksson
 */
public class OpGrayMorphOpen2D extends OpSlice1
{
private int kcx,kcy;
private EvPixels kernel;
public OpGrayMorphOpen2D(int kcx, int kcy, EvPixels kernel)
	{
	this.kcx = kcx;
	this.kcy = kcy;
	this.kernel = kernel;
	}

@Override
public EvPixels exec1(EvPixels... p)
	{
	return open(p[0],kernel, kcx, kcy);
	}

public static EvPixels open(EvPixels in, EvPixels kernel, int kcx, int kcy)
	{
	return OpGrayMorphErode2D.erode(OpGrayMorphDilate2D.dilate(in,kernel,kcx,kcy),kernel,kcx,kcy);
	}
}