/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.champ;

import io.usethesource.capsule.MapFactory;
import io.usethesource.capsule.SetFactory;
import io.usethesource.capsule.SetMultimapFactory;
import io.usethesource.criterion.api.JmhMapBuilder;
import io.usethesource.criterion.api.JmhSetBuilder;
import io.usethesource.criterion.api.JmhSetMultimapBuilder;
import io.usethesource.criterion.api.JmhValueFactory;

public class ChampValueFactory implements JmhValueFactory {

  private final SetFactory setFactory;
  private final MapFactory mapFactory;
  private final SetMultimapFactory setMultimapFactory;

  public ChampValueFactory(final Class<?> targetSetClass, final Class<?> targetMapClass,
      final Class<?> targetSetMultimapClass) {
    setFactory = targetSetClass == null ? null : new SetFactory(targetSetClass);
    mapFactory = targetMapClass == null ? null : new MapFactory(targetMapClass);

    if (targetSetMultimapClass == null) {
      setMultimapFactory = null;
    } else {
      setMultimapFactory = new SetMultimapFactory(targetSetMultimapClass);
    }
  }

  @Override
  public JmhSetBuilder setBuilder() {
    return new ChampSetBuilder(setFactory);
  }

  @Override
  public JmhMapBuilder mapBuilder() {
    return new PersistentChampMapBuilder(mapFactory);
  }

  @Override
  public JmhSetMultimapBuilder setMultimapBuilder() {
    if (setMultimapFactory == null) {
      return new PersistentChampSetMultimapBuilderNew();
    } else {
      return new PersistentChampSetMultimapWriter(setMultimapFactory);
    }
  }

  @Override
  public String toString() {
    return "VF_PDB_PERSISTENT_CURRENT";
  }

}
