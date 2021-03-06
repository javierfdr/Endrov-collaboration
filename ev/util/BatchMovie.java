/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import endrov.data.EvData;
import endrov.ev.BatchThread;
import endrov.ev.CompleteBatch;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.makeMovie.EvMovieMakerFactory;
import endrov.makeMovie.MakeMovieThread;
import endrov.util.EvDecimal;

/**
 * Go through all imagesets in a directory and make movies
 * @author Johan Henriksson
 */
public class BatchMovie
	{
	
	public static boolean first=true;
	public static String getchdesc()
		{
		String s=first?"<channel/> (<frame/>)" : "<channel/>";
		first=false;
		return s;
		}
	
	public static void makeMovie(File file)
		{
		//first=true;
		EvData ost=EvData.loadFile(file);
	
		if(ost==null)
			return;
		else
			{
	
	
			EvMovieMakerFactory factory=EvMovieMakerFactory.getFactory("QT: h.264 (MPEG-4)");
			if(factory==null)
				{
				System.out.println("Cannot get movie maker");
				for(EvMovieMakerFactory f:EvMovieMakerFactory.makers)
					System.out.println(">"+f.getName());
				return;
				}
	
			File outfile=new File(file.getParent(),file.getName()+".mov");
			if(outfile.exists())
				return;
	
			System.out.println("Imageset "+file.getPath());
	
			List<MakeMovieThread.MovieChannel> channelNames=new LinkedList<MakeMovieThread.MovieChannel>();
	
	
			Imageset imset=ost.getIdObjectsRecursive(Imageset.class).values().iterator().next();
	
			int width=336;
	
	
			for(String name:new String[]{"GFPmax","ch0","DIC"})
				if(imset.metaObject.containsKey(name))
					{
					channelNames.add(new MakeMovieThread.MovieChannel(name,""));
	
					//Get original image size
					EvChannel ch=(EvChannel)imset.metaObject.get(name);
					EvStack stack=ch.getFirstStack();
					EvPixels p=stack.firstEntry().snd().getPixels();
					width=p.getWidth();
					}
	
			System.out.println("Now making movie");
	
			BatchThread c=new MakeMovieThread(imset, EvDecimal.ZERO, new EvDecimal("1000000"), 15, 
					channelNames, width, "Maximum", outfile, factory);
	
			new CompleteBatch(c); 
			System.out.println("Movie done");
			}
		}
	
	public static void main(String[] arg)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
	
	
	
	
		if(arg.length==0)
			arg=new String[]{
					"/Volumes/TBU_main01/ost4dgood/",
					"/Volumes/TBU_main02/ost4dgood/",
					"/Volumes/TBU_main03/ost4dgood/",
					"/Volumes/TBU_main04/ost4dgood/",
	
		};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(!file.getName().startsWith("."))
					if(!file.getName().endsWith(".mov"))
						if(file.isFile())
							{
							System.out.println(file);
							//				long currentTime=System.currentTimeMillis();
							makeMovie(file);
							//			System.out.println(" timeY "+(System.currentTimeMillis()-currentTime));
							}
	
		System.exit(0);
	
		}
	
	
	
	}
