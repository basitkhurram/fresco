package dk.alexandra.fresco.demo;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class FrescoTutorial {

  public static Matrix<BigInteger> toMatrix(int[][] rows) {
    int h = rows.length;
    int w = rows[0].length;
    ArrayList<ArrayList<BigInteger>> mat = new ArrayList<>();
    for (int[] row : rows) {
      ArrayList<BigInteger> convertedRow =
          IntStream.range(0, w).mapToObj(idx -> BigInteger.valueOf(row[idx]))
              .collect(Collectors.toCollection(ArrayList::new));
      mat.add(new ArrayList<>(convertedRow));
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

  public static <ResourcePoolT extends ResourcePool> void runParallelMult(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce, ResourcePoolT rp)
      throws IOException {
    int listSize = 100000;
    List<BigInteger> input = IntStream.range(0, listSize).mapToObj(idx -> BigInteger.valueOf(idx))
        .collect(Collectors.toList());

    Application<List<BigInteger>, ProtocolBuilderNumeric> app = root -> {
      DRes<List<DRes<SInt>>> leftClosed = root.collections().closeList(input, 1);
      DRes<List<DRes<SInt>>> rightClosed = root.collections().closeList(input, 1);
      return root.par(par -> {
        List<DRes<SInt>> lout = leftClosed.out();
        List<DRes<SInt>> rout = rightClosed.out();
        List<DRes<SInt>> products = new ArrayList<>(lout.size());
        Numeric numericBuilder = par.numeric();
        for (int i = 0; i < lout.size(); i++) {
          DRes<SInt> nextA = lout.get(i);
          DRes<SInt> nextB = rout.get(i);
          products.add(numericBuilder.mult(nextA, nextB));
        }
        return () -> products;
      }).par((subpar, prods) -> {
        DRes<List<DRes<BigInteger>>> opened = subpar.collections().openList(() -> prods);
        return () -> opened.out().stream().map(el -> el.out()).collect(Collectors.toList());
      });
    };

    // connect to other parties
    rp.getNetwork().connect(10000);
    // run application and retrieve result
    List<BigInteger> result = sce.runApplication(app, rp);
    System.out.println("Result is: " + result.size());
    // shutdown
    sce.shutdownSCE();
    rp.getNetwork().close();
  }

  public static <ResourcePoolT extends ResourcePool> void runAggApp(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce, ResourcePoolT rp)
      throws IOException {
    // input
    int[][] rows = {
        {1, 2}, 
        {2, 4}, 
        {1, 6}, 
        {2, 8}, 
        {1, 10}, 
        {2, 12}, 
        {1, 14}, 
        {2, 16}
    };
    Matrix<BigInteger> inputMatrix = toMatrix(rows);
    // define application (also works as a lambda expression)
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

  public static <ResourcePoolT extends ResourcePool> void runReactiveApp(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce, ResourcePoolT rp)
      throws IOException {
    Application<BigInteger, ProtocolBuilderNumeric> app = new ReactiveApp();
    // connect to other parties
    rp.getNetwork().connect(10000);
    // run application and retrieve result
    BigInteger result = sce.runApplication(app, rp);
    System.out.println("Result is: " + result);
    // shutdown
    sce.shutdownSCE();
    rp.getNetwork().close();
  }

  public static <ResourcePoolT extends ResourcePool> void runSumAndSquareApp(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce, ResourcePoolT rp)
      throws IOException {
    /*
     * Here we define our application. An application is parameterized with a return type
     * (BigInteger) and a builder type (ProtocolBuilderNumeric).
     * 
     * An application can be defined as a concrete class or as a lambda expression.
     * 
     * Fresco will instantiate a new ProtocolBuilder of the type provided (ProtocolBuilderNumeric)
     * and pass that to the buildComputation method of the Application.
     * 
     */
    Application<BigInteger, ProtocolBuilderNumeric> app = new SumAndSquareApp();
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
    // parse command line configuration
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> util = new CmdLineUtil<>();
    util.parse(args);

    // create protocol suite that will execute the application
    ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> psConf = util.getProtocolSuite();

    // application engine
    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(psConf, util.getEvaluator());

    // resource pool contains network
    ResourcePoolT resourcePool = util.getResourcePool();
//    runSumAndSquareApp(sce, resourcePool);
    runReactiveApp(sce, resourcePool);
  }

}
