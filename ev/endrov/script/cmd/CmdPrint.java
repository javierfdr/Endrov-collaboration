package endrov.script.cmd;
import java.util.*;

import endrov.ev.*;
import endrov.script.*;

public class CmdPrint extends Command
	{
	public int numArg()	{return 1;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		Object e=Command.expVal(arg.get(1));
		Log.printLog(""+e);
		return null;
		}
	}