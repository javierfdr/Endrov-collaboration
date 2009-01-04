package endrov.basicWindow;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;


import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.util.JImageButton;

/**
 * Object combo. with children disabled it also works as a EvData selector.
 * New objects can be created as well.
 * @author Johan Henriksson
 *
 */
public abstract class EvComboObject extends JPanel implements ActionListener
	{
	static final long serialVersionUID=0;
	private boolean showChildren;
	private boolean allowNoSelection;
	private List<ActionListener> aListeners=new LinkedList<ActionListener>();
	private EvContainer root;
	
	//If channels were objects then this component could be used for that as well
	//add blobID to EvObject? this would fix the rename issue
	
	private JComboBox combo=new JComboBox();
	
	public EvComboObject(List<EvObject> creators, boolean showChildren, boolean allowNoSelection)
		{
		this.showChildren=showChildren;
		this.allowNoSelection=allowNoSelection;
		
		//TODO listen on object data changes
		//TODO allow delete as well?
		
		JPanel pCreate=new JPanel(new GridLayout(1,creators.size()));
		for(EvObject ob:creators)
			{
			JImageButton b=new JImageButton(BasicIcon.iconMenuNew,"Create new "+ob.getMetaTypeDesc());
			pCreate.add(b);
			System.out.println("to implement");
			
			
			/*
			 * 					CustomObject ob=new CustomObject();
					ob.loadMetadata(new Element(type));
					meta.addMetaObject(ob);
					BasicWindow.updateWindows();

			 */
			//TODO
			}

		updateList();
		
		setLayout(new BorderLayout());
		add(combo,BorderLayout.CENTER);
		add(pCreate,BorderLayout.WEST); //which is best? east? chance of miss-clicking but better grouping
		}

	public void setRoot(EvContainer c)
		{
		root=c;
		updateList();
		}
	public EvContainer getRoot()
		{
		return root;
		}
	
	
	//Empty item. This will allow comparison by pointer
	private ComboItem emptyItem=new ComboItem(new LinkedList<String>(),null,null);
	
	/**
	 * One entry in the combo box. The object it points to should only be null
	 * if this is the empty object.
	 */
	private static class ComboItem
		{
		//another path interface in HW
		String path[];
 		private WeakReference<EvContainer> ob; 
		private WeakReference<EvData> data; 

		public EvContainer getObject()
			{
			return ob.get();
			}
		public EvData getData()
			{
			return data.get();
			}
		
		public ComboItem(List<String> path, EvContainer ob, EvData data)
			{
			this.path=path.toArray(new String[0]);
			this.ob=new WeakReference<EvContainer>(ob);
			this.data=new WeakReference<EvData>(data);
//			System.out.println("new, path: "+path);
			}
		
		public String toString()
			{
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<path.length;i++)
				{
				sb.append(path[i]);
				if(i!=path.length-1)
					sb.append("/");
				}
			return sb.toString();
			}
		
		
		
		}
	
	/**
	 * Update list of objects
	 */
	public void updateList()
		{
		//Remember selection
		ComboItem currentItem=(ComboItem)combo.getSelectedItem();
		EvContainer currentCont=currentItem==null ? null : currentItem.getObject();
		
		combo.removeActionListener(this);
		combo.removeAllItems();

		if(allowNoSelection)
			combo.addItem(emptyItem);

		//Build list. Depending on if there is a root it will be used or all data will be listed
		if(root==null)
			{
			//List EvData
			for(EvData data:EvData.metadata)
				{
				LinkedList<String> paths=new LinkedList<String>();
				paths.add(data.getMetadataName());
				if(includeObject(data))
					combo.addItem(new ComboItem(paths,data,data));
				if(showChildren)
					updateListRec(data, paths, data);
				}
			}
		else
			{
			LinkedList<String> paths=new LinkedList<String>();
			if(root instanceof EvData)
				updateListRec(root, paths, (EvData)root);
			else
				updateListRec(root, paths, null);
			}
		
		if(combo.getItemCount()==0)
			combo.addItem(emptyItem);

		//If null-selection not allowed then reselect any item in the list
		if(currentItem==null || (currentItem==emptyItem && !allowNoSelection))
			currentCont=((ComboItem)combo.getItemAt(0)).getObject();

		//Reselect last item
/*		Map<EvContainer,ComboItem> itemMap=new HashMap<EvContainer, ComboItem>();
		for(int ci=0;ci<combo.getItemCount();ci++)
			{
			ComboItem item=(ComboItem)combo.getItemAt(ci);
			itemMap.put(item.con,item);
//			System.out.println("has item @ "+item.con+" - "+item);
			}
		combo.setSelectedItem(itemMap.get(currentCont));*/
		setSelectedObject(currentCont);
		
	//	System.out.println("selected "+combo.getSelectedIndex());
	//	System.out.println("sesected2 "+combo.getSelectedItem());
		combo.addActionListener(this);
		}

	
	public abstract boolean includeObject(EvContainer cont);
	
	private void updateListRec(EvContainer root, LinkedList<String> contPath, EvData data)
		{
		for(Map.Entry<String, EvObject> entry:root.metaObject.entrySet())
			{
			EvContainer thisCont=entry.getValue();
			contPath.addLast(entry.getKey());
		//	System.out.println("Checking to include "+thisCont+ " "+includeObject(thisCont));
			if(includeObject(thisCont))
				combo.addItem(new ComboItem(contPath, thisCont,data));
			if(showChildren)
				updateListRec(thisCont, contPath, data);
			contPath.removeLast();
			}
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		emitListener();
		}
	
	
	public void addActionListener(ActionListener list)
		{
		aListeners.add(list);
		}
	
	private void emitListener()
		{
		for(ActionListener a:aListeners)
			a.actionPerformed(new ActionEvent(this,0,""));
		}
	
	
	/**
	 * Get a pointer directly to the EvData container of the selected object or null
	 * @return Object or null
	 */
	public EvData getData()
		{
		if(root==null)
			{
			ComboItem ci=(ComboItem)combo.getSelectedItem();
			return ci.getData();
			}
		else
			return null;
		}
	
	/**
	 * Return currently selected object or null
	 */
	public EvContainer getSelectedObject()
		{
		ComboItem ci=(ComboItem)combo.getSelectedItem();
		return ci.getObject();
		}
	
	public void setSelectedObject(EvContainer c)
		{
		Map<EvContainer,ComboItem> itemMap=new HashMap<EvContainer, ComboItem>();
		for(int ci=0;ci<combo.getItemCount();ci++)
			{
			ComboItem item=(ComboItem)combo.getItemAt(ci);
			itemMap.put(item.getObject(),item);
			}
		combo.setSelectedItem(itemMap.get(c));
		//TODO should this emit an event?
		}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	//use case 1: just list Datas.
	//use case 2: list objects for all data. want Data, Path to object, Object
	//(use case 3: as a channel selector)
	//use case 3: list objects for one data. data is the root

	//extended object can be parameterized. get method without cast can then be introduced
	
	
	}