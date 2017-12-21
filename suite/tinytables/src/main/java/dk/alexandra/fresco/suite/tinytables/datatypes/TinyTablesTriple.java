package dk.alexandra.fresco.suite.tinytables.datatypes;

import java.io.Serializable;

public class TinyTablesTriple implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2666542565038907636L;
	private TinyTablesElement a, b, c;

	public static TinyTablesTriple fromShares(boolean aShare, boolean bShare, boolean cShare) {
		return new TinyTablesTriple(new TinyTablesElement(aShare), new TinyTablesElement(bShare),
				new TinyTablesElement(cShare));
	}

	public TinyTablesTriple(TinyTablesElement a, TinyTablesElement b, TinyTablesElement c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public TinyTablesElement getA() {
		return this.a;
	}

	public TinyTablesElement getB() {
		return this.b;
	}

	public TinyTablesElement getC() {
		return this.c;
	}

	public void setC(TinyTablesElement c) {
		this.c = c;
	}

	@Override
	public String toString() {
		return "TinyTablesTriple: " + a + ", " + b + ", " + c;
	}

}
