/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.unclejim;

import java.util.Iterator;
import java.util.function.Consumer;

import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhValue;
import org.organicdesign.fp.collections.PersistentHashSet;

public final class UnclejimSet implements JmhSet {

  private final PersistentHashSet<JmhValue> content;

  public UnclejimSet(PersistentHashSet<JmhValue> content) {
    this.content = content;
  }

  @Override
  public boolean isEmpty() {
    return content.isEmpty();
  }

  @Override
  public JmhSet insert(JmhValue value) {
    return new UnclejimSet(content.put(value));
  }

  @Override
  public JmhSet delete(JmhValue value) {
    return new UnclejimSet(content.without(value));
  }

  @Override
  public int size() {
    return content.size();
  }

  @Override
  public boolean contains(JmhValue value) {
    return content.contains(value);
  }

  @Override
  public Iterator<JmhValue> iterator() {
    return content.iterator();
  }

  @Override
  public java.util.Set<JmhValue> asJavaSet() {
    final java.util.Set<JmhValue> jdkSet = new java.util.HashSet<JmhValue>();
    content.forEach((Consumer<? super JmhValue>) jdkSet::add);
    return jdkSet;
  }

  @Override
  public int hashCode() {
    return content.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other == null) {
      return false;
    }

    if (other instanceof UnclejimSet) {
      UnclejimSet that = (UnclejimSet) other;

      if (this.size() != that.size()) {
        return false;
      }

      return content.equals(that.content);
    }

    return false;
  }

  @Override
  public Object unwrap() {
    return content;
  }

}
