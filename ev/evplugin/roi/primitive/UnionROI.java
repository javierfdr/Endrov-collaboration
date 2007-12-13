package evplugin.roi.primitive;

import java.util.*;

import javax.swing.*;
import org.jdom.*;

import evplugin.imageset.*;
import evplugin.roi.*;

public class UnionROI extends CompoundROI
	{
	

	/******************************************************************************************************
	 *                               Iterator                                                             *
	 *****************************************************************************************************/
	private class UnionLineIterator extends LineIterator
		{
		final String channel;
		final int frame,z;
//		boolean hasNextA, hasNextB;
//		private LineIterator ita;
//		private LineIterator itb;

		private OneIt ita, itb;
		
		private class OneIt
			{
			public OneIt(LineIterator it)
				{
				this.it=it;
				it.next();
				}
			public void next()
				{
				hasNext=it.next();
				}
			public void step()
				{
				ranges=it.ranges;
				y=it.y;
//				z=it.z;
				next();   //bad! interfers with range
				}
			public boolean hasNext;
			public LineIterator it;
			}
		
		public UnionLineIterator(EvImage im, LineIterator ita, LineIterator itb, String channel, int frame, int z)
			{
			this.channel=channel;
			this.frame=frame;
			this.z=z;
			this.ita=new OneIt(ita);
			this.itb=new OneIt(itb);

			//Get all started
			ita.next();
			itb.next();
			}
		
		
		public boolean next()
			{
			if(!ita.hasNext)
				if(!itb.hasNext)
					return false;
				else
					{
					//itb
					return true;
					}
			else
				if(!itb.hasNext)
					{
					//ita
					return true;
					}
				else
					{
					;//both
					return true;
					}
				
			

			/*
			
				if(hasNextB && ita.y>itb.y)
					{
					ranges.clear();
					ranges.addAll(itb.ranges);
					y=itb.y;
					z=itb.z;
					hasNextB=itb.next();
					}
				else if(ita.y<itb.y)
					{
					ranges.clear();
					ranges.addAll(ita.ranges);
					y=ita.y;
					z=ita.z;
					hasNextA=ita.next();
					}
				else //equal y. Merge
					{
					
					}
						
				
				}
			
				*/
			
//			y++;
	//		return y<endY;
			}	
		}
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	
	public String getROIDesc()
		{
		return "Union";
		}
	
	

	/**
	 * Get channels that at least are partially selected
	 */
	public Set<String> getChannels(Imageset rec)
		{
		TreeSet<String> c=new TreeSet<String>();
		for(ROI roi:subRoi)
			c.addAll(roi.getChannels(rec));
		return c;
		}
	
	/**
	 * Get frames that at least are partially selected
	 */
	public Set<Integer> getFrames(Imageset rec, String channel)
		{
		TreeSet<Integer> c=new TreeSet<Integer>();
		for(ROI roi:subRoi)
			c.addAll(roi.getFrames(rec, channel));
		return c;
		}
	
	
	/**
	 * Get slices that at least are partially selected
	 */
	public Set<Integer> getSlice(Imageset rec, String channel, int frame)
		{
		TreeSet<Integer> c=new TreeSet<Integer>();
		for(ROI roi:subRoi)
			c.addAll(roi.getSlice(rec, channel, frame));
		return c;
		}
	
	

	public boolean imageInRange(String channel, double frame, int z)
		{
		for(ROI roi:subRoi)
			if(roi.imageInRange(channel, frame, z))
				return true;
		return false;
		}
	
	/**
	 * Get iterator over one image
	 */
	public LineIterator getLineIterator(EvImage im, final String channel, final int frame, final int z)
		{
		if(imageInRange(channel, frame, z) && !subRoi.isEmpty())
			{
			LineIterator li=subRoi.get(0).getLineIterator(im, channel, frame, z);
			for(int i=1;i<subRoi.size();i++)
				li=new UnionLineIterator(im, subRoi.get(i).getLineIterator(im, channel, frame, z), li, channel, frame, z);
			return li;
			}
		else
			return new EmptyLineIterator();
		}
	
	
	public void saveMetadata(Element e)
		{
		e.setName("ROI union");
		
		}
	
	
	/**
	 * Get widget for editing this ROI
	 */
	public JPanel getROIWidget()
		{
		return null;//There are no options
		}

	
	/**
	 * Get handles for corners
	 */
	public Handle[] getHandles() //should make a set instead, or linked list
		{
		LinkedList<Handle> h=new LinkedList<Handle>();
		for(ROI roi:subRoi)
			for(Handle th:roi.getHandles())
				h.add(th);
//	return (Handle[])h.toArray();
		Handle[] hh=new Handle[h.size()];
		int i=0;
		for(Handle th:h)
			{
			hh[i]=th;
			i++;
			}
		return hh;
		}

	
	}