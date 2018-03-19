[CaseFormat](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CaseFormat.html)
---
 
格式|例子
--|--
[LOWER_CAMEL](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CaseFormat.html#LOWER_CAMEL)|lowerCamel
[LOWER_HYPHEN](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CaseFormat.html#LOWER_HYPHEN)|lower-hyphen
[LOWER_UNDERSCORE](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CaseFormat.html#LOWER_UNDERSCORE)|lower_underscore
[UPPER_CAMEL](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CaseFormat.html#LOWER_UNDERSCORE)|UpperCamel
[UPPER_UNDERSCORE](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CaseFormat.html#UPPER_UNDERSCORE)|UPPER_UNDERSCORE
 
使用方法非常简单：
```
CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, "CONSTANT_NAME")); // returns "constantName"
```

