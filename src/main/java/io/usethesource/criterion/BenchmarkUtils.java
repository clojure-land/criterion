/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion;

import java.util.Arrays;
import java.util.Random;

import io.usethesource.capsule.core.deprecated.TrieMap_5Bits;
import io.usethesource.capsule.core.deprecated.TrieMap_5Bits_AsSetMultimap;
import io.usethesource.capsule.core.deprecated.TrieSet_5Bits;
import io.usethesource.capsule.experimental.heterogeneous.TrieMap_5Bits_Heterogeneous_BleedingEdge;
import io.usethesource.capsule.experimental.memoized.TrieMap_5Bits_Memoized_LazyHashCode;
import io.usethesource.capsule.experimental.memoized.TrieSet_5Bits_Memoized_LazyHashCode;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_ChampBasedPrototype;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_HCHAMP;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_HHAMT;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_HHAMT_Interlinked;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_HHAMT_Specialized;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_HHAMT_Specialized_Interlinked;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_HHAMT_Specialized_Path_Interlinked;
import io.usethesource.criterion.api.JmhValueFactory;

public class BenchmarkUtils {
  public static enum ValueFactoryFactory {
    VF_CLOJURE {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.clojure.ClojureValueFactory();
      }
    },
    VF_SCALA {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.scala.ScalaValueFactory();
      }
    },
    VF_CHAMP {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(
            TrieSet_5Bits.class, TrieMap_5Bits.class, TrieSetMultimap_HCHAMP.class);
      }
    },
    VF_JAVASLANG {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.javaslang.JavaslangValueFactory();
      }
    },
    VF_UNCLEJIM {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.unclejim.UnclejimValueFactory();
      }
    },
    VF_DEXX {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.dexx.DexxValueFactory();
      }
    },
    VF_PCOLLECTIONS {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.pcollections.PcollectionsValueFactory();
      }
    },
    VF_CHAMP_MEMOIZED {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(
            TrieSet_5Bits_Memoized_LazyHashCode.class, TrieMap_5Bits_Memoized_LazyHashCode.class,
            TrieSetMultimap_HCHAMP.class);
      }
    },
    VF_CHAMP_HETEROGENEOUS {
      @Override
      public JmhValueFactory getInstance() {
        // TODO: replace set implementation with heterogeneous set
        // implementation
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(
            TrieSet_5Bits.class, TrieMap_5Bits_Heterogeneous_BleedingEdge.class,
            TrieSetMultimap_HCHAMP.class);
      }
    },
    VF_CHAMP_MAP_AS_MULTIMAP {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(null, null,
            TrieMap_5Bits_AsSetMultimap.class);
      }
    },
    VF_CHAMP_MULTIMAP_PROTOTYPE_OLD {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(null, null,
            TrieSetMultimap_ChampBasedPrototype.class);
      }
    },
    VF_CHAMP_MULTIMAP_HCHAMP {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(null, null,
            TrieSetMultimap_HCHAMP.class);
      }
    },
    VF_CHAMP_MULTIMAP_HHAMT {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(null, null,
            TrieSetMultimap_HHAMT.class);
      }
    },
    VF_CHAMP_MULTIMAP_HHAMT_INTERLINKED {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(null, null,
            TrieSetMultimap_HHAMT_Interlinked.class);
      }
    },
    VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(null, null,
            TrieSetMultimap_HHAMT_Specialized.class);
      }
    },
    /**
     * Option is equal to {@value #VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED}, but used to tag the usage
     * of system property
     * {@literal -Dio.usethesource.capsule.RangecopyUtils.dontUseSunMiscUnsafeCopyMemory=true}.
     */
    VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_NO_COPYMEMORY {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(null, null,
            TrieSetMultimap_HHAMT_Specialized.class);
      }
    },
    VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_INTERLINKED {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(null, null,
            TrieSetMultimap_HHAMT_Specialized_Interlinked.class);
      }
    },
    VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_PATH_INTERLINKED {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(null, null,
            TrieSetMultimap_HHAMT_Specialized_Path_Interlinked.class);
      }
    },
    VF_CHAMP_MULTIMAP_HHAMT_NEW {
      @Override
      public JmhValueFactory getInstance() {
        return new io.usethesource.criterion.impl.persistent.champ.ChampValueFactory(null, null,
            null);
      }
    };

    public abstract JmhValueFactory getInstance();
  }

  public static enum DataType {
    MAP, SET_MULTIMAP, SET
  }

  public enum Archetype {
    MUTABLE, IMMUTABLE, PERSISTENT
  }

  public static enum SampleDataSelection {
    MATCH, RANDOM
  }

  public static int seedFromSizeAndRun(int size, int run) {
    return mix(size) ^ mix(run);
  }

  private static int mix(int n) {
    int h = n;

    h *= 0x5bd1e995;
    h ^= h >>> 13;
    h *= 0x5bd1e995;
    h ^= h >>> 15;

    return h;
  }

  public static int[] generateSortedArrayWithRandomData(int size, int run) {

    int[] randomNumbers = new int[size];

    int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);
    Random rand = new Random(seedForThisTrial);

    // System.out.println(String.format("Seed for this trial: %d.",
    // seedForThisTrial));

    for (int i = size - 1; i >= 0; i--) {
      randomNumbers[i] = rand.nextInt();
    }

    Arrays.sort(randomNumbers);

    return randomNumbers;
  }

  static int[] generateTestData(int size, int run) {
    int[] data = new int[size];

    int seedForThisTrial = seedFromSizeAndRun(size, run);
    Random rand = new Random(seedForThisTrial);

    // System.out.println(String.format("Seed for this trial: %d.",
    // seedForThisTrial));

    for (int i = size - 1; i >= 0; i--) {
      data[i] = rand.nextInt();
    }

    return data;
  }

}
