# Path finding benchmark results

## Objectinve approach to geometric primitives.
Bellow are results of first naive implementation of path finding algorithm.

#### Results
```
....[Thread state: RUNNABLE]........................................................................
 37.8%  37.8% dzida.server.core.world.pathfinding.Polygon.intersectionWithPolygonLines
 12.4%  12.4% dzida.server.core.world.pathfinding.Polygon.isOnBorder
 10.9%  10.9% dzida.server.core.world.pathfinding.AStar.findShortestPath
  6.2%   6.2% dzida.server.core.world.pathfinding.Polygon.intersectInside
  4.7%   4.7% java.util.HashMap.hash
  4.4%   4.4% com.google.common.collect.ImmutableMultimap$Builder.putAll
  3.7%   3.7% java.util.PriorityQueue.siftDownUsingComparator
  2.4%   2.4% java.util.HashMap.getNode
  1.8%   1.8% com.google.common.collect.ImmutableList.copyOf
  1.6%   1.6% java.util.PriorityQueue.siftUp
 14.2%  14.2% <other>



# Run complete. Total time: 00:01:21

Benchmark                                                                 Mode    Cnt       Score      Error   Units
PathFindingBenchmark.lineIntersection                                   sample  39498    1014.648 ±    4.077   us/op
PathFindingBenchmark.lineIntersection:·gc.alloc.rate                    sample     40     647.930 ±    9.724  MB/sec
PathFindingBenchmark.lineIntersection:·gc.alloc.rate.norm               sample     40  690632.139 ±   11.831    B/op
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Eden_Space           sample     40     648.572 ±   12.890  MB/sec
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Eden_Space.norm      sample     40  691285.232 ± 8245.911    B/op
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Survivor_Space       sample     40       0.261 ±    0.070  MB/sec
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Survivor_Space.norm  sample     40     278.631 ±   75.708    B/op
PathFindingBenchmark.lineIntersection:·gc.count                         sample     40     651.000             counts
PathFindingBenchmark.lineIntersection:·gc.time                          sample     40     342.000                 ms
PathFindingBenchmark.lineIntersection:·stack                            sample                NaN                ---
```

#### Findings
Surprisingly despite of lot of inefficiency algorithm perform not so bad.
As expected most of the time is spend on checking intersections with polygon.
Huge amount of allocated memory (675 KB) seem to be the most important issue.

## Used primitives for geometry calculation
List object has been replaced by static function that calculates on primitives.

#### Results
```
 29.5%  29.5% dzida.server.core.world.pathfinding.Polygon.isLineInside
 23.7%  23.7% dzida.server.core.world.pathfinding.Polygon.isLineInPolygon
 16.3%  16.3% dzida.server.core.world.pathfinding.AStar.findShortestPath
  6.5%   6.5% com.google.common.collect.ImmutableMultimap$Builder.putAll
  3.9%   3.9% java.util.PriorityQueue.siftDownUsingComparator
  3.2%   3.2% java.util.PriorityQueue.siftUpUsingComparator
  2.6%   2.6% java.util.HashMap.hash
  2.6%   2.6% java.util.HashMap.getNode
  2.3%   2.3% com.google.common.collect.ImmutableList.copyOf
  1.2%   1.2% java.util.HashMap.putVal
  8.2%   8.2% <other>



# Run complete. Total time: 00:00:40

Benchmark                                                                 Mode    Cnt       Score       Error   Units
PathFindingBenchmark.lineIntersection                                   sample  23445     854.990 ±     4.276   us/op
PathFindingBenchmark.lineIntersection:·gc.alloc.rate                    sample     20     763.838 ±    20.179  MB/sec
PathFindingBenchmark.lineIntersection:·gc.alloc.rate.norm               sample     20  685827.283 ±    21.644    B/op
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Eden_Space           sample     20     764.111 ±    31.368  MB/sec
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Eden_Space.norm      sample     20  685946.034 ± 18690.275    B/op
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Survivor_Space       sample     20       0.219 ±     0.098  MB/sec
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Survivor_Space.norm  sample     20     195.964 ±    85.357    B/op
PathFindingBenchmark.lineIntersection:·gc.count                         sample     20     331.000              counts
PathFindingBenchmark.lineIntersection:·gc.time                          sample     20     177.000                  ms
PathFindingBenchmark.lineIntersection:·stack                            sample                NaN                 ---
```


#### Findings
Algorithm become slightly faster but amount of allocated memory didn't drop. As it runs out memory is mostly allocated by A* algorithm.

Benchmark of path finding of cached graph (no graph building). 
```
Benchmark                                                                 Mode    Cnt       Score       Error   Units
PathFindingBenchmark.lineIntersection                                   sample  46554     215.050 ±     1.121   us/op
PathFindingBenchmark.lineIntersection:·gc.alloc.rate                    sample     10    2415.951 ±    59.764  MB/sec
PathFindingBenchmark.lineIntersection:·gc.alloc.rate.norm               sample     10  545884.002 ±     7.733    B/op
```

## Used arry in graph insead of hash map

#### Results
```
....[Thread state: RUNNABLE]........................................................................
 43.1%  43.1% dzida.server.core.world.pathfinding.Polygon.isLineInside
 37.5%  37.5% dzida.server.core.world.pathfinding.Polygon.isLineInPolygon
  3.4%   3.4% dzida.server.core.basic.unit.Graph.builder
  3.2%   3.2% dzida.server.core.basic.unit.Graph$Builder.put
  2.8%   2.8% dzida.server.core.world.pathfinding.AStar.findShortestPath
  2.8%   2.8% java.util.PriorityQueue.siftDown
  2.1%   2.1% java.util.PriorityQueue.siftUpUsingComparator
  1.3%   1.3% dzida.server.core.basic.unit.Graph$Builder.build
  1.0%   1.0% java.util.PriorityQueue.siftDownUsingComparator
  0.6%   0.6% dzida.server.core.world.pathfinding.Polygon.isLineOutside
  2.2%   2.2% <other>



# Run complete. Total time: 00:01:20

Benchmark                                                                 Mode     Cnt      Score      Error   Units
PathFindingBenchmark.lineIntersection                                   sample  115166    521.022 ±    1.128   us/op
PathFindingBenchmark.lineIntersection:·gc.alloc.rate                    sample      20     90.904 ±    1.939  MB/sec
PathFindingBenchmark.lineIntersection:·gc.alloc.rate.norm               sample      20  49715.974 ±    8.412    B/op
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Eden_Space           sample      20     91.007 ±    3.935  MB/sec
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Eden_Space.norm      sample      20  49764.969 ± 1727.803    B/op
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Survivor_Space       sample      20      0.077 ±    0.023  MB/sec
PathFindingBenchmark.lineIntersection:·gc.churn.PS_Survivor_Space.norm  sample      20     42.319 ±   12.384    B/op
PathFindingBenchmark.lineIntersection:·gc.count                         sample      20    177.000             counts
PathFindingBenchmark.lineIntersection:·gc.time                          sample      20     94.000                 ms
PathFindingBenchmark.lineIntersection:·stack                            sample                NaN                ---
```

#### Finding
WoW!. That was that. Memory usage dropped more than 10 times. Execution time is two time faster. There is still room for improvement in graph building.