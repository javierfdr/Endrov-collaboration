package endrov.flowMeasure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import endrov.imageset.EvStack;

/**
 * Measure: sum (integral) of intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureSumIntensity implements ParticleMeasure.MeasurePropertyType 
	{
	private static String propertyName="sumI";

	public void analyze(EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info)
		{
		HashMap<Integer,Double> sum=new HashMap<Integer, Double>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		for(int az=0;az<stackValue.getDepth();az++)
			{
			double[] arrValue=stackValue.getInt(az).getPixels().convertToDouble(true).getArrayDouble();
			int[] arrID=stackValue.getInt(az).getPixels().convertToInt(true).getArrayInt();
			
			for(int i=0;i<arrValue.length;i++)
				{
				double v=arrValue[i];
				int id=arrID[i];
	
				//TODO should 0 be ignored? sounds good.
				//Can know that it should be ignored by scanning which IDs there are first, and then exclude 0.
				//costs a bit more but convenience makes it worth it. no if needed here then.
//				if(m==0)
				if(id!=0)
					{
					Double lastValue=sum.get(id);
					if(lastValue==null)
						lastValue=0.0;
					sum.put(id, lastValue+v);
					}
				
				}
			
			
			}
		
		//Write into particles
		for(int id:sum.keySet())
			{
			HashMap<String, Object> p=info.getCreate(id);
			p.put(propertyName, sum.get(id));
			}
		}

	public String getDesc()
		{
		return "Sum intensity of any pixel";
		}

	public Set<String> getColumns()
		{
		return Collections.singleton(propertyName);
		}

	
	
	
	}