package nl.cwi.swat.jmh_dscg_benchmarks;

import org.eclipse.imp.pdb.facts.impl.AbstractValue;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.CompilerControl.Mode;

@CompilerControl(Mode.DONT_INLINE)
public class PureInteger extends AbstractValue {
	
	private int value;

	PureInteger(int value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}

		if (other instanceof PureInteger) {
			int otherValue = ((PureInteger) other).value;
			
			return value == otherValue;
		}
		return false;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}