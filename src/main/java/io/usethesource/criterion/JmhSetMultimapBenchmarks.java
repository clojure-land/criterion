/*******************************************************************************
 * Copyright (c) 2014 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package io.usethesource.criterion;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import io.usethesource.criterion.BenchmarkUtils.DataType;
import io.usethesource.criterion.BenchmarkUtils.SampleDataSelection;
import io.usethesource.criterion.BenchmarkUtils.ValueFactoryFactory;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhSetMultimapBuilder;
import io.usethesource.criterion.api.JmhValue;
import io.usethesource.criterion.api.JmhValueFactory;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class JmhSetMultimapBenchmarks {

	private static boolean USE_PRIMITIVE_DATA = false;

	@Param({ "SET_MULTIMAP" })
	public DataType dataType;

	@Param({ "MATCH" })
	public SampleDataSelection sampleDataSelection;

	@Param
	public ValueFactoryFactory valueFactoryFactory;

	/*
	 * (for (i <- 0 to 23) yield s"'${Math.pow(2, i).toInt}'").mkString(", "
	 * ).replace("'", "\"")
	 */
	@Param({ "1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096",
					"8192", "16384", "32768", "65536", "131072", "262144", "524288", "1048576",
					"2097152", "4194304", "8388608" })
	protected int size;

	@Param({ "0" }) // "1", "2", "3", "4", "5", "6", "7", "8", "9"
	protected int run;

	@Param
	public ElementProducer producer;

	public JmhValueFactory valueFactory;

	public JmhSetMultimap testMap;
	private JmhSetMultimap testMapRealDuplicate;
	private JmhSetMultimap testMapDeltaDuplicate;

	public JmhSetMultimap testMapInt;

	private JmhSetMultimap testMapRealDuplicateSameSizeButDifferent;

	public JmhValue VALUE_EXISTING;
	public JmhValue VALUE_NOT_EXISTING;

	public int VALUE_EXISTING_INT;
	public int VALUE_NOT_EXISTING_INT;

	public static final int CACHED_NUMBERS_SIZE = 8;
	public JmhValue[] cachedNumbers = new JmhValue[CACHED_NUMBERS_SIZE];
	public JmhValue[] cachedNumbersNotContained = new JmhValue[CACHED_NUMBERS_SIZE];

	public int[] cachedNumbersInt = new int[CACHED_NUMBERS_SIZE];
	public int[] cachedNumbersIntNotContained = new int[CACHED_NUMBERS_SIZE];

	private JmhSetMultimap singletonMapWithExistingValue;
	private JmhSetMultimap singletonMapWithNotExistingValue;

	@Setup(Level.Trial)
	public void setUp() throws Exception {
		// TODO: look for right place where to put this
		SleepingInteger.IS_SLEEP_ENABLED_IN_HASHCODE = false;
		SleepingInteger.IS_SLEEP_ENABLED_IN_EQUALS = false;

		setUpTestMapWithRandomContent(size, run);

		valueFactory = valueFactoryFactory.getInstance();

		testMap = generateMap(valueFactory, producer, false, size, run);
		testMapRealDuplicate = generateMap(valueFactory, producer, false, size, run);

		VALUE_EXISTING = (JmhValue) generateExistingAndNonExistingValue(valueFactory, producer, false, size, run)[0];
		VALUE_NOT_EXISTING = (JmhValue) generateExistingAndNonExistingValue(valueFactory, producer, false, size, run)[1];

		if (USE_PRIMITIVE_DATA) {
			testMapInt = generateMap(valueFactory, producer, true, size, run);
			// TODO: testMapRealDuplicateInt = ...

			VALUE_EXISTING_INT = (int) generateExistingAndNonExistingValue(valueFactory, producer,
							true, size, run)[0];
			VALUE_NOT_EXISTING_INT = (int) generateExistingAndNonExistingValue(valueFactory,
							producer, true, size, run)[1];
		}
		
		switch (sampleDataSelection) {

		/*
		 * random integers might or might not be in the dataset
		 */
		case RANDOM: {
			// random data generator with fixed seed
			/* seed == Mersenne Prime #8 */
			Random randForOperations = new Random(2147483647L);

			for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
				cachedNumbers[i] = producer.createFromInt(randForOperations.nextInt());
			}
		}

			/*
			 * random integers are in the dataset
			 */
		case MATCH: {
			// random data generator with fixed seed
			int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);
			Random rand = new Random(seedForThisTrial);

			for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
				if (i >= size) {
					cachedNumbers[i] = cachedNumbers[i % size];
					cachedNumbersInt[i] = cachedNumbersInt[i % size];
				} else {
					int nextInt = rand.nextInt();
					cachedNumbers[i] = producer.createFromInt(nextInt);
					cachedNumbersInt[i] = nextInt;
				}
			}

			// random data generator with fixed seed
			/* seed == Mersenne Prime #8 */
			Random anotherRand = new Random(2147483647L);

			for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
				/*
				 * generate random values until a value not part of the data
				 * strucure is found
				 */
				boolean found = false;
				while (!found) {
					final int nextInt = anotherRand.nextInt();

					final JmhValue candidate = producer.createFromInt(nextInt);
					final int candidateInt = nextInt;

					if (testMap.containsKey(candidate) || testMap.containsKey(candidateInt)) {
						continue;
					} else {
						cachedNumbersNotContained[i] = candidate;
						cachedNumbersIntNotContained[i] = candidateInt;
						found = true;
					}
				}
			}

			if (USE_PRIMITIVE_DATA) {
				// assert (contained)
				for (int sample : cachedNumbersInt) {
					if (!testMapInt.containsKey(sample)) {
						throw new IllegalStateException();
					}
				}

				// assert (not contained)
				for (int sample : cachedNumbersIntNotContained) {
					if (testMapInt.containsKey(sample)) {
						throw new IllegalStateException();
					}
				}
			} else {
				// assert (contained)
				for (JmhValue sample : cachedNumbers) {
					if (!testMap.containsKey(sample)) {
						throw new IllegalStateException();
					}
				}

				// assert (not contained)
				for (JmhValue sample : cachedNumbersNotContained) {
					if (testMap.containsKey(sample)) {
						throw new IllegalStateException();
					}
				}
			}
		}
		}

		final JmhSetMultimapBuilder mapWriter1 = valueFactory.setMultimapBuilder();
		mapWriter1.put(VALUE_EXISTING, VALUE_EXISTING);
		singletonMapWithExistingValue = mapWriter1.done();

		final JmhSetMultimapBuilder mapWriter2 = valueFactory.setMultimapBuilder();
		mapWriter2.put(VALUE_NOT_EXISTING, VALUE_NOT_EXISTING);
		singletonMapWithNotExistingValue = mapWriter2.done();

		// System.out.println(String.format("\n\ncachedNumbers = %s",
		// Arrays.toString(cachedNumbers)));
		// System.out.println(String.format("cachedNumbersNotContained =
		// %s\n\n",
		// Arrays.toString(cachedNumbersNotContained)));

		// TODO: look for right place where to put this
		SleepingInteger.IS_SLEEP_ENABLED_IN_HASHCODE = false;
		SleepingInteger.IS_SLEEP_ENABLED_IN_EQUALS = false;

		// OverseerUtils.setup(JmhSetMultimapBenchmarks.class, this);
	}

	protected void setUpTestMapWithRandomContent(int size, int run) throws Exception {

		valueFactory = valueFactoryFactory.getInstance();

		JmhSetMultimapBuilder writer1 = valueFactory.setMultimapBuilder();
		JmhSetMultimapBuilder writer2 = valueFactory.setMultimapBuilder();

		int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);
		Random rand = new Random(seedForThisTrial + 13);
		int existingValueIndex = rand.nextInt(size);

		int[] data = BenchmarkUtils.generateTestData(size, run);

		for (int i = size - 1; i >= 0; i--) {
			// final IValue current = producer.createFromInt(data[i]);

			if (USE_PRIMITIVE_DATA) {
				writer1.put(data[i], data[i]);
				writer2.put(data[i], data[i]);
			} else {
				writer1.put(producer.createFromInt(data[i]), producer.createFromInt(data[i]));
				writer2.put(producer.createFromInt(data[i]), producer.createFromInt(data[i]));
			}

			if (i == existingValueIndex) {
				VALUE_EXISTING = producer.createFromInt(data[i]);
			}
		}

		testMap = writer1.done();
		testMapRealDuplicate = writer2.done();

		/*
		 * generate random values until a value not part of the data strucure is
		 * found
		 */
		while (VALUE_NOT_EXISTING == null) {
			final int candidateInt = rand.nextInt();
			final JmhValue candidate = producer.createFromInt(candidateInt);

			if (!testMap.containsKey(candidateInt) && !testMap.containsKey(candidate)) {
				VALUE_NOT_EXISTING_INT = candidateInt;
				VALUE_NOT_EXISTING = candidate;
			}
		}

		testMapDeltaDuplicate = testMap.put(VALUE_EXISTING, VALUE_NOT_EXISTING).put(VALUE_EXISTING,
						VALUE_EXISTING);

		testMapRealDuplicateSameSizeButDifferent = testMapRealDuplicate.remove(VALUE_EXISTING, VALUE_EXISTING)
				.put(VALUE_NOT_EXISTING, VALUE_NOT_EXISTING);
	}

	protected static JmhSetMultimap generateMap(JmhValueFactory valueFactory, ElementProducer producer,
					boolean usePrimitiveData, int size, int run) throws Exception {

		final int[] data = BenchmarkUtils.generateTestData(size, run);
		final JmhSetMultimapBuilder writer = valueFactory.setMultimapBuilder();

		for (int i = size - 1; i >= 0; i--) {
			if (usePrimitiveData) {
				writer.put(data[i], data[i]);
			} else {
				writer.put(producer.createFromInt(data[i]), producer.createFromInt(data[i]));
			}
		}

		return writer.done();
	}

	protected static Object[] generateExistingAndNonExistingValue(JmhValueFactory valueFactory,
					ElementProducer producer, boolean usePrimitiveData, int size, int run)
									throws Exception {

		int[] data = BenchmarkUtils.generateTestData(size, run);

		int[] sortedData = data.clone();
		Arrays.sort(sortedData);

		int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);
		Random rand = new Random(seedForThisTrial + 13);
		int existingValueIndex = rand.nextInt(size);

		final Object VALUE_EXISTING;

		if (usePrimitiveData) {
			VALUE_EXISTING = data[existingValueIndex];
		} else {
			VALUE_EXISTING = producer.createFromInt(data[existingValueIndex]);
		}

		final Object VALUE_NOT_EXISTING;
		/*
		 * generate random values until a value not part of the data strucure is
		 * found
		 */
		while (true) {
			final int candidateInt = rand.nextInt();

			if (Arrays.binarySearch(sortedData, candidateInt) == -1) {
				if (usePrimitiveData) {
					VALUE_NOT_EXISTING = candidateInt;
				} else {
					VALUE_NOT_EXISTING = producer.createFromInt(candidateInt);
				}
				break;
			}
		}

		return new Object[] { VALUE_EXISTING, VALUE_NOT_EXISTING };
	}

	// @TearDown(Level.Trial)
	// public void tearDown() {
	// OverseerUtils.tearDown();
	// }
	//
	// // @Setup(Level.Iteration)
	// // public void setupIteration() {
	// // OverseerUtils.doRecord(true);
	// // }
	// //
	// // @TearDown(Level.Iteration)
	// // public void tearDownIteration() {
	// // OverseerUtils.doRecord(false);
	// // }
	//
	// @Setup(Level.Invocation)
	// public void setupInvocation() {
	// OverseerUtils.setup(JmhSetMultimapBenchmarks.class, this);
	// OverseerUtils.doRecord(true);
	// }
	//
	// @TearDown(Level.Invocation)
	// public void tearDownInvocation() {
	// OverseerUtils.doRecord(false);
	// }

	// @Benchmark
	// public void timeContainsKeySingle(Blackhole bh) {
	// bh.consume(testMap.containsKey(VALUE_EXISTING));
	// }

	@Benchmark
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public void timeContainsKey(Blackhole bh) {
		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			bh.consume(testMap.containsKey(cachedNumbers[i]));
		}
	}

	@Benchmark
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public void timeContainsKeyInt(Blackhole bh) {
		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			bh.consume(testMapInt.containsKey(cachedNumbersInt[i]));
		}
	}

	@Benchmark
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public void timeContainsKeyNotContained(Blackhole bh) {
		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			bh.consume(testMap.containsKey(cachedNumbersNotContained[i]));
		}
	}

	// @Benchmark
	// public void timeIteration(Blackhole bh) {
	// for (Iterator<JmhValue> iterator = testMap.iterator();
	// iterator.hasNext();) {
	// bh.consume(iterator.next());
	// }
	// }
	//
	// @Benchmark
	// public void timeEntryIteration(Blackhole bh) {
	// for (Iterator<java.util.Map.Entry<JmhValue, JmhValue>> iterator = testMap
	// .entryIterator(); iterator.hasNext();) {
	// bh.consume(iterator.next());
	// }
	// }

	// @Benchmark
	// public void timeInsertSingle(Blackhole bh) {
	// bh.consume(testMap.put(VALUE_NOT_EXISTING, VALUE_NOT_EXISTING));
	// }

	@Benchmark
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public void timeInsert(Blackhole bh) {
		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			bh.consume(testMap.put(cachedNumbersNotContained[i], VALUE_NOT_EXISTING));
		}
	}

//	@Benchmark
//	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
//	public void timeInsertInt(Blackhole bh) {
//		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
//			bh.consume(testMapInt.put(cachedNumbersIntNotContained[i], VALUE_NOT_EXISTING_INT));
//		}
//	}

	@Benchmark
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public void timeInsertContained(Blackhole bh) {
		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			bh.consume(testMap.put(cachedNumbers[i], cachedNumbers[i]));
		}
	}

//	@Benchmark
//	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
//	public void timeRemoveKeyNotContained(Blackhole bh) {
//		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
//			bh.consume(testMap.removeKey(cachedNumbersNotContained[i]));
//		}
//	}
//
//	@Benchmark
//	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
//	public void timeRemoveKey(Blackhole bh) {
//		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
//			bh.consume(testMap.removeKey(cachedNumbers[i]));
//		}
//	}

	@Benchmark
	public void timeEqualsRealDuplicate(Blackhole bh) {
		bh.consume(testMap.equals(testMapRealDuplicate));
	}

	@Benchmark
	public void timeEqualsRealDuplicateModified(Blackhole bh) {
		bh.consume(testMap.equals(testMapRealDuplicateSameSizeButDifferent));
	}

	@Benchmark
	public void timeEqualsDeltaDuplicate(Blackhole bh) {
		bh.consume(testMap.equals(testMapDeltaDuplicate));
	}

	// @Benchmark
	// @BenchmarkMode(Mode.SingleShotTime)
	//// @Warmup(iterations = 0)
	//// @Measurement(iterations = 1)
	// public void timeHashCode(Blackhole bh) {
	// bh.consume(testMap.hashCode());
	// }

	// @Benchmark
	// public void timeJoin(Blackhole bh) {
	// bh.consume(testMap.join(singletonMapWithNotExistingValue));
	// }

	public static void main(String[] args) throws RunnerException {
		/*
		 * /Users/Michael/Development/jku/mx2/graal/jvmci/jdk1.8.0_60/product/
		 * bin/java -jvmci -jar ./target/benchmarks.jar
		 * "JmhSetMultimapBenchmarks.timeContainsKey$" -p
		 * valueFactoryFactory=VF_PDB_PERSISTENT_CURRENT,
		 * VF_PDB_PERSISTENT_BLEEDING_EDGE -p producer=PDB_INTEGER -p
		 * size=4194304 -jvm
		 * /Users/Michael/Development/jku/mx2/graal/jvmci/jdk1.8.0_60/product/
		 * bin/java -jvmArgs "-jvmci" -wi 7 -i 10 -f 0
		 */

		System.out.println(JmhSetMultimapBenchmarks.class.getSimpleName());
		Options opt = new OptionsBuilder()
						.include(".*" + JmhSetMultimapBenchmarks.class.getSimpleName()
										+ ".(timeInsert)$") // ".(timeContainsKey|timeContainsKeyInt|timeInsert|timeInsertInt)$"
						.timeUnit(TimeUnit.NANOSECONDS).mode(Mode.AverageTime).warmupIterations(10)
						.warmupTime(TimeValue.seconds(1)).measurementIterations(10).forks(0)
						.param("dataType", "SET_MULTIMAP").param("run", "0")
//						.param("run", "1")
//						.param("run", "2")
//						.param("run", "3")
//						.param("run", "4")
						.param("producer", "PURE_INTEGER").param("sampleDataSelection", "MATCH")
//						.param("size", "16")
//						.param("size", "2048")
						.param("size", "1048576")
						.param("valueFactoryFactory", "VF_CHAMP")
//						.param("valueFactoryFactory", "VF_CHAMP_HETEROGENEOUS")
						.param("valueFactoryFactory", "VF_CLOJURE")
						.param("valueFactoryFactory", "VF_SCALA")
						// .resultFormat(ResultFormatType.CSV)
						// .result("latest-results-main.csv")
						.build();

		new Runner(opt).run();
	}

}