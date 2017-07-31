/*
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
 */

package dk.alexandra.fresco.lib.lp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class Matrix<T> {

  private final int width;
  private final int height;
  private final ArrayList<ArrayList<T>> matrix;

  /**
   * Creates an matrix from a row building function.
   * @param height height of the matrix
   * @param width width of the matrix
   * @param rowBuilder the function for building rows
   */
  public Matrix(int height, int width, IntFunction<ArrayList<T>> rowBuilder) {
    this.width = width;
    this.matrix = new ArrayList<>(height);
    this.height = height;
    for (int i = 0; i < height; i++) {
      this.matrix.add(rowBuilder.apply(i));
    }
  }

  /**
   * Creates a matrix directly from an ArrayList of ArrayLists.
   * @param height height of the matrix
   * @param width width of the matrix
   * @param matrix the array data
   */
  public Matrix(int height, int width, ArrayList<ArrayList<T>> matrix) {
    this.width = width;
    this.height = height;
    this.matrix = matrix;
  }


  public ArrayList<T> getRow(int i) {
    return matrix.get(i);
  }

  /**
   * Gets the width of the matrix.
   * @return the width of the matrix
   */
  public int getWidth() {
    return width;
  }


  /**
   * Gets the height of the matrix.
   * @return the height of the matrix
   */
  public int getHeight() {
    return height;
  }

  public List<T> getColumn(int i) {
    return this.matrix.stream().map(row -> row.get(i)).collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "Matrix{"
        + "width=" + width
        + ", height=" + height
        + ", matrix=" + matrix
        + '}';
  }

  /**
   * Returns the matrix as a two-dimensional array.
   * @param mapper a mapper from type T to R
   * @param arrayCreator an array creating function 
   * @param doubleCreator a two-dimensional array creating function
   * @return the two-dimensional array
   */
  public <R> R[][] toArray(Function<T, R> mapper, IntFunction<R[]> arrayCreator,
      IntFunction<R[][]> doubleCreator) {
    List<R[]> rows = matrix.stream().map(row -> row.stream().map(mapper).toArray(arrayCreator)
    ).collect(Collectors.toList());
    R[][] array = doubleCreator.apply(rows.size());
    int i = 0;
    for (R[] row: rows) {
      array[i++] = row;
    }
    return array;
  }
}
 