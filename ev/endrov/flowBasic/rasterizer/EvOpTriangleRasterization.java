/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */

package endrov.flowBasic.rasterizer;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector2i;

/**
 * Rasterization of a triangle-area of an image
 * 
 * @author Javier Fernandez
 */

public class EvOpTriangleRasterization extends EvOpSlice1
	{
	final int BG_COLOR = 0;
	private triangleSide[] edges;

	/**
	 * Class representing a triangle side as a line
	 */
	static class triangleSide
		{
		private Vector2i p1;
		private Vector2i p2;
		private double slope;
		private int yLength;

		public triangleSide(Vector2i p1, Vector2i p2)
			{
			this.p1 = p1;
			this.p2 = p2;
			this.slope = ((double)p2.y-p1.y)/((double)p2.x-p1.x);
			this.yLength = Math.abs((p2.y-p1.y)); // Length in y axis
			}
		
		/*
		 * Returns the Y coordinate given a X coordinate solving the line equation
		 */
		public int getYGivenX(int x)
			{
				return (int)(p1.y + slope*(x-p1.x));
			}
	/*
	 * Returns the X coordinate given a Y coordinate solving the line equation
	 */
	public int getXGivenY(int y)
		{
			return (int)((double)(y-p1.y + slope*(p1.x))/slope);
		}
	}
	public EvPixels exec1(EvPixels... p)
		{
		return rasterizeTriangle(p[0]);
		}

	public EvOpTriangleRasterization(Vector2i p1, Vector2i p2, Vector2i p3)
		{
		this.edges = new triangleSide[3];
		this.edges[0] = new triangleSide(p1, p2);
		this.edges[1] = new triangleSide(p1, p3);
		this.edges[2] = new triangleSide(p2, p3);

		}

	private EvPixels rasterizeTriangle(EvPixels inputI)
		{
		// create instance of output image as EvPixels
		inputI = inputI.convertToInt(true);
		int[] array = inputI.getArrayInt();
		EvPixels out = new EvPixels(EvPixelsType.INT, inputI.getWidth(), inputI
				.getHeight());
		int[] outPixels = out.getArrayInt();

		// Set the image with BG_COLOR
		int width = inputI.getWidth();
		int height = inputI.getHeight();

		
		for (int w=0; w<width;w++){ //Possible optimization 
		for (int h=0;h<height; h++){ outPixels[h*width+w] = 210; } 
		}
		 

		// Divide the triangle area in two sub areas. Find longest side
		int maxLength = edges[0].yLength;
		int longerEdge = 0;
		for (int i = 1; i<3; i++)
			{
			if (maxLength<edges[i].yLength)
				{
				maxLength = edges[i].yLength;
				longerEdge = i;
				}
			}

		// Other two edges indexes
		int shortEdge1 = (longerEdge+1)%3;
		int shortEdge2 = (longerEdge+2)%3;

		rasterizeArea(edges[longerEdge], edges[shortEdge1], array, outPixels,
				width, height);
		rasterizeArea(edges[longerEdge], edges[shortEdge2], array, outPixels,
				width, height);
		return out;
		}

	/*
	 * Set in outPixels the pixels of array that are within the inner area between
	 * the lines longSide and shortSisde tracing horizontal lines
	 */
	private void rasterizeArea(triangleSide longSide, triangleSide shortSide,
			int[] array, int[] outPixels, int width, int height)
		{

		int min_x;
		int max_x;
		if ((shortSide.p1.x-shortSide.p2.x)<0)
			{ // This may be faster inline
			min_x = shortSide.p1.x;
			max_x = shortSide.p2.x;
			}
		else
			{
			min_x = shortSide.p2.x;
			max_x = shortSide.p1.x;
			}
		
		// loop covering the whole shortSide. Augmenting 1 pixel
		for (int i = min_x; i<=max_x; i++)
			{ // This can be faster asking if the current line has been covered
				// Finding horizontal line
				int hy = shortSide.getYGivenX(i);
				int longSideX = longSide.getXGivenY(hy);
				
				int init = (longSideX < i)? longSideX: i;
				int end = (longSideX < i)? i: longSideX;
			
				// Horizontal line loop
				for (int hx = init; hx<=end; hx++){
					outPixels[hy*width+hx] = array[hy*width+hx];				
				}				
			}
		}
	}
