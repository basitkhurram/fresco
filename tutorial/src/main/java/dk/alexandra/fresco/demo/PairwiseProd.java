package dk.alexandra.fresco.demo;

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

public class PairwiseProd implements ComputationParallel<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<SInt>>> left;
  private final DRes<List<DRes<SInt>>> right;

  public PairwiseProd(DRes<List<DRes<SInt>>> left, DRes<List<DRes<SInt>>> right) {
    super();
    this.left = left;
    this.right = right;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> lout = left.out();
    List<DRes<SInt>> rout = right.out();
    List<DRes<SInt>> products = new ArrayList<>(lout.size());
    Numeric numericBuilder = builder.numeric();
    for (int i = 0; i < lout.size(); i++) {
      DRes<SInt> nextA = lout.get(i);
      DRes<SInt> nextB = rout.get(i);
      products.add(numericBuilder.mult(nextA, nextB));
    }
    return () -> products;
  }
}
