/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * b - A
 * @author Johan Henriksson
 *
 */
public class EvOpScalarSubImage extends EvOpSlice1
	{
	private Number b;
	public EvOpScalarSubImage(Number b)
		{
		this.b = b;
		}
	public EvPixels exec1(EvPixels... p)
		{
		return EvOpImageSubScalar.minus(b, p[0]);
		}
	}