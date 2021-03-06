/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.misc;

import endrov.ev.*;
import endrov.imagesetOST.*;

import java.io.File;

/**
 * Go through all imagesets in a directory and run the MakeQT plugin
 * @author Johan Henriksson
 */
public class BenchmarkOST
	{
	
	
	/**
	 * Entry point
	 * @param arg Command line arguments
	 */
	public static void main(String[] arg)
		{
		//Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		arg=new String[]{
					"/Volumes/TBU_xeon01_500GB01/ost4dgood/",
					"/Volumes/TBU_xeon01_500GB02/ost3dgood/",
					"/Volumes/TBU_xeon01_500GB02/ost4dgood/"					
		};
		int num=0;
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					{
					new OstImageset(file);
					num++;
					if(num==1)
						{
						System.gc();
						return;
						}
					}
		}
	}
