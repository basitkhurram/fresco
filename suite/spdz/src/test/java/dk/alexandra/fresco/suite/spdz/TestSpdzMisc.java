package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests miscellaneous classes such as util and resource pool
 */
public class TestSpdzMisc {

  @Test
  public void testPreproStrat() {
    for (PreprocessingStrategy pps : PreprocessingStrategy.values()) {
      PreprocessingStrategy strat = PreprocessingStrategy.valueOf(pps.name());
      Assert.assertEquals(pps, strat);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testSpdzExponentiationPipeProtocolExpPipeFailedLength() {
    ModulusBigInteger modulus = new ModulusBigInteger("251");
    SpdzDummyDataSupplier supplier = new SpdzDummyDataSupplier(1, 2,
        new FieldDefinitionBigInteger(modulus), modulus.getBigInteger());
    SpdzResourcePool rp = new SpdzResourcePoolImpl(1, 2, new SpdzOpenedValueStoreImpl(), supplier,
        new AesCtrDrbg(new byte[32]));
    SpdzExponentiationPipeProtocol pro = new SpdzExponentiationPipeProtocol(
        FakeTripGen.EXP_PIPE_SIZE);
    pro.evaluate(0, rp, null);
  }
}
