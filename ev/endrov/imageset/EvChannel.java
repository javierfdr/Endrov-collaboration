package endrov.imageset;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvObject;
import endrov.util.EvDecimal;
import endrov.util.EvListUtil;

/**
 * Images for one channel
 * @author Johan Henriksson
 */
public class EvChannel extends EvObject implements AnyEvImage
	{
	/****************************************************************************************/
	/******************************* Image data *********************************************/
	/****************************************************************************************/

	/** Image loaders */
//	public TreeMap<EvDecimal, TreeMap<EvDecimal, EvImage>> imageLoader=new TreeMap<EvDecimal, TreeMap<EvDecimal, EvImage>>();
	public TreeMap<EvDecimal, EvStack> imageLoader=new TreeMap<EvDecimal, EvStack>();

	/**
	 * Get access to an image
	 */
	public EvImage getImageLoader(EvDecimal frame, EvDecimal z)
		{
		try
			{
			return imageLoader.get(frame).get(z);
			}
		catch(Exception e)
			{
			return null;
			}
		}

	
	/**
	 * Get access to a frame
	 */
	public EvStack getCreateFrame(EvDecimal frame)
		{
		EvStack f=imageLoader.get(frame);
		if(f==null)
			imageLoader.put(frame,f=new EvStack());
		return f;
		}

	/**
	 * Get or create an image
	 */
	public EvImage createImageLoader(EvDecimal frame, EvDecimal z)
		{
		EvImage im=getImageLoader(frame, z);
		if(im!=null)
			return im;
		else
			{
			im=new EvImage();
			setImage(frame, z, im);
			return im;
			}
		}

	/**
	 * Set image
	 */
	public void setImage(EvDecimal frame, EvDecimal z, EvImage im)
		{
		EvStack frames=imageLoader.get(frame);
		if(frames==null)
			{
			frames=new EvStack();
			imageLoader.put(frame, frames);
			}
		frames.put(z, im);
		}
	
	

	/****************************************************************************************/
	/******************************* Find frames/z ******************************************/
	/****************************************************************************************/
	
	
	
	/**
	 * Find out the closest frame
	 * @param frame Which frame to match against
	 * @return If there are no frames or there is an exact match, then frame. Otherwise the closest frame.
	 */
	public EvDecimal closestFrame(EvDecimal frame)
		{
		return EvListUtil.closestFrame(imageLoader, frame); 
		}
	/*
	public EvDecimal closestFrame(EvDecimal frame)
		{
		if(imageLoader.get(frame)!=null || imageLoader.size()==0)
			return frame;
		else
			{
			SortedMap<EvDecimal, EvStack> before=imageLoader.headMap(frame);
			SortedMap<EvDecimal, EvStack> after=imageLoader.tailMap(frame);
			if(before.size()==0)
				return imageLoader.firstKey();
			else if(after.size()==0)
				return imageLoader.lastKey();
			else
				{
				EvDecimal afterkey=after.firstKey();
				EvDecimal beforekey=before.lastKey();
				
				if(afterkey.subtract(frame).less(frame.subtract(beforekey)))
					return afterkey;
				else
					return beforekey;
				}
			}
		}*/
	
	/**
	 * Get the frame before
	 * @param frame Current frame
	 * @return The frame before or the same frame if no frame before found
	 */
	public EvDecimal closestFrameBefore(EvDecimal frame)
		{
		SortedMap<EvDecimal, EvStack> before=imageLoader.headMap(frame); 
		if(before.size()==0)
			return frame;
		else
			return before.lastKey();
		}
	/**
	 * Get the frame after
	 * @param frame Current frame
	 * @return The frame after or the same frame if no frame after found
	 */
	public EvDecimal closestFrameAfter(EvDecimal frame)
		{
		//Can be made faster by iterator
		SortedMap<EvDecimal, EvStack> after=new TreeMap<EvDecimal, EvStack>(imageLoader.tailMap(frame));
		after.remove(frame);
		
		if(after.size()==0)
			return frame;
		else
			return after.firstKey();
		}
	
	
	
	
	/**
	 * Find the closest slice given a frame and slice
	 * @param frame Which frame to search
	 * @param z Z we wish to match
	 * @return Same z if frame does not exist or no slices exist, otherwise the closest z
	 */
	public EvDecimal closestZ(EvDecimal frame, EvDecimal z)
		{
		EvStack slices=imageLoader.get(frame);
		if(slices==null)
			return z;
		else
			return slices.closestZ(z);
		}


	/**
	 * Find the closest slice above given a slice in a frame
	 * @param frame Which frame to search
	 * @param z Z we wish to match
	 * @return Same z if frame does not exist or no slices exist, otherwise the closest z above
	 */
	public EvDecimal closestZAbove(EvDecimal frame, EvDecimal z)
		{
		EvStack slices=imageLoader.get(frame);
		if(slices==null)
			return z;
		else
			return slices.closestZAbove(z);
		}
	
	/**
	 * Find the closest slice below given a slice in a frame
	 * @param frame Which frame to search
	 * @param z Z we wish to match
	 * @return Same z if frame does not exist or no slices exist, otherwise the closest z below
	 */
	public EvDecimal closestZBelow(EvDecimal frame, EvDecimal z)
		{
		EvStack slices=imageLoader.get(frame);
		if(slices==null)
			return z;
		else
			return slices.closestZBelow(z); 
		}		
	
	
	/****************************************************************************************/
	/************************** Channel Meta data *******************************************/
	/****************************************************************************************/

	

	
	/** Binning, a scale factor from the microscope */
	public int chBinning=1;
	
	/** Displacement */
	public double dispX=0, dispY=0;
	
	/** Comppression 0-100, 100=lossless, what compression to apply to new images */
	public int compression=100;
	
	/** Other */
	public HashMap<String,String> metaOther=new HashMap<String,String>();
	
	/** frame data */
	public HashMap<EvDecimal,HashMap<String,String>> metaFrame=new HashMap<EvDecimal,HashMap<String,String>>();

	

	
	/**
	 * Get property assigned to a frame
	 * @param frame Frame
	 * @param prop Property
	 * @return Value of property or null if it does not exist
	 */
	public String getFrameMeta(EvDecimal frame, String prop)
		{
		HashMap<String,String> framedata=metaFrame.get(frame);
		if(framedata==null)
			return null;
		return framedata.get(prop);
		}
	
	
	
	/** Get (other) meta data in form of a string (default="") */
	public String getMetaValueString(String s)
		{
		String t=metaOther.get(s);
		if(t==null)	return "";
		else return t;
		}

	/** Get (other) meta data in form of a double (default=0) */
	public double getMetaValueDouble(String s)
		{
		String t=getMetaValueString(s);
		if(t.equals("")) return 0;
		else return Double.parseDouble(t);
		}
	
	/**
	 * Get a common frame. Creates structure if it does not exist.
	 */
	public HashMap<String,String> getMetaFrame(EvDecimal fid)
		{
		HashMap<String,String> frame=metaFrame.get(fid);
		if(frame==null)
			{
			frame=new HashMap<String,String>();
			metaFrame.put(fid, frame);
			}
		return frame;
		}


	public void buildMetamenu(JMenu menu)
		{
		}


	public String getMetaTypeDesc()
		{
		return "Channel";
		}


	public void loadMetadata(Element e)
		{
		//TODO take from imageset later
		}


	public void saveMetadata(Element e)
		{
		//TODO take from imageset later
		}
	}