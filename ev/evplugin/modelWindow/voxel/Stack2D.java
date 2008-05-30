package evplugin.modelWindow.voxel;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;

import com.sun.opengl.util.j2d.TextureRenderer;
import com.sun.opengl.util.texture.*;

import evplugin.ev.Tuple;
import evplugin.ev.Vector3D;
import evplugin.imageset.*;
import evplugin.imageset.Imageset.ChannelImages;
import evplugin.modelWindow.Camera;
import evplugin.modelWindow.ModelWindow;
import evplugin.modelWindow.Shader;

//if one ever wish to build it in the background:
//GLContext glc=view.getContext();
//glc.makeCurrent(); 
//GL gl=glc.getGL();
// ... glc.release();

/**
 * Benchmark: to upload to card
 * 512x512 bufferedimage 2720ms 
 * 512x512 texturerenderer 740ms 
 */

/*
uploading texture in BG
http://lists.apple.com/archives/Mac-opengl/2007/Feb/msg00063.html
*/

/**
 * Render stack as several textured slices
 * @author Johan Henriksson
 */
public class Stack2D extends StackInterface
	{	
	Double lastframe=null; 
	double resZ;
	private TreeMap<Double,Vector<OneSlice>> texSlices=null;
	private final int skipForward=1; //later maybe allow this to change
	private boolean needLoadGL=false;
	
	
	private static class OneSlice
		{
		int w, h;
		double z;
		double resX,resY;
		TextureRenderer rend;
		Texture tex;
		Color color;
		}
	
	
	
	/**
	 * Get or create slices for one z. Has to be synchronized
	 */ 
	private synchronized Vector<OneSlice> getTexSlicesFrame(double z)
		{
		//Put it texture into list
		Vector<OneSlice> texSlicesV=texSlices.get(z);
		if(texSlicesV==null)
			{
			texSlicesV=new Vector<OneSlice>();
			texSlices.put(z, texSlicesV);
			}
		return texSlicesV;
		}
		
		
	
	
	
	
	/**
	 * Dispose stack. Need GL context, forced by parameter.
	 */
	public void clean(GL gl)
		{
		if(texSlices!=null)
			for(Vector<OneSlice> osv:texSlices.values())
				for(OneSlice os:osv)
					{
					os.tex.dispose();
					if(os.rend!=null)
						os.rend.dispose();
					}
		texSlices=null;
		}
	
	
	public void setLastFrame(double frame)
		{
		lastframe=frame;
		}

	
	public boolean needSettings(double frame)
		{
		return lastframe==null || frame!=lastframe;// || !isBuilt();
		}
	
	
	public void startBuildThread(double frame, HashMap<ChannelImages, VoxelExtension.ChannelSelection> chsel,ModelWindow w)
		{
		stopBuildThread();
		buildThread=new BuildThread(frame, chsel, w);
		buildThread.start();
		}
	public void stopBuildThread()
		{
		if(buildThread!=null)
			buildThread.stop=true;
		}
	
	BuildThread buildThread=null;
	public class BuildThread extends Thread
		{
		private double frame;
		private HashMap<ChannelImages, VoxelExtension.ChannelSelection> chsel;
		public boolean stop=false;
		//private ModelWindow w;
		ModelWindow.ProgressMeter pm;
		public BuildThread(double frame, HashMap<ChannelImages, VoxelExtension.ChannelSelection> chsel,ModelWindow w)
			{
			this.frame=frame;
			this.chsel=chsel;
			//this.w=w;
			pm=w.createProgressMeter();
			}
		public void run()
			{
			pm.set(0);
//			SwingUtilities.invokeLater(new Runnable(){public void run(){w.progress.setValue(0);}});
			
			//im cache safety issues
			Collection<VoxelExtension.ChannelSelection> channels=chsel.values();
			procList.clear();
			int curchannum=0;
			for(VoxelExtension.ChannelSelection chsel:channels)
				{
				int cframe=chsel.ch.closestFrame((int)Math.round(frame));
				//Common resolution for all channels
				resZ=chsel.im.meta.resZ;

				//For every Z
				TreeMap<Integer,EvImage> slices=chsel.ch.imageLoader.get(cframe);
				int skipcount=0;
				if(slices!=null)
					for(int i:slices.keySet())
						{
						if(stop)
							{
							pm.done();
//							SwingUtilities.invokeLater(new Runnable(){public void run(){w.progress.setValue(0);}});
							return; //Just stop thread if needed
							}
						skipcount++;
						if(skipcount>=skipForward)
							{
							final int progressSlices=i*1000/(channels.size()*slices.size());
							final int progressChan=1000*curchannum/channels.size();
							pm.set(progressSlices+progressChan);
//							SwingUtilities.invokeLater(new Runnable(){public void run(){w.progress.setValue(progressSlices+progressChan);}});
							
							skipcount=0;
//							System.out.println("loading #"+i);
							EvImage evim=slices.get(i);
							if(!chsel.filterSeq.isIdentity())
								evim=chsel.filterSeq.applyReturnImage(evim);
							Tuple<TextureRenderer,OneSlice> proc=processImage(evim, i, chsel);
							procList.add(proc);
							}
						}
				curchannum++;
				}

			needLoadGL=true;
			pm.done();
//			SwingUtilities.invokeLater(new Runnable(){public void run(){w.progress.setValue(0);w.view.repaint();}});
			}
		}
	
	

	public Tuple<TextureRenderer,OneSlice> processImage(EvImage evim, int z, VoxelExtension.ChannelSelection chsel)
		{
		BufferedImage bim=evim.getJavaImage(); //1-2 sec tot?
		OneSlice os=new OneSlice();
		
		os.w=bim.getWidth();
		os.h=bim.getHeight();
		os.resX=evim.getResX()/evim.getBinning(); //px/um
		os.resY=evim.getResY()/evim.getBinning();
		os.z=z/resZ;
		os.color=chsel.color;

		int bw=suitablePower2(os.w);
		os.resX/=os.w/(double)bw;
		os.w=bw;
		int bh=suitablePower2(os.h);
		os.resY/=os.h/(double)bh;
		os.h=bh;

		//Load bitmap, scale down
		TextureRenderer rend=TextureRenderer.createAlphaOnlyRenderer(os.w, os.h);
		Graphics2D g=rend.createGraphics();
		
		g.scale(os.w/(double)bim.getWidth(), os.h/(double)bim.getHeight()); //0.5 sec tot maybe
		g.drawImage(bim,0,0,Color.BLACK,null);
		
		return new Tuple<TextureRenderer, OneSlice>(rend,os);
		}
	
	
	public void addSlice(GL gl, List<Tuple<TextureRenderer,OneSlice>> procList)
		{
		clean(gl);
		texSlices=new TreeMap<Double,Vector<OneSlice>>();
		for(Tuple<TextureRenderer,OneSlice> proc:procList)
			{
			OneSlice os=proc.snd();
			os.tex=proc.fst().getTexture();
			
			Vector<OneSlice> texSlicesV=getTexSlicesFrame(os.z);
			texSlicesV.add(os);
			}
		}
	
	LinkedList<Tuple<TextureRenderer,OneSlice>> procList=new LinkedList<Tuple<TextureRenderer,OneSlice>>();
	public void loadGL(GL gl)
		{
		if(needLoadGL)
			{
			needLoadGL=false;
			System.out.println("uploading to GL");
			long t=System.currentTimeMillis();
			addSlice(gl,procList);
			System.out.println("voxels loading ok:"+(System.currentTimeMillis()-t));
			}
		}
	
	
	/**
	 * Round to best 2^
	 */
	private static int suitablePower2(int s)
		{
		//An option to restrict max texture size would be good
//		if(true)return 256;
		
		if(s>380) return 512;
		else if(s>192) return 256;
		else if(s>96) return 128;
		else if(s>48) return 64;
		else if(s>24) return 32;
		else if(s>12) return 16;
		else if(s>6) return 8;
		else return 4;
		}
	
	
	
	/**
	 * Render entire stack
	 */
	public void render(GL gl, Camera cam, boolean solidColor, boolean drawEdges, boolean mixColors)
		{
		if(isBuilt())
			{
			//Draw edges
			if(drawEdges && !texSlices.isEmpty())
				{
				OneSlice os=texSlices.get(texSlices.lastKey()).lastElement();
				double w=os.w/os.resX;
				double h=os.h/os.resY;
				double d=os.z;
				renderEdge(gl, w, h, d);
				}

			
			
			gl.glDisable(GL.GL_CULL_FACE);
			if(!solidColor)
				{
				if(mixColors)
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				else
					gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_ONE_MINUS_SRC_COLOR);
				gl.glEnable(GL.GL_BLEND);
				gl.glDepthMask(false);
				}
			
			//Sort planes, O(n) since pre-ordered
			SortedMap<Double,Vector<OneSlice>> frontMap=texSlices.headMap(cam.pos.z);
			SortedMap<Double,Vector<OneSlice>> backMap=texSlices.tailMap(cam.pos.z);
			LinkedList<Vector<OneSlice>> frontList=new LinkedList<Vector<OneSlice>>();
			LinkedList<Vector<OneSlice>> backList=new LinkedList<Vector<OneSlice>>();
			frontList.addAll(frontMap.values());
			for(Vector<OneSlice> os:backMap.values())
				backList.addFirst(os);
			
			render(gl, frontList);
			render(gl, backList);
			
			gl.glDisable(GL.GL_BLEND);
			gl.glDepthMask(true);
			gl.glEnable(GL.GL_CULL_FACE);
			}
		}

	
	private boolean isBuilt()
		{
		return texSlices!=null;
		}

	
	/**
	 * TODO move to voxext?
	 */
	private Shader shader2d=null;
	
	
	/**
	 * Render list of slices
	 */
	public void render(GL gl, LinkedList<Vector<OneSlice>> list)
		{
		if(isBuilt())
			{
			//Shader
			if(shader2d==null)
				shader2d=new Shader(gl,Stack3D.class.getResource("2dvert.glsl"),Stack3D.class.getResource("2dfrag.glsl"));

			//Planes
			for(Vector<OneSlice> osv:list)
				{
				for(OneSlice os:osv)
					{
				
					//Select texture
					os.tex.enable();
					os.tex.bind();
										
					//Find size and position
					double w=os.w/os.resX;
					double h=os.h/os.resY;
					TextureCoords tc=os.tex.getImageTexCoords();
					
					gl.glBegin(GL.GL_QUADS);
					//gl.glColor3d(1, 1, 1);
					gl.glColor3d(os.color.getRed()/255.0, os.color.getGreen()/255.0, os.color.getBlue()/255.0);
					
		//			gl.glColor4d(1, 1, 1, 0.2);
					gl.glTexCoord2f(tc.left(), tc.top());	   gl.glVertex3d(0, 0, os.z); //check
					gl.glTexCoord2f(tc.right(),tc.top());    gl.glVertex3d(w, 0, os.z);
					gl.glTexCoord2f(tc.right(),tc.bottom()); gl.glVertex3d(w, h, os.z);
					gl.glTexCoord2f(tc.left(), tc.bottom()); gl.glVertex3d(0, h, os.z);
					gl.glEnd();
		
					os.tex.disable();
					}
				}
			}
		}
	
	
	
	public Collection<Double> adjustScale(ModelWindow w)
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			OneSlice os=texSlices.get(texSlices.firstKey()).get(0);
			double width=os.w/os.resX;
			
			return Collections.singleton(width);
			}
		else
			return Collections.emptySet();
		}
	
	
	/**
	 * Give suitable center of all objects
	 */
	public Collection<Vector3D> autoCenterMid()
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			OneSlice os=texSlices.get(texSlices.firstKey()).get(0);
			double width=os.w/os.resX;
			double height=os.h/os.resY;
			return Collections.singleton(new Vector3D(width/2.0,height/2.0,(texSlices.firstKey()+texSlices.lastKey())/2.0));
			}
		else
			return Collections.emptySet();
		}
	
	
	/**
	 * Given a middle position, figure out radius required to fit objects
	 */
	public Double autoCenterRadius(Vector3D mid, double FOV)
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			OneSlice os=texSlices.get(texSlices.firstKey()).lastElement();
			double width=os.w/os.resX;
			double height=os.h/os.resY;
			double depth=os.z;
			
			double dx=Math.max(Math.abs(0-mid.x), Math.abs(width-mid.x));
			double dy=Math.max(Math.abs(0-mid.y), Math.abs(height-mid.y));
			double dz=Math.max(Math.abs(0-mid.z), Math.abs(depth-mid.z));
			double d=Math.sqrt(dx*dx+dy*dy+dz*dz);
			
			//Find how far away the camera has to be. really have FOV in here?
			return d/Math.sin(FOV);
			}
		else
			return null;
		}
	
	
	
	
	
	
	
	}
