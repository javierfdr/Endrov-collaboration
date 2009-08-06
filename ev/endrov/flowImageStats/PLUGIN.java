package endrov.flowImageStats;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: filters";
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
				FlowUnitConvGaussian2D.class,
				FlowUnitConvGaussian3D.class,
				FlowUnitBilateralFilter2D.class,
				FlowUnitKuwaharaFilter.class,
				FlowUnitAverageRect.class,
				FlowUnitEntropyRect.class,
				FlowUnitSumRect.class,
				FlowUnitVarianceRect.class,FlowUnitVarianceCircle.class,
				FlowUnitPercentileRect.class,
				
		};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}