/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.integrateExpression;

import java.util.Map;

import endrov.util.EvDecimal;
import endrov.util.ImVector2;
import endrov.util.ImVector3d;

/**
 * Integrate expression along LR-axis
 * @author Johan Henriksson
 *
 */
public class IntegratorGeneralLR extends IntegratorSlice 
	{

	public IntegratorGeneralLR(IntExp integrator, String newLinName, int numSubDiv, Map<EvDecimal, Double> bg)
		{
		super(integrator, newLinName, numSubDiv, bg);
		}
	
	//Normalized with inverse length of axis 
	public ImVector3d getDirVec()
		{
		double axisLength=2*shell.major;
		ImVector2 dirvec=ImVector2.polar(shell.major, shell.angle).normalize().mul(-1.0/axisLength);
		return new ImVector3d(dirvec.x, dirvec.y, 0);
		}
	
	public ImVector3d getMidPos()
		{
		return new ImVector3d(shell.midx, shell.midy, shell.midz); 
		}	
	}
