/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.integrateExpression;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Vector3d;

import util2.integrateExpression.IntExp.Integrator;
import endrov.coordinateSystem.CoordinateSystem;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * Integrate expression on an overlaid cube. Maps into a single slice
 * @author Johan Henriksson
 *
 */
public class IntegratorXYZnNO implements Integrator
	{
	private int numSubDiv;
	private double[][] sliceExp; // y,x
	private int[][] sliceVol; // y,x
	private NucLineage lin;

	private Map<EvDecimal, Double> bg = new HashMap<EvDecimal, Double>();

	private CoordinateSystem cs;

	public IntegratorXYZnNO(IntExp integrator, String newLinName,
			int numSubDiv, Map<EvDecimal, Double> bg)
		{
		this.numSubDiv = numSubDiv;
		this.bg = bg;

		// TODO need to group lineage and shell. introduce a new object?
		lin = new NucLineage();

		// Virtual nuc
		for (int ay = 0; ay<numSubDiv; ay++)
			for (int ax = 0; ax<numSubDiv*numSubDiv; ax++)
					{
					NucLineage.Nuc nuc=lin.getCreateNuc("xyz2_"+ay+"_"+ax);
					nuc.pos.clear();
					nuc.exp.clear();
					}

		integrator.imset.metaObject.remove("indX");
		integrator.imset.metaObject.remove("indY");
		integrator.imset.metaObject.remove("indZ");
		}

	/**
	 * Set up coordinate system, return if successful
	 */
	public boolean setupCS(NucLineage refLin)
		{
		NucLineage.Nuc nucP2 = refLin.nuc.get("P2'");
		NucLineage.Nuc nucABa = refLin.nuc.get("ABa");
		NucLineage.Nuc nucABp = refLin.nuc.get("ABp");
		NucLineage.Nuc nucEMS = refLin.nuc.get("EMS");

		if (nucP2==null||nucABa==null||nucABp==null||nucEMS==null
				||nucP2.pos.isEmpty()||nucABa.pos.isEmpty()||nucABp.pos.isEmpty()
				||nucEMS.pos.isEmpty())
			{
			System.out.println("Does not have required 4-cell stage marked, will not produce cube");
			return false;
			}
		else
			{
			System.out.println("Will do XYZ");
			}

		Vector3d posP2 = nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy();
		Vector3d posABa = nucABa.pos.get(nucABa.pos.lastKey()).getPosCopy();
		Vector3d posABp = nucABp.pos.get(nucABp.pos.lastKey()).getPosCopy();
		Vector3d posEMS = nucEMS.pos.get(nucEMS.pos.lastKey()).getPosCopy();

		Vector3d v1 = new Vector3d();
		Vector3d v2 = new Vector3d();
		v1.sub(posABa, posP2);
		v2.sub(posEMS, posABp);

		// By using all 4 cells for mid it should be less sensitive to
		// abberrations
		Vector3d mid = new Vector3d();
		mid.add(posABa);
		mid.add(posABp);
		mid.add(posEMS);
		mid.add(posP2);
		mid.scale(0.25);

		// Create coordinate system. Enlarge by 20%
		cs = new CoordinateSystem();
		double scale = 1.35;
		cs.setFromTwoVectors(v1, v2, v1.length()*scale, v2.length()*scale, v2.length()*scale, mid);

		return true;
		}

	public void integrateStackStart(IntExp integrator)
		{
		// Zero out arrays
		sliceExp = new double[numSubDiv][numSubDiv*numSubDiv];
		sliceVol = new int[numSubDiv][numSubDiv*numSubDiv];
		}

	public void integrateImage(IntExp integrator)
		{
		integrator.ensureImageLoaded();

		EvChannel chIndexX = integrator.imset.getCreateChannel("indX");
		EvChannel chIndexY = integrator.imset.getCreateChannel("indY");

		// Calculate index map lazily
		EvImage indX = chIndexX.getImageLoader(EvDecimal.ZERO, integrator.curZ);
		EvPixels pX;
		EvPixels pY;
		if(indX==null)
			{
			System.out.println("XYZ setting up index channels");
			indX = chIndexX.createImageLoader(EvDecimal.ZERO, integrator.curZ);
			EvImage indY = chIndexY.createImageLoader(EvDecimal.ZERO,	integrator.curZ);
			int w = integrator.pixels.getWidth();
			int h = integrator.pixels.getHeight();
			indX.setPixelsReference(pX = new EvPixels(EvPixelsType.INT, w*numSubDiv, h));
			indY.setPixelsReference(pY = new EvPixels(EvPixelsType.INT, w*numSubDiv, h));

			int[] lineX = pX.getArrayInt();
			int[] lineY = pY.getArrayInt();

			// Calculate indices
			for(int ay = 0; ay<integrator.pixels.getHeight(); ay++)
				{
				for(int ax = 0; ax<integrator.pixels.getWidth(); ax++)
					{
					// Convert to world coordinates
					Vector3d pos = new Vector3d(integrator.stack.transformImageWorldX(ax),
							integrator.stack.transformImageWorldY(ay), integrator.curZ.doubleValue());

					Vector3d insys = cs.transformToSystem(pos);

					int cx = (int) ((insys.x+0.5)*numSubDiv);
					int cy = (int) ((insys.y+0.5)*numSubDiv);
					int cz = (int) ((insys.z+0.5)*numSubDiv);

					int index = pX.getPixelIndex(ax, ay);
					if (cx>=0 && cy>=0 && cz>=0 && 
							cx<numSubDiv && cy<numSubDiv && cz<numSubDiv)
						{
						lineX[index] = cx+numSubDiv*cz;
						lineY[index] = cy;
						}
					else
						lineX[index] = -1;
					}
				}

			}
		else
			{
			//Load precalculated index
			pX = chIndexX.getImageLoader(EvDecimal.ZERO, integrator.curZ).getPixels();
			pY = chIndexY.getImageLoader(EvDecimal.ZERO, integrator.curZ).getPixels();
			}

		// Integrate this area
		int[] lineX = pX.getArrayInt();
		int[] lineY = pY.getArrayInt();
		for (int i = 0; i<integrator.pixelsLine.length; i++)
			{
			int cx = lineX[i];
			if (cx!=-1)
				{
				int cy = lineY[i];
				sliceExp[cy][cx] += integrator.pixelsLine[i];
				sliceVol[cy][cx]++;
				}
			}

		}

	/**
	 * One stack processed
	 */
	public void integrateStackDone(IntExp integrator)
		{
		// Store pattern in lineage
		for (int ay = 0; ay<numSubDiv; ay++)
			for (int ax = 0; ax<numSubDiv*numSubDiv; ax++)
				{
				double curbg = bg.get(integrator.frame);
				double vol = sliceVol[ay][ax];
				double avg;

				if(vol==0)
					avg=0;
				else
					avg=(sliceExp[ay][ax]/vol-curbg)/integrator.expTime;

				NucLineage.Nuc nuc = lin.nuc.get("xyz2_"+ax+"_"+ay);
				NucExp exp = nuc.getCreateExp(integrator.expName);
				exp.level.put(integrator.frame, avg);
				}
		}

	/**
	 * All frames processed
	 */
	public void done(IntExp integrator, TreeMap<EvDecimal, Tuple<Double, Double>> correctedExposure)
		{
		// Normalization is needed before exposure correction to make sure the
		// threshold for detecting jumps always works
		ExpUtil.normalizeSignal(lin, integrator.expName, ExpUtil.getSignalMax(
				lin, integrator.expName), 0, 1);
		ExpUtil.correctExposureChange(correctedExposure, lin, integrator.expName);

		double outImRes = 16;

		// This is only for the eye
		double sigMax = ExpUtil.getSignalMax(lin, integrator.expName);
		double sigMin = ExpUtil.getSignalMin(lin, integrator.expName);
		ExpUtil.normalizeSignal(lin, integrator.expName, sigMax, sigMin, 255);

		// Store expression as a new channel
		EvChannel chanxyz = integrator.imset.getCreateChannel("XYZ");
		//chanxyz.chBinning = outImRes;
		for (EvDecimal frame : lin.nuc.get("xyz_0_0_0").exp.get(integrator.expName).level.keySet())
			{
			System.out.println("frame "+frame);
			for (int az = 0; az<numSubDiv; az++)
				{
				EvImage evim = chanxyz.createImageLoader(frame, new EvDecimal(az));
				EvStack stack = chanxyz.imageLoader.get(frame);
				EvPixels p = new EvPixels(EvPixelsType.DOUBLE, numSubDiv, numSubDiv);
				evim.setPixelsReference(p);
				stack.resX = stack.resY = outImRes;
				double[] line = p.getArrayDouble();
				for (int ay = 0; ay<numSubDiv; ay++)
					for (int ax = 0; ax<numSubDiv; ax++)
						{
						NucLineage.Nuc nuc = lin.nuc.get("xyz_"+ax+"_"+ay+"_"+az);
						line[p.getPixelIndex(ax, ay)] = 
						/*(int)*/ (double) nuc.exp.get(integrator.expName).level.get(frame);
						}
				}
			}
		}

	}
