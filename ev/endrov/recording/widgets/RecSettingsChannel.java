/**
 * 
 */
package endrov.recording.widgets;

import java.util.LinkedList;
import java.util.List;

import endrov.util.EvDecimal;

public class RecSettingsChannel
{
public static class OneChannel
	{
	public String name;
	public EvDecimal exposure;
	public boolean lightCompensate;
	public int zInc, z0, tinc, averaging;
	}

public String metaStateGroup;
public List<RecSettingsChannel.OneChannel> channels=new LinkedList<RecSettingsChannel.OneChannel>();
}