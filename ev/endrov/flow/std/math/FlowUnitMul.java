package endrov.flow.std.math;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.EvChannel;
import endrov.unsortedImageFilters.imageMath.ImageMulImageOp;
import endrov.unsortedImageFilters.imageMath.ImageMulScalarOp;

/**
 * Flow unit: *
 * @author Johan Henriksson
 *
 */
public class FlowUnitMul extends FlowUnitMathBinop
	{
	private static final String metaType="mul";
	
	public FlowUnitMul()
		{
		super("A*B",metaType);
		}
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","*",metaType,FlowUnitMul.class, null,"Multiply numbers"));
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		/*
		if(a instanceof Double)
			lastOutput.put("C", ((Double)a)*toDouble(b));
		else
			throw new Exception("Unsupported numerical type "+a.getClass());
		*/
		
		
		
		if(a==null || b==null)
			{
			throw new BadTypeFlowException("Null values "+a+" "+b);
			}
		else if(a instanceof Number && b instanceof Number)
			{
			lastOutput.put("C", NumberMath.mul((Number)a, (Number)b));
			}
		else if(a instanceof EvChannel && b instanceof Number)
			{
			EvChannel ch=new ImageMulScalarOp((Number)b).exec((EvChannel)a);
			lastOutput.put("C", ch);
			}
		else if(b instanceof EvChannel && a instanceof Number)
			{
			EvChannel ch=new ImageMulScalarOp((Number)a).exec((EvChannel)b);
			lastOutput.put("C", ch);
			}
		else if(a instanceof EvChannel && b instanceof EvChannel)
			{
			EvChannel ch=new ImageMulImageOp().exec((EvChannel)a,(EvChannel)b);
			lastOutput.put("C", ch);
			}
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());

		
		
		
		}

	
	}
