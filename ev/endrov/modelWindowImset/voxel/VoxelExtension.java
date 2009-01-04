package endrov.modelWindowImset.voxel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.*;
import endrov.ev.*;
import endrov.filter.FilterSeq;
import endrov.filter.WindowFilterSeq;
import endrov.imageset.*;
import endrov.imageset.Imageset.ChannelImages;
import endrov.modelWindow.*;
import endrov.util.EvDecimal;

//for now does not update if image updated. would need a data update CB but
//better to wait until data observer system is ready.

//http://www.java-tips.org/other-api-tips/jogl/vertex-buffer-objects-nehe-tutorial-jogl-port-2.html

//Preferably disk data should be cached

/**
 * 3D stack renderers 
 * @author Johan Henriksson
 */
public class VoxelExtension implements ModelWindowExtension
	{
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new VoxelExtension());
		}

	public void newModelWindow(ModelWindow w)
		{
		//TODO Optimally only add if shaders are supported
		w.modelWindowHooks.add(new Hook(w)); 
		}



	/**
	 * Settings for one channel in an imageset
	 */
	public static class ChannelSelection
		{
		public Imageset im;
		public ChannelImages ch;
		public FilterSeq filterSeq;
		public Color color=new Color(0,0,0);
		}
	
	private class Hook implements ModelWindowHook, ActionListener
		{
		private ModelWindow w;
		private JPanel totalPanel=new JPanel(new GridLayout(1,3));
		private StackInterface currentStack=new Stack3D();
		private Vector<StackInterface> removableStacks=new Vector<StackInterface>();

		private OneImageChannel icR=new OneImageChannel("R", Color.RED);
		private OneImageChannel icG=new OneImageChannel("G", Color.GREEN);
		private OneImageChannel icB=new OneImageChannel("B", Color.BLUE);
	
		public JRadioButtonMenuItem miRender3dTexture=new JRadioButtonMenuItem("Use 3D texture",true);
		public JRadioButtonMenuItem miRender2dTexture=new JRadioButtonMenuItem("Use 2D texture",false);
		private ButtonGroup bgTexture=new ButtonGroup();
		public JCheckBoxMenuItem miSolidColor=new JCheckBoxMenuItem("Solid color");
		public JCheckBoxMenuItem miDrawEdge=new JCheckBoxMenuItem("Draw edge");
		public JCheckBoxMenuItem miMixColors=new JCheckBoxMenuItem("Mix colors");

		
		public Hook(ModelWindow w)
			{
			this.w=w;
			
			totalPanel.add(icR);
			totalPanel.add(icG);
			totalPanel.add(icB);
			
			bgTexture.add(miRender3dTexture);
			bgTexture.add(miRender2dTexture);
			
			JMenu miVoxels=new JMenu("Voxels");
			w.menuModel.add(miVoxels);
		
			miVoxels.add(miRender2dTexture);
			miVoxels.add(miRender3dTexture);
			miVoxels.addSeparator();
			miVoxels.add(miSolidColor);
			miVoxels.add(miDrawEdge);
			miVoxels.add(miMixColors);
			
			
			miRender2dTexture.addActionListener(this);
			miRender3dTexture.addActionListener(this);
			miSolidColor.addActionListener(this);
			miDrawEdge.addActionListener(this);
			miMixColors.addActionListener(this);
			}
		
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==miDrawEdge || e.getSource()==miMixColors)
				w.view.repaint();
			else
				{
				//System.out.println("voxel repaint2");
				icR.stackChanged();
				icG.stackChanged();
				icB.stackChanged();
				}
			}



		
		
		public Collection<Double> adjustScale()
			{
			return currentStack.adjustScale(w);
			}
		public Collection<Vector3d> autoCenterMid()
			{
			return currentStack.autoCenterMid();
			}
		public Collection<Double> autoCenterRadius(Vector3d mid, double FOV)
			{
			Double r=currentStack.autoCenterRadius(mid, FOV);
			if(r==null)
				return Collections.emptySet();
			else
				return Collections.singleton(r);
			}
		public boolean canRender(EvObject ob){return false;}
		public void displayInit(GL gl){}
		public void displaySelect(GL gl){}
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void select(int id){}
		public void fillModelWindowMenus()
			{
			w.bottomPanelItems.add(totalPanel);
			}

	
		
		public EvDecimal getFrame()
			{
			return this.w.frameControl.getFrame();
			}

		public void datachangedEvent()
			{
			//System.out.println("voxel datachanged event");
			EvContainer data=w.getSelectedData();
			Imageset im=data instanceof Imageset ? (Imageset)data : new Imageset();
//			List<Imageset> ims=data.getObjects(Imageset.class);
//			ims.isEmpty() ? new Imageset() : ims.get(0);
			
			icR.channelCombo.setExternalImageset(im);
			icG.channelCombo.setExternalImageset(im);
			icB.channelCombo.setExternalImageset(im);
			icR.checkStackChanged();
			icG.checkStackChanged();
			icB.checkStackChanged();
			}
		
		
		
		
		
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			try
				{
				//Remove prior data
				for(StackInterface s:removableStacks)
					s.clean(gl);
				removableStacks.clear();

				EvDecimal frame=getFrame();
				
				//Build list of which channels should be rendered
				if(currentStack.needSettings(frame))
					{
					currentStack.setLastFrame(frame);
					
					//Build set of channels in Swing loop. Then there is no need to worry about strange GUI interaction
					HashMap<ChannelImages, ChannelSelection> chsel=new HashMap<ChannelImages, ChannelSelection>(); 
					for(OneImageChannel oc:new OneImageChannel[]{icR, icG, icB})
						{
						Imageset im=oc.channelCombo.getImageset();
//					System.out.println("im: "+im);
//					System.out.println("im2: "+oc.channelCombo.getChannel());
						ChannelImages chim=im.getChannel(oc.channelCombo.getChannel());
						if(chim!=null)
							{
							ChannelSelection sel=chsel.get(chim);
							if(sel==null)
								{
								sel=new ChannelSelection();
								chsel.put(chim, sel);
								}
							sel.im=im;
							sel.ch=chim;
							sel.filterSeq=oc.filterSeq;
							sel.color=new Color(
									sel.color.getRed()+oc.color.getRed(),
									sel.color.getGreen()+oc.color.getGreen(),
									sel.color.getBlue()+oc.color.getBlue());
							}
						}
					
					//Start build thread
		//			System.out.println("start build thread");
					currentStack.startBuildThread(frame, chsel, w);
					}
				else
					{
					//Render
		//			System.out.println("voxloadgl");
					currentStack.loadGL(gl);
		//			System.out.println("voxrender");
					currentStack.render(gl,transparentRenderers,w.view.camera,miSolidColor.isSelected(),miDrawEdge.isSelected(), miMixColors.isSelected());
					}
				}
			catch (Exception e)
				{
				//This catches exceptions related to shaders
				e.printStackTrace();
				}
			}
		

	

		/**
		 * GUI component to control channel settings
		 */
		private class OneImageChannel extends JPanel implements ActionListener
			{
			static final long serialVersionUID=0;
			private JButton bFs=FilterSeq.createFilterSeqButton();
			private OldChannelCombo channelCombo=new OldChannelCombo(null,true);
			private Color color;
			private FilterSeq filterSeq=new FilterSeq();
			
			public OneImageChannel(String name,Color color)
				{
				this.color=color;
				setLayout(new BorderLayout());
				add(new JLabel(name),BorderLayout.WEST);
				add(channelCombo,BorderLayout.CENTER);
				add(bFs,BorderLayout.EAST);
				channelCombo.addActionListener(this);
				bFs.addActionListener(this);
				filterSeq.observer.addWeakListener(filterSeqObserver);
				}
			
			/** Invoked when filter sequence changed */
			private SimpleObserver.Listener filterSeqObserver=new SimpleObserver.Listener()
				{public void observerEvent(Object o){stackChanged();}};
			
//			public WeakReference<Imageset> lastImageset=new WeakReference<Imageset>(null);
//			public String lastChannel="";
			public WeakReference<Imageset.ChannelImages> lastChannelImages=new WeakReference<ChannelImages>(null);
			
			/** Update stack */ 
			public void stackChanged()
				{
				//TODO: when filter seq updated, a signal should be sent back
				if(currentStack!=null)
					currentStack.stopBuildThread();
				removableStacks.add(currentStack);
				System.out.println("new stack");
				if(miRender3dTexture.isSelected()) 
					currentStack=new Stack3D(); 
				else
					currentStack=new Stack2D();
//				lastImageset=new WeakReference<Imageset>(channelCombo.getImagesetNull());
//				lastChannel=channelCombo.getChannelNotNull();

				Imageset.ChannelImages images=channelCombo.getImageset().channelImages.get(channelCombo.getChannel());
				lastChannelImages=new WeakReference<ChannelImages>(images);
				
				System.out.println("voxel repaint");
				w.view.repaint();
				}
			
			/** Update stack if imageset or channel changed */
			public void checkStackChanged()
				{
/*				Imageset ims=lastImageset.get();
				String ch=lastChannel;
				Imageset newImageset=channelCombo.getImagesetNull();
				String newChannel=channelCombo.getChannelNotNull();*/
				Imageset.ChannelImages images=channelCombo.getImageset().channelImages.get(channelCombo.getChannel());
				if(images!=lastChannelImages.get())
//				if(ims!=newImageset || !ch.equals(newChannel))
					stackChanged();
				}
				
			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==channelCombo)
					{
					stackChanged();
//					System.out.println("call stack change");
					}
				else if(e.getSource()==bFs)
					{
					new WindowFilterSeq(filterSeq);
					}
				}
			
			
			}
		
		
		public EvDecimal getFirstFrame(){return null;}
		public EvDecimal getLastFrame(){return null;}
		
		}
	
	}
