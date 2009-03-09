package endrov.macBinding;
import endrov.ev.*;

public class PLUGIN extends PluginDef
	{
	public boolean isDefaultEnabled()
		{
		return true;
		}

	public String getPluginName()
		{
		return "Mac binding";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return EV.isMac();
		}
	
	public String cite()
		{
		return "";
		}
	
	public String[] requires()
		{
		return new String[]{};
		}
	
	public Class<?>[] getInitClasses()
		{
		try
			{
			return new Class[]{Class.forName("endrov.macBinding.OSXAdapter"),Class.forName("endrov.macBinding.EncodeQT")};
			}
		catch (ClassNotFoundException e)
			{
			e.printStackTrace();
			return new Class[]{};
			}
		//return new Class[]{OSXAdapter.class, EncodeQT.class};
		}
	
	
	}
