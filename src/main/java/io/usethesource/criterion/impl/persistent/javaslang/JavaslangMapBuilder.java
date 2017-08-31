/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.javaslang;

import io.usethesource.criterion.api.JmhValue;
import io.usethesource.criterion.impl.AbstractMapBuilder;
import javaslang.collection.HashMap;

final class JavaslangMapBuilder extends AbstractMapBuilder<JmhValue, HashMap<JmhValue, JmhValue>> {

  JavaslangMapBuilder() {
    super(HashMap.empty(), map -> map::put, JavaslangMap::new);
  }

}
