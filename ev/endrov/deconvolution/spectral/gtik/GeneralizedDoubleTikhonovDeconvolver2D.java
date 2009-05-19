package endrov.deconvolution.spectral.gtik;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import endrov.deconvolution.Deconvolver2D;
import endrov.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.deconvolution.spectral.SpectralEnums.SpectralResizingType;
import endrov.imageset.EvPixels;

/**
 * Deconvolution in 2D using generalized tikhonov
 * @author Johan Henriksson
 *
 */
public class GeneralizedDoubleTikhonovDeconvolver2D extends Deconvolver2D
	{
	private final EvPixels imPSF;
	private final DoubleMatrix2D stencil;
	private final SpectralResizingType resizing;
	private final double regParam;
	private final double threshold;
	private final SpectralPaddingType padding;
	
	 public GeneralizedDoubleTikhonovDeconvolver2D(EvPixels imPSF, DoubleMatrix2D stencil, SpectralResizingType resizing,double regParam, double threshold, SpectralPaddingType padding) 
		 {
		 this.imPSF=imPSF;
		 this.stencil=stencil;
		 this.resizing=resizing;
		 this.regParam=regParam;
		 this.threshold=threshold;
		 this.padding=padding;
		 }
   
	protected EvPixels internalDeconvolve(EvPixels ipB)
		{
		if(padding.equals(SpectralPaddingType.PERIODIC))
			{
			DoublePeriodicGeneralizedTikhonov2D d=new DoublePeriodicGeneralizedTikhonov2D(ipB, imPSF, stencil, resizing, regParam, threshold);
			return d.internalDeconvolve();
			}
		else
			{
			DoubleReflexiveGeneralizedTikhonov2D d=new DoubleReflexiveGeneralizedTikhonov2D(ipB, imPSF, stencil, resizing, regParam, threshold);
			return d.internalDeconvolve();
			}
		}
	
	
	}
