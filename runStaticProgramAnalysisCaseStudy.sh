#!/bin/bash

mvn clean install
mkdir -p target/results
mkdir -p target/result-logs

export DOMINATOR_BENCHMARK_ENUM="CHART,VF_CHAMP_MULTIMAP_HHAMT"

export COMMON_JVM_SETTINGS="-Xms12g -Xmx12g"

# Settings for cache measurements:
export COMMON_SETTINGS="-wi 3 -i 10 -r 10 -f 1 -t 1 -tu s -to 150m -p run=0 -gc true -rf csv -v EXTRA -foe false -bm ss"

LD_LIBRARY_PATH=~/lib/ java -jar target/benchmarks.jar "dom.JmhCfgDominatorBenchmarks.timeDominatorCalculation$" -p dominatorBenchmarkEnum=$DOMINATOR_BENCHMARK_ENUM -jvmArgs "$COMMON_JVM_SETTINGS -Doverseer.utils.output.file=./target/result-logs/results.perf-stat.JmhCfgDominatorBenchmarks.timeDominatorCalculation.sizeAll.log" $COMMON_SETTINGS -rff ./target/results/results.JmhCfgDominatorBenchmarks.timeDominatorCalculation.sizeAll.log 2>./target/result-logs/results.err-console.JmhCfgDominatorBenchmarks.timeDominatorCalculation.sizeAll.log | tee ./target/result-logs/results.std-console.JmhCfgDominatorBenchmarks.timeDominatorCalculation.sizeAll.log

TIMESTAMP=`date +"%Y%m%d_%H%M"`

INPUT_FILES=target/results/results.Jmh*.log
RESULTS_FILE=target/results/results.all-real-world-$TIMESTAMP.log

RESULT_HEADER=`echo $INPUT_FILES | xargs -n 1 head -n 1 | head -n 1`
{
	for f in $INPUT_FILES
	do
		tail -n +2 $f
	done
} | cat <(echo $RESULT_HEADER) - > $RESULTS_FILE

STD_CONSOLE_LOG_FILES=target/result-logs/results.std-console.*.log
PERF_STAT_LOG_FILES=target/result-logs/results.perf-stat.*.log

RESULTS_FILE_PERF_STAT=target/results/results.all-real-world-$TIMESTAMP.perf-stat.log

mkdir -p `pwd`/../data/$TIMESTAMP
ARCHIVE_PATH=`echo $(cd ../data/$TIMESTAMP; pwd)`
ARCHIVE_NAME=$ARCHIVE_PATH/hamt-benchmark-results-real-world-$TIMESTAMP.tar.xz

RESULTS_FILES=target/results/results.all-real-world-$TIMESTAMP*

cp $RESULTS_FILES $ARCHIVE_PATH
cd target && tar -cfJ $ARCHIVE_NAME results result-logs *.csv
