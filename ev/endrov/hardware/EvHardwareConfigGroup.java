package endrov.hardware;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom.Element;

import endrov.ev.EV;
import endrov.ev.PersonalConfig;


/**
 * Config group for hardware aka a meta state. Should maybe even be made a new type of device?
 * 
 * @author Johan Henriksson
 *
 */
public class EvHardwareConfigGroup
	{
	/**
	 * Config groups
	 */
	public static Map<String,EvHardwareConfigGroup> groups=new TreeMap<String, EvHardwareConfigGroup>();
	
	/**
	 * Which properties should be included in this group. Only used as an aid to create new states, since
	 * a group have no states when created and the list must be stored somewhere
	 */
	public Set<EvDevicePropPath> propsToInclude=new TreeSet<EvDevicePropPath>();
	
	/**
	 * The states
	 */
	public Map<String, State> states=new TreeMap<String, State>();
	
	/**
	 * One state
	 * @author Johan Henriksson
	 *
	 */
	public static class State
		{
		public Map<EvDevicePropPath, String> propMap=new HashMap<EvDevicePropPath, String>();

		/**
		 * Set state - change all other devices. Synchronization?
		 */
		public void activate()
			{
			System.out.println("Activate");
			for(Map.Entry<EvDevicePropPath, String> e:propMap.entrySet())
				{
				EvDevice dev=e.getKey().device.getDevice();
				String prop=e.getKey().property;
				dev.setPropertyValue(prop, e.getValue());
				}
			}
		}

	/**
	 * Get one state by name
	 */
	public State getState(String name)
		{
		return states.get(name);
		}
	
	/**
	 * Store current state as a new state
	 */
	public void captureCurrentState(String name)
		{
		State st=new State();
		for(EvDevicePropPath p:propsToInclude)
			st.propMap.put(p, p.device.getDevice().getPropertyValue(p.property));
		states.put(name,st);
		}

	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		
		
		EV.personalConfigLoaders.put("configgroups",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				for(Object o:e.getChildren())
					{
					Element eGroup=(Element)o;

					EvHardwareConfigGroup group=new EvHardwareConfigGroup();
					groups.put(eGroup.getAttributeValue("name"), group);

					for(Object oo:e.getChildren())
						if(e.getName().equals("group"))
							{
							//One stage group
							Element eState=(Element)oo;
							State state=new State();
							EvDevicePropPath dev=new EvDevicePropPath(
									new EvDevicePath(eState.getAttributeValue("device")),
									eState.getAttributeValue("property"));
							state.propMap.put(dev, 
									eState.getAttributeValue("value"));
							}
						else if(e.getName().equals("include"))
							{
							//Properties to include
							Element eState=(Element)oo;
							EvDevicePropPath dev=new EvDevicePropPath(
									new EvDevicePath(eState.getAttributeValue("device")),
									eState.getAttributeValue("property"));
							group.propsToInclude.add(dev);
							}

					}
				}
			
			public void savePersonalConfig(Element e)
				{
				
				for(Map.Entry<String,EvHardwareConfigGroup> ge:groups.entrySet())
					{
					Element eGroup=new Element("group");
					eGroup.setAttribute("name", ge.getKey());
					EvHardwareConfigGroup group=ge.getValue();
					
					//Properties to include
					Element eInc=new Element("include");
					for(EvDevicePropPath ve:group.propsToInclude)
						{
						Element eSetting=new Element("setting");
						eSetting.setAttribute("device", ve.device.toString());
						eSetting.setAttribute("property", ve.property);
						eInc.addContent(eSetting);
						}
					eGroup.addContent(eInc);

					//Config groups
					for(Map.Entry<String, State> se:group.states.entrySet())
						{
						Element eState=new Element("state");
						eState.setAttribute("name", se.getKey());

						State state=se.getValue();
						for(Map.Entry<EvDevicePropPath,String> ve:state.propMap.entrySet())
							{
							Element eSetting=new Element("setting");
							eSetting.setAttribute("device", ve.getKey().device.toString());
							eSetting.setAttribute("property", ve.getKey().property);
							eSetting.setAttribute("value", ve.getValue());
							eState.addContent(eSetting);
							}
						eGroup.addContent(eState);
						}
					e.addContent(eGroup);
					}
				
				}
			});
			
		}
	
	}
