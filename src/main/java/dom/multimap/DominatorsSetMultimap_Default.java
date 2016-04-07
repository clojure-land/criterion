package dom.multimap;

import static dom.AllDominatorsRunner.DATA_SET_SINGLE_FILE_NAME;
import static dom.multimap.Util_Default.EMPTY;
import static dom.multimap.Util_Default.carrier;
import static dom.multimap.Util_Default.intersect;
import static dom.multimap.Util_Default.project;
import static dom.multimap.Util_Default.subtract;
import static dom.multimap.Util_Default.toMultimap;
import static dom.multimap.Util_Default.union;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.openjdk.jmh.infra.Blackhole;
import org.rascalmpl.interpreter.utils.Timing;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IMap;
import org.rascalmpl.value.IMapWriter;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISetWriter;
import org.rascalmpl.value.ITuple;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.value.io.BinaryValueReader;

import dom.DominatorBenchmark;
import dom.JmhCfgDominatorBenchmarks;
import io.usethesource.capsule.DefaultTrieSet;
import io.usethesource.capsule.ImmutableMap;
import io.usethesource.capsule.ImmutableSet;
import io.usethesource.capsule.ImmutableSetMultimap;
import io.usethesource.capsule.TransientSet;
import io.usethesource.capsule.TransientSetMultimap;
import io.usethesource.capsule.TrieSetMultimap_HHAMT;

@SuppressWarnings("deprecation")
public class DominatorsSetMultimap_Default implements DominatorBenchmark {

	private ImmutableSet setofdomsets(ImmutableSetMultimap dom, ImmutableSet preds) {
		TransientSet result = DefaultTrieSet.transientOf();

		for (Object p : preds) {
			ImmutableSet ps = dom.get(p);

			result.__insert(ps == null ? EMPTY : ps);
		}

		return result.freeze();
	}

	public ImmutableSet<IConstructor> top(ImmutableSet<ITuple> graph) {
		return subtract(project(graph, 0), project(graph, 1));
	}

	public IConstructor getTop(ImmutableSet<ITuple> graph) {
		for (IConstructor candidate : top(graph)) {
			switch (candidate.getName()) {
			case "methodEntry":
			case "functionEntry":
			case "scriptEntry":
				return candidate;
			}
		}

		throw new NoSuchElementException("No candidate found.");
	}

	public ImmutableSetMultimap<IConstructor, IConstructor> calculateDominators(ImmutableSet<ITuple> graph) {
//		IConstructor n0 = getTop(graph);
//		ImmutableSet<IConstructor> nodes = carrier(graph);
//		// if (!nodes.getElementType().isAbstractData()) {
//		// throw new RuntimeException("nodes is not the right type");
//		// }
//		ImmutableMap<IConstructor, ImmutableSet<IConstructor>> preds = toMap(project(graph, 1, 0));
//		// nodes = nodes.delete(n0);
//
//		TransientMap<IConstructor, ImmutableSet<IConstructor>> w = DefaultTrieMap.transientOf();
//		w.__put(n0, DefaultTrieSet.of(n0));
//		for (IConstructor n : nodes.__remove(n0)) {
//			w.__put(n, nodes);
//		}
//		ImmutableMap<IConstructor, ImmutableSet<IConstructor>> dom = w.freeze();
//		
//		ImmutableMap prev = DefaultTrieMap.of();
//		/*
//		 * solve (dom) for (n <- nodes) dom[n] = {n} + intersect({dom[p] | p <-
//		 * preds[n]?{}});
//		 */
//		while (!prev.equals(dom)) {
//			prev = dom;
//
//			TransientMap<IConstructor, ImmutableSet<IConstructor>> newDom = DefaultTrieMap.transientOf();
//
//			for (IConstructor n : nodes) {
//				ImmutableSet ps = (ImmutableSet) preds.get(n);
//				if (ps == null) {
//					ps = EMPTY;
//				}
//				ImmutableSet sos = setofdomsets(dom, ps);
//				// if (!sos.getType().isSet() ||
//				// !sos.getType().getElementType().isSet() ||
//				// !sos.getType().getElementType().getElementType().isAbstractData())
//				// {
//				// throw new RuntimeException("not the right type: " +
//				// sos.getType());
//				// }
//				ImmutableSet intersected = intersect(sos);
//				// if (!intersected.getType().isSet() ||
//				// !intersected.getType().getElementType().isAbstractData()) {
//				// throw new RuntimeException("not the right type: " +
//				// intersected.getType());
//				// }
//				ImmutableSet newValue = union(intersected, DefaultTrieSet.of(n));
//				// ImmutableSet newValue = intersected.__insert(n);
//				// if (!newValue.getElementType().isAbstractData()) {
//				// System.err.println("problem");
//				// }
//				newDom.__put(n, newValue);
//			}
//	
//			// if
//			// (!newDom.done().getValueType().getElementType().isAbstractData())
//			// {
//			// System.err.println("not good");
//			// }
//			dom = newDom.freeze();
//		}
//	
//		return dom;
		
//		long totalNrOfUniqueKeys = 0;
//		long totalNrOfTuple = 0; 

//		long unique = 0;
//		long tuples = 0; 
//		long one2one = 0;
		
		IConstructor n0 = getTop(graph);
		ImmutableSet<IConstructor> nodes = carrier(graph);
		
		ImmutableSetMultimap<IConstructor, IConstructor> preds = toMultimap(project(graph, 1, 0));
		
		Iterator<Entry<IConstructor, Object>> it = preds.nativeEntryIterator();
		
		while (it.hasNext()) {
			Entry<IConstructor, Object> tuple = it.next();
			
			Object singletonOrSet = tuple.getValue();
			
			if (singletonOrSet instanceof ImmutableSet) {
				JmhCfgDominatorBenchmarks.unique++;
				JmhCfgDominatorBenchmarks.tuples+=((ImmutableSet) singletonOrSet).size();				
			} else {
				JmhCfgDominatorBenchmarks.unique++;
				JmhCfgDominatorBenchmarks.tuples++;
				JmhCfgDominatorBenchmarks.tuples_one2one++;
			}
		}
		
		TransientSetMultimap<IConstructor, IConstructor> w = TrieSetMultimap_HHAMT.transientOf();
		w.__insert(n0, n0);
//		JmhCfgDominatorBenchmarks.unique++;
//		JmhCfgDominatorBenchmarks.tuples++;
//		JmhCfgDominatorBenchmarks.one2one++;
		for (IConstructor n : nodes.__remove(n0)) {
			w.__put(n, nodes); // TODO: implement method to put a whole set at once!
//			JmhCfgDominatorBenchmarks.unique++;
//			JmhCfgDominatorBenchmarks.tuples+=nodes.size();
		}
		ImmutableSetMultimap<IConstructor, IConstructor> dom = w.freeze();
		
		ImmutableSetMultimap<IConstructor, IConstructor> prev = TrieSetMultimap_HHAMT.of();
		
		/*
		 * solve (dom) for (n <- nodes) dom[n] = {n} + intersect({dom[p] | p <-
		 * preds[n]?{}});
		 */
		while (!prev.equals(dom)) {
			prev = dom;
			
			TransientSetMultimap<IConstructor, IConstructor> newDom = TrieSetMultimap_HHAMT.transientOf();

			for (IConstructor n : nodes) {
				ImmutableSet ps = (ImmutableSet) preds.get(n);
				if (ps == null) {
					ps = EMPTY;
				}
				ImmutableSet<ImmutableSet<IConstructor>> sos = setofdomsets(dom, ps);
				ImmutableSet<IConstructor> intersected = intersect(sos);
				
				if (!intersected.isEmpty()) {
					ImmutableSet<IConstructor> newValue = union(intersected, DefaultTrieSet.of(n));
					newDom.__put(n, newValue); // TODO: implement method to put a whole set at once!
//					JmhCfgDominatorBenchmarks.unique++;
//					JmhCfgDominatorBenchmarks.tuples+=newValue.size();					
				} else {
					newDom.__insert(n, n); // TODO: implement method to put a
											// whole set at once!
//					JmhCfgDominatorBenchmarks.unique++;
//					JmhCfgDominatorBenchmarks.tuples++;
//					JmhCfgDominatorBenchmarks.one2one++;
				}
			}

			dom = newDom.freeze();
		}

//		System.out.println("unique:" + unique);
//		System.out.println("tuples:" + tuples);
//		System.out.println("one2one:" + one2one);
//
//		System.out.println("ratio:" + tuples / unique);
		
		return dom;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		testOne();
		// assertDominatorsEqual();
	}

	public static IMap testOne() throws IOException, FileNotFoundException {
		IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

		ISet data = (ISet) new BinaryValueReader().read(vf, new FileInputStream(DATA_SET_SINGLE_FILE_NAME));

		// convert data to remove PDB dependency
		ImmutableSet<ITuple> graph = pdbSetToImmutableSet(data);

		long before = Timing.getCpuTime();
		ImmutableSetMultimap<IConstructor, IConstructor> results = new DominatorsSetMultimap_Default()
				.calculateDominators(graph);
		System.err.println("PDB_LESS_IMPLEMENTATION" + "\nDuration: " + ((Timing.getCpuTime() - before) / 1000000000)
				+ " seconds\n");

//		IMap pdbResults = immutableMapToPdbMap(results);
//
//		if (LOG_BINARY_RESULTS)
//			new BinaryValueWriter().write(pdbResults,
//					new FileOutputStream("data/dominators-java-without-pdb-single.bin"));
//
//		if (LOG_TEXTUAL_RESULTS)
//			new StandardTextWriter().write(pdbResults, new FileWriter("data/dominators-java-without-pdb-single.txt"));
//
//		return pdbResults;
		
		return null;
	}

	public static ISet testAll(IMap sampledGraphs) throws IOException, FileNotFoundException {
		// convert data to remove PDB dependency
		ArrayList<ImmutableSet<ITuple>> graphs = pdbMapToArrayListOfValues(sampledGraphs);

		TransientSet<ImmutableSetMultimap<IConstructor, IConstructor>> result = DefaultTrieSet.transientOf();
		long before = Timing.getCpuTime();
		for (ImmutableSet<ITuple> graph : graphs) {
			try {
				result.__insert(new DominatorsSetMultimap_Default().calculateDominators(graph));
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
			}
		}
		System.err.println("PDB_LESS_IMPLEMENTATION" + "\nDuration: " + ((Timing.getCpuTime() - before) / 1000000000)
				+ " seconds\n");

//		// convert back to PDB for serialization
//		ISet pdbResults = immutableSetOfMapsToSetOfMapValues(result.freeze());
//
//		if (LOG_BINARY_RESULTS)
//			new BinaryValueWriter().write(pdbResults, new FileOutputStream("data/dominators-java.bin"));
//
//		if (LOG_TEXTUAL_RESULTS)
//			new StandardTextWriter().write(pdbResults, new FileWriter("data/dominators-java-without-pdb.txt"));
//
//		return pdbResults;
		
		return null;
	}

	private static ArrayList<ImmutableSet<ITuple>> pdbMapToArrayListOfValues(IMap data) {
		// convert data to remove PDB dependency
		ArrayList<ImmutableSet<ITuple>> graphs = new ArrayList<>(data.size());
		for (IValue key : data) {
			ISet value = (ISet) data.get(key);

			TransientSet<ITuple> convertedValue = DefaultTrieSet.transientOf();
			for (IValue tuple : value) {
				convertedValue.__insert((ITuple) tuple);
			}

			graphs.add(convertedValue.freeze());
		}

		return graphs;
	}

	private static ISet immutableSetOfMapsToSetOfMapValues(
			ImmutableSet<ImmutableMap<IConstructor, ImmutableSet<IConstructor>>> result) {
		// convert back to PDB for serialization
		IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

		ISetWriter resultBuilder = vf.setWriter();

		for (ImmutableMap<IConstructor, ImmutableSet<IConstructor>> dominatorResult : result) {
			IMapWriter builder = vf.mapWriter();

			for (Map.Entry<IConstructor, ImmutableSet<IConstructor>> entry : dominatorResult.entrySet()) {
				builder.put(entry.getKey(), immutableSetToPdbSet(entry.getValue()));
			}

			resultBuilder.insert(builder.done());
		}

		return resultBuilder.done();
	}

	private static IMap immutableMapToPdbMap(ImmutableMap<IConstructor, ImmutableSet<IConstructor>> result) {
		// convert back to PDB for serialization
		IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

		IMapWriter builder = vf.mapWriter();

		for (Map.Entry<IConstructor, ImmutableSet<IConstructor>> entry : result.entrySet()) {
			builder.put(entry.getKey(), immutableSetToPdbSet(entry.getValue()));
		}

		return builder.done();
	}

	private static <K extends IValue> ISet immutableSetToPdbSet(ImmutableSet<K> set) {
		IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

		ISetWriter builder = vf.setWriter();

		for (K key : set) {
			builder.insert(key);
		}

		return builder.done();
	}

	// private static <K extends IValue, V extends IValue> IMap
	// immutableMapToPdbMap(
	// ImmutableMap<K, V> map) {
	// IValueFactory vf =
	// org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();
	//
	// IMapWriter builder = vf.mapWriter();
	//
	// for (Map.Entry<K, V> entry : map.entrySet()) {
	// builder.put(entry.getKey(), entry.getValue());
	// }
	//
	// return builder.done();
	// }

	private static ImmutableSet<ITuple> pdbSetToImmutableSet(ISet set) {
		TransientSet<ITuple> builder = DefaultTrieSet.transientOf();

		for (IValue tuple : set) {
			builder.__insert((ITuple) tuple);
		}

		return builder.freeze();
	}

	public static void assertDominatorsEqual() throws FileNotFoundException, IOException {
		IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

		ISet dominatorsRascal = (ISet) new BinaryValueReader().read(vf,
				new FileInputStream("data/dominators-rascal.bin"));
		ISet dominatorsJava = (ISet) new BinaryValueReader().read(vf, new FileInputStream("data/dominators-java.bin"));

		if (!dominatorsRascal.equals(dominatorsJava)) {
			throw new Error("Dominator calculations do differ!");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void performBenchmark(Blackhole bh, ArrayList<?> sampledGraphsNative) {
		for (ImmutableSet<ITuple> graph : (ArrayList<ImmutableSet<ITuple>>) sampledGraphsNative) {
			try {
				bh.consume(new DominatorsSetMultimap_Default().calculateDominators(graph));
			} catch (NoSuchElementException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	@Override
	public ArrayList<?> convertDataToNativeFormat(ArrayList<ISet> sampledGraphs) {
		// convert data to remove PDB dependency
		ArrayList<ImmutableSet<ITuple>> sampledGraphsNative = new ArrayList<>(sampledGraphs.size());

		for (ISet graph : sampledGraphs) {
			TransientSet<ITuple> convertedValue = DefaultTrieSet.transientOf();

			for (IValue tuple : graph) {
				convertedValue.__insert((ITuple) tuple);
			}

			sampledGraphsNative.add(convertedValue.freeze());
		}

		return sampledGraphsNative;
	}

}

class Util_Default {

	@SuppressWarnings("rawtypes")
	public final static ImmutableSet EMPTY = DefaultTrieSet.of();

	/*
	 * Intersect many sets.
	 */
	@SuppressWarnings("unchecked")
	public static <K> ImmutableSet<K> intersect(ImmutableSet<ImmutableSet<K>> sets) {
		if (sets == null || sets.isEmpty() || sets.contains(EMPTY)) {
			return EMPTY;
		}

		ImmutableSet<K> first = sets.iterator().next();
		sets = sets.__remove(first);

		ImmutableSet<K> result = first;
		for (ImmutableSet<K> elem : sets) {
			result = Util_Default.intersect(result, elem);
		}

		return result;
	}

	/*
	 * Intersect two sets.
	 */
	public static <K> ImmutableSet<K> intersect(ImmutableSet<K> set1, ImmutableSet<K> set2) {
		if (set1 == set2)
			return set1;
		if (set1 == null)
			return DefaultTrieSet.of();
		if (set2 == null)
			return DefaultTrieSet.of();

		final ImmutableSet<K> smaller;
		final ImmutableSet<K> bigger;

		final ImmutableSet<K> unmodified;

		if (set2.size() >= set1.size()) {
			unmodified = set1;
			smaller = set1;
			bigger = set2;
		} else {
			unmodified = set2;
			smaller = set2;
			bigger = set1;
		}

		final TransientSet<K> tmp = smaller.asTransient();
		boolean modified = false;

		for (Iterator<K> it = tmp.iterator(); it.hasNext();) {
			final K key = it.next();
			if (!bigger.contains(key)) {
				it.remove();
				modified = true;
			}
		}

		if (modified) {
			return tmp.freeze();
		} else {
			return unmodified;
		}
	}

	/*
	 * Subtract one set from another.
	 */
	public static <K> ImmutableSet<K> subtract(ImmutableSet<K> set1, ImmutableSet<K> set2) {
		if (set1 == null && set2 == null)
			return DefaultTrieSet.of();
		if (set1 == set2)
			return DefaultTrieSet.of();
		if (set1 == null)
			return DefaultTrieSet.of();
		if (set2 == null)
			return set1;

		final TransientSet<K> tmp = set1.asTransient();
		boolean modified = false;

		for (K key : set2) {
			if (tmp.__remove(key)) {
				modified = true;
			}
		}

		if (modified) {
			return tmp.freeze();
		} else {
			return set1;
		}
	}

	/*
	 * Union two sets.
	 */
	public static <K> ImmutableSet<K> union(ImmutableSet<K> set1, ImmutableSet<K> set2) {
		if (set1 == null && set2 == null)
			return DefaultTrieSet.of();
		if (set1 == null)
			return set2;
		if (set2 == null)
			return set1;

		if (set1 == set2)
			return set1;

		final ImmutableSet<K> smaller;
		final ImmutableSet<K> bigger;

		final ImmutableSet<K> unmodified;

		if (set2.size() >= set1.size()) {
			unmodified = set2;
			smaller = set1;
			bigger = set2;
		} else {
			unmodified = set1;
			smaller = set2;
			bigger = set1;
		}

		final TransientSet<K> tmp = bigger.asTransient();
		boolean modified = false;

		for (K key : smaller) {
			if (tmp.__insert(key)) {
				modified = true;
			}
		}

		if (modified) {
			return tmp.freeze();
		} else {
			return unmodified;
		}
	}

	/*
	 * Flattening of a set (of ITuple elements). Because of the untyped nature
	 * of ITuple, the implementation is not strongly typed.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Iterable<?>, T> ImmutableSet<T> carrier(ImmutableSet<K> set1) {
		TransientSet<Object> builder = DefaultTrieSet.transientOf();

		for (K iterable : set1) {
			for (Object nested : iterable) {
				builder.__insert(nested);
			}
		}

		return (ImmutableSet<T>) builder.freeze();
	}

	/*
	 * Projection from a tuple to single field.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends IValue> ImmutableSet<K> project(ImmutableSet<ITuple> set1, int field) {
		TransientSet<K> builder = DefaultTrieSet.transientOf();

		for (ITuple tuple : set1) {
			builder.__insert((K) tuple.select(field));
		}

		return builder.freeze();
	}

	/*
	 * Projection from a tuple to another tuple with (possible reordered) subset
	 * of fields.
	 */
	public static ImmutableSet<ITuple> project(ImmutableSet<ITuple> set1, int field1, int field2) {
		TransientSet<ITuple> builder = DefaultTrieSet.transientOf();

		for (ITuple tuple : set1) {
			builder.__insert((ITuple) tuple.select(field1, field2));
		}

		return builder.freeze();
	}

	/*
	 * Convert a set of tuples to a map; value in old map is associated with a
	 * set of keys in old map.
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> ImmutableSetMultimap<K, V> toMultimap(ImmutableSet<ITuple> st) {
		TransientSetMultimap<K, V> mm = TrieSetMultimap_HHAMT.transientOf();

		for (ITuple t : st) {
			K key = (K) t.get(0);
			V val = (V) t.get(1);

			mm.__insert(key, val);
		}

		return mm.freeze();
	}	
	
//	/*
//	 * Convert a set of tuples to a map; value in old map is associated with a
//	 * set of keys in old map.
//	 */
//	@SuppressWarnings("unchecked")
//	public static <K, V> ImmutableMap<K, ImmutableSet<V>> toMap(ImmutableSet<ITuple> st) {
//		Map<K, TransientSet<V>> hm = new HashMap<>();
//
//		for (ITuple t : st) {
//			K key = (K) t.get(0);
//			V val = (V) t.get(1);
//			TransientSet<V> wValSet = hm.get(key);
//			if (wValSet == null) {
//				wValSet = DefaultTrieSet.transientOf();
//				hm.put(key, wValSet);
//			}
//			wValSet.__insert(val);
//		}
//
//		TransientMap<K, ImmutableSet<V>> w = DefaultTrieMap.transientOf();
//		for (K k : hm.keySet()) {
//			w.__put(k, hm.get(k).freeze());
//		}
//		return w.freeze();
//	}

}
