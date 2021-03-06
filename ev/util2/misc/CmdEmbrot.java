/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.misc;
import java.io.*;
import org.jdom.*;

import endrov.data.*;
import endrov.ev.*;
import endrov.nuc.*;

/**
 * Calculate embryo rotation
 * @author Johan Henriksson
 */
public class CmdEmbrot
	{
	
	
	public static void dumprot(String name, EvData rec)
		{
		
		for(NucLineage lin:rec.getObjects(NucLineage.class))
			{

			//Make a deep copy
			NucLineage linrot=(NucLineage)lin.clone();

			Element e=new Element("embrot");

			//Rotate embryo to standard position
			try
				{
				rotate(linrot,e);




//				saveFile((new File(rec.datadir(),"070927.coord")).getAbsolutePath(), rec, linrot);
				saveFile(name,(new File(new File("/Volumes/TBU_main03/userdata/jurgen/embrot2"),name+".coord")).getAbsolutePath(), rec, lin);
				}
			catch (Exception e1)
				{
				e1.printStackTrace();
				}
			}
		

		
		
		}
	
	
	public static void rotate(NucLineage lin, Element e) throws Exception
		{
		//Put posterior in center
		NucLineage.NucPos postp=lin.nuc.get("post").pos.get(lin.nuc.get("post").pos.firstKey());
		double postX=postp.x, postY=postp.y, postZ=postp.z;
		for(NucLineage.Nuc n:lin.nuc.values())
			for(NucLineage.NucPos p:n.pos.values())
				{
				p.x-=postX;
				p.y-=postY;
				p.z-=postZ;
				}

		//Remove rotation in x-y plane. This rotation is not of interest
		//y for anterior will become 0
		NucLineage.NucPos antp=lin.nuc.get("ant").pos.get(lin.nuc.get("ant").pos.firstKey());
		double angleXY=-Math.atan2(antp.y, antp.x);
		double m11=Math.cos(angleXY), m12=-Math.sin(angleXY);
		double m21=Math.sin(angleXY), m22=Math.cos(angleXY);
		for(NucLineage.Nuc n:lin.nuc.values())
			for(NucLineage.NucPos p:n.pos.values())
				{
				double x=m11*p.x+m12*p.y;
				double y=m21*p.x+m22*p.y;
				p.x=x;
				p.y=y;
				}

		//remove x-z rotation. this one is more interesting
		//z=0 for anterior
		double angleXZ=-Math.atan2(antp.z, antp.x);		
		m11=Math.cos(angleXZ); m12=-Math.sin(angleXZ);
		m21=Math.sin(angleXZ); m22=Math.cos(angleXZ);
		for(NucLineage.Nuc n:lin.nuc.values())
			for(NucLineage.NucPos p:n.pos.values())
				{
				double x=m11*p.x+m12*p.z;
				double z=m21*p.x+m22*p.z;
				p.x=x;
				p.z=z;
				}


		//Normalize size of embryo length-wise
		double embryoLength=antp.x;
		for(NucLineage.Nuc n:lin.nuc.values())
			for(NucLineage.NucPos p:n.pos.values())
				{
				p.x/=embryoLength;
				p.y/=embryoLength;
				p.z/=embryoLength;
				p.r/=embryoLength;
				}


		//Calculate angle around yz for every nuc
		double rotatedXY=-angleXY;
		double rotatedXZ=-angleXZ;

		Element exy=new Element("rotXY");
		Element exz=new Element("rotXZ");
		Element elen=new Element("length");
		exy.addContent(""+rotatedXY);
		exz.addContent(""+rotatedXZ);
		elen.addContent(""+embryoLength);
		e.addContent(exy);
		e.addContent(exz);
		e.addContent(elen);
		}
	
	
	public static void saveFile(String name, String filename, EvData rec, NucLineage lin)
		{
		try
			{
			BufferedWriter fp = new BufferedWriter(new FileWriter(filename));
			System.out.println(">> "+filename);
			
			for(String nucName:lin.nuc.keySet())
				{
				NucLineage.Nuc n=lin.nuc.get(nucName);
				for(int frame:n.pos.keySet())
					{
					NucLineage.NucPos p=n.pos.get(frame);
					fp.write(""+name+"\t"+nucName+"\t"+  frame+" "+p.x+"\t"+p.y+"\t"+p.z+"\n");
					}
				}
			fp.close();
			}
		catch (IOException e)
			{
			Log.printError("Error writing file",e);
			}
		
		
		
		
		
		
		}
	}
