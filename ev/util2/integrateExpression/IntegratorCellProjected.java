/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.integrateExpression;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import util2.integrateExpression.IntExp.Integrator;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * Integrate expression on single-cell level
 * 
 * This version takes the content within the projected sphere
 * 
 * @author Johan Henriksson
 *
 */
public class IntegratorCellProjected implements Integrator
	{
	private NucLineage lin;

	private Map<String, Double> expLevel;
	private Map<String, Integer> nucVol;
	private Map<NucSel, NucLineage.NucInterp> inter;
	private Map<EvDecimal, Double> bg;

	private EvDecimal linStart, linEnd;
	
	public IntegratorCellProjected(IntExp integrator, NucLineage lin,
			Map<EvDecimal, Double> bg)
		{
		this.lin = lin;
		this.bg = bg;
		ExpUtil.clearExp(lin, integrator.expName);
		ExpUtil.clearExp(lin, "CEH-5"); // TEMP
		
		linStart=lin.firstFrameOfLineage().fst();
		linEnd=lin.lastFrameOfLineage().fst();
		}

	public void integrateStackStart(IntExp integrator)
		{
		expLevel = new HashMap<String, Double>();
		nucVol = new HashMap<String, Integer>();
		inter = lin.getInterpNuc(integrator.frame);
		}

	
	private boolean includeFrame(IntExp integrator)
		{
		return linStart!=null && integrator.frame.greaterEqual(linStart) && integrator.frame.lessEqual(linEnd);
		}
	
	
	/**
	 * Integrate one plane
	 */
	public void integrateImage(IntExp integrator)
		{
		if(includeFrame(integrator))
			{
			
			double imageZw = integrator.curZ.doubleValue();

			// For all nuc
			for (Map.Entry<NucSel, NucLineage.NucInterp> e : inter.entrySet())
				{
				String nucName = e.getKey().snd();
				NucLineage.NucPos pos = e.getValue().pos;

				Double pr = projectSphere(pos.r, pos.z, imageZw);
				if (pr!=null)
					{
					int midSx = (int) integrator.stack.transformWorldImageX(pos.x);
					int midSy = (int) integrator.stack.transformWorldImageY(pos.y);
					int rS = (int) integrator.stack.scaleWorldImageX(pr);
					if (rS>0)
						{
						if (!expLevel.containsKey(nucName))
							{
							expLevel.put(nucName, 0.0);
							nucVol.put(nucName, 0);
							}

						integrator.ensureImageLoaded();

						// Integrate this area
						int sy = Math.max(midSy-rS, 0);
						int ey = Math.min(midSy+rS, integrator.pixels.getHeight());
						int sx = Math.max(midSx-rS, 0);
						int ex = Math.min(midSx+rS, integrator.pixels.getWidth());
						int area = 0;
						double exp = 0;
						for (int y = sy; y<ey; y++)
							{
							int lineIndex = integrator.pixels.getRowIndex(y);
							for (int x = sx; x<ex; x++)
								{
								int dx = x-midSx;
								int dy = y-midSy;
								if (dx*dx+dy*dy<rS*rS)
									{
									int v = integrator.pixelsLine[lineIndex+x];
									area++;
									exp += v;
									}
								}
							}

						// Sum up volume and area
						nucVol.put(nucName, nucVol.get(nucName)+area);
						expLevel.put(nucName, expLevel.get(nucName)+exp);
						}
					}
				}
			
			
			}
		

		}

	/**
	 * Done integrating one stack
	 */
	public void integrateStackDone(IntExp integrator)
		{
		if(includeFrame(integrator))
			{
			
			// Store value in XML
			for (String nucName : expLevel.keySet())
				{
				// Assumption: a cell does not move to vol=0 in the mid so it is fine to
				// throw away these values.
				// they have to be set to 0 otherwise
				double vol = nucVol.get(nucName);
				if (vol!=0)
					{
					double avg = expLevel.get(nucName)/vol-bg.get(integrator.frame);
					avg /= integrator.expTime;
					// System.out.println(nucName+" "+avg);
					NucExp exp = lin.nuc.get(nucName).getCreateExp(integrator.expName);
					if (lin.nuc.get(nucName).pos.lastKey().greaterEqual(integrator.frame)
							&&lin.nuc.get(nucName).pos.firstKey().lessEqual(integrator.frame))
						exp.level.put(integrator.frame, avg);
					}
				
				}
			
			}
		}

	/**
	 * Done with all stacks
	 */
	public void done(IntExp integrator,
			TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
		{

		// Use prior correction on this expression as well
		Double max1 = ExpUtil.getSignalMax(lin, integrator.expName);
		if (max1==null)
			System.out.println("max==null, there is no signal!");
		else
			{
			ExpUtil.normalizeSignal(lin, integrator.expName, max1, 0, 1);
			ExpUtil.correctExposureChange(correctedExposure, lin, integrator.expName);
			}

		}


	/**
	 * Project sphere onto plane. Assumes resx=resy
	 * 
	 * @param nucRw
	 *          Radius
	 * @param nucZw
	 *          Relative z
	 */
	public static Double projectSphere(double nucRw, double nucZw, double imageZw)
		{
		double dz = nucZw-imageZw;
		double tf = nucRw*nucRw-dz*dz;
		if (tf>0)
			return Math.sqrt(tf);
		else
			return null;
		}


	}
