package evplugin.imageWindow;

import java.awt.*;
import java.awt.event.*;

//either send down variables or add accessors to imagewindow

public interface ImageWindowTool
	{
	public boolean isToggleable();
	public String toolCaption();
	public boolean enabled();
	
	public void mouseClicked(MouseEvent e);
	public void mousePressed(MouseEvent e);
	public void mouseReleased(MouseEvent e);
	public void mouseDragged(MouseEvent e, int dx, int dy);
	public void paintComponent(Graphics g);
	public void mouseMoved(MouseEvent e, int dx, int dy);
	public void keyPressed(KeyEvent e);
	public void keyReleased(KeyEvent e);
	public void mouseExited(MouseEvent e);

	}
