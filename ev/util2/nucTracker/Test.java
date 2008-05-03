package util2.nucTracker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

import evplugin.data.EvObject;
import evplugin.ev.EV;
import evplugin.ev.Log;
import evplugin.ev.StdoutLog;
import evplugin.imageset.EvImage;
import evplugin.imageset.Imageset;
import evplugin.imagesetOST.OstImageset;
import evplugin.nuc.NucLineage;

public class Test
	{
	public static NucLineage getLin(Imageset ost)
		{
		for(EvObject evob:ost.metaObject.values())
			{
			if(evob instanceof NucLineage)
				{
				NucLineage lin=(NucLineage)evob;
				return lin;
				}
			}
		return null;
		}
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		int startFrame=1500;
		int endFrame=2000;
		
		//Load all worms
		String[] wnlist={
				"/Volumes/TBU_main02/ost4dgood/TB2164_080118",
//				"/Volumes/TBU_main02/ost4dgood/N2_071114",
				}; 
		Vector<Imageset> worms=new Vector<Imageset>();
		for(String s:wnlist)
			{
			Imageset ost=new OstImageset(s);
			if(getLin(ost)!=null)
				worms.add(ost);
			}

//		String channelName="DIC";
		String channelName="RFP";

		AdaBoost adaboost=new AdaBoost();

		//7% false pos. choose better.
		adaboost.addClassifier(1.0767747569167745, new SimpleClassifier(
				0.9302935235468791, 1.0,
				new SimpleClassifier.FeatureRect(0.10399528641411032, 0.10399528641411032, 0.8960047135858897, 0.8960047135858897),
				new SimpleClassifier.FeatureRect(0.0, 0.0, 1.0, 1.0)));
/*		adaboost.addClassifier(0.3841017621992794, new SimpleClassifier(
				0.9611703154893632, 1.0,
				new SimpleClassifier.FeatureRect(0.1462173607284372, 0.1462173607284372, 0.8537826392715628, 0.8537826392715628),
				new SimpleClassifier.FeatureRect(0.0, 0.0, 1.0, 1.0)));*/
		
		/*
		1.0767747569167745: {0.9302935235468791 1.0
		0.10399528641411032 0.10399528641411032 0.8960047135858897 0.8960047135858897
		0.0 0.0 1.0 1.0}
		0.3841017621992794: {0.9611703154893632 1.0
		0.1462173607284372 0.1462173607284372 0.8537826392715628 0.8537826392715628
		0.0 0.0 1.0 1.0}*/

		//For all imagesets
		for(Imageset ost:worms)
			{
			NucLineage lin=getLin(ost);

			for(int frame:ost.getChannel(channelName).imageLoader.keySet())
				{
				if(frame<startFrame || frame>endFrame)
//				if(frame!=1202)
					continue;
//				Map<NucPair, NucLineage.NucInterp> inter=lin.getInterpNuc(frame);


				for(int z:ost.getChannel(channelName).imageLoader.get(frame).keySet())
					{
//					if(z!=46)
//						continue;
					System.out.println("frame "+frame+ " z "+z);
					
					
					EvImage im=ost.getChannel(channelName).getImageLoader(frame, z);
					BufferedImage jim=im.getJavaImage();
					TImage tim=new TImage();
					tim.createCumIm(jim);
					tim.valueY=1;

					try
						{
						ImageIO.write(jim,"png",
								new File("/Volumes/TBU_main03/userdata/henriksson/traintrack/"+channelName+"/test.png"));
						}
					catch (IOException e)
						{
						// TODO Auto-generated catch block
						e.printStackTrace();
						}

					//Scan image
					for(int wsize=16;wsize<40;wsize+=1)
						{
						int jump=wsize/5;
						if(jump<3)
							jump=3;
						jump=1;
//						System.out.println("wsize "+wsize);

						for(int x=0;x<jim.getWidth()-wsize;x+=jump)
							for(int y=0;y<jim.getHeight()-wsize;y+=jump)
								{
								if(adaboost.eval(tim, wsize, x, y)==1)
									{

									System.out.println("candidate z"+z+" wsize "+wsize+" "+x+" "+y);
									
									
									}
								
								}


						}


					}

				break;

				}





			}





		}




	
	}