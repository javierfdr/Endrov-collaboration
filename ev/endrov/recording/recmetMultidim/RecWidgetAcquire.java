/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetMultidim;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.EvComboObject;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.recording.EvAcquisition;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;


/**
 * Widget for recording settings: Acquire button
 * @author Johan Henriksson
 *
 */
public abstract class RecWidgetAcquire extends JPanel implements ActionListener, EvAcquisition.AcquisitionListener
	{
	private static final long serialVersionUID = 1L;

	//TODO where to save it down?
	//TODO color cameras?
	//TODO autoshutter
	//TODO autofocus, have it with position settings?
	
	private JTextField tStoreName=new JTextField("im");
	private JButton bStartStop=new JImageButton(BasicIcon.iconButtonRecord,"Start acquisition");
	private JLabel labelStatus=new JLabel(" ");
	private EvAcquisition.AcquisitionThread thread;

	private EvComboObject objectCombo=new EvComboObject(new LinkedList<EvObject>(), true, false)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return cont instanceof EvContainer;
			}
		};
	
	public RecWidgetAcquire()
		{
		setBorder(BorderFactory.createTitledBorder("Acquire"));
		setLayout(new GridLayout(1,1));
		add(
				EvSwingUtil.layoutCompactVertical(
						EvSwingUtil.layoutTableCompactWide(
								new JLabel("Store in: "), objectCombo,
								new JLabel("Name: "), tStoreName, 
								new JLabel("Status: "), labelStatus),
								bStartStop
				)
		);

		getAcquisition().addListener(this);
		bStartStop.addActionListener(this);
		}

	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bStartStop)
			{
			if(thread!=null)
				{
				thread.stopAcquisition();
				}
			else
				{
				if(objectCombo.getSelectedObject()==null)
					BasicWindow.showErrorDialog("Need to select a place to store the acquisition (e.g. File -> New)");
				else
					{
					EvAcquisition acq=getAcquisition();
					acq.setStoreLocation(objectCombo.getSelectedObject(), tStoreName.getText());
					getAcquisitionSettings();
					thread=acq.startAcquisition();
					}

				bStartStop.setIcon(BasicIcon.iconPlayStop);
				}
			
			}
		}
	
	public void acqStopped()
		{
		SwingUtilities.invokeLater(new Runnable(){
		public void run()
			{
			bStartStop.setIcon(BasicIcon.iconButtonRecord);
			thread=null;
			labelStatus.setText(" ");
			}
		});
		}
	
	public void newAcquisitionStatus(final String s)
		{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
				{
				labelStatus.setText(s);
				}
		});
		}
	
	/**
	 * Get acquisition object. Must be the same every time
	 */
	public abstract EvAcquisition getAcquisition();
	
	/**
	 * Store settings in acquisition object. Return if successful
	 */
	public abstract boolean getAcquisitionSettings();

	public void setStoreName(String string)
		{
		tStoreName.setText(string);
		}

	public void dataChangedEvent()
		{
		objectCombo.updateList();
		}

	
	
	}
