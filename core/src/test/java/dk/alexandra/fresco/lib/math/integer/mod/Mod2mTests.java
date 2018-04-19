package dk.alexandra.fresco.lib.math.integer.mod;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class Mod2mTests {

  public static class TestMod2mBaseCase<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        private Pair<List<BigInteger>, List<BigInteger>> getExpecteds(int m, int k) {
          BigInteger two = new BigInteger("2");
          List<BigInteger> inputs = new ArrayList<>();
          inputs.add(BigInteger.ONE);
          inputs.add(two.pow(78));
          inputs.add(two.pow(k - 2).add(new BigInteger("3")));
          inputs.add(new BigInteger("3573894"));
          inputs.add(new BigInteger("-1"));

          List<BigInteger> outputs = new ArrayList<>();
          outputs.add(BigInteger.ONE);
          outputs.add(BigInteger.ZERO);
          outputs.add(new BigInteger("3"));
          outputs.add(new BigInteger("3573894"));
          outputs.add(two.pow(m).add(new BigInteger("-1")));
          return new Pair<List<BigInteger>, List<BigInteger>>(
              inputs, outputs);
        }

        @Override
        public void test() {
          int m = 32;
          int k = 64;
          int kappa = 40;
          Pair<List<BigInteger>, List<BigInteger>> expecteds = getExpecteds(
              m, k);
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            // Make input list into list of differed, known, shared integers
            List<DRes<SInt>> inputs = expecteds.getFirst().stream().map(
                input -> builder.numeric().known(
                input)).collect(Collectors.toList());
            // Apply mod2m to each of the inputs, open the result
            List<DRes<BigInteger>> results = inputs.stream().map(
                input -> builder.numeric().open(builder.seq(
                new Mod2m(input, m, k, kappa)))).collect(Collectors.toList());
            return () -> results.stream().map(DRes::out).collect(Collectors
                .toList());
          };
          List<BigInteger> actuals = runApplication(app);
          Assert.assertArrayEquals(expecteds.getSecond().toArray(), actuals
              .toArray());
        }
      };
    }
  }

}
