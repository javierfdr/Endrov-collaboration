package evplugin.imagesetBioformats;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.data.*;
import evplugin.script.*;
import evplugin.imageset.*;

/**
 * Open Bioformats
 * @author Johan Henriksson
 */
public class CmdDBIO extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Exp e=arg.get(1);
		if(e instanceof ExpVal)
			{
			ExpVal v=(ExpVal)e;

			
			Imageset rec=new BioformatsImageset(v.stringValue());
			EvData.metadata.add(rec);
			
			BasicWindow.updateWindows();
			
			//TODO: print error?
			
			return new ExpVal(rec);
			}
		else
			throw new Exception("Incompatible type");
		}
	
	}
