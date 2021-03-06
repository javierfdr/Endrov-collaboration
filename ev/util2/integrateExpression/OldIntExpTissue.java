/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.integrateExpression;

import java.io.File;
import java.io.IOException;
import java.util.*;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.shell.Shell;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;
import endrov.util.ImVector2;

public class IntExpTissue
	{

	
	
	public static void main(String arg[])
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		EvData data=EvData.loadFile(new File("/Volumes/TBU_main01/ost4dgood/TB2141070621b.ost/"));
		
		int numSubDivX=20;
		int numSubDivYZ=20;
		doProfile(data, "T:CEH-5","CEH-5",numSubDivX,numSubDivYZ);
		//data.saveData(); NOOO
		
		System.exit(0);
		}
		
	public static void doProfile(EvData data, String newLinName, String expName, int numSubDivX, int numSubDivYZ)
		{
		String channelName="GFP";

		
		//TODO
		/**
		 * 
		 * How to get coordinates? Can assume 4 cells exist but this is not optimal. Can try
		 * to fit against model using first frame with enough cells (>2). Can refit over time
		 * to account for rotation. 
		 * 
		 * new metaobject: coordinate system
		 * 
		 * 
		 * 
		 * 
		 */
		
		
		
		
		
		
		Imageset imset=data.getObjects(Imageset.class).get(0);

		//For all lineages
		//TODO need to group lineage and shell. introduce a new object?
		NucLineage lin=imset.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
		Shell shell=imset.getIdObjectsRecursive(Shell.class).values().iterator().next();
		ExpUtil.clearExp(lin, expName);

		//Virtual nuc for AP
		for(int i=0;i<numSubDivX;i++)
			lin.getCreateNuc("_slice"+i);
		
		
		
		TreeMap<EvDecimal, Double> bgLevel=new TreeMap<EvDecimal, Double>();
		
		
		HashMap<EvDecimal, EvPixels> distanceMap=new HashMap<EvDecimal, EvPixels>();
		
		
		
		EvChannel ch=imset.getChannel(channelName);
		
		
		
		//For all frames
		System.out.println("num frames: "+imset.getChannel(channelName).imageLoader.size());
		EvDecimal lastFrame=ch.imageLoader.lastKey();
		for(EvDecimal frame:ch.imageLoader.keySet())
			if(frame.less(new EvDecimal("30000")) && frame.greater(new EvDecimal("29000")))
			{
			System.out.println();
			System.out.println("frame "+frame+" / "+lastFrame);

			//Map<String, Double> expLevel=new HashMap<String, Double>();
			//Map<String, Integer> nucVol=new HashMap<String, Integer>();

			//Get exposure time
			String sExpTime=imset.metaFrame.get(frame).get("exposuretime");
			double expTime=1;
			if(sExpTime!=null)
				expTime=Double.parseDouble(sExpTime);
			
			int bgIntegral=0;
			int bgVolume=0;

			int[] sliceExp=new int[numSubDivX];
			int[] sliceVol=new int[numSubDivX];

			//For all z
			EvStack stack=ch.imageLoader.get(frame);
			for(Map.Entry<EvDecimal, EvImage> eim:stack.entrySet())
				{
				EvDecimal curZ=eim.getKey();
				EvImage im=eim.getValue();
				EvPixels pixels=null;
				int[] pixelsLine=null;
							
				//Load images lazily (for AP not really needed)
				if(pixels==null)
					{
					/*
					BufferedImage b=im.getJavaImage();
					pixels=new EvPixels(b);
					pixels=pixels.getReadOnly(EvPixels.TYPE_INT);*/
					pixels=im.getPixels().getReadOnly(EvPixelsType.INT);
					pixelsLine=pixels.getArrayInt();

					//Integrate background
					for(int i=0;i<pixels.getWidth();i++)
						bgIntegral+=pixelsLine[i];
					bgVolume+=pixels.getWidth();
					}
				
				
				//Calculate distance mask lazily
				EvPixels lenMap;
				double[] lenMapArr;
				if(distanceMap.containsKey(curZ))
					{
					lenMap=distanceMap.get(curZ);
					lenMapArr=lenMap.getArrayDouble();
					}
				else
					{
					lenMap=new EvPixels(EvPixelsType.DOUBLE, pixels.getWidth(), pixels.getHeight());
					lenMapArr=lenMap.getArrayDouble();

					ImVector2 dirvec=ImVector2.polar(shell.major, shell.angle);
					ImVector2 startpos=dirvec.add(new ImVector2(shell.midx,shell.midy));
					dirvec=dirvec.normalize().mul(-1);

					//Calculate distances
					for(int ay=0;ay<pixels.getHeight();ay++)
						{
						int lineIndex=lenMap.getRowIndex(ay);
						for(int ax=0;ax<pixels.getWidth();ax++)
							{
							//Convert to world coordinates
							ImVector2 pos=new ImVector2(stack.transformImageWorldX(ax),stack.transformImageWorldY(ay));

							//Check if this is within ellipse boundary
							ImVector2 elip=pos.sub(new ImVector2(shell.midx, shell.midy)).rotate(shell.angle); //TODO angle? what?
							double len;
							if(1 >= elip.y*elip.y/(shell.minor*shell.minor) + elip.x*elip.x/(shell.major*shell.major) )
								len=pos.sub(startpos).dot(dirvec)/(2*shell.major);	//xy . dirvecx = cos(alpha) ||xy|| ||dirvecx||
							else
								len=-1;
							lenMapArr[lineIndex+ax]=len;
							}
						}
					}
					

				
				//Integrate this area
				for(int y=0;y<pixels.getHeight();y++)
					{
					int lineIndex=pixels.getRowIndex(y);
					for(int x=0;x<pixels.getWidth();x++)
						{
						int i=lineIndex+x;
						double len=lenMapArr[i];
						if(len>-1)
							{
							int sliceNum=(int)(len*20); //may need to bound in addition
							sliceExp[sliceNum]+=pixelsLine[i];
							sliceVol[sliceNum]++;
							}
						}
					}

				
				/*
				//Integrate this area
				int area=0;
				double exp=0;
				for(int y=0;y<pixels.getHeight();y++)
					{
					int lineIndex=pixels.getRowIndex(y);
					for(int x=0;x<pixels.getWidth();x++)
						{
						int v=pixelsLine[lineIndex+x];
						area++;
						exp+=v;
						}
					}
				*/
				}

			
			for(int i=0;i<numSubDivX;i++)
				{
				double avg=(double)sliceExp[i]/(double)sliceVol[i];
				avg/=expTime;
				
		
				NucLineage.Nuc nuc=lin.getCreateNuc("_slice"+i);
				NucExp exp=nuc.getCreateExp(expName);
				exp.level.put(frame, avg);
				
				
				/*
				NucExp exp=lin.nuc.get(nucName).getExpCreate(expName);
				if(lin.nuc.get(nucName).pos.lastKey().greaterEqual(frame) && 
						lin.nuc.get(nucName).pos.firstKey().lessEqual(frame)) 
					exp.level.put(frame,avg);*/
				
//				if(minExpLevel==null || avg<minExpLevel) minExpLevel=avg;
//				if(maxExpLevel==null || avg>maxExpLevel) maxExpLevel=avg;
				}
		



			//Store bglevel in list
			if(bgVolume!=0)
				{
				bgLevel.put(frame, bgIntegral/(double)bgVolume);
				System.out.println("BG: "+bgLevel.get(frame));
				}



			
			/*
			
			//Store value in XML
			for(String nucName:expLevel.keySet())
				{
				double avg=expLevel.get(nucName)/nucVol.get(nucName);
				avg/=expTime;
				//				System.out.println(nucName+" "+avg);
				NucExp exp=lin.nuc.get(nucName).getExpCreate(expName);
				if(lin.nuc.get(nucName).pos.lastKey().greaterEqual(frame) && 
						lin.nuc.get(nucName).pos.firstKey().lessEqual(frame)) 
					exp.level.put(frame,avg);




				}*/

			}

		

		/*
		TreeSet<EvDecimal> framesSorted=new TreeSet<EvDecimal>(bgLevel.keySet());
		ExpUtil.correctExposureChange(imset, lin, expName, framesSorted);
		ExpUtil.normalizeSignal(lin, expName);
*/
		//TODO TODO TODO get corrections from AP calc. or use tissue? 

		
		
		try
			{
			StringBuffer outf=new StringBuffer();
			
			here: for(EvDecimal frame:ch.imageLoader.keySet())
				{
				for(int i=0;i<numSubDivX;i++)
					{
					NucLineage.Nuc nuc=lin.nuc.get("_slice"+i);
					NucExp nexp=nuc.exp.get(expName);
					Double level=nexp.level.get(frame);
					if(level==null)
						continue here;
					outf.append(level);
					}
				outf.append("\n");
				}
			EvFileUtil.writeFile(new File("/tmp/out.txt"), outf.toString());
			
			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
/*
		//Subtract background. 
		//TODO But using minExpLevel, I don't like it. should use some image average. border? first line?
		double expSize=maxExpLevel-minExpLevel;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			if(nuc.exp.containsKey(expName))
				for(Map.Entry<EvDecimal, Double> e:nuc.exp.get(expName).level.entrySet())
					{
					nuc.exp.get(expName).level.put(e.getKey(), (e.getValue()-minExpLevel)*5);
					}
		
	*/	
		
		}
	
	}
