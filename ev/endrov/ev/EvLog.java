/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ev;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;


/**
 * Receiver of message and log events
 * 
 * @author Johan Henriksson
 */
public abstract class EvLog
	{
	/******************************************************************************************************
	 *                               Interface                                                            *
	 *****************************************************************************************************/

	public abstract void listenDebug(String s);
	public abstract void listenError(String s, Exception e);
	public abstract void listenLog(String s);
	
	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/

	public static HashSet<EvLog> listeners=new HashSet<EvLog>();

	/**
	 * Keep track of what has happen in memory in case one wants to look at it aposteriori
	 */
	public static MemoryLog memoryLog=new MemoryLog();
	static
	{
	listeners.add(memoryLog);
	}
	
	/**
	 * Log debugging information
	 */
	public static void printDebug(String s)
		{
		for(EvLog l:listeners)
			l.listenDebug(s);
		}

	/**
	 * Log an error
	 * @param s Human readable description, may be null
	 * @param e Low-level error, may be null
	 */
	public static void printError(String s, Exception e)
		{
		for(EvLog l:listeners)
			l.listenError(s,e);
		}

	public static void printError(Exception e)
		{
		printError(null,e);
		}
	
	
	/**
	 * Log normal/expected message
	 */
	public static void printLog(String s)
		{
		for(EvLog l:listeners)
			l.listenLog(s);
		}

	
	/**
	 * Take an exception and print the error to a string
	 */
	public static String logPrintString(Exception e)
		{
		StringWriter sw=new StringWriter();
		PrintWriter s2=new PrintWriter(sw);
		e.printStackTrace(s2);
		s2.flush();
		return sw.toString();
		}
	}
