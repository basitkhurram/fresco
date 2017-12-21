package dk.alexandra.fresco.suite.tinytables;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;
import org.junit.Test;

public class TestDatatypes {

  TinyTablesElement elm = new TinyTablesElement(true);

  @Test
  public void testElementsToString() {
    assertEquals("TinyTablesElement [share=true]", elm.toString());
  }

  @Test
  public void testTriplesToString() {
    TinyTablesTriple trip = new TinyTablesTriple(elm, elm, elm);
    assertEquals("TinyTablesTriple: " + elm + ", " + elm + ", " + elm, trip.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTinyTableIncorrectLength() {
    new TinyTable(elm, elm, elm);
  }

  @Test
  public void testTinyTableToString() {
    TinyTable table = new TinyTable(elm, elm, elm, elm);
    System.out.println(table.toString());
    assertEquals(
        "[[TinyTablesElement [share=true],TinyTablesElement [share=true]],"
            + "[TinyTablesElement [share=true],TinyTablesElement [share=true]]]",
        table.toString());
  }

  @Test
  public void testTinyTablesSBoolToString() {
    TinyTablesSBool bool = new TinyTablesSBool(elm);
    assertEquals("TinyTablesSBool [value=TinyTablesElement [share=true]]", bool.toString());
  }

  @Test
  public void testTinyTablesPreproSBoolToString() {
    TinyTablesPreproSBool bool = new TinyTablesPreproSBool(elm);
    System.out.println(bool);
    assertEquals("TinyTablesPreproSBool [value=TinyTablesElement [share=true]]", bool.toString());
  }
}
