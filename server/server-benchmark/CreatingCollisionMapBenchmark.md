# Path finding benchmark results

## Initial implementation
Bellow are results of first naive implementation of path finding algorithm.

#### Results
```
....[Thread state: RUNNABLE]........................................................................
 77.1%  77.2% dzida.server.core.world.pathfinding.Polygon.isLineInPolygon
 16.5%  16.6% dzida.server.core.world.pathfinding.Polygon.isLineInside
  2.3%   2.3% dzida.server.core.basic.unit.Graph$Builder.put
  1.2%   1.2% dzida.server.core.world.pathfinding.CollisionMapFactory.lambda$null$2
  1.2%   1.2% dzida.server.core.world.pathfinding.BitMapTracker.lambda$null$0
  0.6%   0.6% dzida.server.core.basic.unit.BitMap.isSet
  0.2%   0.2% java.util.ArrayList.forEach
  0.2%   0.2% java.util.ArrayList$ArrayListSpliterator.forEachRemaining
  0.1%   0.1% sun.misc.Unsafe.park
  0.1%   0.1% org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call
  0.5%   0.5% <other>



# Run complete. Total time: 00:00:16

Benchmark                                                                          Mode  Cnt        Score          Error   Units
CreatingCollisionMapBenchmark.createPathFinder                                   sample   57      266.729 ±        3.769   ms/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate                    sample    3       18.569 ±        3.087  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate.norm               sample    3  5198262.737 ±   105597.212    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space           sample    3       21.027 ±      132.930  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space.norm      sample    3  5886742.456 ± 37203099.230    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space       sample    3        0.054 ±        1.616  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space.norm  sample    3    15322.386 ±   457169.224    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.count                         sample    3        5.000                 counts
CreatingCollisionMapBenchmark.createPathFinder:·gc.time                          sample    3       13.000                     ms
CreatingCollisionMapBenchmark.createPathFinder:·stack                            sample               NaN                    ---
```
```
 72.1%  72.1% dzida.server.core.world.pathfinding.Polygon.isLineInPolygon
              dzida.server.core.world.pathfinding.Polygon.isLineInside
              dzida.server.core.world.pathfinding.CollisionMapFactory.lambda$findPointsInLineOfSight$3
              dzida.server.core.world.pathfinding.CollisionMapFactory$$Lambda$15/617435921.test
              java.util.stream.ReferencePipeline$2$1.accept
              java.util.ArrayList$ArrayListSpliterator.forEachRemaining
              java.util.stream.AbstractPipeline.copyInto
              java.util.stream.AbstractPipeline.wrapAndCopyInto
              java.util.stream.ReduceOps$ReduceOp.evaluateSequential
              java.util.stream.AbstractPipeline.evaluate

 16.4%  16.4% dzida.server.core.world.pathfinding.Polygon.isLineInside
              dzida.server.core.world.pathfinding.CollisionMapFactory.lambda$findPointsInLineOfSight$3
              dzida.server.core.world.pathfinding.CollisionMapFactory$$Lambda$15/617435921.test
              java.util.stream.ReferencePipeline$2$1.accept
              java.util.ArrayList$ArrayListSpliterator.forEachRemaining
              java.util.stream.AbstractPipeline.copyInto
              java.util.stream.AbstractPipeline.wrapAndCopyInto
              java.util.stream.ReduceOps$ReduceOp.evaluateSequential
              java.util.stream.AbstractPipeline.evaluate
              java.util.stream.ReferencePipeline.collect

  4.0%   4.0% dzida.server.core.world.pathfinding.Polygon.isLineInPolygon
              dzida.server.core.world.pathfinding.Polygon.isLineOutside
              dzida.server.core.world.pathfinding.CollisionMapFactory.lambda$null$2
              dzida.server.core.world.pathfinding.CollisionMapFactory$$Lambda$16/18502331.test
              java.util.stream.MatchOps$1MatchSink.accept
              java.util.ArrayList$ArrayListSpliterator.tryAdvance
              java.util.stream.ReferencePipeline.forEachWithCancel
              java.util.stream.AbstractPipeline.copyIntoWithCancel
              java.util.stream.AbstractPipeline.copyInto
              java.util.stream.AbstractPipeline.wrapAndCopyInto
```

It turned out that most of the time is spent during creating a graph of visibility, exactly during finding points in line of sight.

## Finding point in line in sight
The algorithm for each convexPoints check with all other convexPoints if there are in line of sight.
Line of sight algorithm check if there is any thing that may block the line, which do multiple operations for each point of the map.
There is *n^2\*4\*m* complexity where *m* is number of points of the map and *n* in number of concave points of the map and object inside.

Possible options to optimize algorithm.
- Find a better way to check if a line belongs to a polygon
- Reduce number of points of a map
- Reduce number of concave points to check

A simple way to reduce number of points is to filter them by range, so line of sight will be checked only with point that are close to the given point.
Checking range is cheap so that should give an improvement.

#### Results after limiting check of line of sight to point that are in a range of 10 units.
```
Benchmark                                                                          Mode  Cnt       Score        Error   Units
CreatingCollisionMapBenchmark.createPathFinder                                   sample  895   33746.463 ±    272.483   us/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate                    sample   10      23.061 ±      0.405  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate.norm               sample   10  816880.813 ±   1222.302    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space           sample   10      22.868 ±     10.928  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space.norm      sample   10  809677.746 ± 384338.956    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space       sample   10       0.068 ±      0.277  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space.norm  sample   10    2382.733 ±   9655.469    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.count                         sample   10      12.000               counts
CreatingCollisionMapBenchmark.createPathFinder:·gc.time                          sample   10      13.000                   ms
CreatingCollisionMapBenchmark.createPathFinder:·stack                            sample              NaN                  ---

```

#### Results after limiting check of line of sight to point that are in a range of 5 units.
```
# Run complete. Total time: 00:01:21

Benchmark                                                                          Mode   Cnt       Score        Error   Units
CreatingCollisionMapBenchmark.createPathFinder                                   sample  3049   19742.586 ±    114.876   us/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate                    sample    20      32.203 ±      0.653  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate.norm               sample    20  667458.518 ±    306.952    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space           sample    20      32.153 ±      4.701  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space.norm      sample    20  667390.095 ± 101774.510    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space       sample    20       0.018 ±      0.013  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space.norm  sample    20     374.283 ±    272.055    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.count                         sample    20      48.000               counts
CreatingCollisionMapBenchmark.createPathFinder:·gc.time                          sample    20      30.000                   ms
CreatingCollisionMapBenchmark.createPathFinder:·stack                            sample               NaN                  ---
```

#### Findings
This optimisation reduced the time of building map 8-12 times and memory by 100/300 times. 
Decreasing range doesn't add much benefits in terms of performance as there are other important factors, 
however it reduce significantly amount of used memory. Building visibility graph is a place that allocate a lot of memory.

## Tracking polygon in bitmap
Event it's not relevant but maybe there is some quick win there.

#### Results using streams
```
....[Thread state: RUNNABLE]........................................................................
 61.5%  61.5% java.util.ArrayList$ArrayListSpliterator.tryAdvance
 32.9%  32.9% dzida.server.core.basic.unit.BitMap.isSet
  3.3%   3.3% java.util.ArrayList$ArrayListSpliterator.estimateSize
  1.0%   1.0% dzida.server.core.basic.unit.BitMap.forEach
  0.8%   0.8% java.util.stream.ReferencePipeline.forEachWithCancel
  0.2%   0.2% sun.misc.Unsafe.unpark
  0.1%   0.1% dzida.server.core.basic.unit.BitMap$InverseBitMap.isSetUnsafe
  0.1%   0.1% java.util.stream.StreamSupport.stream
  0.1%   0.1% java.util.ArrayList.spliterator
  0.1%   0.1% sun.reflect.Reflection.getClassAccessFlags



# Run complete. Total time: 00:00:25

Benchmark                                                                          Mode   Cnt       Score        Error   Units
CreatingCollisionMapBenchmark.createPathFinder                                   sample  2903    5168.789 ±     52.166   us/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate                    sample     3     175.328 ±     24.162  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate.norm               sample     3  951489.171 ±     23.305    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space           sample     3     176.212 ±     12.131  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space.norm      sample     3  956336.818 ± 186023.088    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space       sample     3       0.101 ±      0.344  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space.norm  sample     3     550.072 ±   1939.458    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.count                         sample     3      84.000               counts
CreatingCollisionMapBenchmark.createPathFinder:·gc.time                          sample     3      44.000                   ms
CreatingCollisionMapBenchmark.createPathFinder:·stack                            sample               NaN                  ---
```

It seams that streams are very expensive

#### Results after replacing streams by foreach
```
....[Thread state: RUNNABLE]........................................................................
 64.2%  64.2% dzida.server.core.basic.unit.BitMap.forEach
 33.5%  33.5% dzida.server.core.basic.unit.BitMap.isSet
  1.2%   1.2% java.util.ArrayList.iterator
  0.4%   0.4% dzida.server.core.world.pathfinding.BitMapTracker.trackPath
  0.2%   0.2% dzida.server.core.basic.unit.BitMap$InverseBitMap.isSetUnsafe
  0.1%   0.1% sun.misc.Unsafe.putObject
  0.1%   0.1% sun.reflect.NativeMethodAccessorImpl.invoke0
  0.1%   0.1% java.lang.System.nanoTime
  0.1%   0.1% java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire
  0.1%   0.1% dzida.server.core.basic.unit.PointList.builder



# Run complete. Total time: 00:00:25

Benchmark                                                                          Mode   Cnt       Score        Error   Units
CreatingCollisionMapBenchmark.createPathFinder                                   sample  2994    5007.424 ±     50.169   us/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate                    sample     3      42.826 ±      4.060  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate.norm               sample     3  225141.295 ±    202.409    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space           sample     3      40.304 ±     83.052  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space.norm      sample     3  211904.168 ± 439796.562    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space       sample     3       0.042 ±      0.237  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space.norm  sample     3     218.730 ±   1232.405    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.count                         sample     3      11.000               counts
CreatingCollisionMapBenchmark.createPathFinder:·gc.time                          sample     3       6.000                   ms
CreatingCollisionMapBenchmark.createPathFinder:·stack                            sample               NaN                  ---
```
Replacing them didn't give much benefits. They just mislead stacktrace profiler. 

#### Results of using unsafe isSet
```
....[Thread state: RUNNABLE]........................................................................
 98.5%  98.6% dzida.server.core.basic.unit.BitMap.forEach
  0.9%   0.9% dzida.server.core.basic.unit.BitMap.isSet
  0.1%   0.1% dzida.server.core.world.pathfinding.BitMapTracker.lambda$track$0
  0.1%   0.1% java.util.concurrent.locks.AbstractQueuedSynchronizer.compareAndSetState
  0.1%   0.1% java.lang.System.nanoTime
  0.1%   0.1% org.sample.generated.CreatingCollisionMapBenchmark_createPathFinder_jmhTest.createPathFinder_sample_jmhStub
  0.1%   0.1% org.openjdk.jmh.infra.BenchmarkParamsL2.getOpsPerInvocation
  0.1%   0.1% dzida.server.core.world.pathfinding.BitMapTracker.trackPath
  0.1%   0.1% dzida.server.core.basic.unit.PointList.builder



# Run complete. Total time: 00:00:25

Benchmark                                                                          Mode   Cnt      Score                 Error   Units
CreatingCollisionMapBenchmark.createPathFinder                                   sample  3155   4754.861 ±              45.559   us/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate                    sample     3        ≈ 0                        MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate.norm               sample     3        ≈ 0                          B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space           sample     3      8.525 ±             134.684  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space.norm      sample     3  42366.745 ±          669375.445    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space       sample     3      0.039 ±               1.245  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space.norm  sample     3    196.102 ±            6196.649    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.count                         sample     3      2.000                        counts
CreatingCollisionMapBenchmark.createPathFinder:·gc.time                          sample     3      5.000                            ms
CreatingCollisionMapBenchmark.createPathFinder:·stack                            sample              NaN                           ---
```
Probably lambda in forEach is consuming most of the time.

#### Results after in-lining forEach with lambda 
```
....[Thread state: RUNNABLE]........................................................................
 97.3%  97.3% dzida.server.core.world.pathfinding.BitMapTracker.track
  1.5%   1.5% dzida.server.core.basic.unit.BitMap.isSet
  0.4%   0.4% dzida.server.core.world.pathfinding.BitMapTracker.trackPath
  0.2%   0.2% dzida.server.core.basic.unit.PointList.builder
  0.2%   0.2% org.sample.generated.CreatingCollisionMapBenchmark_createPathFinder_jmhTest.createPathFinder_sample_jmhStub
  0.1%   0.1% dzida.server.core.basic.unit.BitMap$InverseBitMap.isSetUnsafe
  0.1%   0.1% org.openjdk.jmh.runner.BenchmarkHandler$BenchmarkTask.call
  0.1%   0.1% dzida.server.core.basic.unit.PointList$Builder.build
  0.1%   0.1% java.lang.Thread.isInterrupted
  0.1%   0.1% sun.misc.Unsafe.compareAndSwapInt



# Run complete. Total time: 00:00:25

Benchmark                                                                          Mode   Cnt      Score        Error   Units
CreatingCollisionMapBenchmark.createPathFinder                                   sample  3113   4818.642 ±     45.033   us/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate                    sample     3      8.468 ±      3.310  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate.norm               sample     3  42841.423 ±    976.268    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space           sample     3      8.522 ±    134.637  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space.norm      sample     3  43390.131 ± 685949.603    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space       sample     3      0.043 ±      1.344  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space.norm  sample     3    212.241 ±   6706.609    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.count                         sample     3      2.000               counts
CreatingCollisionMapBenchmark.createPathFinder:·gc.time                          sample     3      4.000                   ms
CreatingCollisionMapBenchmark.createPathFinder:·stack                            sample              NaN                  ---
```
Now we see that tracking actually take all the time. Lets try to optimize it.

#### Results after removing unnecessary point allocation
```
....[Thread state: RUNNABLE]........................................................................
 97.9%  98.0% dzida.server.core.world.pathfinding.BitMapTracker.track
  1.7%   1.7% dzida.server.core.basic.unit.BitMap.isSet
  0.2%   0.2% dzida.server.core.world.pathfinding.BitMapTracker.trackPath
  0.1%   0.1% java.lang.Thread.currentThread
  0.1%   0.1% sun.misc.Unsafe.compareAndSwapInt



# Run complete. Total time: 00:00:25

Benchmark                                                                          Mode   Cnt      Score        Error   Units
CreatingCollisionMapBenchmark.createPathFinder                                   sample  3175   4725.147 ±     44.825   us/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate                    sample     3      8.626 ±      1.361  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.alloc.rate.norm               sample     3  42791.312 ±    503.881    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space           sample     3      8.522 ±    134.639  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Eden_Space.norm      sample     3  42369.785 ± 669495.502    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space       sample     3      0.043 ±      1.344  MB/sec
CreatingCollisionMapBenchmark.createPathFinder:·gc.churn.PS_Survivor_Space.norm  sample     3    213.254 ±   6738.637    B/op
CreatingCollisionMapBenchmark.createPathFinder:·gc.count                         sample     3      2.000               counts
CreatingCollisionMapBenchmark.createPathFinder:·gc.time                          sample     3      3.000                   ms
CreatingCollisionMapBenchmark.createPathFinder:·stack                            sample              NaN                  ---
```
That didn't give much performance but reduced allocated memory by half.

#### Findings
No idea how to simple fix it. Simple actions reduced time by a 10% which is not much.