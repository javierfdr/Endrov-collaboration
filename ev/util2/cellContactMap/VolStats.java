/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.cellContactMap;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import util2.ConnectImserv;

import endrov.data.EvData;
import endrov.ev.*;
import endrov.imagesetImserv.EvImserv;
import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.util.EvDecimal;


/**
 * Calculate volume statistics
 * @author Johan Henriksson
 */
public class VolStats
	{
	public static boolean showNeigh=false;
	public static boolean saveNormalized=true;
	public static int NUMTRY=0;
	public static EvDecimal frameInc=new EvDecimal(10);

	public static NucLineage loadLin() throws Exception
		{
		System.out.println("Connecting");
		String url=ConnectImserv.url;
		/*EvImserv.EvImservSession session=*/EvImserv.getSession(new EvImserv.ImservURL(url));
		System.out.println("Loading imsets");
		
		String s="celegans2008.2";
		
		System.out.println("loading "+s);
		EvData data=EvData.loadFile(url+s);
//		Imageset im=EvImserv.getImageset(url+s); 
		//TODO: should be able to go trough session to avoid url+s
		for(NucLineage lin:data.getIdObjects(NucLineage.class).values())
			return lin;
		throw new Exception("did not find");
		}
	
	
	
	/**
	 * Find the first keyframe ever mentioned in a lineage object
	 */
	/*
	public static EvDecimal firstFrameOfLineage(NucLineage lin)
		{
		EvDecimal minframe=null;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			EvDecimal n=nuc.firstFrame();
			if(minframe==null || (n!=null && n.less(minframe)))
				minframe=nuc.firstFrame();
			}
		return minframe;
		}*/
	
	
	public static EvDecimal lastOkFrame(NucLineage lin)
		{
		EvDecimal lastFrame=new EvDecimal(Integer.MAX_VALUE);
		for(String nuc:lin.nuc.keySet())
			{
			if(lin.nuc.get(nuc).child.size()<2 && !lin.nuc.get(nuc).pos.isEmpty())
				{
				EvDecimal f=lin.nuc.get(nuc).pos.lastKey();
				if(f.less(lastFrame))
					{
					lastFrame=f;
					System.out.println("Ending with "+nuc+" at "+lastFrame);
					}
				}
			}
		
		//override
//		lastFrame=1800;
		
		return lastFrame;
		}
	
	
	public static void calcVolStat(NucLineage lin) throws Exception
		{
		final EvDecimal fminframe=lin.firstFrameOfLineage().fst();
		final EvDecimal fmaxframe=lastOkFrame(lin);
//		final EvDecimal fmaxframe=new EvDecimal("");
		
		PrintWriter pw=new PrintWriter(new FileWriter("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/volstats.txt"));

		for(EvDecimal curframe=fminframe;curframe.less(fmaxframe);curframe=curframe.add(frameInc))
			{
			if(curframe.intValue()%30==0)
				System.out.println("frame "+curframe);

			//Interpolate for this frame
			Map<NucSel, NucLineage.NucInterp> interp=lin.getInterpNuc(curframe);
			//Only keep visible nuclei
			Set<NucSel> visibleNuc=new HashSet<NucSel>();
			for(Map.Entry<NucSel, NucLineage.NucInterp> e:interp.entrySet())
				if(e.getValue().isVisible())
					visibleNuc.add(e.getKey());
			interp.keySet().retainAll(visibleNuc);
			
			//Count #cells for this frame
			int numCellsNow=interp.size();
			
			//Total nuclei volume
			double totNucVol=0;
			for(Map.Entry<NucSel, NucLineage.NucInterp> entry:interp.entrySet())
				{
				double r=entry.getValue().pos.r;
				totNucVol+=4*Math.PI*r*r*r/3;
				}
			
			//Output
			pw.println(""+curframe+"\t"+numCellsNow+"\t"+totNucVol);
			}
		pw.close();
		}
	

	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		try
			{
			EvLog.listeners.add(new EvLogStdout());
			EV.loadPlugins();
			
			calcVolStat(loadLin());
			
			System.out.println("Done");
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		System.exit(0);
		}

	}
	
	

	
	
