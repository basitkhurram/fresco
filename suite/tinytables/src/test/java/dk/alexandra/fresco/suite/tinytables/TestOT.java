package dk.alexandra.fresco.suite.tinytables;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.tinytables.ot.Encoding;
import dk.alexandra.fresco.suite.tinytables.ot.base.NetworkWrapper;
import dk.alexandra.fresco.suite.tinytables.ot.datatypes.OTInput;

import static org.junit.Assert.assertEquals;

import java.security.InvalidParameterException;
import org.junit.Test;

public class TestOT {

  @Test(expected=InvalidParameterException.class)
  public void testOTInputFaultyConstructor() {
    new OTInput(new boolean[] {true}, new boolean[] {true, true});
  }

  @Test
  public void testConstructionOfEncoding() {
    // Check that we can construct an Encoding object
    new Encoding();
  }

  @Test
  public void testClosebility() {
    Network dummyNet = new Network() {

      @Override
      public void send(int partyId, byte[] data) {
      }

      @Override
      public byte[] receive(int partyId) {
        return new byte[1];
      }

      @Override
      public int getNoOfParties() {
        return 1;
      }
    };
    NetworkWrapper wrapper = new NetworkWrapper(dummyNet, 0);
    // Check that the network is not closed by default
    assertEquals(false, wrapper.isClosed());
    // Check that {@code close} can be called
    wrapper.close();
  }
}
