package evgui;


import evplugin.basicWindow.*;
import evplugin.ev.*;
import evplugin.imageWindow.*;

import java.io.*;

//http://lopica.sourceforge.net/faq.html#nosandbox
//System.setSecurityManager(null)

/**
 * Start graphical user interface
 * @author Johan Henriksson
 */
public class GUI
	{
	/**
	 * Entry point
	 * @param args Command line arguments
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());

		//Log.listeners.add(new SwingLog());

		//A window showing initialization could be useful here
		
		EvSplashScreen ss=null;
		if(EvSplashScreen.isSplashEnabled())
			ss=new EvSplashScreen();
		
		
		//if(!PluginInfo.storedInJar())
			{
			String javalib=System.getProperty("java.library.path");
			File javalibfile=new File(javalib);
			Log.printLog("Loading native libraries from "+javalibfile.getAbsolutePath());
			}
		
		try
			{
			EV.loadPlugins();
			BasicWindowExitLast.integrate();
			EV.loadPersonalConfig();		
			if(BasicWindow.getWindowList().size()==0)
				{
				//Make sure at least one window is open
				new ImageWindow();
				}
			if(ss!=null)
				ss.dispose();
			}
		catch (Exception e)
			{
			Log.printError("EVGUI", e);
			}
		
		//Help memory debugging; remove dead objects
		System.gc();
		}
	}
