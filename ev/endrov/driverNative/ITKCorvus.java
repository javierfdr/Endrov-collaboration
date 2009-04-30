package endrov.driverNative;

import java.util.SortedMap;
import java.util.TreeMap;

import endrov.hardware.Hardware;
import endrov.hardware.HardwareProvider;
import endrov.hardware.PropertyType;

public class ITKCorvus extends HardwareProvider implements Hardware
	{
	public final static String newLine="\r\n";  //NO
	
	
	public HWSerial serial=null;
	
	
	public OlympusIX()
		{
		hw.put("shutter1", new DevShutter(1));
		hw.put("shutter2", new DevShutter(2));
		hw.put("MU", new DevMirrorUnit());
		hw.put("Objective", new DevObjective());
		hw.put("Condenser", new DevCondenser());
		hw.put("LampSource", new DevLampSource());
		hw.put("LampIntensity", new DevLampIntensity());
		}
	
	public synchronized void sendCommand(String cmd)
		{
		if(serial!=null)
			serial.writePort(cmd+newLine);
		}
	
	public synchronized String queryCommand(String cmd)
		{
		serial.writePort(cmd+newLine);
		String s=serial==null ? "\r\n" : serial.readUntilTerminal("\r\n");
		s=s.substring(cmd.length());
		s=s.substring(0,s.length()-2);
		System.out.println("#"+s+"#");
		return s; //which?
		//return "123";
//		return s;
		}
	
	/** Shutter */
	public class DevShutter extends BasicNativeCachingStateDevice implements HWShutter
		{
		final int shutterNum;
		public DevShutter(int shutterNum)
			{
			this.shutterNum=shutterNum;
			System.out.println("---------------create shutter "+this.shutterNum);
			}
		public DevShutter()
			{
			this(1);
			}
		public String getDescName(){return "IX shutter";}
		public int getCurrentStateHW()
			{
			System.out.println("shutternum "+shutterNum);
			return queryCommand("1SHUT"+shutterNum+"?").equals("IN") ? 1 : 0;
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1SHUT"+shutterNum+" "+(state!=0?"IN":"OUT"));
			//
			}
		}

	/** Prism */
	public class DevPrism extends BasicNativeCachingStateDevice 
		{
		public DevPrism(){super(1,2);}
		public String getDescName(){return "IX prism";}
		public int getCurrentStateHW()
			{
			return Integer.parseInt(queryCommand("1PRISM?"));
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1PRISM "+state);
			//
			}
		}
	
	
	/** Mirror unit */
	public class DevMirrorUnit extends BasicNativeCachingStateDevice 
		{
		public DevMirrorUnit(){super(1,5);}
		public String getDescName(){return "IX mirror unit";}
		public int getCurrentStateHW()
			{
			return Integer.parseInt(queryCommand("1MU?"));
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1MU "+state);
			//
			}
		}
	
	/** Objective */
	public class DevObjective extends BasicNativeCachingStateDevice 
		{
		public DevObjective(){super(1,5);}
		public String getDescName(){return "IX objective";}
		public int getCurrentStateHW()
			{
			return Integer.parseInt(queryCommand("1OB?"));
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1OB "+state);
			//
			}
		}
	
	/** Condenser */
	public class DevCondenser extends BasicNativeCachingStateDevice 
		{
		public DevCondenser(){super(1,5);}
		public String getDescName(){return "IX condenser";}
		public int getCurrentStateHW()
			{
			return Integer.parseInt(queryCommand("1CD?"));
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1CD "+state);
			//
			}
		}
	
	
	/** Lamp source */
	public class DevLampSource extends BasicNativeCachingStateDevice 
		{
		public DevLampSource(){super(new int[]{0,1},new String[]{"DIA","EPI"});}
		public String getDescName(){return "IX lamp source";}
		public int getCurrentStateHW()
			{
			return queryCommand("1LMPSEL?").equals("DIA")?0:1;
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1LMPSEL "+state);
			//
			}
		}
	
	/** Lamp intensity */
	public class DevLampIntensity extends BasicNativeCachingStateDevice 
		{
		public DevLampIntensity()
			{
			//TODO no idea about range
			super(0,10);
			}
		public String getDescName(){return "IX lamp source";}
		public int getCurrentStateHW()
			{
			return Integer.parseInt(queryCommand("1LMP?"));
			//TODO what about intermediate state?
			}
		public void setCurrentStateHW(int state)
			{
			sendCommand("1LMP "+state);
			//
			}
		}
	
	
	

	public Set<Hardware> autodetect()
		{
		return null;
		}

	public void getConfig(Element root)
		{
		}

	public List<String> provides()
		{
		return null;
		}
	public Hardware newProvided(String s)
		{
		return null; //TODO
		}

	public void setConfig(Element root)
		{
		}

	public String getDescName()
		{
		return "ITK Corvus";
		}

	public SortedMap<String, String> getPropertyMap()
		{
		return new TreeMap<String, String>();
		}

	public SortedMap<String, PropertyType> getPropertyTypes()
		{
		return new TreeMap<String, PropertyType>();
		}

	public String getPropertyValue(String prop)
		{
		return null;
		}

	public Boolean getPropertyValueBoolean(String prop)
		{
		return null;
		}

	public void setPropertyValue(String prop, boolean value)
		{
		}

	public void setPropertyValue(String prop, String value)
		{
		}
	
	}