/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.imserv;

import java.rmi.Remote;
import java.util.Date;
import java.util.Map;

import bioserv.ClientSessionIF;


/**
 * Server "imserv" object: interface
 * 
 * @author Johan Henriksson
 */
public interface ImservIF extends Remote
	{
	public ClientSessionIF auth(String user, String pass) throws Exception;
  
  public String[] getDataKeys(String filter) throws Exception;
  public String[] getDataKeys() throws Exception;
  
  public DataIF getData(String name) throws Exception;
  
  public Map<String,DataIF> getDataMap() throws Exception;
  
  
  public String[] getTags() throws Exception;
  
  //polling is bad. is there a better way to call in the opposite dir?
  //call & stall?
  public Date getLastUpdate() throws Exception;
	}
