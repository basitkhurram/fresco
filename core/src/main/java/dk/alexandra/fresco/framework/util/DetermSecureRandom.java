/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.framework.util;

import dk.alexandra.fresco.framework.MPCException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Java's SecureRandom is not 'deterministic' in the sense that calls to setSeed
 * (at least using some crypto providers) only adds to the initial seed, that is
 * taken from system state when SecureRandom was created.
 * 
 * Often, in our protocols, we need a SecureRandom that can be seeded
 * 'determinitically', e.g. in the sense that two instances created with the
 * same seed yields the same sequence of random bytes.
 * 
 * Note that DetermSecureRandom is not threadsafe.
 * 
 */
public class DetermSecureRandom {

  private static final String ALGORITHM = "SHA-512";
  private MessageDigest md = null;
  private byte[] seed = null;
  private int amount;

  /**
   * Deterministic secure random means that given a seed, it is deterministic what the output
   * becomes next time. This differs from Java's original SecureRandom in that if you give a seed to
   * this, it merely adds it to the entropy. It also differs in that this version is hardly secure,
   * so think twice before using this in production.
   * 
   * @param amount is the amount of bytes used from each iteration of the hash-function. The hash
   *        function returns 64 bytes by default.
   * @throws MPCException if the algorithm is not found.
   */
  public DetermSecureRandom(int amount) throws MPCException, IllegalArgumentException {
    try {
      md = MessageDigest.getInstance(ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new MPCException("Error while instantiating DetermSecureRandom", e);
    }
    if (amount >= md.getDigestLength()) {
      throw new IllegalArgumentException(
          "amount (" + amount + ") is not allowed to surpass the length of the digest (which is "
              + md.getDigestLength() + ")");
    }
    this.amount = amount;
  }

  /**
   * Convenience constructor. It does the same as a call to DetermSecureRandom(1) would do.
   */
  public DetermSecureRandom() throws MPCException {
    this.amount = 1;
    try {
      md = MessageDigest.getInstance(ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new MPCException("Error while instantiating DetermSecureRandom", e);
    }
  }

  public synchronized void nextBytes(byte[] bytes) {
    byte[] res = null;
    this.md.update(this.seed);
    res = this.md.digest();
    int index = 0;
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = res[index++];
      if (index >= amount) {
        res = md.digest();
        index = 0;
      }
    }
  }

  public void setSeed(byte[] seed) {
    this.md.reset();
    this.seed = seed;
  }

}
