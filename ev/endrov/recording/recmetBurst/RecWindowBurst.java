/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetBurst;


import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.EvData;

/**
 * Burst acquisition
 * @author Johan Henriksson 
 */
public class RecWindowBurst extends BasicWindow
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
	public RecWindowBurst()
		{
		this(new Rectangle(400,300));
		}
	
	public RecWindowBurst(Rectangle bounds)
		{

		
		
		/**
		 * From micromanager:
		 * 
		 * sequence length
		 * directory root
		 * name prefix
		 * buffer status
		 * disk status
		 * interval [ms]
		 * output to: disk/screen
		 * [] display while acquiring
		 * 
		 * Start acq
		 * Stop
		 * stop saving
		 * 
		 * 
		 * 
		 * 
		 */
		
		
		
		///////////////// Acquire ///////////////////////////////////////

		
		
		
		////////////////////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
//		add(EvSwingUtil.compactVertical(zpanel,tpanel),
	//			BorderLayout.CENTER);
//		add(new JLabel(""),BorderLayout.CENTER);
		
		
		
		
		
		//Window overall things
		setTitleEvWindow("Burst acquisition");
		packEvWindow();
		setVisibleEvWindow(true);
//		setBoundsEvWindow(bounds);
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		
		} 
	public void freeResources()
		{
		}
	
	public static void main(String[] args)
		{
		new RecWindowBurst();
		
		}

	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
			public void newBasicWindow(BasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(),new Hook());
				}
			class Hook implements BasicWindowHook, ActionListener
				{
				public void createMenus(BasicWindow w)
					{
					JMenuItem mi=new JMenuItem("Burst acquisition",new ImageIcon(getClass().getResource("tangoCamera.png")));
					mi.addActionListener(this);
					BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowBurst();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		
		
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
			public void newBasicWindow(BasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(),new Hook());
				}
			class Hook implements BasicWindowHook, ActionListener
				{
				public void createMenus(BasicWindow w)
					{
					JMenuItem mi=new JMenuItem("Burst acq Window",new ImageIcon(getClass().getResource("tangoCamera.png")));
					mi.addActionListener(this);
					BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowBurst();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		}
	
	}
