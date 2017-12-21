package dk.alexandra.fresco.suite.tinytables;

import dk.alexandra.fresco.suite.tinytables.ot.datatypes.OTInput;
import java.security.InvalidParameterException;
import org.junit.Test;

public class TestOT {

  @Test(expected=InvalidParameterException.class)
  public void testOTInputFaultyConstructor() {
    new OTInput(new boolean[] {true}, new boolean[] {true, true});
  }
}
