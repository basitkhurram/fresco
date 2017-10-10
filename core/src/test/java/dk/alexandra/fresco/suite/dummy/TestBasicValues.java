package dk.alexandra.fresco.suite.dummy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanSBool;
import java.math.BigInteger;
import org.junit.Test;

public class TestBasicValues {

  @Test
  public void testSBoolEquals() {
    DummyBooleanSBool b1 = new DummyBooleanSBool(true);
    DummyBooleanSBool b2 = new DummyBooleanSBool(false);
    DummyBooleanSBool b3 = new DummyBooleanSBool(true);
    
    assertFalse(b1.equals(b2));
    assertFalse(b1.equals(null));
    assertFalse(b1.equals(""));
    assertFalse(b1.equals(new DummyBooleanSBool()));
    assertTrue(b1.equals(b3));
    
    assertNotNull(b1.toString());
  }
  
  
  @Test
  public void testSIntEquals() {
    DummyArithmeticSInt b1 = new DummyArithmeticSInt(BigInteger.ONE);
    DummyArithmeticSInt b2 = new DummyArithmeticSInt(4);
    DummyArithmeticSInt b3 = new DummyArithmeticSInt(1);
    
    assertFalse(b1.equals(b2));
    assertFalse(b1.equals(null));
    assertFalse(b1.equals(""));
    assertTrue(b1.equals(b3));
    
    assertTrue(b1.hashCode() == b3.hashCode());
    assertNotNull(b1.toString());
  }
}
