/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.pluginWindow;

import endrov.basicWindow.*;

import java.awt.event.*;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class PluginWindowBasic implements BasicWindowExtension
	{
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Plugins",new ImageIcon(getClass().getResource("silkPluginWindow.png")));
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new PluginWindow();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}
