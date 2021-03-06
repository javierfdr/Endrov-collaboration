/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Serialization of images to and from bytes
 * @author Johan Henriksson
 */
public class SendFile
	{
	
	public static byte[] getBytesFromFile(File file) throws IOException 
		{
		InputStream is = new FileInputStream(file);
	
		// Get the size of the file
		long length = file.length();
	
		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) 
			throw new IOException("File too large "+file.getName());
	
	
		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];
	
		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) 
			offset += numRead;
	
	
		// Ensure all the bytes have been read in
		if (offset < bytes.length) 
			throw new IOException("Could not completely read file "+file.getName());
	
	
		// Close the input stream and return bytes
		is.close();
		return bytes;
		}

	
	
	public static BufferedImage getImageFromBytes(byte[] imgBytes)
		{
	
		try
			{
			if(imgBytes==null)
				return null;
			else
				{
				return ImageIO.read(new ByteArrayInputStream(imgBytes));
				}
			}
		catch(Exception e)
			{
			e.printStackTrace();
			return null;
			}
		}

	public static byte[] getBytesFromImage(BufferedImage im)
		{
		try
			{
			ByteArrayOutputStream os=new ByteArrayOutputStream();
			ImageIO.write(im, "png", os);
			return os.toByteArray();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		return null;
		}
	
	}
