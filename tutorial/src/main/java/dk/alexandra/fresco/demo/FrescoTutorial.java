package dk.alexandra.fresco.demo;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class FrescoTutorial {

  public static <ResourcePoolT extends ResourcePool> void runSumApp(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce, ResourcePoolT rp)
      throws IOException {
    // get some user input
    Scanner reader = new Scanner(System.in);
    System.out.println("Enter a number: ");
    final BigInteger myInput = BigInteger.valueOf(reader.nextInt());
    reader.close();
    System.out.println("Will run as party " + rp.getMyId() + " with input " + myInput);

    // define application
    Application<BigInteger, ProtocolBuilderNumeric> app = root -> {
      DRes<SInt> inputPartyOne = null;
      DRes<SInt> inputPartyTwo = null;
      if (root.getBasicNumericContext().getMyId() == 1) {
        inputPartyOne = root.numeric().input(myInput, 1);
        inputPartyTwo = root.numeric().input(null, 2);
      } else {
        inputPartyOne = root.numeric().input(null, 1);
        inputPartyTwo = root.numeric().input(myInput, 2);
      }
      // add up inputs
      DRes<SInt> sum = root.numeric().add(inputPartyOne, inputPartyTwo);
      return root.numeric().open(sum);
    };

    // connect to other parties
    rp.getNetwork().connect(10000);
    // run application and retrieve result
    BigInteger result = sce.runApplication(app, rp);
    System.out.println(result);
    // shutdown
    sce.shutdownSCE();
    rp.getNetwork().close();
  }

  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> util = new CmdLineUtil<>();
    util.parse(args);

    ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> psConf = util.getProtocolSuite();

    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(psConf, util.getEvaluator());

    ResourcePoolT resourcePool = util.getResourcePool();
    runSumApp(sce, resourcePool);
  }

}
