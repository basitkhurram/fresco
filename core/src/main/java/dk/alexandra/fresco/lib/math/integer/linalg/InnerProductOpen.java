package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class InnerProductOpen implements ComputationBuilder<SInt, ProtocolBuilderNumeric> {

  private final List<BigInteger> aVector;
  private final List<Computation<SInt>> bVector;

  public InnerProductOpen(List<BigInteger> aVector,
      List<Computation<SInt>> bVector) {
    this.aVector = aVector;
    this.bVector = bVector;
  }

  @Override
  public Computation<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder
        .par(parallel -> {
          List<Computation<SInt>> result = new ArrayList<>(aVector.size());
          NumericBuilder numericBuilder = parallel.numeric();
          for (int i = 0; i < aVector.size(); i++) {
            BigInteger nextA = aVector.get(i);
            Computation<SInt> nextB = bVector.get(i);
            result.add(numericBuilder.mult(nextA, nextB));
          }
          return () -> result;
        })
        .seq((seq, list) -> seq.advancedNumeric().sum(list)
        );
  }
}