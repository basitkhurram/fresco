package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.preprocessing.MascotFormatConverter;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotResourcePoolImpl;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.InputMask;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpdzMascotDataSupplier implements SpdzDataSupplier {

  private static final Logger logger = LoggerFactory.getLogger(SpdzMascotDataSupplier.class);
  private final int myId;
  private final int numberOfPlayers;
  private final Supplier<Network> tripleNetwork;
  private final BigInteger modulus;
  private final Function<Integer, SpdzSInt[]> preprocessedValues;
  private final FieldElement ssk;

  private Mascot mascot;
  private int prgSeedLength;
  private ArrayDeque<MultTriple> triples;
  private ArrayDeque<InputMask> masks;
  private ArrayDeque<AuthenticatedElement> randomElements;
  private int modBitLength;
  private int batchSize;
  private byte[] randomSeed;

  public static SpdzMascotDataSupplier createSimpleSupplier(int myId, int numberOfPlayers,
      Supplier<Network> tripleNetwork, int modBitLength,
      Function<Integer, SpdzSInt[]> preprocessedValues) {
    return new SpdzMascotDataSupplier(myId, numberOfPlayers, tripleNetwork,
        ModulusFinder.findSuitableModulus(modBitLength), modBitLength, preprocessedValues, 256,
        16);
  }

  private SpdzMascotDataSupplier(int myId, int numberOfPlayers, Supplier<Network> tripleNetwork,
      BigInteger modulus, int maxBitLength, Function<Integer, SpdzSInt[]> preprocessedValues,
      int prgSeedLength, int batchSize) {
    this(myId, numberOfPlayers, tripleNetwork, modulus, maxBitLength, preprocessedValues,
        prgSeedLength, batchSize, createRandomSsk(myId, modulus, maxBitLength, prgSeedLength),
        new byte[]{7, 127, -1});
  }

  public SpdzMascotDataSupplier(int myId, int numberOfPlayers, Supplier<Network> tripleNetwork,
      BigInteger modulus, int modBitLength, Function<Integer, SpdzSInt[]> preprocessedValues,
      int prgSeedLength, int batchSize, FieldElement ssk, byte[] randomSeed) {
    this.myId = myId;
    this.numberOfPlayers = numberOfPlayers;
    this.tripleNetwork = tripleNetwork;
    this.modulus = modulus;
    this.preprocessedValues = preprocessedValues;
    this.triples = new ArrayDeque<>();
    this.masks = new ArrayDeque<>();
    this.randomElements = new ArrayDeque<>();
    this.prgSeedLength = prgSeedLength;
    this.modBitLength = modBitLength;
    this.batchSize = batchSize;
    this.ssk = ssk;
    this.randomSeed = randomSeed;
  }

  static FieldElement createRandomSsk(int myId, BigInteger modulus, int modBitLength,
      int prgSeedLength) {
    byte[] seedBytes = new byte[32];
    seedBytes[0] = (byte) myId;
    StrictBitVector seed =
        new StrictBitVector(prgSeedLength, new PaddingAesCtrDrbg(seedBytes, 32 * 8));
    FieldElementPrg localSampler = new FieldElementPrgImpl(seed);
    return localSampler.getNext(modulus, modBitLength);
  }

  @Override
  public SpdzTriple getNextTriple() {
    ensureInitialized();
    if (triples.isEmpty()) {
      logger.trace("Getting another triple batch");
      triples.addAll(mascot.getTriples(batchSize));
      logger.trace("Got another triple batch");
    }
    MultTriple triple = triples.pop();
    return MascotFormatConverter.toSpdzTriple(triple);
  }

  protected void ensureInitialized() {
    if (mascot != null) {
      return;
    }
    List<Integer> partyIds =
        IntStream.range(1, numberOfPlayers + 1).boxed().collect(Collectors.toList());

    int numLeftFactors = 3;
    Network network = tripleNetwork.get();
    mascot = new Mascot(new MascotResourcePoolImpl(myId, partyIds, 1,
        new PaddingAesCtrDrbg(randomSeed, prgSeedLength), new HashMap<>(), getModulus(),
        modBitLength,
        modBitLength, prgSeedLength, numLeftFactors), network, ssk);
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public BigInteger getSecretSharedKey() {
    return this.ssk.toBigInteger();
  }

  @Override
  public SpdzSInt getNextRandomFieldElement() {
    ensureInitialized();
    if (randomElements.isEmpty()) {
      logger.trace("Getting another random element batch");
      randomElements.addAll(mascot.getRandomElements(batchSize));
      logger.trace("Got another random element batch");
    }
    return new SpdzSInt(MascotFormatConverter.toSpdzElement(randomElements.pop()));
  }

  @Override
  public SpdzSInt[] getNextExpPipe() {
    logger.trace("Getting another exp pipe");
    SpdzSInt[] pipe = preprocessedValues.apply(modBitLength);
    logger.trace("Got another exp pipe");
    return pipe;
  }

  @Override
  public SpdzInputMask getNextInputMask(int towardPlayerID) {
    ensureInitialized();
    if (masks.isEmpty()) {
      logger.trace("Getting another mask batch");
      masks.addAll(mascot.getInputMasks(towardPlayerID, batchSize));
      logger.trace("Got another mask batch");
    }
    InputMask mask = masks.pop();
    return MascotFormatConverter.toSpdzInputMask(mask);
  }

  @Override
  public SpdzSInt getNextBit() {
    // TODO Nikolaj Volgusjef will fix
    return new SpdzSInt(new SpdzElement(BigInteger.ZERO, BigInteger.ZERO, getModulus()));
  }

}
