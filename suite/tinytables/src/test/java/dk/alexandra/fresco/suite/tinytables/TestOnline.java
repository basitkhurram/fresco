package dk.alexandra.fresco.suite.tinytables;

import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanBuilderFactory;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesBuilderFactory;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;

public class TestOnline {
  private TinyTablesBuilderFactory factory;
  private ProtocolBuilderBinary builder;

  @Before
  public void setup() {
    BuilderFactoryBinary builderFactory = new DummyBooleanBuilderFactory();
    builder = builderFactory.createSequential();
    factory = new TinyTablesBuilderFactory();
  }

  /**** NEGATIVE TESTS. ****/
  @Test(expected = UnsupportedOperationException.class)
  public void testUnimplementedRandomBit() {
    // Ensure that unimplemented method throws an UnsupportedOperationException
    Binary bin = factory.createBinary(builder);
    bin.randomBit();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUnimplementedOpen() {
    // Ensure that unimplemented method throws an UnsupportedOperationException
    Binary bin = factory.createBinary(builder);
    bin.open(new TinyTablesSBool(), 0);
  }
}
