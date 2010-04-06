package endrov.skeleton;

import endrov.imageset.EvPixels;

public abstract class Skeleton
	{
	private EvPixels image;
	private int[] dt; // distance transformation of image
	private int w, h; // width and height of image

	/**
	 * General abstraction for a Skeleton. Contains an initial image and a
	 * distance transformation of the given image
	 * 
	 * @param image
	 *          the image from which the skeleton is taken
	 * @param dt
	 *          a distance transformation of image
	 * @param w
	 * 					the width of image
	 * @param h
	 *          the height of image
	 */
	public Skeleton(EvPixels image, int[] dt, int w, int h)
		{
		this.image = image;
		this.dt = dt;
		this.w = w;
		this.h = h;
		}

	/**
	 * General abstraction for a Skeleton. Contains an initial image and a
	 * distance transformation of the given image
	 * 
	 * @param image
	 *          image the image from which the skeleton is taken
	 * @param dt
	 *          a distance transformation of image
	 */
	public Skeleton(EvPixels image, int[] dt)
		{
		this.image = image;
		this.dt = dt;
		this.w = image.getWidth();
		this.h = image.getHeight();
		}

	}
