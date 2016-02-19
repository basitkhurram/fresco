package dk.alexandra.fresco.lib.compare;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.ComparisonProtocolBuilder;

public class SortingProtocolBuilder extends ComparisonProtocolBuilder {

	public SortingProtocolBuilder(ComparisonProtocolFactory comProvider, BasicNumericFactory bnf) {
		super(comProvider, bnf);
		// TODO Auto-generated constructor stub
	}

	public SInt isSorted(SInt[] values){
		//first compare the values pairwise
		SInt[] comparisons = new SInt[values.length-1];
		//initialize comparisons array	
		for (int i=0;i<comparisons.length;i++)
			comparisons[i]=getBnf().getSInt();
		//build parallel comparison circuit
		beginParScope();  
		//TODO maybe split this in chunks of reasonable sizes
		for (int i=0;i<comparisons.length;i++) 
			comparisons[i]=compare(values[i], values[i+1]);
		endCurScope();
		//then multiply the results sequentially.
		//set result to 1
		SInt result=getBnf().getSInt(); 
		append(getBnf().getSInt(1,result));
		//multiply by values from comparison
		for (int i=0;i<comparisons.length;i++) 
			append(getBnf().getMultCircuit(result, comparisons[i], result));
		return result;
	}


	private static int FloorLog2(int value)
	{
		int result = -1;
		for (int i = 1; i < value; i <<= 1, ++result);
		return result;
	}

	final OInt minusOne = getBnf().getOInt(BigInteger.valueOf(-1L));
	
	public void compareAndSwap(int a,int b, SInt[] values){
		 //Non splitting version
		 
		
		//Reporter.info(a+","+b);
		beginSeqScope();
		SInt c = getBnf().getSInt();
		SInt d = getBnf().getSInt();
		SInt comparison=compare(values[a],values[b]);
		
		
		//a = comparison*a+(1-comparison)*b ==> comparison*(a-b)+b
		//b = comparison*b+(1-comparison)*a ==>  -comparison*(a-b)+a

		append(getBnf().getSubtractCircuit(values[a], values[b], c));
		append(getBnf().getMultCircuit(comparison, c, c));
		append(getBnf().getMultCircuit(minusOne, c, d));
		
		beginParScope();
		append(getBnf().getAddCircuit(c, values[b], c));
		append(getBnf().getAddCircuit(d, values[a], d));		
		endCurScope();

		values[a]=c;
		values[b]=d;
		endCurScope();
	}

	
	/*public void alternativeCompareAndSwap(int a,int b, SInt[] values){
		 a comparisonswap uses: 
		 * 1 comparision
		 * 2 multiplications
		 * 2 substractions
		 * 2 additions.
		 
		
		//Reporter.info(a+","+b);
		beginSeqScope();
		SInt c = getBnf().getSInt();
		SInt d = getBnf().getSInt();
		SInt comparison=compare(values[a],values[b]);

		beginParScope();
		//a = comparison*a+(1-comparison)*b ==> comparison*(a-b)+b
		beginSeqScope();
		append(getBnf().getSubtractCircuit(values[a], values[b], c));
		append(getBnf().getMultCircuit(comparison, c, c));
		append(getBnf().getAddCircuit(c, values[b], c));
		endCurScope();
		//b = comparison*b+(1-comparison)*a ==>  comparison*(b-a)+a
		beginSeqScope();
		append(getBnf().getSubtractCircuit(values[b], values[a], d));
		append(getBnf().getMultCircuit(comparison, d, d));
		append(getBnf().getAddCircuit(d, values[a], d));		
		endCurScope();
		endCurScope();

		values[a]=c;
		values[b]=d;
		endCurScope();
	}*/
	
	public void sort(SInt[] values){
		//sort using Batcher´s Merge Exchange 	

		int t = FloorLog2(values.length);
		int p0 = (1 << t);
		int p = p0;
	
		do
		{
			int q = p0;
			int r = 0;
			int d = p;
		
			while (r == 0 || q != p)
			{
				//Reporter.info("--");
				beginParScope();

				if (r != 0)
				{
					d = q - p;
					q >>= 1;
				}

				for (int i = 0; i < values.length-d; i++)
				{

					if ((i & p) == r) 
						compareAndSwap(i,i+d, values);		
				}
				r = p;
				endCurScope();
			}
			p >>= 1;
					
		}	
		while (p > 0);
	}
	
}
