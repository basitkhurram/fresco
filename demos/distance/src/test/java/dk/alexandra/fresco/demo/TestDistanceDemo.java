package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;

public class TestDistanceDemo {

  private static void runTest(TestThreadFactory test, int n) {
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<Integer>(n);
    for (int i=1; i<=n; i++) {
      ports.add(9000 + i*10);
    }
    Map<Integer, NetworkConfiguration> netConf = TestConfiguration.getNetworkConfigurations(n, ports, Level.FINE);
    Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
    for (int i : netConf.keySet()) {
      TestThreadConfiguration ttc = new TestThreadConfiguration();
      ttc.netConf = netConf.get(i);         
      ProtocolSuiteConfiguration suite = new SpdzConfiguration() {

        @Override
        public ProtocolSuite createProtocolSuite(int myPlayerId) {
          return new SpdzProtocolSuite(this);
        }

        @Override
        public PreprocessingStrategy getPreprocessingStrategy() {
          return PreprocessingStrategy.DUMMY;
        }

        @Override
        public String fuelStationBaseUrl() {
          return null;
        }

        @Override
        public int getMaxBitLength() {
          return 150;
        }

        @Override
        public SpdzResourcePool createResourcePool(int myId, int size, Network network, Random rand,
            SecureRandom secRand) {
          return new SpdzResourcePoolImpl(myId, size, network, null, rand,
              secRand, this);
        }
      };            
      ttc.sceConf = new TestSCEConfiguration(suite, NetworkingStrategy.KRYONET, new SequentialEvaluator(), netConf.get(i), new InMemoryStorage(), false);
      conf.put(i, ttc);
    }
    TestThreadRunner.run(test, conf);

  }

  @Test
  public void testDistance() throws Exception {
    final TestThreadFactory f = new TestThreadFactory() {
      @Override
      public TestThread next(TestThreadConfiguration conf) {
        return new TestThread() {
          @Override
          public void test() throws Exception {     
            int x, y;
            if(conf.getMyId() == 1) {
              x = 10;
              y = 10;
            } else {
              x = 20;
              y = 15;
            }
            System.out.println("Running with x: "+x+", y: "+y);
            DistanceDemo distDemo = new DistanceDemo(conf.getMyId(), x, y);
            secureComputationEngine.runApplication(distDemo, SecureComputationEngineImpl.createResourcePool(conf.sceConf, conf.sceConf.getSuite()));
            double distance = distDemo.getOutput().out().doubleValue();
            distance = Math.sqrt(distance);
            Assert.assertEquals(11.1803, distance, 0.0001);
          }
        };
      };
    };      
    runTest(f, 2);
  }
}
