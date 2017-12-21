package dk.alexandra.fresco.suite.tinytables.datatypes;

import dk.alexandra.fresco.framework.util.BitVector;
import java.io.Serializable;


/**
 * This class implements a storage optimised way of keeping TinyTablesElements. Here each is
 * represented only by it's share in a {@link BitVector}.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesElementVector implements Serializable {

  private static final long serialVersionUID = -2405648771000699453L;
  private BitVector values;

  public TinyTablesElementVector(byte[] shares, int size) {
    this.values = new BitVector(shares, size);
  }

  public TinyTablesElementVector(int size) {
    this.values = new BitVector(size);
  }

  public void setShare(int index, boolean share) {
    this.values.set(index, share);
  }

  public byte[] payload() {
    return this.values.asByteArr();
  }

  public int getSize() {
    return values.getSize();
  }

  public static BitVector open(TinyTablesElementVector... vectors) {
    BitVector values = new BitVector(vectors[0].getSize());
    for (TinyTablesElementVector vector : vectors) {
      values.xor(vector.values);
    }
    return values;
  }

}
