/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.adAlldab.oldOrTest;

import java.io.*;

public class BenchmarkRAFile
	{

	public static void main(String[] args)
		{
		
		
		String seq2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		StringBuffer seq=new StringBuffer();
		for(int i=0;i<10000;i++)
			seq.append(seq2);		
		//String sf=
			seq.toString();

	/*	
		try
			{
			FileWriter fstream = new FileWriter("genometest.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(sf);
			out.close();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
*/
		
		/*
		int numloop=1000000;
		int seqlen=10;
		int seqstart=100000;
		long starttime=System.currentTimeMillis();
		for(int i=0;i<numloop;i++)
			{
			sf.substring(seqstart, seqstart+seqlen);
			}
		long endtime=System.currentTimeMillis();

		System.out.println("t: "+(endtime-starttime));
		*/

		
		try
			{
			int numloop=1000000;
			int seqlen=10;
			int seqstart=100000;
			RandomAccessFile fp=new RandomAccessFile("genometest.txt","r");
			long starttime=System.currentTimeMillis();
			for(int i=0;i<numloop;i++)
				{
				byte[] b=new byte[seqlen];
				fp.seek(seqstart);
				fp.read(b);
				}
			long endtime=System.currentTimeMillis();

			System.out.println("t: "+(endtime-starttime));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}

		
		
		}

	}
