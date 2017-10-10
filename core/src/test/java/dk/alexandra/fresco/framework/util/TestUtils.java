package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestUtils {

  @Test
  public void testBasic() {
    long beforeTime = System.nanoTime();
    Timing timing = new Timing();
    timing.start();
    long whenStartedTime = System.nanoTime();
    try{
      Thread.sleep(200);
    } catch(Exception ignored) {
    }
    long beforeStoppedTime = System.nanoTime();
    long stopped = timing.stop();
    long afterTime = System.nanoTime();
    Assert.assertTrue(timing.getTimeInNanos() <= afterTime-beforeTime);
    Assert.assertTrue(timing.getTimeInNanos() >= beforeStoppedTime-whenStartedTime);
    Assert.assertThat(stopped, Is.is(timing.getTimeInNanos()));
  }

  @Test
  public void testToHex() {
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{false, false, false, false, false, false, false, false}), Is.is("00"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true, true, true, true, true}), Is.is("ff"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true, true, true, true}), Is.is("7f"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true, true, true}), Is.is("3f"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true, true}), Is.is("1f"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true}), Is.is("0f"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{false, true, true, true, true, true, true, true, true,}), Is.is("00ff"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true, true, true, true, true, true,}), Is.is("01ff"));
  }
  
  @Test
  public void testPair() {
    String s1 = "test";
    Boolean b1 = new Boolean(true);
    String s2 = "test2";
    Boolean b2 = new Boolean(false);
    Pair<String, Boolean> p1 = new Pair<>(s1, b1);    
    Pair<String, Boolean> p2 = new Pair<>(s1, b2);
    Pair<String, Boolean> p3 = new Pair<>(s2, b1);
    Assert.assertTrue(p1.equals(p1));
    Assert.assertTrue(p1.hashCode() == p1.hashCode());
    Assert.assertFalse(p1.equals(p2));
    Assert.assertFalse(p1.equals(p3));
    Assert.assertNotNull(p1.toString());
  }
  
  @Test
  public void testBinaryMatrix() {
    BinaryMatrix m1 = new BinaryMatrix(2, 2);
    BinaryMatrix m2 = new BinaryMatrix(2, 2);
    Assert.assertFalse(m1.get(0, 0));
    Assert.assertFalse(m1.getColumn(0).get(0));
    Assert.assertFalse(m1.getRow(0).get(0));
    Assert.assertTrue(m1.equals(m2));
    m1.clearColumn(0);
    Assert.assertTrue(m1.getHeight() == 2);
    Assert.assertTrue(m1.getWidth() == 2);
    
    m2.set(0, 0, true);
    m1.add(m2);
    Assert.assertTrue(m1.get(0, 0));
    m1 = m1.multiply(m2);
    m1 = m1.transpose();
    Assert.assertTrue(m1.get(0, 0));
    BinaryMatrix cs = m1.getColumns(new int[] {0});
    Assert.assertTrue(cs.get(0, 0));
    Assert.assertFalse(cs.get(1, 0));
    
    try {
      cs.get(0, 1);      
      fail("Does not have two columns");
    } catch (IndexOutOfBoundsException e) {
      
    }
    
    //create with byte arr
    byte[] bytes = new byte[] {0x00, 0x02, 0x00, 0x02, 0x03};
    BinaryMatrix m3 = new BinaryMatrix(bytes);
    Assert.assertEquals(true, m3.get(0, 0));
    Assert.assertEquals(true, m3.get(1, 0));
    Assert.assertEquals(false, m3.get(0, 1));
    Assert.assertEquals(false, m3.get(1, 1));
    
    BinaryMatrix m4 = new BinaryMatrix(m3.toByteArray());
    Assert.assertEquals(m3, m4);
    
    BitSet c1 = m4.getColumn(0);
    BitSet c2 = m4.getColumn(1);    
    List<BitSet> cols = new ArrayList<>();
    cols.add(c1);
    cols.add(c2);
    BinaryMatrix m5 = BinaryMatrix.fromColumns(cols, 2);
    Assert.assertEquals(m3, m5);
    
    BinaryMatrix m6 = BinaryMatrix.outerProduct(2, 2, c1, c1);
    Assert.assertEquals(2, m6.getHeight());
    Assert.assertEquals(2, m6.getWidth());
    
    BitSet multRes = m6.multiply(c1);
    Assert.assertEquals(BitSet.valueOf(new byte[] {0x00}), multRes);
    
    BinaryMatrix random = BinaryMatrix.getRandomMatrix(5, 5, new Random(0));
    //we know the values Random seeded with 0 will yield, so we can verify this.
    Assert.assertNotNull(random.toString());
    Assert.assertEquals(true, random.get(0, 0));
    Assert.assertEquals(true, random.get(1, 1));
    Assert.assertEquals(false, random.get(2, 2));
    Assert.assertEquals(true, random.get(3, 3));
    Assert.assertEquals(false, random.get(4, 4));
  }
  
  @Test
  public void testBitSetUtils() {
    BitSet bs = BitSetUtils.getRandomBits(4, new Random(0));
    BitSet bsShift = BitSetUtils.shiftRight(bs, 2);
    bsShift = BitSetUtils.shiftLeft(bsShift, 2);
    Assert.assertEquals(bs, bsShift);
    boolean[] arr = BitSetUtils.toArray(bs, 4);
    List<Boolean> list = BitSetUtils.toList(bs, 4);
    Boolean[] list_arr = list.toArray(new Boolean[0]);
    boolean[] list_arr_prim = new boolean[list_arr.length];
    for(int i = 0; i < list_arr.length; i++) {
      list_arr_prim[i] = list_arr[i];
    }
    Assert.assertArrayEquals(arr, list_arr_prim);
  }
  
  @Test
  public void testDetermSecureRandDefaultConstructor() {
    byte[] seed = new byte[] {0x02, 0x10, 0x05};
    DetermSecureRandom rand1 = new DetermSecureRandom();
    rand1.setSeed(seed);
    
    
    DetermSecureRandom rand2 = new DetermSecureRandom();
    rand2.setSeed(seed);
    
    byte[] bytes1 = new byte[10];
    byte[] bytes2 = new byte[10];
    
    rand1.nextBytes(bytes1);
    rand2.nextBytes(bytes2);
    
    Assert.assertArrayEquals(bytes1, bytes2);
  }
  
  @Test
  public void testDetermSecureRandAmountSet() {
    byte[] seed = new byte[] {0x02, 0x10, 0x05};
    DetermSecureRandom rand1 = null;
    try {
      rand1 = new DetermSecureRandom(65);
      fail("Should not be possible to create");
    } catch(IllegalArgumentException e) {
      rand1 = new DetermSecureRandom(32);
    }
    rand1.setSeed(seed);
    
    
    DetermSecureRandom rand2 = new DetermSecureRandom(32);
    rand2.setSeed(seed);
    
    byte[] bytes1 = new byte[10];
    byte[] bytes2 = new byte[10];
    
    rand1.nextBytes(bytes1);
    rand2.nextBytes(bytes2);
    System.out.println(Arrays.toString(bytes1));
    
    Assert.assertArrayEquals(bytes1, bytes2);
  }
  
  @Test
  public void testBitVector() {
    BitVector vec1 = new BitVector(1);
    vec1.set(0, true);
    BitVector vec2 = new BitVector(new boolean[] {true});
    vec1.xor(vec2);    
    Assert.assertFalse(vec1.get(0));
    BitVector vec3 = new BitVector(new byte[] {0x02}, 2);
    Assert.assertTrue(vec3.get(1));
    Assert.assertFalse(vec3.get(0));
  }
  
  @Test
  public void testByteArithmetic() {
    byte x = 1;
    byte[] y = new byte[] {1};
    byte[] res = new byte[1];
    ByteArithmetic.mult(x, y, res);
    Assert.assertArrayEquals(new byte[] {1}, res);
    x = ByteArithmetic.xor(x, x);    
    ByteArithmetic.mult(x, y, res);
    Assert.assertArrayEquals(new byte[] {0}, res);
    BitSet bs = ByteArithmetic.intToBitSet(9);
    byte[] byteRep = bs.toByteArray();
    Assert.assertArrayEquals(new byte[] {0x09}, byteRep);
    
    x = 1;
    Assert.assertEquals((byte)0, ByteArithmetic.not(x));
  }
  
/*
 * TODO  @Test
 
  public void testFormatting(){
    String format = Timing.formatNanosAsMilliSeconds(200);
    Assert.assertThat(format, Is.is("2.0E-4 ms"));
  }
 */
}

