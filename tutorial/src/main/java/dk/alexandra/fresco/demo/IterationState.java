package dk.alexandra.fresco.demo;

import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public final class IterationState implements DRes<IterationState> {

  private final int round;
  private final DRes<List<DRes<SInt>>> intermediate;

  public IterationState(int round, DRes<List<DRes<SInt>>> intermediate) {
    this.round = round;
    this.intermediate = intermediate;
  }

  @Override
  public IterationState out() {
    return this;
  }

  public int getRound() {
    return round;
  }

  public DRes<List<DRes<SInt>>> getIntermediate() {
    return intermediate;
  }

}
