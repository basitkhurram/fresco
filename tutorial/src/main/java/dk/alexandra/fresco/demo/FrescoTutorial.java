package dk.alexandra.fresco.demo;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class FrescoTutorial {

  public static <T> Matrix<T> toMatrix(T[][] rows) {
    int h = rows.length;
    int w = rows[0].length;
    ArrayList<ArrayList<T>> mat = new ArrayList<>();
    for (T[] row : rows) {
      mat.add(new ArrayList<>(Arrays.asList(row)));
    }
    return new Matrix<>(h, w, mat);
  }

  public static Matrix<BigInteger> getInputMatrix(int numRows, int numCols) {
    ArrayList<ArrayList<BigInteger>> mat = new ArrayList<>();
    int counter = 0;
    for (int r = 0; r < numRows; r++) {
      ArrayList<BigInteger> row = new ArrayList<>();
      for (int c = 0; c < numCols; c++) {
        row.add(BigInteger.valueOf(counter++));
      }
      mat.add(row);
    }
    return new Matrix<>(numRows, numCols, mat);
  }

  public static <ResourcePoolT extends ResourcePool> void runAggApp(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce, ResourcePoolT rp)
      throws IOException {
    // input
    BigInteger[][] rows = {{BigInteger.valueOf(1), BigInteger.valueOf(2)},
        {BigInteger.valueOf(1), BigInteger.valueOf(4)},
        {BigInteger.valueOf(1), BigInteger.valueOf(6)},
        {BigInteger.valueOf(1), BigInteger.valueOf(8)},
        {BigInteger.valueOf(2), BigInteger.valueOf(10)},
        {BigInteger.valueOf(2), BigInteger.valueOf(12)},
        {BigInteger.valueOf(2), BigInteger.valueOf(14)},
        {BigInteger.valueOf(2), BigInteger.valueOf(16)}};
    Matrix<BigInteger> inputMatrix = toMatrix(rows);
    // define application
    Application<Matrix<BigInteger>, ProtocolBuilderNumeric> app = root -> {
      DRes<Matrix<DRes<SInt>>> closedMatrix = null;
      if (root.getBasicNumericContext().getMyId() == 1) {
        // assume input is from party 1
        closedMatrix = root.collections().closeMatrix(inputMatrix, 1);
      } else {
        // other parties need to provide matrix dimensions
        closedMatrix = root.collections().closeMatrix(8, 2, 1);
      }
      // shuffle
      DRes<Matrix<DRes<SInt>>> shuffled = root.collections().shuffle(closedMatrix);
      // aggregate
      DRes<Matrix<DRes<SInt>>> aggregated = root.collections().leakyAggregateSum(shuffled, 0, 1);
      // open
      DRes<Matrix<DRes<BigInteger>>> opened = root.collections().openMatrix(aggregated);
      // once result is ready, "unwrap" deferred results and return
      return () -> new MatrixUtils().unwrapMatrix(opened);
    };
    // connect to other parties
    rp.getNetwork().connect(10000);
    // run application and retrieve result
    Matrix<BigInteger> result = sce.runApplication(app, rp);
    System.out.println("Result is: " + result);
    // shutdown
    sce.shutdownSCE();
    rp.getNetwork().close();
  }

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
      // input phase
      DRes<SInt> inputPartyOne = null;
      DRes<SInt> inputPartyTwo = null;
      if (root.getBasicNumericContext().getMyId() == 1) {
        // player one provides first input and receives second input
        inputPartyOne = root.numeric().input(myInput, 1);
        inputPartyTwo = root.numeric().input(null, 2);
      } else {
        // player receives first input and provides second input
        inputPartyOne = root.numeric().input(null, 1);
        inputPartyTwo = root.numeric().input(myInput, 2);
      }
      // sum
      DRes<SInt> sum = root.numeric().add(inputPartyOne, inputPartyTwo);
      // prod
      DRes<SInt> prod = root.numeric().mult(sum, sum);
      // open result to all parties
      return root.numeric().open(prod);
    };
    // connect to other parties
    rp.getNetwork().connect(10000);
    // run application and retrieve result
    BigInteger result = sce.runApplication(app, rp);
    System.out.println("Result is: " + result);
    // shutdown
    sce.shutdownSCE();
    rp.getNetwork().close();
  }

  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> util = new CmdLineUtil<>();
    util.parse(args);

    // create protocol suite that will execute the application
    ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> psConf = util.getProtocolSuite();

    // application engine
    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(psConf, util.getEvaluator());

    // resource pool contains network
    ResourcePoolT resourcePool = util.getResourcePool();
    runSumApp(sce, resourcePool);
    // runAggApp(sce, resourcePool);
  }

}
