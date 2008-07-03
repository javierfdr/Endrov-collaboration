package endrov.filterMorphological;
import endrov.ev.PluginDef;
import endrov.filterMorphological.BinMorph2DFilter;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Morphological filters";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return true;
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
		return new Class[]{
				BinMorph2DFilter.class
				};
		}
	}