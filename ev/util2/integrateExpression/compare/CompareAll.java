/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.integrateExpression.compare;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.jdom.Document;
import org.jdom.Element;

import util2.integrateExpression.FindAnnotatedStrains;
import util2.integrateExpression.IntExp;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowColocalization.ColocCoefficients;
import endrov.frameTime.FrameTime;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;
import endrov.util.EvXmlUtil;
import endrov.util.Tuple;

/**
 * Pairwise comparison of recordings
 * @author Johan Henriksson
 *
 */
public class CompareAll
	{
	
	public final static File outputBaseDir=new File("/home/tbudev3/expsummary");

	public final static File cachedValuesFileT=new File(outputBaseDir,"comparisonT.xml");
	public final static File cachedValuesFileAP=new File(outputBaseDir,"comparisonAP.xml");
	public final static File cachedValuesFileXYZ=new File(outputBaseDir,"comparisonXYZ.xml");
	
	private final static int imageMaxTime=100; //Break down to 100 time points

	
	/**
	 * Normalize time between recordings
	 */
	private static FrameTime buildFrametime(NucLineage coordLin)
		{
		//Fit model time using a few markers
		//Times must be relative to a sane time, such that if e.g. venc is missing, linear interpolation still makes sense
		FrameTime ft=new FrameTime();
		System.out.println("has nucs: "+coordLin.nuc.keySet());
		
		NucLineage.Nuc nucABa=coordLin.nuc.get("ABa");
		if(nucABa!=null)
			ft.add(nucABa.pos.firstKey(), new EvDecimal("0").multiply(imageMaxTime));

		NucLineage.Nuc nucGast=coordLin.nuc.get("gast"); //Gastrulation
		if(nucGast!=null)
			ft.add(nucGast.pos.firstKey(), new EvDecimal("0.1").multiply(imageMaxTime));

		NucLineage.Nuc nucVenc=coordLin.nuc.get("venc"); //Ventral enclosure
		if(nucVenc!=null)
			ft.add(nucVenc.pos.firstKey(), new EvDecimal("0.43").multiply(imageMaxTime));

		NucLineage.Nuc nuc2ft=coordLin.nuc.get("2ftail"); //2-fold tail
		if(nuc2ft!=null)
			ft.add(nuc2ft.pos.firstKey(), new EvDecimal("0.54").multiply(imageMaxTime));

		System.out.println("ftmap "+ft.mapFrame2time);
		
		//times from BC10075_070606
		// "go to frame" seems buggy
		// ABa 3h1m10s      10870     0
		// gast 4h7m40s     14860     0.1
		// venc 7h49m20s    28160     0.43
		// 2ftail 8h59m20s  32360     0.54
		
		//System.out.println("should be 0: "+ft.interpolateTime(nucABa.pos.firstKey()).doubleValue());
		//System.out.println("should be 0: "+ft.interpolateTime(nuc2ft.pos.firstKey()).doubleValue());
		
		return ft;
		}
	
	
	/**
	 * Coloc calculation requires two images that can overlap. Generate these from the AP or T lineage
	 */
	public static double[][] apToArray(EvData data, String newLinName, String expName, NucLineage coordLin)
		{
		Imageset imset = data.getObjects(Imageset.class).get(0);
		NucLineage lin = null;

		//Find lineage
		for(Map.Entry<EvPath, NucLineage> e:imset.getIdObjectsRecursive(NucLineage.class).entrySet())
			{
			if(e.getKey().getLeafName().equals(newLinName))
				{
				lin=e.getValue();
				break;
				}
			}
		if(lin==null)
			throw new RuntimeException("No lineage "+newLinName);
		
		//Autodetect number of subdivisions
		int numSubDiv=0;
		for(String nn:lin.nuc.keySet())
			if(nn.startsWith("_slice"))
				{
				int curnum=Integer.parseInt(nn.substring("_slice".length()));
				numSubDiv=Math.max(curnum+1,numSubDiv);
				}
			else
				System.out.println("Strange exp: "+nn);
		System.out.println("Detected subdiv "+numSubDiv);
		
		double[][] image=new double[imageMaxTime][];//[numSubDiv];
		
		FrameTime ft=buildFrametime(coordLin);
		
		//Fill in image
		int lastTime=0;
		//System.out.println("curtime: ");
		NucLineage.Nuc refNuc=lin.nuc.get("_slice0");
		for (EvDecimal frame : refNuc.exp.get(expName).level.keySet())
			{
			//Map to image
			int time=(int)ft.interpolateTime(frame).doubleValue();
			//System.out.println("curtime: "+time);
			//System.out.print(time+" ");
			if(time<0)
				time=0;
			else if(time>=imageMaxTime)
				break;
			//time=imageMaxTime-1;
			
			//For each slice
			image[time]=new double[numSubDiv];
			for (int i = 0; i<numSubDiv; i++)
				{
				NucLineage.Nuc nuc = lin.nuc.get("_slice"+i);
				NucExp nexp = nuc.exp.get(expName);
				Double level = nexp.level.get(frame);
				for(int y=lastTime;y<time+1;y++)
					image[time][i]=level;
				}
			lastTime=time;
			}
		//System.out.println();
		
		//System.out.println("numSubDiv: "+numSubDiv);
		/*
		for(double[] d:image)
			{
			System.out.print("im>");
			for(double e:d)
				System.out.print(" "+e);
			System.out.println();
			}
		*/
		//TODO warn for bad recordings. maybe obvious from result?

		//If it doesn't go far enough, the rest of the arrays will be null.
		//The first values will be a replica of the first frame; should seldom
		//be a problem
		
		return image;
		}
	
	public static double channelAverageDt(EvChannel chan)
		{
		return chan.imageLoader.lastKey().subtract(chan.imageLoader.firstKey()).doubleValue()/chan.imageLoader.size();
		}
	
	
	/**
	 * Coloc over XYZ
	 */
	public static ColocCoefficients colocXYZ(EvData dataA, EvData dataB, NucLineage coordLinA, NucLineage coordLinB, String chanNameA, String chanNameB)
		{
		Imageset imsetA = dataA.getObjects(Imageset.class).get(0);
		//Imageset imsetB = dataA.getObjects(Imageset.class).get(0);
		Imageset imsetB = dataB.getObjects(Imageset.class).get(0);
		
		FrameTime ftA=buildFrametime(coordLinA);
		FrameTime ftB=buildFrametime(coordLinB);

		if(ftA.getNumPoints()<2 || ftB.getNumPoints()<2)
			{
			//Bad data survival
			System.out.println("!!!!! too few timepoints");
			return new ColocCoefficients();
			}
		
		EvChannel chanA=(EvChannel)imsetA.getChild(chanNameA); 
		EvChannel chanB=(EvChannel)imsetB.getChild(chanNameB);

		if(chanA==null || chanB==null)
			{
			//Bad data survival
			System.out.println("!!!!! missing channels");
			return new ColocCoefficients();
			}
		
		//Figure out how many steps to take
		double dt=channelAverageDt(chanA);
		EvDecimal frame0A=ftA.interpolateFrame(new EvDecimal(0));
		EvDecimal frame100A=ftA.interpolateFrame(new EvDecimal(100));
		int numSteps=frame100A.subtract(frame0A).divide(new EvDecimal(dt)).intValue();
		System.out.println("Num steps "+numSteps);
		
		//TODO verify that this idea is correct
		
		//Compare channels
		ColocCoefficients coloc=new ColocCoefficients();
		int cnt=0;
		for(double time=0;time<imageMaxTime;time+=1.0/numSteps)
			{
			//Corresponding frames
			EvDecimal frameA=ftA.interpolateFrame(new EvDecimal(time));
			EvDecimal frameB=ftB.interpolateFrame(new EvDecimal(time));
			
			//If outside range, do not bother with this time point
			if(frameA.less(chanA.imageLoader.firstKey()) || frameA.greater(chanA.imageLoader.firstKey()) ||
					frameB.less(chanB.imageLoader.firstKey()) || frameB.greater(chanB.imageLoader.firstKey()))
				{
				//System.out.println("Skip: "+frameA+"\t"+frameB);
				continue;
				}
			cnt++;
			//Use closest frame in each
			EvStack stackA=chanA.imageLoader.get(chanA.closestFrame(frameA));
			EvStack stackB=chanB.imageLoader.get(chanB.closestFrame(frameB));

			if(stackA.getDepth()!=stackB.getDepth())
				System.out.println("Different number of slices in Z from frames "+frameA+" vs "+frameB);
			
			//Compare each slice. Same number of slices since it has been normalized
			int numz=stackA.getDepth();
			for(int i=0;i<numz;i++)
				{
//				EvPixels pA=stackA.get(new EvDecimal(i)).getPixels();
//				EvPixels pB=stackB.get(new EvDecimal(i)).getPixels();
				EvPixels pA=stackA.getInt(i).getPixels();
				EvPixels pB=stackB.getInt(i).getPixels();
				if(pA==null || pB==null)
					System.out.println("Null pixels at frame "+frameA+" vs "+frameB);
				double[] arrA=pA.convertToDouble(true).getArrayDouble();
				double[] arrB=pB.convertToDouble(true).getArrayDouble();
				coloc.add(arrA, arrB);
				}
			}
		System.out.println("Num xyz compared: "+cnt);
		
		return coloc;
		}
	
	
	/**
	 * Code from ImageJ, fire LUT.
	 * should if possible use the same as gnuplot
	 * @param reds
	 * @param greens
	 * @param blues
	 * @return
	 */
	/*
	private int fire(byte[] reds, byte[] greens, byte[] blues) 
		{
		int[] r = {0,0,1,25,49,73,98,122,146,162,173,184,195,207,217,229,240,252,255,255,255,255,255,255,255,255,255,255,255,255,255,255};
		int[] g = {0,0,0,0,0,0,0,0,0,0,0,0,0,14,35,57,79,101,117,133,147,161,175,190,205,219,234,248,255,255,255,255};
		int[] b = {0,61,96,130,165,192,220,227,210,181,151,122,93,64,35,5,0,0,0,0,0,0,0,0,0,0,0,35,98,160,223,255};
		for (int i=0; i<r.length; i++) 
			{
			reds[i] = (byte)r[i];
			greens[i] = (byte)g[i];
			blues[i] = (byte)b[i];
			}
		return r.length;
		}
	*/
	

	
	/**
	 * Generate overview graph for XYZ expression. Graph size is fixed so it throws out a lot of information.
	 * This is why this code is separate from coloc analysis
	 */
	public static void fancyGraphXYZ(EvData dataA, NucLineage coordLinA, File outputFile, String chanNameA) throws IOException
		{
		Imageset imsetA = dataA.getObjects(Imageset.class).get(0);
		
		FrameTime ftA=buildFrametime(coordLinA);

		//Bad data survival
		if(ftA.getNumPoints()<2)
			{
			System.out.println("Cannot make XYZ graph");
			return;
			}
		else
			System.out.println("Making XYZ summary file");
		
		EvChannel chanA=(EvChannel)imsetA.getChild(chanNameA); 

		//Graph size is fixed
		int numSteps=50; //?
		
		int xyzSize=20;
		
		//xyzSize x xyzSize xyzSize columns
		BufferedImage bim=new BufferedImage((xyzSize+2)*xyzSize, numSteps*(xyzSize+2), BufferedImage.TYPE_3BYTE_BGR);
		//Graphics gbim=bim.getGraphics();
		
		
		//Compare channels
		for(int time=0;time<numSteps;time++)
			{
			//Corresponding frame
			EvDecimal modelTime=new EvDecimal(time*imageMaxTime/(double)numSteps);
			System.out.println("Modeltime: "+modelTime);
			EvDecimal frameA=ftA.interpolateFrame(modelTime);
			
			//If outside range, stop calculating
			if(frameA.less(chanA.imageLoader.firstKey()) || frameA.greater(chanA.imageLoader.lastKey()))
				{
				//System.out.println("Skip fancy xyz: "+frameA);
				continue;
				}
			//else
				//System.out.println("doing "+frameA);
			
			//Use closest frame
			EvStack stackA=chanA.imageLoader.get(chanA.closestFrame(frameA));

			//Compare each slice. Same number of slices since it has been normalized
			int numz=stackA.getDepth();
			if(numz!=xyzSize)
				System.out.println("wtf. numz "+numz);
			for(int cz=0;cz<xyzSize;cz++)
				{
				//BufferedImage thisPlane=stackA.getInt(cz).getPixels().quickReadOnlyAWT();
				//gbim.drawImage(thisPlane, cz*(16+2), (16+2)*time, null);
				

				//System.out.println(""+time+"  "+cz);
				EvPixels p=stackA.getInt(cz).getPixels().convertToDouble(true);
				double[] inarr=p.getArrayDouble();
				double arrmin=getMin(inarr);
				double arrmax=getMax(inarr);
				
				
				WritableRaster raster=bim.getRaster();
				for(int ay=0;ay<xyzSize;ay++)
					for(int ax=0;ax<xyzSize;ax++)
						{
//						System.out.println(""+ax+"  "+ay+"  "+time+"  "+cz);
						double val=(inarr[ay*p.getWidth()+ax]-arrmin)/(arrmax-arrmin);
						if(val>1)
							System.out.println("val: val");
						double scale=255;
//						raster.setPixel(cz*(xyzSize+2)+ax, (xyzSize+2)*time+ay, new double[]{Math.sin(360*val),val*val*val,Math.sqrt(val)}); //bgr
						raster.setPixel(cz*(xyzSize+2)+ax, (xyzSize+2)*time+ay, new double[]{scale*Math.sin(2*Math.PI*val),scale*val*val*val,scale*Math.sqrt(val)}); //bgr
						/**
						 * gnuplot palette equation:
						 * rgb: 
						 * sqrt(x) 
						 * x³
						 * sin(360x)
						 */
						}
				
				
				}
			}
		
		ImageIO.write(bim, "png", outputFile);
		System.out.println("wrote "+outputFile);
		}
	
	private static double getMin(double[] arr)
		{
		Double ret=null;
		for(double d:arr)
			if(ret==null || d<ret)
				ret=d;
		return ret;
		}
	
	private static double getMax(double[] arr)
		{
		Double ret=null;
		for(double d:arr)
			if(ret==null || d>ret)
				ret=d;
		return ret;
		}
	
	/**
	 * Final graph from XYZ should be 2d with fixed dy/dt 
	 */
	
	
	/**
	 * BG calculation: otsu? could use for first frame at least.
	 * actually follows automatically. solved?
	 */
	
	
	public static boolean ensureCalculated(File f)
		{
		return IntExp.doOne(f);
		}
	
	
	public static Map<Tuple<File,File>, ColocCoefficients> loadCache(Set<File> datas, File cachedValuesFile)
		{
		//Read past calculated values from disk if they exist
		Map<Tuple<File,File>, ColocCoefficients> comparison=new HashMap<Tuple<File,File>, ColocCoefficients>();
		if(cachedValuesFile.exists())
			{
			System.out.println("Read stats calculated before");
			try
				{
				Document doc=EvXmlUtil.readXML(cachedValuesFile);
				Element root=doc.getRootElement();
				for(Object o:root.getChildren())
					{
					Element e=(Element)o;
					File fa=new File(e.getAttributeValue("fa"));
					File fb=new File(e.getAttributeValue("fb"));
					if(datas.contains(fa) && datas.contains(fb))
						{
						ColocCoefficients c=new ColocCoefficients();
						c.fromXML(e);
						comparison.put(Tuple.make(fa, fb), c);
						}
					}
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		return comparison;
		}
	
	public static <E> Collection<E> randomOrder(Collection<E> in)
		{
		List<E> out=new ArrayList<E>(in);
		Collections.shuffle(out);
		return out;
		}
	
	/**
	 * Calculate the colocalization given two processed AP images
	 */
	public static ColocCoefficients colocAP(double[][] imA, double[][] imB)
		{
		ColocCoefficients coeff=new ColocCoefficients();
		for(int i=0;i<imA.length;i++)
			if(imA[i]!=null && imB[i]!=null)
				coeff.add(imA[i], imB[i]);
		return coeff;
		}
	
	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		
		Set<String> argsSet=new HashSet<String>();
		for(String s:args)
			argsSet.add(s);
		
		//Find recordings to compare
		Set<File> datas=FindAnnotatedStrains.getAnnotated();
		System.out.println(datas);

		//Read past calculated values from disk 
		Map<Tuple<File,File>, ColocCoefficients> comparisonT=new TreeMap<Tuple<File,File>, ColocCoefficients>();
		Map<Tuple<File,File>, ColocCoefficients> comparisonAP=new TreeMap<Tuple<File,File>, ColocCoefficients>();
		Map<Tuple<File,File>, ColocCoefficients> comparisonXYZ=new TreeMap<Tuple<File,File>, ColocCoefficients>();
		if(!argsSet.contains("nocache"))
			{
			comparisonT=loadCache(datas, cachedValuesFileT);
			comparisonAP=loadCache(datas, cachedValuesFileAP);
			comparisonXYZ=loadCache(datas, cachedValuesFileXYZ);
			}
		
		//Do pairwise. For user simplicity, can do symmetric and reflexive
		//Each slice, different bg.
		if(!argsSet.contains("nocalc"))
			{
			System.out.println("Calculate pair-wise statistics");
			for(File fa:randomOrder(datas))
				for(File fb:randomOrder(datas))
					{
					Tuple<File,File> key=Tuple.make(fa, fb);
					
					//Check if cached calculation does not exist
					if(!comparisonT.containsKey(key) || !comparisonAP.containsKey(key) || !comparisonXYZ.containsKey(key))
						{
						System.out.println("todo: "+key);
	
						boolean calculated=ensureCalculated(fa) && ensureCalculated(fb);
	
						System.out.println("-----calculated: "+calculated);
						if(calculated)
							{
							EvData dataA=EvData.loadFile(fa);
							EvData dataB=EvData.loadFile(fb);
							
							Imageset imsetA = dataA.getObjects(Imageset.class).get(0);
							Imageset imsetB = dataB.getObjects(Imageset.class).get(0);
							
							String chanNameA=imsetA.getChild("GFP")!=null ? "GFP" : "RFP";
							String chanNameB=imsetB.getChild("GFP")!=null ? "GFP" : "RFP";
							
							//Check if XYZ summary generated. This should not be repeated as it is expensive! 
							//Only have to check the first image
							try
								{
								File outputFileXYZimageA=new File(new File(key.fst(),"data"),"expXYZ.png");
								if(!outputFileXYZimageA.exists())
									fancyGraphXYZ(dataA, coordLineageFor(dataA), outputFileXYZimageA, chanNameA);
								}
							catch (IOException e1)
								{
								e1.printStackTrace();
								}
							
							System.out.println("Comparing: "+key);
		
							String expName="exp";
							
							//Slices: T
							try
								{
								double[][] imtA=apToArray(dataA, "AP"+1+"-"+chanNameA, expName, coordLineageFor(dataA));
								double[][] imtB=apToArray(dataB, "AP"+1+"-"+chanNameB, expName, coordLineageFor(dataB));
								ColocCoefficients coeffT=colocAP(imtA, imtB);
								comparisonT.put(Tuple.make(fa,fb), coeffT);
								
								NewRenderHTML.toTimage(imtA, fa, ""+fa.getName());
								NewRenderHTML.toTimage(imtB, fb, ""+fb.getName());
								
								System.out.println("coeffT "+coeffT.n+" "+coeffT.sumX+" "+coeffT.sumXX+" "+coeffT.sumY);
								System.out.println("pearsonT "+ coeffT.getPearson());
								}
							catch (IOException e)
								{
								e.printStackTrace();
								}
							
							//Slices: AP
							try
								{
								double[][] imapA=apToArray(dataA, "AP"+20+"-"+chanNameA, expName, coordLineageFor(dataA));
								double[][] imapB=apToArray(dataB, "AP"+20+"-"+chanNameB, expName, coordLineageFor(dataB));
								ColocCoefficients coeffAP=colocAP(imapA, imapB);
								comparisonAP.put(Tuple.make(fa,fb), coeffAP);

								NewRenderHTML.toAPimage(imapA, fa, ""+fa.getName());
								NewRenderHTML.toAPimage(imapB, fb, ""+fb.getName());
								
								System.out.println("coeffAP "+coeffAP.n+" "+coeffAP.sumX+" "+coeffAP.sumXX+" "+coeffAP.sumY);
								}
							catch (IOException e)
								{
								e.printStackTrace();
								}
							
							//Slices: XYZ
							ColocCoefficients coeffXYZ=colocXYZ(dataA, dataB, coordLineageFor(dataA), coordLineageFor(dataB), chanNameA, chanNameB);
							comparisonXYZ.put(Tuple.make(fa,fb), coeffXYZ);
													
							//Store down this value too
							storeCache(comparisonT, cachedValuesFileT);
							storeCache(comparisonAP, cachedValuesFileAP);
							storeCache(comparisonXYZ, cachedValuesFileXYZ);
							}

						}
					}
			}
		
		
		if(true)
			{
			try
				{
				NewRenderHTML.makeSummaryAPT(new File(outputBaseDir,"exphtml"), datas);
				}
			catch (IOException e)
				{
				e.printStackTrace();
				}
			}
	
		File intStatsDir=new File(outputBaseDir,"intstats");
		intStatsDir.mkdirs();
		writeHTMLfromFiles(datas, comparisonT, intStatsDir,"T");
		writeHTMLfromFiles(datas, comparisonAP, intStatsDir,"AP");
		writeHTMLfromFiles(datas, comparisonXYZ, intStatsDir,"XYZ");
		
		
		
		
		System.exit(0);
		}

	
	public static NucLineage coordLineageFor(EvContainer data)
		{
		NucLineage lin=null;
		//Find lineage
		for(Map.Entry<EvPath, NucLineage> e:data.getIdObjectsRecursive(NucLineage.class).entrySet())
			if(!e.getKey().getLeafName().startsWith("AP"))
				{
				if(e.getValue()==null)
					System.out.println("!!!!! lineage is null in tree");
				return e.getValue();
				}
//				lin=e.getValue();
		if(lin!=null)
			System.out.println("no lineage. got: "+data.getIdObjectsRecursive(NucLineage.class).keySet());
		return lin;
		}
	
	
	/**
	 * Store calculated values for the next time
	 */
	public static void storeCache(Map<Tuple<File,File>, ColocCoefficients> comparison, File cachedValuesFile)
		{
		try
			{
			Element root=new Element("comparison");

			for(Tuple<File,File> t:comparison.keySet())
				{
				Element e=new Element("c");
				e.setAttribute("fa", t.fst().toString());
				e.setAttribute("fb",t.snd().toString());
				comparison.get(t).toXML(e);
//				e.setAttribute("value",""+comparison.get(t));
				root.addContent(e);
				}
			Document doc=new Document(root);
			cachedValuesFile.getParentFile().mkdirs();
			EvXmlUtil.writeXmlData(doc, cachedValuesFile);
			}
		catch (Exception e1)
			{
			e1.printStackTrace();
			}
		}
	
	
	/**
	 * How to get gene name from strain name?
	 * genotype makes more sense. deffiz claims it exists as a field
	 */

	public static String getName(File data)
		{
		return data.getName();
		}
	
	
	public static void writeHTMLfromFiles(Set<File> datas, Map<Tuple<File,File>, ColocCoefficients> comparison, File targetFile, String profType)
		{
		//Turn into HTML
		try
			{
			Set<String> titles=new TreeSet<String>();
			Map<Tuple<String,String>,ColocCoefficients> map=new HashMap<Tuple<String,String>, ColocCoefficients>();
			for(File d:datas)
				titles.add(getName(d));
			for(Tuple<File,File> t:comparison.keySet())
				map.put(Tuple.make(getName(t.fst()), getName(t.snd())), comparison.get(t));
			writeHTML(titles, map, targetFile, profType);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	
	public abstract static class TableWriter
		{
		public StringBuffer sb=new StringBuffer();
		
		public TableWriter(Set<String> titles)
			{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);
			
			//First line with only titles
			sb.append("<tr>");
			sb.append("<td>&nbsp;</td>");
			for(String t:titles)
				{
				sb.append("<td valign=\"top\">");
				for(char c:t.toCharArray())
					{
					sb.append(c);
					sb.append("<br/>");
					}
				sb.append("</td>");
				}
			sb.append("</tr>\n");
			
			//All other lines
			for(String ta:titles)
				{
				//Title
				sb.append("<tr>");
				sb.append("<td>");
				sb.append(ta);
				sb.append("</td>");
				
				for(String tb:titles)
					{
					Double val=getValue(ta, tb);
					sb.append("<td>");
					if(val==null)
						sb.append("?");
					else if(val.isInfinite())
						sb.append("Inf");
					else if(val.isNaN())
						sb.append("NaN");
					else
						sb.append(""+nf.format(val));
					sb.append("</td>");
					}
				sb.append("</tr>\n");
				}
			}
		
		public abstract Double getValue(String ta, String tb);
		}

	
	/**
	 * Write HTML-files
	 * @param titles
	 * @param map (row, column)
	 * @param targetDir
	 */
	public static void writeHTML(Set<String> titles, final Map<Tuple<String,String>,ColocCoefficients> map, File targetDir, String profType) throws IOException
		{
		TableWriter twPearson=new TableWriter(titles){
			public Double getValue(String ta, String tb)
				{
				ColocCoefficients val=map.get(Tuple.make(ta,tb));
				return val==null ? null : val.getPearson();
				}};

		TableWriter twManders1=new TableWriter(titles){
		public Double getValue(String ta, String tb)
			{
			ColocCoefficients val=map.get(Tuple.make(ta,tb));
			return val==null ? null : val.getMandersX();
			}};

		TableWriter twK1=new TableWriter(titles){
		public Double getValue(String ta, String tb)
			{
			ColocCoefficients val=map.get(Tuple.make(ta,tb));
			return val==null ? null : val.getKX();
			}};
			
			

		String template=EvFileUtil.readFile(EvFileUtil.getFileFromURL(CompareAll.class.getResource("templateCompare.html")));
		EvFileUtil.writeFile(new File(targetDir,"table"+profType+"Pearson.html"),template.replace("COEFF","Pearson").replace("BODY", twPearson.sb.toString()));
		EvFileUtil.writeFile(new File(targetDir,"table"+profType+"Manders1.html"),template.replace("COEFF","Manders<sub>1</sub>").replace("BODY", twManders1.sb.toString()));
		EvFileUtil.writeFile(new File(targetDir,"table"+profType+"K1.html"),template.replace("COEFF","k<sub>1</sub>").replace("BODY", twK1.sb.toString()));

		}
	
	
	
	
	}
