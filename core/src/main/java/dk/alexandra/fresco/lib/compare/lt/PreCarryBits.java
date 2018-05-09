package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreCarryBits implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<SIntPair>>> pairsDef;

  PreCarryBits(DRes<List<DRes<SIntPair>>> pairs) {
    this.pairsDef = pairs;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> pairsDef)
        .whileLoop((pairs) -> pairs.size() > 1,
            (prevSeq, pairs) -> prevSeq.par(par -> {
              // TODO this will reverse the actual list, not just the view. more efficient to only reverse the view
              Collections.reverse(pairs);
              padIfUneven(pairs);
              List<DRes<SIntPair>> nextRoundInner = new ArrayList<>(pairs.size() / 2);
              for (int i = 0; i < pairs.size() / 2; i++) {
                DRes<SIntPair> left = pairs.get(2 * i + 1);
                DRes<SIntPair> right = pairs.get(2 * i);
                nextRoundInner.add(par.seq(new CarryHelper(left, right)));
              }
              Collections.reverse(nextRoundInner);
              return () -> nextRoundInner;
            })).seq((ignored, out) -> out.get(0).out().getSecond());
  }

  /**
   * Pad with dummy null element if number of pairs is uneven.
   */
  private void padIfUneven(List<DRes<SIntPair>> pairs) {
    int size = pairs.size();
    if (size % 2 != 0 && size != 1) {
      pairs.add(null);
    }
  }

}
