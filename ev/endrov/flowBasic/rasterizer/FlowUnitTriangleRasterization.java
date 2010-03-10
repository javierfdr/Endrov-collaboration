/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */

package endrov.flowBasic.rasterizer;
/**
 *  Flow unit: rasterizer
 * @author Javier Fernandez
 *
 */

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.AnyEvImage;
import endrov.util.Vector2i;


public class FlowUnitTriangleRasterization extends FlowUnitBasic
	{
	private static final String metaType = "rasterizeTriangle";
	private static final String showName = "Triangle Rasterizer";
	public static Color bgColor = new Color(50, 10, 120);

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()
		{
		}

	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Rasterizer", showName, metaType,
				FlowUnitTriangleRasterization.class, null, "Rasterize the triangle"));
		}

	public String getBasicShowName()
		{
		return showName;
		}

	public ImageIcon getIcon()
		{
		return null;
		}

	public String toXML(Element e)
		{
		return metaType;
		}

	public void fromXML(Element e)
		{
		}

	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("image", FlowType.ANYIMAGE); 
		types.put("side 1",FlowType.TVECTOR2I);
		types.put("side 2",FlowType.TVECTOR2I);
		types.put("side 3",FlowType.TVECTOR2I);
		}

	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", FlowType.ANYIMAGE);
		}

	public Color getBackground()
		{
		return bgColor;
		}

	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String, Object> lastOutput = exec.getLastOutput(this);
		AnyEvImage inputImage = (AnyEvImage) flow
				.getInputValue(this, exec, "image");
		Vector2i side1 = (Vector2i) flow.getInputValue(this,exec,"side 1");
		Vector2i side2 = (Vector2i) flow.getInputValue(this,exec,"side 2");
		Vector2i side3 = (Vector2i) flow.getInputValue(this,exec,"side 3");
		
		lastOutput.put("out", new EvOpTriangleRasterization(side1,side2,side3).exec1Untyped(inputImage));
		}

	}
