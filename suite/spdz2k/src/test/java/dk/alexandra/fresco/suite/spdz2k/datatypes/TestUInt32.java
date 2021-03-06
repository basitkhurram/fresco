package dk.alexandra.fresco.suite.spdz2k.datatypes;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

public class TestUInt32 {

  private final UInt32 left = new UInt32(42);
  private final UInt32 right = new UInt32(123);

  @Test
  public void testAdd() {
    Assert.assertEquals(42 + 123, left.add(right).toInt());
  }

  @Test
  public void multiply() {
    Assert.assertEquals(42 * 123, left.multiply(right).toInt());
  }

  @Test
  public void subtract() {
    Assert.assertEquals(42 - 123, left.subtract(right).toInt());
  }

  @Test
  public void negate() {
    Assert.assertEquals(-42, left.negateUInt().toInt());
  }

  @Test
  public void isZero() {
    Assert.assertFalse(left.isZero());
    Assert.assertTrue(new UInt32(0).isZero());
  }

  @Test
  public void getBitLength() {
    Assert.assertEquals(32, left.getBitLength());
  }

  @Test
  public void toByteArray() {
    byte[] expected = new byte[left.getBitLength() / Byte.SIZE];
    expected[expected.length - 1] = 42;
    Assert.assertArrayEquals(expected, left.toByteArray());
  }

  @Test
  public void toBigInteger() {
    Assert.assertEquals(BigInteger.valueOf(42), left.toBigInteger());
  }

  @Test
  public void toLong() {
    Assert.assertEquals(42, left.toLong());
    Assert.assertEquals(123, right.toLong());
  }

  @Test
  public void toInt() {
    Assert.assertEquals(42, left.toInt());
    Assert.assertEquals(123, right.toInt());
  }

  @Test
  public void testToString() {
    Assert.assertEquals("42", left.toString());
    Assert.assertEquals("123", right.toString());
  }

}
