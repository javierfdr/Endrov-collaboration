/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imagesetBioformats;

/*import java.awt.RenderingHints;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.RasterOp;*/
import java.io.*;
import java.util.*;

import ome.xml.model.primitives.NonNegativeInteger;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.*;
import loci.formats.in.OMETiffReader;
import loci.formats.meta.*;
import loci.formats.services.OMEXMLService;
import endrov.data.*;
import endrov.imageset.*;
import endrov.imagesetOST.EvIODataOST;
import endrov.util.EvDecimal;
import endrov.util.Tuple;


//how to write the files:
//https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/utils/MinimumWriter.java


//metaretriever getPixelsBigEndian
//in imageraeder, int getPixelType();
//http://hudson.openmicroscopy.org.uk/job/LOCI/javadoc/loci/formats/FormatTools.html   types

/**
 * Support for proprietary formats through LOCI Bioformats
 * 
 * @author Johan Henriksson (binding to library only)
 */
public class EvIODataBioformats implements EvIOData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	
	
	/******************************************************************************************************
	 *                               Image I/O class                                                      *
	 *****************************************************************************************************/
	
	/** Path to imageset */
	public File basedir;

	
	public IFormatReader imageReader=null;
	public IMetadata retrieve=null;
	
	/**
	 * Open a new recording
	 */
	public EvIODataBioformats(EvData d, File basedir) throws Exception
		{
		this.basedir=basedir;
		if(!basedir.exists())
			throw new Exception("File does not exist");

		
		imageReader=new ImageReader();
		
		//Populate OME-XML i.e. actually parse metadata
		imageReader.setOriginalMetadataPopulated(true);
		try 
			{
			ServiceFactory factory = new ServiceFactory();
			OMEXMLService service = factory.getInstance(OMEXMLService.class);
			retrieve=service.createOMEXMLMetadata(null, null);
			imageReader.setMetadataStore(retrieve);
			}
		catch (DependencyException de) 
			{
			throw new MissingLibraryException(OMETiffReader.NO_OME_XML_MSG, de);
			}
		catch (ServiceException se) 
			{
			throw new FormatException(se);
			}
		
		System.out.println("bioformats set id "+basedir);
		imageReader.setId(basedir.getAbsolutePath());
		System.out.println("bioformats adding channel separator");
		imageReader=new ChannelSeparator(imageReader);
		
		
		
		System.out.println("bioformats building database");
		buildDatabase(d);
		}
	
	

	public File datadir()
		{
		return basedir.getParentFile();
		}

	/**
	 * This plugin saves metadata into FILENAME.ostxml. This function constructs the name
	 * 
	 * TODO: call it bfxml instead?
	 */
	private File getMetaFile()
		{
		return new File(basedir.getParent(),basedir.getName()+".ostxml");
		}
	
	/**
	 * Save data to disk
	 */
	public void saveData(EvData d, EvData.FileIOStatusCallback cb)
		{
		try
			{
			
			
			
			
			EvIODataOST.saveMeta(d, getMetaFile());
			d.setMetadataNotModified();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	

	//Consider using this instead
	/*
	private static int getPlaneIndex(IFormatReader r, int planeNum) 
		{
		MetadataRetrieve retrieve = (MetadataRetrieve) r.getMetadataStore();
		int imageIndex = r.getSeries();
		int planeCount = retrieve.getPlaneCount(imageIndex, 0);
		int[] zct = r.getZCTCoords(planeNum);
		for (int i=0; i<planeCount; i++) 
			{
			Integer theC = retrieve.getPlaneTheC(imageIndex, 0, i);
			Integer theT = retrieve.getPlaneTheT(imageIndex, 0, i);
			Integer theZ = retrieve.getPlaneTheZ(imageIndex, 0, i);
			if (zct[0] == theZ.intValue() && zct[1] == theC.intValue() && zct[2] == theT.intValue())
				return i;
			}
		return -1;
		}
	*/

	
	
	@SuppressWarnings("deprecation")
	private static EvDecimal parseBFDate(String s)
		{
		//2002-06-17T18:35:59
		//Note that there is no time zone here. Use the local one. 
		try
			{
			int year=Integer.parseInt(s.substring(0,4));
			int month=Integer.parseInt(s.substring(5,7));
			int day=Integer.parseInt(s.substring(8,10));
			int hour=Integer.parseInt(s.substring(11,13));
			int minute=Integer.parseInt(s.substring(14,16));
			int second=Integer.parseInt(s.substring(17,19));
			Date d=new Date(year-1900,month-1,day,hour,minute,second);
			return new EvDecimal(d.getTime());
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
		}
	
	/**
	 * Scan recording for channels and build a file database
	 */
	//@SuppressWarnings("unchecked") 
	public void buildDatabase(EvData d)
		{
		//Load metadata from added OSTXML-file. This has to be done first or all image loaders are screwed
		File metaFile=getMetaFile();
		if(metaFile.exists())
			d.loadXmlMetadata(metaFile);

		HashSet<String> usedImsetNames=new HashSet<String>();
		for(int seriesIndex=0;seriesIndex<imageReader.getSeriesCount();seriesIndex++)
			{
			//Setting series will re-populate the metadata store as well
			imageReader.setSeries(seriesIndex);
			
			System.out.println("bioformats looking at series "+seriesIndex);

			String imageName=retrieve.getImageName(seriesIndex);

			//On windows, bio-formats uses the entire path. This is ugly so cut off the part
			//until the last file
			if(imageName.contains("\\"))
				imageName=imageName.substring(imageName.lastIndexOf('\\'));
			
			//The image name usually sucks, don't do this anymore!
			//String imsetName=imageName==null || imageName.equals("") ? "im"+seriesIndex : "im-"+imageName;
			String imsetName="im"+seriesIndex;
			
			
//			if(d.metaObject.containsKey(imsetName))
			if(usedImsetNames.contains(imsetName)) //In case channel already exist in XML, do not overwrite it
				imsetName="im-"+imageName;
			usedImsetNames.add(imsetName);
			
			Imageset imset=(Imageset)d.metaObject.get(imsetName);
			if(imset==null)
				d.metaObject.put(imsetName, imset=new Imageset());
			for(String s:new LinkedList<String>(imset.getChannels().keySet()))
				{
				//TODO Keep metaobjects below channel?
				imset.metaObject.remove(s);
				}

			//For all images (an image can have channels and planes)
			for(int imageIndex=0;imageIndex<retrieve.getImageCount();imageIndex++)
				{
				//Read resolution
				//Note: values are optional!!!
				Double resX=retrieve.getPixelsPhysicalSizeX(imageIndex); //[um/px]
				Double resY=retrieve.getPixelsPhysicalSizeY(imageIndex); //[um/px]
				Double resZ=retrieve.getPixelsPhysicalSizeZ(imageIndex); //[um/px]
				if(resX==null || resX==0) resX=1.0;
				if(resY==null || resY==0) resY=1.0;
				if(resZ==null || resZ==0) resZ=1.0;

				int planeCount=retrieve.getPlaneCount(imageIndex);
				for(int planeIndex=0;planeIndex<planeCount;planeIndex++)
					{
					NonNegativeInteger c=retrieve.getPlaneTheC(imageIndex, planeIndex);
					NonNegativeInteger framenum=retrieve.getPlaneTheT(imageIndex, planeIndex);
					NonNegativeInteger z=retrieve.getPlaneTheZ(imageIndex, planeIndex);

					
					//Calculate which frame this is. Note that we only consider the time of the first plane!
					EvDecimal frame=null;
					Double timeIncrement=retrieve.getPixelsTimeIncrement(imageIndex);   //TODO ?????
					if(timeIncrement!=null)
						//Time increment [s] is optional
						frame=new EvDecimal(framenum.getValue()*timeIncrement);
					else
						{
						//Time since beginning of experiment [s] is optional
						Double deltaT=retrieve.getPlaneDeltaT(imageIndex, 0);
						if(deltaT!=null)
							frame=new EvDecimal(deltaT);
						else
							frame=new EvDecimal(framenum.getValue());
						}
					
					//int numChannel=retrieve.getChannelCount(imageIndex);
					//System.out.println("# channel "+numChannel);
					
					//TODO channel metadata
					EvChannel ch=imset.getCreateChannel("ch"+c);
					
					String creationDate = retrieve.getImageAcquiredDate(0);  //TODO. per-image data, throwing away data here!
					if(creationDate!=null)
						ch.dateCreate=parseBFDate(creationDate);

					//Populate stack metadata
					EvStack stack=ch.imageLoader.get(frame);
					if(stack==null)
						{
						stack=ch.getCreateFrame(frame);
						stack.resX=resX;
						stack.resY=resY;
						stack.resZ=resZ;
						
						/*Double stagePosX=retrieve.getPlanePositionX(imageIndex, planeIndex);
						Double stagePosY=retrieve.getPlanePositionY(imageIndex, planeIndex);
						Double stagePosZ=retrieve.getPlanePositionZ(imageIndex, planeIndex);*/
						}
					
					
					EvImage evim=new EvImage();
					evim.io=new BioformatsSliceIO(imageReader, imageReader.getIndex(z.getValue(), c.getValue(), framenum.getValue()), basedir, false);
					stack.putInt(z.getValue(), evim);
					
					//Optional, [s]. Note: per-plane. Data thrown away!
					Double expTime=retrieve.getPlaneExposureTime(imageIndex, planeIndex); 
					if(expTime!=null)
						ch.setFrameMeta(frame, "exposure",""+(expTime*1000));
					
					}
				
				
				}
			}
		
		// http://hudson.openmicroscopy.org.uk/job/LOCI/javadoc/
		
		}


	public RecentReference getRecentEntry()
		{
		return new RecentReference(getMetadataName(), basedir.getPath());
		}

	public String getMetadataName()
		{
		String imageset=basedir.getName();
		return imageset;
		}

	

	@Override
	protected void finalize() throws Throwable
		{
		super.finalize();
		if(imageReader!=null)
			{
			//System.out.println("Closed eviodatabioformats for "+basedir);
			imageReader.close();
			imageReader=null;
			}
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedFileFormats.add(new EvDataSupport(){
			public Integer loadSupports(String fileS)
				{
				//ImageReader r=new ImageReader(); //Possible to get all suffixes and match
				
				File file=new File(fileS);
				return file.isFile() ? 100 : null; //Low priority; need to find a way to check extensions
				}
			public List<Tuple<String,String[]>> getLoadFormats()
				{
				ImageReader r=new ImageReader();
				//TreeSet<String> sufs=new TreeSet<String>();
				LinkedList<Tuple<String,String[]>> formats=new LinkedList<Tuple<String,String[]>>(); 
				for(IFormatHandler h:r.getReaders())
					{
					/*
					StringBuffer sb=new StringBuffer();
					sb.append(h.getFormat()+" (");
					boolean first=true;
					for(String suf:h.getSuffixes())
						{
						sufs.add(suf);
						if(!first)
							sb.append(", ");
						first=false;
						sb.append(suf);
						}
					sb.append(")");*/
					formats.add(new Tuple<String,String[]>(h.getFormat(),h.getSuffixes()));
					}				
				return formats;
				}
			public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception
				{
				EvData d=new EvData();
				d.io=new EvIODataBioformats(d, new File(file));
				return d;
				}
			public Integer saveSupports(String file){return null;}
			public List<Tuple<String,String[]>> getSaveFormats(){return new LinkedList<Tuple<String,String[]>>();};
			public EvIOData getSaver(EvData d, String file) throws IOException{return null;}
		});
		}

	
	}
