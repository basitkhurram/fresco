package dk.alexandra.fresco.suite.tinytables.util;

import java.security.MessageDigest;
import java.util.BitSet;
import dk.alexandra.fresco.framework.util.ExceptionConverter;

public class Util {

	/**
	 * Outputs a hash of j and the given bits of size l. We assume that l < 256
	 * since the underlying hash function is SHA-256.
	 *
	 * @param j
	 * @param bits
	 * @param l
	 * @return
	 */
	public static BitSet hash(int j, BitSet bits, int l) {
    MessageDigest digest = ExceptionConverter.safe(() -> MessageDigest
        .getInstance("SHA-256"),
        "Internal hash algorithm, SHA-256, required by the protocol could not be found");
		digest.update((byte) j);
		byte[] binary = digest.digest(bits.toByteArray());
		return BitSet.valueOf(binary).get(0, l);
	}

	public static int otherPlayerId(int myId) {
		return myId == 1 ? 2 : 1;
	}
}
