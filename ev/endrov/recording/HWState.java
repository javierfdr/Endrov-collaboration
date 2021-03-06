/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import java.util.*;

import endrov.hardware.Device;

public interface HWState extends Device, HWMagnifier
	{

	public List<String> getStateNames();

	public int getCurrentState();
	public String getCurrentStateLabel();
	
	public void setCurrentState(int state);
	public void setCurrentStateLabel(String label);

	}
