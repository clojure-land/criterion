package io.usethesource.criterion;

import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.CompilerControl.Mode;

import io.usethesource.criterion.api.JmhValue;

@CompilerControl(Mode.DONT_INLINE)
public class PureSeparateHashCodeInteger implements JmhValue {

  private final int value;
  private final int hash;

  PureSeparateHashCodeInteger(int value, int hash) {
    this.value = value;
    this.hash = hash;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }

    if (other instanceof PureSeparateHashCodeInteger) {
      int otherValue = ((PureSeparateHashCodeInteger) other).value;

      return value == otherValue;
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("%d [hash = %d]", value, hash);
  }

}
