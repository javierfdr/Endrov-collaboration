package endrov.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.swing.SwingUtilities;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import endrov.basicWindow.BasicWindow;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.PersonalConfig;
import endrov.util.EvDecimal;

/**
 * Root of container tree, handler of types
 * @author Johan Henriksson
 */
public class EvData extends EvContainer
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	/**
	 * Supported file formats and associated routines to load it
	 */
	public static Vector<EvDataSupport> supportedFileFormats=new Vector<EvDataSupport>();
	
	/**
	 * Registered types of metadata
	 */
	public static TreeMap<String,Class<? extends EvObject>> supportedMetadataFormats=new TreeMap<String,Class<? extends EvObject>>();
	
	/**
	 * Data opened by the user and hence visible as a working data set
	 */
	public static Vector<EvData> openedData=new Vector<EvData>();
	
	
	public static void initPlugin() {}
	static
		{		
	
		//Store recent entries in personal config
		EV.personalConfigLoaders.put("recentlyLoaded",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				RecentReference rref=new RecentReference(e.getAttributeValue("desc"),e.getAttributeValue("url"));
				recentlyLoadedFiles.add(rref);
				BasicWindow.updateWindows(); //Semi-ugly. Done many times.
				}
			public void savePersonalConfig(Element root)
				{
				try
					{
					for(RecentReference rref:EvData.recentlyLoadedFiles)
						{
						Element e=new Element("recentlyLoaded");
						e.setAttribute("desc",rref.descName);
						e.setAttribute("url",rref.url);
						root.addContent(e);
						}
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				}
			});
		
		BasicWindow.addBasicWindowExtension(new EvDataMenu());
//		BasicWindow.updateWindows();
		//maybe update on new extension?
		//priorities on update? windows should really go last. then the updateWindows call here is solved.
		}
	


	
	


	/******************************************************************************************************
	 *                               Static: Last path                                                    *
	 *****************************************************************************************************/
	
	/**
	 * Remember last path used to load an imageset 
	 */
	private static File lastDataPath=EV.getHomeDir();
	
	/**
	 * Get last path used to open or save data
	 */
	public static File getLastDataPath()
		{
		if(lastDataPath==null)
			return EV.getHomeDir();
		else
			return lastDataPath;
		}
	
	/**
	 * Set last path used to open or save data
	 */
	public static void setLastDataPath(File s)
		{
		if(s!=null)
			lastDataPath=s;
		}

	/******************************************************************************************************
	 *                               Static: Data registration                                            *
	 *****************************************************************************************************/

	
	/**
	 * List of recently loaded files
	 */
	public static Vector<RecentReference> recentlyLoadedFiles=new Vector<RecentReference>();

	/**
	 * Unregister loaded data from the GUI 
	 */
	public void unregisterOpenedData()
		{
		EvData.openedData.remove(this);
		BasicWindow.updateWindows();
		}
	
	
	/** 
	 * Register loaded data in GUI 
	 */
	public static void registerOpenedData(EvData data)
		{
		if(data!=null)
			{
			openedData.add(data);
			//EvData.registerLoadedDataGUI(data);
			RecentReference rref=data.getRecentEntry();
			if(rref!=null)
				{
				boolean isAdded=false;
				for(RecentReference rref2:recentlyLoadedFiles)
					if(rref2.url.equals(rref.url))
						isAdded=true;
				if(!isAdded)
					{
					recentlyLoadedFiles.add(0,rref);
					while(recentlyLoadedFiles.size()>10)
						recentlyLoadedFiles.remove(recentlyLoadedFiles.size()-1);
					}
				}
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){BasicWindow.updateWindows();}
			});
			}
		}


	/**
	 * Callback for current status on I/O (saving, loading)
	 * @author Johan Henriksson
	 */
	public interface FileIOStatusCallback
		{
		/**
		 * Tell current status. Fraction is within 0-1
		 */
		public void fileIOStatus(double frac, String text);
		}

	/**
	 * File I/O status callback: Doesn't do anything, to be used when status does not have to be presented
	 */
	public static FileIOStatusCallback deafFileIOCB=new FileIOStatusCallback(){
		public void fileIOStatus(double frac, String text){}
	};
	
	/******************************************************************************************************
	 *                               Loading                                                              *
	 *****************************************************************************************************/

	/** Load file by path/URI */
	public static EvData loadFile(String file){return loadFile(file,deafFileIOCB);}
	
	/** Load file by path */
	public static EvData loadFile(File file){return loadFile(file.getPath(),deafFileIOCB);}
	
	/** Load file by path, receive feedback on process */
	public static EvData loadFile(File file, FileIOStatusCallback cb){return loadFile(file.getPath(),cb);}

	/**
	 * Load file by path, receive feedback on process
	 */
	public static EvData loadFile(String file, FileIOStatusCallback cb)
		{
		EvDataSupport thes=null;
		int lowest=0;
		for(EvDataSupport s:EvData.supportedFileFormats)
			{
			Integer sup=s.loadSupports(file);
			if(sup!=null && (thes==null || lowest>sup))
				{
				thes=s;
				lowest=sup;
				}
			}
		if(thes!=null)
			{
			try
				{
				EvData data=thes.load(file, cb);
				if(cb!=null)
					cb.fileIOStatus(0, "Loading "+file);
				if(data!=null)
					return data;
				}
			catch (Exception e)
				{
				e.printStackTrace();
				return null;
				}
			}
		return null;
		}

	
	
	
	
	

	/******************************************************************************************************
	 *                               Saving                                                               *
	 *****************************************************************************************************/

	/** Save by file or URI */
	public void saveDataAs(String file) throws IOException {saveDataAs(file,deafFileIOCB);}

	/** Save by path */
	public void saveDataAs(File file) throws IOException {saveDataAs(file.getPath(),deafFileIOCB);}

	/**
	 * Point I/O to a new file, prepare for saving. This does not affect currently loaded data 
	 */
	public void setSaver(String file) throws IOException
		{
		EvDataSupport thes=null;
		int lowest=0;
		for(EvDataSupport s:EvData.supportedFileFormats)
			{
			Integer sup=s.saveSupports(file);
			if(sup!=null && (thes==null || lowest>sup))
				{
				thes=s;
				lowest=sup;
				}
			}
		if(thes!=null)
			{
			EvIOData io=thes.getSaver(this, file);
			if(io!=null)
				this.io=io;
			else
				throw new IOException("Plugin does not support saving this file");
			}
		else
			throw new IOException("No suitable plugin to save file");
		}
	
	/**
	 * Save file by path, receive feedback on process. Return if ok.
	 */
	public void saveDataAs(String file, FileIOStatusCallback cb) throws IOException
		{
		setSaver(file);
		saveData(cb);
		}


	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/



	/**
	 * Connection with disk for partial I/O etc
	 */
	public EvIOData io=null;

	/** Version of metadata */
	public String metadataVersion="0";


	public void saveData()
		{
		saveData(deafFileIOCB);
		}
	
	public void saveData(EvData.FileIOStatusCallback cb)
		{
		io.saveData(this, cb);
		setMetadataModified(false);
		}
	
	

	
	/******************************************************************************************************
	 *                               Instance: XML tools                                                  *
	 *****************************************************************************************************/

	
	/**
	 * Load metadata from XML-file
	 */
	public void loadXmlMetadata(File filename)
		{
		try
			{
			FileInputStream fileInputStream = new FileInputStream(filename);
			loadXmlMetadata(fileInputStream);
			}
		catch (FileNotFoundException e)
			{
			e.printStackTrace();
			}
		}
	private static void help331(Element e, EvDecimal timestep)
		{
		mulAttrib331(e, "f", timestep);
		mulAttrib331(e, "frame", timestep);
		mulAttrib331(e, "start", timestep);
		mulAttrib331(e, "end", timestep);
/*		String aF=e.getAttributeValue("f");
		if(aF!=null)
			e.setAttribute("f",new EvDecimal(aF).multiply(timestep).toString());
		String aFrame=e.getAttributeValue("frame");
		if(aFrame!=null)
			e.setAttribute("frame",new EvDecimal(aFrame).multiply(timestep).toString());*/
		for(Element child:EV.castIterableElement(e.getChildren()))
			help331(child, timestep);
		}
	private static void mulAttrib331(Element e, String name, EvDecimal timestep)
		{
		String s=e.getAttributeValue(name);
		if(s!=null)
			e.setAttribute(name,new EvDecimal(s).multiply(timestep).toString());
		}
	
	public void loadXmlMetadata(InputStream is)
		{
		metaObject.clear();
    Document document = null;
    try 
    	{
  		SAXBuilder saxBuilder = new SAXBuilder();
  		document = saxBuilder.build(is);
  		Element element = document.getRootElement();

  		if(element.getAttribute("version")!=null)
  			metadataVersion=element.getAttributeValue("version");
  		
  		//metadata 3->3.2
  		if(!metadataVersion.equals("3.2"))
	  		{
	  		System.out.println("Updating metadata to 3.2");
	  		Element eIm=element.getChild("imageset");
	  		EvDecimal timestep=EvDecimal.ONE;
	  		if(eIm!=null)
	  			{
		  		Element timestepe=eIm.getChild("timestep");
		  		if(timestepe!=null)
		  			timestep=new EvDecimal(timestepe.getText());
	  			}
	  		help331(element, timestep);
	  		}
  		
  		recursiveLoadMetadata(element);
  		/*
  		//Extract objects
  		for(Element child:EV.castIterableElement(element.getChildren()))
  			{
  			EvObjectType ext=extensions.get(child.getName());
  			EvObject o;
  			if(ext==null)
  				{
  				o=new CustomObject(child);
  				Log.printLog("Found unknown meta object of type "+child.getName());
  				}
  			else
  				{
  				o=ext.extractObjects(child);
  				Log.printLog("Found meta object of type "+child.getName());
					}
  			String sid=child.getAttributeValue("id");
  			String id;
  			if(sid==null) 
  				//This is only needed for imagesets without the EV extended attributes
  				//should maybe grab a free one (detect)
  				//id=""+-1;
  				id="im"; //This is for the OST3 transition
  			else
  				id=sid;
 				metaObject.put(id, o);
  			}
  			*/
    	} 
    catch (Exception e) 
    	{
    	e.printStackTrace();
    	} 
		}

	
	/**
	 * Get all child-objects from XML
	 * TODO What about object id?
	 * TODO can extractsubobject replace this function?
	 * @deprecated
	 */
	public static Vector<EvObject> getChildObXML(Element element)
		{
		Vector<EvObject> obs=new Vector<EvObject>();
		for(Element child:EV.castIterableElement(element.getChildren()))
			{
			Class<? extends EvObject> ext=supportedMetadataFormats.get(child.getName());
			EvObject o=null;
			if(ext==null)
				{
				o=new CustomObject();
				o.loadMetadata(child);
				Log.printLog("Found unknown meta object of type "+child.getName());
				}
			else
				{
				try
					{
					o=ext.newInstance();
					o.loadMetadata(child);
					if(EV.debugMode)
						Log.printLog("Found meta object of type "+child.getName());
					}
				catch (InstantiationException e)
					{
					e.printStackTrace();
					}
				catch (IllegalAccessException e)
					{
					e.printStackTrace();
					}
				}
			obs.add(o);
			}
		return obs;
		}
	
	/**
	 * Put all meta objects into an XML document
	 */
	public Document saveXmlMetadata() 
		{
		Element ostElement=new Element("ost");
		ostElement.setAttribute("version","3.2");
		Document doc = new Document(ostElement);
//		save
		recursiveSaveMetadata(ostElement);
//		saveSubObjectsXML(ostElement);
		return doc;
		}

	
	
	/**
	 * Get the name of this metadata to be displayed in menus
	 */
	public String getMetadataName()
		{
		if(io==null)
			return "<unnamed>";
		else
			return io.getMetadataName();
		}
	
	public String toString()
		{
		return getMetadataName();
		}

	
	/**
	 * Get entry for the "Load Recent"-menu or null if not possible
	 */
	public RecentReference getRecentEntry()
		{
		if(io==null)
			return null;
		else
			return io.getRecentEntry();
		}

	
	}
