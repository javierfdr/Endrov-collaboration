package util2.brian;

import java.io.*;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.TreeMap;

import bioserv.seqserv.io.GFF;
import bioserv.seqserv.io.HMMER;
import bioserv.seqserv.io.GFF.Entry;


//GFF: directionality

public class Krai2
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{

		try
			{
			System.out.println(BrianSQL.connectPostgres("//193.11.32.108/tbu", "postgres", "wuermli"));
			
			
			System.out.println("Reading HMMER");
			HMMER h=new HMMER(new File("/home/tbudev3/bioinfo/krai/pfam.out"));
			System.out.println("#entries: "+h.entry.size());
			/*
			System.out.println("Reading GFF: CDS");
			GFF cds=new GFF(new File("/home/bioinfo/incdata/celegans/gff2"),new GFF.EntryFilter(){
			public boolean keep(Entry e)
				{
				e.attributes="";
				e.source="";
				return e.feature.equals(GFF.featureCDS) && e.start<e.end;
				}
			});
			System.out.println("#entries: "+cds.entry.size());
			*/
			
			
			
			
			
			
			System.out.println("Reading GFF: exons");
			GFF gff=new GFF(new File("/home/bioinfo/incdata/celegans/gff2"),new GFF.EntryFilter(){
				public boolean keep(Entry e)
					{
					e.attributes="";
					e.source="";
					return e.feature.equals(GFF.featureEXON) /*|| e.feature.equals(GFF.featureINTRON)*/;
					}
			});
			System.out.println("#entries: "+gff.entry.size());
			
			//Here assuming no overlaps of exons. Then this can be done in O(nlogn + n) time
			System.out.println("Sorting HMMER");
			TreeMap<Integer, HMMER.Entry> sortedH=new TreeMap<Integer, HMMER.Entry>();
			for(HMMER.Entry he:h.entry)
				sortedH.put(he.start, he);
			System.out.println("Sorting GFF");
			TreeMap<Integer, GFF.Entry> sortedGFF=new TreeMap<Integer, GFF.Entry>();
			for(GFF.Entry ge:gff.entry)
				sortedGFF.put(ge.start, ge);
			
			
			System.out.println("Filtering by exons");
			Iterator<GFF.Entry> itG=sortedGFF.values().iterator();
			GFF.Entry curG=itG.next();
			totalFor: for(HMMER.Entry curH:sortedH.values())
				{
				while(curG.end<curH.start)
					if(!itG.hasNext())
						break totalFor;
					else
						curG=itG.next();
				//Now curG.end>=curH.start
				if(!(curH.start>curG.end || curH.end<curG.start))
					{
					//System.out.println("Removing "+curH);
					h.entry.remove(curH);
					}
				}
			System.out.println("#entries after filter: "+h.entry.size());

			
			hmmerToSQL(h);
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		}
	
	public static void hmmerToSQL(HMMER h) throws Exception
		{
		System.out.println("Uploading to sql");
		BrianSQL.runUpdate("delete from krai1");
		PreparedStatement ps=BrianSQL.conn.prepareStatement("insert into krai1 values(?,?,?,?,?)");
		for(HMMER.Entry e:h.entry)
			{
			ps.setString(1, e.seqName);
			ps.setInt(2, e.start);
			ps.setInt(3, e.end);
			ps.setString(4, e.chromSeq);
			ps.setDouble(5, e.Evalue);
			ps.execute();
			}
		}

	}
