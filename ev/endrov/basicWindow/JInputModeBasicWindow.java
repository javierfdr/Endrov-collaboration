/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.basicWindow;

import endrov.keyBinding.JInputManager;
import endrov.keyBinding.JInputMode;
import endrov.keyBinding.JinputListener;

/**
 * Send input to the currently active BasicWindow
 * 
 * @author Johan Henriksson
 *
 */
public class JInputModeBasicWindow implements JInputMode
	{
	public static BasicWindow getWindow()
		{
		return BasicWindow.windowManager.getFocusWindow();
		}
	
	public void bindAxisPerformed(JInputManager.EvJinputStatus status)
		{
		BasicWindow w=getWindow();
		if(w!=null)
			{
			for(JinputListener listener:w.jinputListeners.keySet())
				listener.bindAxisPerformed(status);
			}
		}
	
	
	public void bindKeyPerformed(JInputManager.EvJinputButtonEvent e)
		{
		BasicWindow w=getWindow();
		if(w!=null)
			{
			for(JinputListener listener:w.jinputListeners.keySet())
				listener.bindKeyPerformed(e);
			}
		}

	
	
	}
