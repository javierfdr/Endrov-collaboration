/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.starter;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
//import java.util.StringTokenizer;

/**
 * Classloader that can find both JARs, CLASSs (like a URLClassLoader) and also JNI
 * @author Johan Henriksson
 *
 */
public class ResourceClassLoader extends URLClassLoader
	{
	private List<String> binfiles;

	/*
	private SpecialClassLoader parentResources;
	
	private static class SpecialClassLoader extends URLClassLoader
		{
		public SpecialClassLoader(ClassLoader parent)
			{
			super(new URL[]{},parent);
			}
		
		public String breakingFindLibrary(String libname)
			{
			return findLibrary(libname);
			}
		}
	*/
	
	public ResourceClassLoader(URL[] urls, Collection<String> binfiles, ClassLoader parent)
		{
		super(urls,parent);
		this.binfiles=new LinkedList<String>(binfiles);
		//parentResources=new SpecialClassLoader(libParent);
		}
	
	protected String findLibrary(String libname)
		{
		//System.out.println("-----------Trying to find library "+libname);

		/*
		String above=super.findLibrary(libname);
		
		if(above!=null)
			{
			System.out.println("Found already "+above);
			return above;
			}
		*/
		
		//Figure out operating system
		String OS=System.getProperty("os.name").toLowerCase();

		//Create OS-specific expected name of library
		if(OS.equals("mac os x"))
			libname="lib"+libname+".jnilib";
		else if(OS.startsWith("windows"))
			libname=libname+".dll";
		else
			libname="lib"+libname+".so";
		
		for(String s:binfiles)
			{
			File f=new File(s,libname);
			if(f.exists())
				return f.getAbsolutePath();
			}
		
		return super.findLibrary(libname);
		}
	
	
	public static Collection<String> getBinDirs(ClassLoader cl)
		{
		try
			{
			//There is some magic that disallows this
			if(cl instanceof ResourceClassLoader)
			//if(cl.getClass().getCanonicalName().equals("endrov.starter.ResourceClassLoader"))
				return new LinkedList<String>(((ResourceClassLoader)cl).binfiles);
			else
				{
				System.out.println(cl);
				return getBinDirs(cl.getParent());
				}
			}
		catch (Exception e)
			{
			//e.printStackTrace();
			return new LinkedList<String>();
			}
		}


	}
