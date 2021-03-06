/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.imserv;

import java.io.File;
import bioserv.SendFile;

/**
 * Generate thumbnails
 * 
 * @author Johan Henriksson
 */
public class ThumbMaker
	{
	private boolean isOST3(File file)
		{
		return file.isDirectory();
		}

	
	
	public byte[] getThumb(DataImpl data, File file) throws Exception
		{
		//OST3
		if(isOST3(file))
			{
			File datadir=new File(file,"data");
			File thumbfile=new File(datadir,"imserv.png");

			
			//TODO
			
			//Generate thumb and save down if it doesn't exist
			/*
			try
				{
				if(!thumbfile.exists() || file.lastModified()>thumbfile.lastModified())
					{
					//Locate slice to use
					File chandir=new File(file,"DIC");
					if(!chandir.exists())
						{
						for(String tag:data.tags.keySet())
							if(tag.startsWith("chan:"))
								chandir=new File(file,tag.substring("chan:".length()));
						}
					
					LinkedList<String> frames=new LinkedList<String>();
					for(File child:chandir.listFiles())
						if(!child.getName().startsWith(".") && child.isDirectory())
							frames.add(child.getName());
					File framedir=new File(chandir,frames.get(frames.size()/2));
					
					LinkedList<String> slices=new LinkedList<String>();
					for(File child:framedir.listFiles())
						if(isImage(child.getName()))
							slices.add(child.getName());
					File slicefile=new File(framedir,slices.get(slices.size()/2));
					System.out.println(slicefile);

					//Draw image onto thumb
					BufferedImage im=new BufferedImage(80,80,BufferedImage.TYPE_3BYTE_BGR);
					Graphics g=im.getGraphics();
					BufferedImage sliceimage=ImageIO.read(slicefile);
					g.setColor(Color.RED);
					g.fillRect(0, 0, 80,80);
					
					g.drawImage(sliceimage, 0, 0, im.getWidth(), im.getHeight(), 
							Color.BLACK, null);
					datadir.mkdir();
					ImageIO.write(im, "png", thumbfile);
					}
				}
			catch (RuntimeException e)
				{
				e.printStackTrace();
				}*/
			
			if(thumbfile.exists())
				return SendFile.getBytesFromFile(thumbfile);
			}
		return null;
		}


//return SendFile.getBytesFromImage(im);

	
	/*private static boolean isImage(String name)
		{
		return name.endsWith(".png") || name.endsWith(".jpg");
		}*/
	
	
	}
