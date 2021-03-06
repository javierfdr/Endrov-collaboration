/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv;

import java.rmi.server.UnicastRemoteObject;

import javax.swing.JComponent;

import org.jdom.Element;

/**
 * Module extendng bioserv
 * @author Johan Henriksson
 * 
 */
public abstract class BioservModule extends UnicastRemoteObject 
	{
	static final long serialVersionUID=0;
	
	protected BioservModule() throws Exception 
		{
		super(BioservDaemon.PORT, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory());
		}
	
	public abstract String getBioservModuleName();
	
	public abstract JComponent getBioservModuleSwingComponent(BioservGUI gui);
	
	public abstract void loadConfig(Element e);
	public abstract void saveConfig(Element e);
	
	public abstract void start(BioservDaemon daemon);
	
	}
