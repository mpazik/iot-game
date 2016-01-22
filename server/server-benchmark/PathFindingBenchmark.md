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