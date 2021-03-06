/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imagesetBasic;

import java.io.File;
import endrov.imageset.EvIOImage;
import endrov.imageset.EvCommonImageIO;
import endrov.imageset.EvPixels;

/**
 * Read single-slice image into memory
 * @author Johan Henriksson
 *
 */
public class BasicSliceIO implements EvIOImage
	{
	private File file;
	private Integer z=null;
	
	public BasicSliceIO(File f)
		{
		file=f;
		}
	public BasicSliceIO(File f, int z)
		{
		file=f;
		this.z=z;
		}
	
	public EvPixels loadJavaImage()
		{
		return EvCommonImageIO.loadPixels(file, z);
		}

	
	
	}
