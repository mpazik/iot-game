
# Test naming convention

### Test class name 
`{class}Test`.

### Method names 
`{method}_{conditions}_{expected-value}`. 
It may be read as `{method} when {expected-value} should result {conditions}`.

Example: `getIntersection_linesAreIntersecting_intersectionPointIsDefined

If `HierarchicalContextRunner` is used then subclass names comes from method name and conditions.