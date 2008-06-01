package evplugin.modelWindow.basicExt;

import java.awt.event.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL;
import javax.swing.*;
import javax.vecmath.Vector3d;

import org.jdom.*;

import evplugin.data.EvObject;
import evplugin.modelWindow.ModelWindow;
import evplugin.modelWindow.ModelWindowExtension;
import evplugin.modelWindow.ModelWindowHook;
import evplugin.modelWindow.TransparentRender;

/**
 * Grid in model window
 * 
 * @author Johan Henriksson
 */
public class ModelWindowGrid implements ModelWindowExtension
	{
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new ModelWindowGrid());
		}
	
	public void newModelWindow(final ModelWindow w)
		{
		w.modelWindowHooks.add(new ModelWindowGridHook(w));
		}
	private class ModelWindowGridHook implements ModelWindowHook, ActionListener
		{
		private ModelWindow w;
		
		public JCheckBoxMenuItem miShowGrid=new JCheckBoxMenuItem("Show grid",true); 
		public JCheckBoxMenuItem miShowRuler=new JCheckBoxMenuItem("Show ruler",false); 
		
		public ModelWindowGridHook(ModelWindow w)
			{
			this.w=w;
			w.menuModel.add(miShowGrid);
			w.menuModel.add(miShowRuler);
			miShowGrid.addActionListener(this);
			miShowRuler.addActionListener(this);
			}
		
		
		
		public void readPersonalConfig(Element e)
			{
			try{setShowGrid(e.getAttribute("showGrid").getBooleanValue());}
			catch (DataConversionException e1){}
			}
		public void savePersonalConfig(Element e)
			{
			e.setAttribute("showGrid",""+miShowGrid.isSelected());
			}
		
		

		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==miShowGrid)
				w.repaint();
			}
		
		
		
		/**
		 * View setting: display grid?
		 */
		public void setShowGrid(boolean b)
			{
			miShowGrid.setSelected(b);
			}
			
			
		public Collection<Double> adjustScale(){return Collections.emptySet();}
		public Collection<Vector3d> autoCenterMid(){return Collections.emptySet();}
		public Collection<Double> autoCenterRadius(Vector3d mid, double FOV){return Collections.emptySet();}
		public boolean canRender(EvObject ob){return false;}
		public void displayInit(GL gl){}
		public void displaySelect(GL gl){}
		public void select(int id){}
		public void fillModelWindomMenus(){}
		public void datachangedEvent(){}

		
		/**
		 * Render all grid planes
		 */
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			gl.glPushMatrix(); 
			gl.glRotatef(90,0,1,0); 
			gl.glRotatef(90,1,0,0); 
			gl.glColor3d(0.4,0,0); 
			double gridsize=w.view.getRepresentativeScale();
			if(miShowGrid.isSelected())
				renderGridPlane(gl,gridsize); 
			if(miShowRuler.isSelected())
				{
				gl.glColor3d(1,1,1); 
				renderRuler(gl,transparentRenderers,gridsize);
				}
			gl.glPopMatrix();

			gl.glColor3d(0,0.4,0);  
			if(miShowGrid.isSelected())
				renderGridPlane(gl,gridsize); 
			if(miShowRuler.isSelected())
				{
				gl.glColor3d(1,1,1); 
				renderRuler(gl,transparentRenderers,gridsize);
				}

			gl.glPushMatrix(); 
			gl.glRotatef(90,0,0,1); 
			gl.glRotatef(90,1,0,0); 
			gl.glColor3d(0,0,0.4); 
			if(miShowGrid.isSelected())
				renderGridPlane(gl,gridsize); 
			if(miShowRuler.isSelected())
				{
				gl.glColor3d(1,1,1); 
				renderRuler(gl,transparentRenderers,gridsize);
				}
			gl.glPopMatrix();
			}

		/**
		 * Render one grid plane
		 */
		private void renderGridPlane(GL gl, double gsize)
			{
			int gnum=10;
			gl.glBegin(GL.GL_LINES);
			for(int i=-gnum;i<=gnum;i++)
				{
				gl.glVertex3d(0,-gsize*gnum, i*gsize);
				gl.glVertex3d(0, gsize*gnum, i*gsize);
				gl.glVertex3d(0,i*gsize, -gsize*gnum);
				gl.glVertex3d(0,i*gsize,  gsize*gnum);
				}
			gl.glEnd();
			}
		
		
		/**
		 * Render scale
		 */
		public void renderRuler(GL gl,List<TransparentRender> transparentRenderers, double gsize)
			{
			int gnum=10;
			for(int i=-gnum;i<=gnum;i++)
				if(i!=0)
					{
					gl.glPushMatrix();
					gl.glTranslated(0, i*gsize, 0);
					w.view.renderString(gl, transparentRenderers, (float)(gsize*0.004), ""+i*gsize);
					gl.glPopMatrix();
					}
			}
		
		
		}
	
	
	}
