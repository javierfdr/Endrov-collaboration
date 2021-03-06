/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.bookmark;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowExtension;
import endrov.imageWindow.ImageWindowTool;
import endrov.util.EvSwingUtil;

/**
 * Bookmark integration with image window
 * @author Johan Henriksson
 *
 */
public class BookmarkImageWindowHook implements ImageWindowExtension
	{
	public void newImageWindow(final ImageWindow w)
		{
		w.imageWindowTools.add(new ImageWindowTool(){
			JMenu miBookmark=new JMenu("Bookmarks");
			JMenuItem miAddBookmark=new JMenuItem("Add bookmark");
			
			//At the moment, can expect menu rebuild signal from outside
			public JMenuItem getMenuItem()
				{
				EvSwingUtil.tearDownMenu(miBookmark);
				miBookmark.add(miAddBookmark);
				miAddBookmark.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e)
						{
						Bookmark b=Bookmark.addBookmarkDialog(w, w.getSelectedData());
						if(b!=null)
							{
							b.frame=w.frameControl.getFrame();
							b.z=w.frameControl.getModelZ();
							BasicWindow.updateWindows();
							}
						/*
						EvContainer data=w.getSelectedData();
						if(data==null)
							BasicWindow.showErrorDialog("No container selected");
						else
							{
							String name=JOptionPane.showInputDialog(w, "Name of bookmark");
							if(name!=null)
								{
								if(data.metaObject.containsKey(name))
									BasicWindow.showErrorDialog("Object with this name exists already");
								else
									{
									Bookmark b=new Bookmark();
									b.frame=w.frameControl.getFrame();
									b.z=w.frameControl.getModelZ();
									
									data.metaObject.put(name, b);
									BasicWindow.updateWindows();
									}
								}
							
							}
							*/
						}
					});
				miBookmark.addSeparator();
				
				for(EvData data:EvData.openedData)
					{
					JMenu miData=new JMenu(data.getMetadataName());
					for(Map.Entry<EvPath, Bookmark> e:data.getIdObjectsRecursive(Bookmark.class).entrySet())
						{
						JMenuItem miGoto=new JMenuItem("=> "+e.getKey());
						miData.add(miGoto);
						final Bookmark m=e.getValue();
						
						miGoto.addActionListener(new ActionListener()
							{
							public void actionPerformed(ActionEvent e)
								{
								if(w.frameControl!=null)
									{
									w.frameControl.setFrame(m.frame);
									if(m.z!=null)
										w.frameControl.setZ(m.z);
									}
								w.repaint();
								}
							});
						}
					if(miData.getItemCount()>0)
						miBookmark.add(miData);				
					}
				
				
				
				return miBookmark;
				}

			public void keyPressed(KeyEvent e)
				{
				}

			public void keyReleased(KeyEvent e)
				{
				}

			public void mouseClicked(MouseEvent e)
				{
				}

			public void mouseDragged(MouseEvent e, int dx, int dy)
				{
				}

			public void mouseExited(MouseEvent e)
				{
				}

			public void mouseMoved(MouseEvent e, int dx, int dy)
				{
				}

			public void mousePressed(MouseEvent e)
				{
				}

			public void mouseReleased(MouseEvent e)
				{
				}

			public void paintComponent(Graphics g)
				{
				}

			public void deselected()
				{
				}
			
			
		
		});
		}
	}

	