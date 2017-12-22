package dk.alexandra.fresco.suite.tinytables;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesANDProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesCloseProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesNOTProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesOpenToAllProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesXORProtocol;

public class TestProtocol {
  private Drbg rand;
  private Network network;
  private ResourcePoolImpl resources;
  private TinyTablesSBool one;
  private TinyTablesSBool two;
  private TinyTablesSBool three;

  @Before
  public void setup() {
    rand = new AesCtrDrbg(new byte[32]);
    resources = new ResourcePoolImpl(0, 2, rand);
    network = new Network() {

      @Override
      public void send(int partyId, byte[] data) {
      }

      @Override
      public byte[] receive(int partyId) {
        return new byte[1];
      }

      @Override
      public int getNoOfParties() {
        return 0;
      }
    };
    one = new TinyTablesSBool(new TinyTablesElement(false));
    two = new TinyTablesSBool(new TinyTablesElement(true));
    three = new TinyTablesSBool(new TinyTablesElement(true));
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testOutputOfXORProtocol() {
    TinyTablesXORProtocol prot = new TinyTablesXORProtocol(one, two, three);
    // Output of the protocol should be XOR of the inputs, which should be true as one=false and
    // two=true
    assertTrue(((TinyTablesSBool) prot.out()).getValue().getShare());
  }

  /**** NEGATIVE TESTS. ****/
  @Test(expected = MPCException.class)
  public void testAddIllegalRound() {
    TinyTablesANDProtocol prot = new TinyTablesANDProtocol(0, one, two);
    // There is only supposed to be 2 rounds, counting from 0
    prot.evaluate(3, resources, network);
  }

  @Test(expected = MPCException.class)
  public void testCloseIllegalRound() {
    TinyTablesCloseProtocol prot = new TinyTablesCloseProtocol(0, 1, false);
    // There is only supposed to be 2 rounds, counting from 0
    prot.evaluate(3, resources, network);
  }

  @Test(expected = MPCException.class)
  public void testNOTIllegalRound() {
    TinyTablesNOTProtocol prot = new TinyTablesNOTProtocol(one, two);
    // There is only supposed to be 1 rounds, counting from 0
    prot.evaluate(1, resources, network);
  }

  @Test(expected = MPCException.class)
  public void testOpenToAllIllegalRound() {
    TinyTablesOpenToAllProtocol prot = new TinyTablesOpenToAllProtocol(0, one);
    // There is only supposed to be 2 rounds, counting from 0
    prot.evaluate(2, resources, network);
  }

  @Test(expected = MPCException.class)
  public void testXORIllegalRound() {
    TinyTablesXORProtocol prot = new TinyTablesXORProtocol(one, two, three);
    // There is only supposed to be 1 round, counting from 0
    prot.evaluate(1, resources, network);
  }
}
