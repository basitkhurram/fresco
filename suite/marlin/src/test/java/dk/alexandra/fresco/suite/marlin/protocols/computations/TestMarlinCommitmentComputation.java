package dk.alexandra.fresco.suite.marlin.protocols.computations;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.AbstractMarlinTest;
import dk.alexandra.fresco.suite.marlin.MarlinProtocolSuite128;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128Factory;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePoolImpl;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDummyDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStoreImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import org.junit.Test;

public class TestMarlinCommitmentComputation extends
    AbstractMarlinTest<MarlinResourcePool<CompUInt128>> {

  @Test
  public void testCommitmentTwo() {
    runTest(new TestTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testCommitmentThree() {
    runTest(new TestTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Override
  protected MarlinResourcePool<CompUInt128> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    CompUIntFactory<CompUInt128> factory = new CompUInt128Factory();
    MarlinResourcePool<CompUInt128> resourcePool =
        new MarlinResourcePoolImpl<>(
            playerId,
            noOfParties, null,
            new MarlinOpenedValueStoreImpl<>(),
            new MarlinDummyDataSupplier<>(playerId, noOfParties, factory.createRandom(), factory),
            factory);
    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected ProtocolSuiteNumeric<MarlinResourcePool<CompUInt128>> createProtocolSuite() {
    return new MarlinProtocolSuite128();
  }

  private static class TestTest<ResourcePoolT extends MarlinResourcePool<CompUInt128>>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          int noParties = conf.getResourcePool().getNoOfParties();
          List<byte[]> inputs = new ArrayList<>();
          Random random = new Random(42);
          for (int i = 1; i <= noParties; i++) {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            inputs.add(bytes);
          }
          Application<List<byte[]>, ProtocolBuilderNumeric> testApplication =
              root -> new MarlinCommitmentComputation(
                  conf.getResourcePool().getCommitmentSerializer(),
                  inputs.get(root.getBasicNumericContext().getMyId() - 1))
                  .buildComputation(root);
          List<byte[]> actual = runApplication(testApplication);
          assertEquals(inputs.size(), actual.size());
          for (int i = 0; i < actual.size(); i++) {
            assertArrayEquals(inputs.get(i), actual.get(i));
          }
        }
      };
    }
  }

}
