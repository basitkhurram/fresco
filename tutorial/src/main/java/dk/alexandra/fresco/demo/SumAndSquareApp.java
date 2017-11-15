package dk.alexandra.fresco.demo;

import java.math.BigInteger;
import java.util.Scanner;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/*
 * Here we define an example application. An application is parameterized with a return type
 * (BigInteger) and a builder type (ProtocolBuilderNumeric).
 * 
 * We could also define this as a lambda expression instead of a concrete class!
 * 
 */
public class SumAndSquareApp implements Application<BigInteger, ProtocolBuilderNumeric> {

  /*
   * FRESCO will instantiate a new ProtocolBuilder of the type provided (ProtocolBuilderNumeric) and
   * pass that to the buildComputation method of the Application.
   * 
   * Conceptually the builder is the FRESCO API. It holds all the Computations available in FRESCO
   * (in the arithmetic setting).
   * 
   * In this application two parties each input a number which we sum, square, and output.
   */
  @Override
  public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric builder) {
    // who am I?
    Integer myPartyId = builder.getBasicNumericContext().getMyId();

    // get input from user
    final BigInteger myInput = getUserInput(myPartyId);
    System.out.println("Will run as party " + myPartyId + " with input " + myInput);

    /*
     * The way to define new computations is by using a protocol builder. A protocol builder gives
     * us access to all of the computations available in FRESCO.
     * 
     * These are exposed via computation directories. A computation directory are conceptually like
     * Java packages. They contain related computations. The Numeric computation directory contains
     * basic arithmetic operators such as addition, the Collections computation directory contains
     * list and matrix operations etc
     */
    // we only need basic numeric functionality
    Numeric numericCompDir = builder.numeric();

    // each party provides its input to the MPC
    DRes<SInt> inputPartyOne = null;
    DRes<SInt> inputPartyTwo = null;
    if (myPartyId == 1) {
      // player one provides first input and receives second input
      inputPartyOne = numericCompDir.input(myInput, 1);
      inputPartyTwo = numericCompDir.input(null, 2);
    } else {
      // player receives first input and provides second input
      inputPartyOne = numericCompDir.input(null, 1);
      inputPartyTwo = numericCompDir.input(myInput, 2);
    }

    // sum
    DRes<SInt> sum = numericCompDir.add(inputPartyOne, inputPartyTwo);
    // prod
    DRes<SInt> prod = numericCompDir.mult(sum, sum);

    // open result to both parties
    return numericCompDir.open(prod);
  }

  private BigInteger getUserInput(Integer partyId) {
    // read some user input from console
    Scanner reader = new Scanner(System.in);
    System.out.println("Enter your input: ");
    final BigInteger myInput = BigInteger.valueOf(reader.nextInt());
    reader.close();
    return myInput;
  }

}
