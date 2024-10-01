## How to run
Build 
```
./mvnw clean install -DskipTests
./mvnw clean verify -pl 'thirdeye-benchmarks'
```

Run benchmark
```
java -jar thirdeye-benchmarks/target/benchmarks.jar
# or with a regex filter on ClassName.methodName
java -jar thirdeye-benchmarks/target/benchmarks.jar applyContext 
```

## Speed-Optimizations
### 1 - StringTemplateUtils - Template properties
Problem: StringTemplateUtils#applyContext - the method to apply properties to template - was extremely slow.
An ObjectMapper was instantiated at each call, so the ser/deserializer obtained by reflection (slow) were re-computed each time.
Optimization: re-use the ObjectMapper. It's a bit tricky because we use custom ser/deser to inject values. Values change at each call. 
So we mutate the custom ser/deserializer of the re-used ObjectMapper at each call. This is not thread safe so we introduce a pool of re-usable ObjectMappers. 

Before:
```
Benchmark                                  Mode  Cnt     Score    Error  Units
StringTemplateUtilsBenchmark.applyContext  avgt    7  1480.610 ± 56.571  us/op
```

After:
```
Benchmark                                  Mode  Cnt   Score   Error  Units
StringTemplateUtilsBenchmark.applyContext  avgt    7  55.539 ± 0.272  us/op
```

For an alert with 1000 enumeration items:
 - before: applying properties takes ~ 1000 *1480us ~= 1.5 seconds --> slow in the UI 
 - after: applying properties takes ~ 1000 *55us ~= 50 milliseconds

Please make sure this does not get slower.
I think there is still a 10x speed improvement opportunity here but this should be fast enough for the moment.


### 2 - LoadTemplatesBenchmark - Caching default templates or not ? 
Problem: when the scheduler starts, it updates templates for all namespaces. See ResourcesBootstrapService#bootstrap.
When the number of namespaces is big, the question is whether reading templates from file (the "file" will actually be in the jar) for each namespace will be slow.  

We compare two approaches:
`loadWithNoCache`: reload from "file" every time.
`loadWithCacheAndCopy`: load from "file" once, then copy with ObjectMapper.

```
Benchmark                                    (numNamespaces)  Mode  Cnt      Score     Error  Units
LoadTemplatesBenchmark.loadWithNoCache                 10     avgt    7   7861.987 ± 118.917  us/op
LoadTemplatesBenchmark.loadWithCacheAndCopy            10     avgt    7   4679.431 ±   8.247  us/op

LoadTemplatesBenchmark.loadWithNoCache                100     avgt    7  75868.979 ± 618.079  us/op
LoadTemplatesBenchmark.loadWithCacheAndCopy           100     avgt    7  41180.665 ± 233.718  us/op
```

Analysis:   
Reading from file or performing a copy with an ObjectMapper is the same order of magnitude in terms of speed.
The speed boost with caching is not worth it and introduces complexity.
The bottleneck will most likely be the writes to the db.

Note that it's likely the difference would be bigger in the enterprise ThirdEye case, 
because some templates are generated on the fly. Assumption is it would still be the same order of magnitude. 

Conclusion:  
The update for all namespaces should be multi-threaded to reduce the db write bottleneck. 
No need to optimize the read of templates.




