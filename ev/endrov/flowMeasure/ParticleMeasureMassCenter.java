package endrov.flowMeasure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Vector3d;

import endrov.imageset.EvStack;

/**
 * Measure: center of mass
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureMassCenter implements ParticleMeasure.MeasurePropertyType 
	{
	private static String propertyName="massCenter";

	public void analyze(EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info)
		{
		//TODO should thickness be taken into account? world or pixel coordinates?
		
		
		HashMap<Integer,Vector3d> sum=new HashMap<Integer, Vector3d>();
		HashMap<Integer,Integer> vol=new HashMap<Integer, Integer>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		for(int az=0;az<stackValue.getDepth();az++)
			{
			double[] arrValue=stackValue.getInt(az).getPixels().convertToDouble(true).getArrayDouble();
			int[] arrID=stackValue.getInt(az).getPixels().convertToInt(true).getArrayInt();
			
			int w=stackValue.getWidth();
			int h=stackValue.getHeight();

			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					{
					int index=ay*w+ax;

					double v=arrValue[index];
					int id=arrID[index];
		
					if(id!=0)
						{
						Vector3d lastSum=sum.get(id);
						if(lastSum==null)
							sum.put(id,lastSum=new Vector3d());
						lastSum.add(new Vector3d(ax*v,ay*v,az*v));
						
						
						Integer lastVol=vol.get(id);
						if(lastVol==null)
							lastVol=0;
						vol.put(id, lastVol+1);
						}

					
					}
			
			}
		
		//Write into particles
		for(int id:sum.keySet())
			{
			HashMap<String, Object> p=info.getCreate(id);
			Vector3d s=sum.get(id);
			double v=vol.get(id);
			p.put(propertyName+"X", s.getX()/v);
			p.put(propertyName+"Y", s.getY()/v);
			p.put(propertyName+"Z", s.getZ()/v);
			}
		}

	public String getDesc()
		{
		return "Center of mass (takes intensity into account)";
		}

	public Set<String> getColumns()
		{
		HashSet<String> set=new HashSet<String>();
		set.add(propertyName+"X");
		set.add(propertyName+"Y");
		set.add(propertyName+"Z");
		return set;
		}

	
	
	
	}