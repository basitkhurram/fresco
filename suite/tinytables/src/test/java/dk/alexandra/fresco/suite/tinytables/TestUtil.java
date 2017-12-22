package dk.alexandra.fresco.suite.tinytables;

import java.security.SecureRandom;

import org.junit.Test;

import dk.alexandra.fresco.suite.tinytables.ot.OTFactory;
import dk.alexandra.fresco.suite.tinytables.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.TinyTablesTripleGenerator;

public class TestUtil {

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalConstructor() {
    // The player ID must be either 1 or 2, here we test with 0
    new TinyTablesTripleGenerator(0, new SecureRandom(new byte[] { 0x42 }),
        new OTFactory() {

          @Override
          public OTSender createOTSender() {
            return null;
          }

          @Override
          public OTReceiver createOTReceiver() {
            return null;
          }
        });
  }
}
