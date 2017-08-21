/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.math.bool.log;

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.binary.ComputationBuilderBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.math.Util;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * This class implements logarithm base 2 for binary protocols. 
 * It is currently up to the application programmer to check if the 
 * input is 0. It is well-defined to input 0 and will return
 * 0, but this is not correct as log_2(0) = NaN It uses a method 
 * consisting of 3 steps: 
 * - Prefix OR:
 * Starting from most significant bit of the input X, OR with 
 * the next bit. This causes a bit vector of the form Y=[0,0,...,1,1,...,1]. 
 * 
 * - XOR sum: The resulting bit Zi in the bit vector Z is given
 * as: Y(i+1) XOR Yi This gives a result of the form: Z=[0,...,0,1,0,...,0].
 * This is the result of the function 2^{floor(log(X))+1}. 
 * 
 * - Finally, we get hold of only floor(log(X))+1 by having the
 * result Ai become: forall j: XOR (Zj AND i'th bit of j) 
 * This means fx if Z = [0,1,0], then A0 becomes = (Z0 AND 0'th bit of 0) XOR (Z1 AND 0'th bit of 1) XOR (Z2 AND 0'th bit of 2) = 0 XOR 1
 * XOR 0 = 1 
 * Whereas A1 = (Z0 AND 1'th bit of 0) XOR (Z1 AND 1'th bit of 1) XOR (Z2 AND 1'th bit of
 * 2) = 0 XOR 0 XOR 0 = 0 and A2 is also 0, which gives the correct result of A = [0,0,1].
 *
 * @author Kasper Damgaard
 */
public class LogProtocolImpl implements ComputationBuilderBinary<List<Computation<SBool>>> {

  private List<Computation<SBool>> number;

  /**
   * Note that on an input of 0, this implementation yields 0, which is incorrect.
   * The application is itself responsible for checking that we do indeed not input 0.
   *
   * @param number The number which we want to calculate log base 2 on.
   */
  public LogProtocolImpl(List<Computation<SBool>> number) {
    this.number = number;
  }
  
  
  @Override
  public Computation<List<Computation<SBool>>> build(SequentialBinaryBuilder builder) {
    return builder.seq(seq -> {
      List<Computation<SBool>> ors = new ArrayList<Computation<SBool>>();
      ors.add(number.get(0));
      for(int i = 1; i< number.size(); i++) {
        ors.add(seq.advancedBinary().or(number.get(i), ors.get(i-1)));
      }
      return () -> ors;
    }).seq((ors, seq) -> {
      List<Computation<SBool>> xors = new ArrayList<Computation<SBool>>();
      xors.add(seq.binary().xor(ors.get(0), seq.binary().known(false)));

      for (int i = 1; i < number.size(); i++) {
        xors.add(seq.binary().xor(ors.get(i-1), ors.get(i)));
      }
      xors.add(seq.binary().known(false));
      return () -> xors;
    }).seq((xors, seq) -> {
      List<Computation<SBool>> res = new ArrayList<Computation<SBool>>();
      for (int j = 0; j < Util.log2(number.size()) + 1; j++) {
        res.add(seq.binary().known(false));
      }
      for (int j = 0; j < Util.log2(number.size()) + 1; j++) {
        for (int i = 0; i < xors.size(); i++) {
          boolean ithBit = Util.ithBit(xors.size() - 1 - i, res.size() - 1 - j); //j'th bit of i
          Computation<SBool> tmp = seq.binary().and(xors.get(i), seq.binary().known(ithBit));
          res.add(j, seq.binary().xor(tmp, res.remove(j)));
        }
      }
      return () -> res;
    });
  }
}
