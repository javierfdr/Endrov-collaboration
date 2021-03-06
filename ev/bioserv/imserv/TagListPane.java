/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.imserv;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.JComponent;
import javax.swing.JPanel;



/**
 * Panel with list of tags, selectable, removable
 * 
 * @author Johan Henriksson
 *
 */
public class TagListPane extends JPanel implements MouseListener
	{
	public static final long serialVersionUID=0;
	
	private Vector<String> tags=new Vector<String>(); 
	public Set<String> selected=new HashSet<String>();
	
	private final Font font=Font.decode("Dialog PLAIN");
	private final int fonth,fonta;
	private final FontMetrics fm;
	private final int csize;
	private final int totc;
	
	private int curClick=-1;
	
	public TagListPane()
		{
		fm=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB).getGraphics().getFontMetrics(font);
		fonth=fm.getHeight();
		fonta=fm.getAscent();
		csize=(fonth/2-2)*2;
		totc=csize*2+8;
		addMouseListener(this);
		}

	
	public Set<String> getSelectedValues()
		{
		return selected;
		}

	public void setList(Collection<String> c)
		{
		tags.clear();
		tags.addAll(c);
		selected.retainAll(c);
		revalidate();
		Container cc=getParent();
		if(cc instanceof JComponent)
			((JComponent)cc).revalidate();
		repaint();
		}
	
	
	
	protected void paintComponent(Graphics g)
		{
		Dimension d=getSize();
		
		//Clear
		g.setColor(Color.WHITE);
		g.fillRect(0,0, d.width, d.height);

		for(int i=0;i<tags.size();i++)
			{
			int y=(fonth+1)*i;
			int cy=y+1;
			int cxa=1;
			int cxb=1+3+csize;
			
			if(selected.contains(tags.get(i)))
				{
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0, y, d.width, fonth);
				}
			
			
			g.setColor(Color.BLUE);
			
			//TODO: need to know if hover + or -
			
			g.fillOval(cxa, cy, csize, csize);
			g.fillOval(cxb, cy, csize, csize);
			g.setColor(Color.WHITE);
			g.drawLine(cxa+1, cy+csize/2, cxa+csize-1, cy+csize/2);
			g.drawLine(cxa+csize/2, cy+1, cxa+csize/2, cy+csize-1);
			g.drawLine(cxb+1, cy+csize/2, cxb+csize-1, cy+csize/2);
			
			
			g.setColor(Color.BLACK);
			g.drawString(tags.get(i).toString(), totc, y+fonta);
			}
		}

	public Dimension getPreferredSize()
		{
		int w=0;
		for(Object s:tags)
			{
			int nw=fm.stringWidth(s.toString());
			if(w<nw)
				w=nw;
			}
		return new Dimension(w+totc,tags.size()*(fonth+1));
		}


	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e)
		{
		curClick=(e.getY())/(fonth+1);
		repaint();
		}
	public void mouseReleased(MouseEvent e)
		{
		if(e.getX()<csize+3)
			{
			if(curClick<tags.size())
				{
				String item=tags.get(curClick);
				for(TagListListener l:listeners.keySet())
					l.tagListAddRemove(item, true);
				}
			}
		else if(e.getX()<csize*2+6)
			{
			if(curClick<tags.size())
				{
				String item=tags.get(curClick);
				for(TagListListener l:listeners.keySet())
					l.tagListAddRemove(item, false);
				}
			}
		else
			{
			//Shift
			if((e.getModifiersEx()&MouseEvent.META_DOWN_MASK)==0)
				selected.clear();
			if(curClick<tags.size())
				selected.add(tags.get(curClick));
			for(TagListListener l:listeners.keySet())
				l.tagListSelect();
			repaint();
			}
		curClick=-1;
		}
	
	private WeakHashMap<TagListListener, Object> listeners=new WeakHashMap<TagListListener, Object>();
	
	public void addTagListListener(TagListListener listener)
		{
		listeners.put(listener,null);
		}
	
	public static interface TagListListener
		{
		public void tagListSelect();
		public void tagListAddRemove(String item, boolean toAdd);
		}
	
	}
