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

```
Benchmark                                  Mode  Cnt   Score   Error  Units
StringTemplateUtilsBenchmark.applyContext  avgt    7  55.539 ± 0.272  us/op
```

For an alert with 1000 enumeration items:
 - before: applying properties takes ~ 1000 *1480us ~= 1.5 seconds --> slow in the UI 
 - after: applying properties takes ~ 1000 *55us ~= 50 milliseconds

Please make sure this does not get slower.
I think there is still a 10x speed improvement opportunity here but this should be fast enough for the moment.
