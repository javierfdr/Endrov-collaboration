package endrov.flow;

import java.util.Map;

import endrov.imageset.EvChannel;
import endrov.imageset.EvIOImage;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;
import endrov.util.Memoize;

/**
 * Image operation defined by operation on stacks
 * @author Johan Henriksson
 *
 */
public abstract class EvOpStack extends EvOpGeneral
	{
	//By necessity, stack operators have to deal with laziness manually.
	//Example: avgZ only computes one slice and then duplicates it. other operands compute entire
	//stack. cannot fit together. possible to make functions beneath this.
	//public abstract EvStack[] exec(EvStack... p);
	
	public EvPixels[] exec(EvPixels... p)
		{
		//TODO only one pixel supported
		//TODO where is lazyness? where is events?
		EvImage im=new EvImage();
		im.setPixelsReference(p[0]);
		EvStack stack[]=new EvStack[]{new EvStack()};
		stack[0].put(EvDecimal.ZERO, im);
		stack=exec(stack);
		EvPixels[] ret=new EvPixels[stack.length];
		for(int ac=0;ac<ret.length;ac++)
			ret[ac]=stack[ac].get(EvDecimal.ZERO).getPixels();
		return ret;
		}
	
	
	

	public EvChannel[] exec(EvChannel... ch)
		{
		System.out.println("here1");
		return applyStackOp(ch, this);
		}
	
	public EvPixels exec1(EvPixels... p)
		{
		return exec(p)[0];
		}

	public EvStack exec1(EvStack... p)
		{
		return exec(p)[0];
		}
	
	public EvChannel exec1(EvChannel... ch)
		{
		System.out.println("#### "+exec(ch)[0]);
		return exec(ch)[0];
		}

	
	/**
	 * Lazily create a channel using an operator that combines input channels.
	 * 
	 * Should ONLY be used for EvOpStack and EvOpStack1 
	 * 
	 */
	static EvChannel[] applyStackOp(EvChannel[] ch, EvOpGeneral op)
		{
		//System.out.println("here3 ");

		//Not quite final: what if changes should go back into the channel? how?
		EvChannel[] retch=new EvChannel[op.getNumberChannels()];
		
		//First argument decides which frames to apply for
		EvChannel refChannel=ch[0];
		
		//System.out.println("#chan "+retch.length+"  zzz "+refChannel.imageLoader);
		for(int ac=0;ac<retch.length;ac++)
			{
			EvChannel newch=new EvChannel();
			
			//How to combine channels? if A & B, B not exist, make B black?
			
			//Currently operates on common subset of channels
			
			for(Map.Entry<EvDecimal, EvStack> se:refChannel.imageLoader.entrySet())
				{
				EvStack newstack=new EvStack();
				EvStack stack=se.getValue();
				
				//TODO register lazy operation
				
				final EvStack[] imlist=new EvStack[ch.length];
				int ci=0;
				for(EvChannel cit:ch)
					{
					imlist[ci]=cit.imageLoader.get(se.getKey());
					ci++;
					}
				
				final Memoize<EvStack[]> ms=new MemoizeExec(imlist, op);
				
				
				//TODO without lazy stacks, prior stacks are forced to be evaluated.
				//only fix is if the laziness is added directly at the source.
				
				final int thisAc=ac;
				for(Map.Entry<EvDecimal, EvImage> pe:stack.entrySet())
					{
					EvImage newim=new EvImage();
					newstack.put(pe.getKey(), newim);
					
					newstack.getMetaFrom(stack); //This design makes it impossible to generate resolution lazily
					
					final EvDecimal z=pe.getKey();
						
					newim.io=new EvIOImage(){public EvPixels loadJavaImage(){return ms.get()[thisAc].get(z).getPixels();}};
					
					newim.registerLazyOp(ms);		
							
					}
				newch.imageLoader.put(se.getKey(), newstack);
				}
			System.out.println("here2 "+newch);
			retch[ac]=newch;
			}
		return retch;
		}
	
	
	private static class MemoizeExec extends Memoize<EvStack[]>
		{
		private EvStack[] imlist;
		private EvOpGeneral op;
		
		public MemoizeExec(EvStack[] imlist, EvOpGeneral op)
			{
			this.imlist = imlist;
			this.op = op;
			}

		@Override
		protected EvStack[] eval()
			{
			EvStack[] ret=op.exec(imlist);
			//Help GC. There might be space leaks without this.
			op=null;
			imlist=null;
			return ret;
			}
	
		}
	
	}