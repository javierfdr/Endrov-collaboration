/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.makeStdWormDist4;

import java.io.File;
import java.util.*;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import endrov.data.*;
import endrov.ev.*;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import endrov.nuc.NucLineage.NucInterp;
import endrov.util.EvParallel;


//Do not use rigid transforms, use point dist.

//in fitting all to one: possible to store individual rots, average, invert on assembly and hope it cancels

/**
 * Assemble c.e model
 * @author Johan Henriksson
 */
public class MakeStdWormDist4
	{
	public static boolean showNeigh=false;
	public static boolean saveNormalized=true;
	public static int NUMTRY=0;

	public static Vector<EvData> worms=new Vector<EvData>();
	public static SortedMap<String, NucLineage> lins=new TreeMap<String, NucLineage>();
	public static NucStats nucstats=new NucStats();

	
	public static void loadAllNuc()
		{
		String[] dirs={"/Volumes/TBU_main01/ost4dgood","/Volumes/TBU_main02/ost4dgood","/Volumes/TBU_main03/ost4dgood"};
		for(String dir:dirs)
			{
			for(File f:new File(dir).listFiles())
				if(!f.getName().startsWith("."))
					{
					String s=f.getName();
					EvData ost=new EvDataXML(f.getPath()+"/rmd.ostxml");
					worms.add(ost);
					for(EvObject evob:ost.metaObject.values())
						{
						if(evob instanceof NucLineage)
							{
							NucLineage lin=(NucLineage)evob;
							if(lin.nuc.containsKey("ABa") && lin.nuc.containsKey("ABp") &&
									lin.nuc.containsKey("EMS") && lin.nuc.containsKey("P2'") && //these are required for the coord sys
									(lin.nuc.containsKey("ABal") || lin.nuc.containsKey("ABar")) &&
									(lin.nuc.containsKey("ABpl") || lin.nuc.containsKey("ABpr"))) //these make sense
								{
								lins.put(s, lin);
								System.out.println("ok:"+s);
								}
							}
						}
				
				}
			}
		
		
		}

	public static void loadSelected()
		{
		//These all have timestep 10. NEED TO ADJUST LATER!
		//Load all worms to standardize from
		String[] wnlist={
				"/Volumes/TBU_main02/ost4dgood/N2_071114.ost",
				"/Volumes/TBU_main02/ost4dgood/N2_071116.ost",
				"/Volumes/TBU_main02/ost4dgood/TB2142_071129.ost",
				"/Volumes/TBU_main03/ost4dgood/TB2167_0804016.ost",  
				"/Volumes/TBU_main02/ost4dgood/TB2164_080118.ost",
				"/Volumes/TBU_main03/ost4dgood/TB2167_080409b.ost",
				}; 
		for(String s:wnlist)
			{
			EvData ost=new EvDataXML(s+"/rmd.ostxml");
//			Imageset ost=(Imageset)EvData.loadFile(new File(s));
//			System.out.println("Timestep "+ost.meta.metaTimestep);
			worms.add(ost);
			for(EvObject evob:ost.metaObject.values())
				{
				if(evob instanceof NucLineage)
					{
					NucLineage lin=(NucLineage)evob;
					if(lin.nuc.containsKey("ABa") && lin.nuc.containsKey("ABp") &&
							lin.nuc.containsKey("EMS") && lin.nuc.containsKey("P2'") && //these are required for the coord sys
							(lin.nuc.containsKey("ABal") || lin.nuc.containsKey("ABar")) &&
							(lin.nuc.containsKey("ABpl") || lin.nuc.containsKey("ABpr"))) //these make sense
						{
						lins.put(new File(s).getName(), lin);
						System.out.println("ok:"+s);
						}
					}
				}
			}
		
		}
	
	/**
	 * Copy lineage tree: all names and PC relations. no coordinates
	 */
	public static NucLineage copyTree(NucLineage lin)
		{
		NucLineage newlin=new NucLineage();
			
		for(Map.Entry<String, NucLineage.Nuc> e:lin.nuc.entrySet())
			{
			NucLineage.Nuc nuc=e.getValue();
			NucLineage.Nuc newnuc=newlin.getNucCreate(e.getKey());
			for(String s:nuc.child)
				newnuc.child.add(s);
			newnuc.parent=nuc.parent;
			}
		return newlin;
		}
	
	
	/**
	 * Normalize lineages in terms of size and rotation
	 */
	public static TreeMap<String, NucLineage> normalizeRot(SortedMap<String, NucLineage> lins)
		{
		System.out.println("--- normalize rigidbody ---");
		double avsize=0;
		TreeMap<String, NucLineage> newLins=new TreeMap<String, NucLineage>();
		for(Map.Entry<String, NucLineage> le:lins.entrySet())
			{
			NucLineage lin=le.getValue();
			//These define the normalized coord sys
			if(lin.nuc.containsKey("ABa") && lin.nuc.containsKey("ABp") &&
					lin.nuc.containsKey("EMS") && lin.nuc.containsKey("P2'"))
				{
				//Adjust pos
				newLins.put(le.getKey(),lin);
				center(lin);
				double thisSize=rotate1(lin);
				avsize+=thisSize;
				rotate2(lin);
				rotate3(lin);

				//Adjust radius
				for(NucLineage.Nuc nuc:lin.nuc.values())
					for(NucLineage.NucPos pos:nuc.pos.values())
						pos.r/=thisSize;
				}
			else
				System.out.println("one lin is not ok");
			}
		avsize/=newLins.size();
		System.out.println("avsize: "+avsize);
		for(NucLineage lin:newLins.values())
			{
			//Pos
			Matrix3d m=new Matrix3d();
			m.setIdentity();
			m.mul(avsize);
			applyMat(lin, m);
			
			//Adjust radius
			for(NucLineage.Nuc nuc:lin.nuc.values())
				for(NucLineage.NucPos pos:nuc.pos.values())
					pos.r*=avsize;
			}
		
		return newLins;
		}
	
	
	
	/**
	 * Normalize lineages in terms of time.
	 * The duration and start of a cell will match the reference 
	 */
	public static SortedMap<String, NucLineage> normalizeT(SortedMap<String, NucLineage> lins)
		{
		System.out.println("--- normalize T");
		TreeMap<String, NucLineage> newLins=new TreeMap<String, NucLineage>();
		for(Map.Entry<String, NucLineage> le:lins.entrySet())
			{
			NucLineage lin=le.getValue();
			NucLineage newlin=copyTree(lin);
			newLins.put(le.getKey(), newlin);
			
			for(Map.Entry<String, NucLineage.Nuc> e:lin.nuc.entrySet())
				{
				NucLineage.Nuc nuc=e.getValue();
				NucLineage.Nuc newnuc=newlin.getNucCreate(e.getKey());
				NucStats.NucStatsOne one=nucstats.nuc.get(e.getKey());
				double thisDur;
				int thisFirstFrame=nuc.pos.firstKey();
				if(nuc.child.isEmpty())
					thisDur=one.getLifeLen();
				else
					thisDur=nuc.lastFrame()-nuc.pos.firstKey();
				double oneLifeLen=one.getLifeLen();
				//potential trouble if no child and thisdur wrong
				for(int frame:e.getValue().pos.keySet())
					{
					//This is the optimal place to take different timesteps into account
					int newFrame=(int)(one.lifeStart+oneLifeLen*(frame-thisFirstFrame)/thisDur);
					System.out.println("> "+e.getKey()+" "+one.lifeStart+" "+frame+" -> "+newFrame+" // "+one.lifeEnd);
					
					NucLineage.NucPos pos=nuc.pos.get(frame);
					newnuc.pos.put(newFrame, new NucLineage.NucPos(pos));
					}
				}
			}
		return newLins;
		}
	
	/**
	 * Set end frame of all cells without children to last frame. This stops them from occuring in interpolations.
	 */
	public static void endAllCells(SortedMap<String, NucLineage> lins)
		{
		//End all nuc without children for clarity
		for(NucLineage lin:lins.values())
			for(NucLineage.Nuc nuc:lin.nuc.values())
				if(nuc.child.isEmpty() && !nuc.pos.isEmpty())
					nuc.overrideEnd=nuc.pos.lastKey();
		}
	
	/**
	 * Get names of nuclei that appear in an interpolated frame
	 */
	public static SortedSet<String> interpNucNames(Map<NucPair, NucLineage.NucInterp> inter)
		{
		TreeSet<String> names=new TreeSet<String>();
		for(NucPair p:inter.keySet())
			names.add(p.snd());
		return names;
		}

	/**
	 * Given all loaded lineages, figure out average life span of cells and collect the total lineage tree.
	 */
	public static void assembleTree()
		{
		//Collect tree
		System.out.println("--- collect tree");
		for(NucLineage lin:lins.values())
			{
			//Relative time between AB and P1'
			//Could take child times into account as well to increase resolution
			if(lin.nuc.containsKey("AB") && lin.nuc.containsKey("P1'"))
				nucstats.ABPdiff.add(lin.nuc.get("AB").lastFrame()-lin.nuc.get("P1'").lastFrame());
			
			//Life length and children
			for(String nucname:lin.nuc.keySet())
				{
				NucLineage.Nuc nuc=lin.nuc.get(nucname);
				
				int start=nuc.pos.firstKey();
				int end=nuc.pos.lastKey();
				NucStats.NucStatsOne one=nucstats.get(nucname);
				if(nuc.parent!=null)
					one.parent=nuc.parent;

				//Should only add life time of this cell if it has children, otherwise there is no
				//guarantee that the length is correct.
				if(!nuc.child.isEmpty())
					one.lifetime.add(end-start+1);
				}
			}
		nucstats.deriveLifetime();
		}

	
	/**
	 * Helper for rigid transform fitter: write transformed coordinates to a lineage object
	 */
	public static void writeRigidFitCoord(NucLineage newlin, BestFitRotTransScale bf, Map<NucPair, NucLineage.NucInterp> cRef, int curframe)
		{
		bf.newpoint.clear();
		for(Map.Entry<NucPair, NucLineage.NucInterp> e:cRef.entrySet())
			bf.newpoint.add(e.getValue().pos.getPosCopy());
		Vector<Vector3d> trans=bf.getTransformed();
		int i=0;
		for(Map.Entry<NucPair, NucLineage.NucInterp> e:cRef.entrySet())
			{
			NucPair p=e.getKey();
			newlin.getNucCreate(p.snd()).getPosCreate(curframe).setPosCopy(trans.get(i));
			newlin.getNucCreate(p.snd()).getPosCreate(curframe).r=e.getValue().pos.r;
			i++;
			}
		}
	
	/**
	 * Find the last keyframe ever mentioned in a lineage object
	 */
	public static int lastFrameOfLineage(NucLineage lin)
		{
		Integer maxframe=null;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			if(maxframe==null || nuc.pos.lastKey()>maxframe)
				maxframe=nuc.pos.lastKey();
			}
		return maxframe;
		}
	
	/**
	 * Find the first keyframe ever mentioned in a lineage object
	 */
	public static int firstFrameOfLineage(NucLineage lin)
		{
		Integer minframe=null;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			if(minframe==null || nuc.pos.firstKey()<minframe)
				minframe=nuc.pos.firstKey();
			}
		return minframe;
		}
	
	
	/**
	 * Fit nuclei objects to one reference nuclei using rigid body transformations
	 */
	public static void rigidFitOverTime()
		{
		//Choose one lineage for rotation reference
		final NucLineage refLin=lins.get("TB2167_0804016.ost");
		final int fminframe=firstFrameOfLineage(refLin);
		final int fmaxframe=lastFrameOfLineage(refLin);
		
		
		System.out.println("--- rigid fit ---");
		//Fit all to reference
		lins=EvParallel.fmapValues(lins, new EvParallel.FuncAB<NucLineage, NucLineage>(){
			public NucLineage func(NucLineage lin)
				{
				if(lin==refLin)
					return lin;
				else
					{
					NucLineage newlin=copyTree(lin);
					
					BestFitRotTransScale firstBF=null;
					BestFitRotTransScale bf=new BestFitRotTransScale();
					for(int curframe=fminframe;curframe<fmaxframe;curframe++)
//					for(int curframe=fminframe;curframe<1200;curframe++)
						{
						if(curframe%100==0)
							System.out.println("frame "+curframe);
						//Interpolate for this frame
						Map<NucPair, NucLineage.NucInterp> gRef=refLin.getInterpNuc(curframe);
						Map<NucPair, NucLineage.NucInterp> cRef=lin.getInterpNuc(curframe);
						
						//Which names are in common?
						TreeSet<String> common=new TreeSet<String>(interpNucNames(gRef));
						common.retainAll(interpNucNames(cRef));

						if(common.isEmpty())
							continue;
						
						//Fit
						bf.goalpoint.clear();
						bf.newpoint.clear();
						for(String nucname:common)
							{
//							System.out.println("+ "+nucname);
							bf.goalpoint.add(gRef.get(new NucPair(refLin,nucname)).pos.getPosCopy());
							bf.newpoint.add(cRef.get(new NucPair(lin,nucname)).pos.getPosCopy());
							}
						bf.iterate(10, 1000, 1e-10);
						
						//Remember first rotation
						if(firstBF==null)
							firstBF=new BestFitRotTransScale(bf);
						
						//Write rotated coordinates
						writeRigidFitCoord(newlin, bf, cRef, curframe);
						}
					//Keep rotation and keep going
					for(int curframe=fmaxframe;curframe<lastFrameOfLineage(lin);curframe++)
						{
						if(curframe%100==0)
							System.out.println("frame "+curframe);
						Map<NucPair, NucLineage.NucInterp> cRef=newlin.getInterpNuc(curframe);
						writeRigidFitCoord(lin, bf, cRef, curframe);
						}
					//Use first rotation for first frames
					for(int curframe=firstFrameOfLineage(lin);curframe<fminframe;curframe++)
						{
						if(curframe%100==0)
							System.out.println("frame "+curframe);
						Map<NucPair, NucLineage.NucInterp> cRef=lin.getInterpNuc(curframe);
						writeRigidFitCoord(newlin, firstBF, cRef, curframe);
						}
					
					return newlin;
					}
				}
		});	
		}

	
	
	


	/**
	 * Assemble model using averaging
	 */
	public static void assembleModel(NucLineage refLin)
		{
		//Fit coordinates
		int maxframe=nucstats.maxFrame();
		int minframe=nucstats.minFrame();
		System.out.println("--- fitting, from "+minframe+" to "+maxframe);
		for(int frame=minframe;frame<maxframe;frame++)
			{
			if(frame%100==0)
				System.out.println(frame);

//			Map<String, NucStatsOne> curnuc=nucstats.getAtFrame(frame);
//			System.out.println("num ent "+curnuc.size());

			for(Map.Entry<String, NucStats.NucStatsOne> onee:nucstats.nuc.entrySet())
				{
				NucStats.NucStatsOne one=onee.getValue();
				if(onee.getValue().existAt(frame))
					{

					List<Vector3d> poshere=one.collectedPos.get(frame);
					if(poshere!=null && !poshere.isEmpty())
						{
						one.curposAvg[0].clear();
						one.curposAvg[1].clear();
						one.curposAvg[2].clear();
						for(Vector3d u:poshere)
							{
							one.curposAvg[0].count(u.x);
							one.curposAvg[1].count(u.y);
							one.curposAvg[2].count(u.z);
							}
						one.curpos=new Vector3d(one.curposAvg[0].getMean(), one.curposAvg[1].getMean(), one.curposAvg[2].getMean());
						}
					else
						System.out.println("isempty "+onee.getKey()+" @ "+frame);

					}
				else
					one.curpos=null;
				}
			nucstats.writeCoord(refLin, frame);			

			/*
			for(String s:curnuc.keySet())
				{
				NucStatsOne one=nucstats.get(s);
				one.findNeigh(frame);

				if(showNeigh)
					{
					System.out.print(""+s+":: ");
					for(NucStats.Neigh n:one.neigh)
						System.out.print(""+n.name+":"+n.dist+" ");
					System.out.println();
					}
				}*/

			//This guesses current coordinates. children get coordinates from their parent with a small perturbation
			//to avoid numerical issues
//			nucstats.prepareCoord(refLin, frame);


			/*
			//Guess: Get position by averaging over normalized sets
			for(String name:nucstats.nuc.keySet())
				{
				NucStatsOne one=nucstats.nuc.get(name);
				List<Vector3d> poshere=one.collectedPos.get(one.toLocalFrame(frame));
				if(poshere!=null && !poshere.isEmpty())
					{
					one.curposAvg[0].clear();
					one.curposAvg[1].clear();
					one.curposAvg[2].clear();
					for(Vector3d u:poshere)
						{
						one.curposAvg[0].count(u.x);
						one.curposAvg[1].count(u.y);
						one.curposAvg[2].count(u.z);
						}
					one.curpos=new Vector3d(one.curposAvg[0].getMean(), one.curposAvg[1].getMean(), one.curposAvg[2].getMean());
					}
				}



			//Write out coordinates
			nucstats.writeCoord(refLin, frame);
			 */

			}


		}




	

	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	
	public static void applyMat(NucLineage lin, Matrix3d m)
		{
		for(NucLineage.Nuc nuc:lin.nuc.values())
			for(NucLineage.NucPos pos:nuc.pos.values())
				{
				Vector3d v=pos.getPosCopy();
				m.transform(v);
				pos.setPosCopy(v);
				}
		}
	
	public static void center(NucLineage lin)
		{
		NucLineage.Nuc nucABa=lin.nuc.get("ABa");
		NucLineage.NucPos posABa=nucABa.pos.get(nucABa.pos.lastKey());
		Vector3d sub=new Vector3d(posABa.getPosCopy());
		for(NucLineage.Nuc nuc:lin.nuc.values())
			for(NucLineage.NucPos pos:nuc.pos.values())
				{
				Vector3d v=pos.getPosCopy();
				v.sub(sub);
				pos.setPosCopy(v);
				}
		}
	
	
	
	
	public static double rotate1(NucLineage lin)
		{
		NucLineage.Nuc nucABa=lin.nuc.get("ABa");
		NucLineage.Nuc nucP2 =lin.nuc.get("P2'");

		NucLineage.NucPos posABa=nucABa.pos.get(nucABa.pos.lastKey());
		NucLineage.NucPos posP2 =nucP2.pos.get(nucP2.pos.lastKey());

		Vector3d vdir=new Vector3d(posP2.x,posP2.y,posP2.z);
		vdir.sub(new Vector3d(posABa.x,posABa.y,posABa.z));
		double size=vdir.length();

		//Rotate XY to align X
		//normalize length
		double ang=Math.atan2(vdir.y,vdir.x);
		Matrix3d m=new Matrix3d();
		m.rotZ(-ang);
		m.mul(1.0/size);
		
		applyMat(lin,m);
		
		System.out.println("pos1 "+nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy());
		
		return size;
		}
	public static void rotate2(NucLineage lin)
		{
		NucLineage.Nuc nucABa=lin.nuc.get("ABa");
		NucLineage.Nuc nucP2 =lin.nuc.get("P2'");
		NucLineage.NucPos posABa=nucABa.pos.get(nucABa.pos.lastKey());
		NucLineage.NucPos posP2 =nucP2.pos.get(nucP2.pos.lastKey());

		Vector3d vdir=new Vector3d(posP2.x,posP2.y,posP2.z);
		vdir.sub(new Vector3d(posABa.x,posABa.y,posABa.z));

		double ang=Math.atan2(vdir.z,vdir.x); 
		Matrix3d m=new Matrix3d();
		m.rotY(ang);

		applyMat(lin,m);
		
		System.out.println("pos2 "+nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy());
		}
	public static void rotate3(NucLineage lin)
		{
		NucLineage.Nuc nucABp=lin.nuc.get("ABp");
		NucLineage.Nuc nucEMS=lin.nuc.get("EMS");

		NucLineage.NucPos posABp=nucABp.pos.get(nucABp.pos.lastKey());
		NucLineage.NucPos posEMS=nucEMS.pos.get(nucEMS.pos.lastKey());
		Vector3d vdir=new Vector3d(posEMS.x,posEMS.y,posEMS.z);
		vdir.sub(new Vector3d(posABp.x,posABp.y,posABp.z));
		System.out.println("dir "+vdir);

		double ang=Math.atan2(vdir.z,vdir.y);
		Matrix3d m=new Matrix3d();
		m.rotX(-ang);

		applyMat(lin,m);
		
		NucLineage.Nuc nucP2 =lin.nuc.get("P2'");
		System.out.println("pos3 "+nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy());
		
		Vector3d vdir2=new Vector3d(posEMS.x,posEMS.y,posEMS.z);
		vdir2.sub(new Vector3d(posABp.x,posABp.y,posABp.z));
		System.out.println("dir2 "+vdir2);
		}
	
	
	
	
	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		String outputName="/Volumes/TBU_main02/ostxml/model/stdcelegansNew2.ostxml";
		
		loadSelected();

		//Get names of nuclei
		TreeSet<String> nucNames=new TreeSet<String>();
		for(NucLineage lin:lins.values())
			nucNames.addAll(lin.nuc.keySet());
		
		//Remove all :-nucs from all lineages, as well as just crap
		for(NucLineage lin:lins.values())
			{
			TreeSet<String> nucstocopynot=new TreeSet<String>();
			for(String n:lin.nuc.keySet())
				if(n.startsWith(":") || 
						n.startsWith("shell") || n.equals("ant") || n.equals("post") || 
						n.equals("venc") || n.equals("germline") ||n.equals("2ftail") ||
						n.equals("P") || n.indexOf('?')>=0 || n.indexOf('_')>=0)
					nucstocopynot.add(n);
			for(String n:nucstocopynot)
				lin.removeNuc(n);
			}
		
		
		assembleTree();


		lins=normalizeRot(lins);
		lins=normalizeT(lins);
		endAllCells(lins); //Important for later interpolation, not just visualization
		rigidFitOverTime();
		endAllCells(lins);


		//temp
		/*
		if(saveNormalized)
			{
			EvDataXML output2=new EvDataXML("/Volumes/TBU_main02/ostxml/model/normalize.ostxml");
			output2.metaObject.clear();
			for(Map.Entry<String, NucLineage> e:lins.entrySet())
				output2.metaObject.put(e.getKey(),e.getValue());
			output2.saveMeta();
			}
		*/
		

		//Write tree to XML
		NucLineage combinedLin=nucstats.generateXMLtree();
		
		
		//Collect distances and radii
		//TODO: all are now the same time!
		System.out.println("--- collect spatial statistics");

		for(int curframe=nucstats.minFrame();curframe<nucstats.maxFrame();curframe++)
			{
			if(curframe%100==0)
				System.out.println(curframe);
			for(NucLineage lin:lins.values())
				{
				Map<NucPair, NucInterp> inter=lin.getInterpNuc(curframe);
				for(Map.Entry<NucPair, NucInterp> ie:inter.entrySet())
					{
					String thisnucname=ie.getKey().snd();
					NucInterp ni=ie.getValue();
					
					NucStats.NucStatsOne one=nucstats.nuc.get(thisnucname);
					if(one!=null)
						{
						one.addRadius(curframe, ni.pos.r);
						one.addCollPos(curframe, ni.pos.getPosCopy());
	/*				
					//Get distances
					if(NUMTRY>0)
						for(NucPair otherpair:inter.keySet())
							if(!otherpair.snd().equals(thisnucname))
								{
								String othernucname=otherpair.snd();
								NucStats.NucStatsOne otherOne=nucstats.get(othernucname);
								NucInterp otheri=inter.get(otherpair);

								Vector3d diff=thisi.pos.getPosCopy();
								diff.sub(otheri.pos.getPosCopy());
								double dist=diff.length();
								one.addDistance(frame, othernucname, dist);
								otherOne.addDistance(otherOne.toLocalFrame(one.toGlobalFrame(frame)), thisnucname, dist); //to make it symmetric
								}
*/
						}
					else
						System.out.println("no one  for "+thisnucname);
					}
				}
			}
		
		assembleModel(combinedLin);

		
		//Save normalized lineages
		if(saveNormalized)
			{
			EvDataXML output2=new EvDataXML("/Volumes/TBU_main02/ostxml/model/normalize2.ostxml");
			output2.metaObject.clear();
			for(Map.Entry<String, NucLineage> e:lins.entrySet())
				output2.metaObject.put(e.getKey(),e.getValue());
			output2.metaObject.put("model", combinedLin);
			output2.saveMeta();
			}
		
	
		//Save reference
		EvDataXML output=new EvDataXML(outputName);
		output.metaObject.clear();
		output.addMetaObject(combinedLin);
		output.saveMeta();
		
		
		}

	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	public static void fitting(NucLineage refLin)
		{
		//Fit coordinates
		int maxframe=nucstats.maxFrame();
		int minframe=nucstats.minFrame();
		System.out.println("--- fitting, from "+minframe+" to "+maxframe);
		for(int frame=minframe;frame<maxframe;frame++)
			{
			if(frame%100==0)
				System.out.println(frame);
			Map<String, NucStatsOne> curnuc=nucstats.getAtFrame(frame);
//			System.out.println("num ent "+curnuc.size());
			
			
			BestFitLength bf=new BestFitLength();
			bf.nuc=curnuc;
			for(String s:curnuc.keySet())
				{
				NucStatsOne one=nucstats.get(s);
				one.findNeigh(frame);
				
				if(showNeigh)
					{
					System.out.print(""+s+":: ");
					for(NucStats.Neigh n:one.neigh)
						System.out.print(""+n.name+":"+n.dist+" ");
					System.out.println();
					}
				}

			//This guesses current coordinates. children get coordinates from their parent with a small perturbation
			//to avoid numerical issues
			nucstats.prepareCoord(refLin, frame);
			
			//Guess: Get position by averaging over normalized sets
			for(String name:nucstats.nuc.keySet())
				{
				NucStatsOne one=nucstats.nuc.get(name);
				List<Vector3d> poshere=one.collectedPos.get(one.toLocalFrame(frame));
				if(poshere!=null && !poshere.isEmpty())
					{
					one.curposAvg[0].clear();
					one.curposAvg[1].clear();
					one.curposAvg[2].clear();
					for(Vector3d u:poshere)
						{
						one.curposAvg[0].count(u.x);
						one.curposAvg[1].count(u.y);
						one.curposAvg[2].count(u.z);
						}
					one.curpos=new Vector3d(one.curposAvg[0].getMean(), one.curposAvg[1].getMean(), one.curposAvg[2].getMean());
					}
				}
			
			//Optimize guess
			if(NUMTRY==1)
				{
				double minEps=1e-4;
				bf.iterate(500, 1000, minEps);
				for(String name:nucstats.nuc.keySet())
					if(nucstats.nuc.get(name).curpos!=null)
						nucstats.nuc.get(name).curpos=new Vector3d(nucstats.nuc.get(name).curpos);
				}
				
			
			//Write out coordinates
			nucstats.writeCoord(refLin, frame);

			
//			System.out.println("frame: "+frame+"   eps: "+bf.eps);
			}
		
		
		}
	*/
	
	
	
			
	
	
