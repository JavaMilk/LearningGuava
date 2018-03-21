# 不可变集合

## 示例

```java
public static final ImmutableSet<String> COLOR_NAMES = ImmutableSet.of(
  "red",
  "orange",
  "yellow",
  "green",
  "blue",
  "purple");

class Foo {
  final ImmutableSet<Bar> bars;
  Foo(Set<Bar> bars) {
    this.bars = ImmutableSet.copyOf(bars); // 防御拷贝
  }
}

```

## 为什么？
不可变对象具有很多有点，主要包括：
* 被不信任的库安全使用。
* 线程安全：在竞争条件下可以无风险的被多个线程使用。
* 不需要支持改变，基于这种假定可以节约时间和空间。所有不可变集合的实现都要比他们的可变兄弟实现更加内存友好。（[分析](https://github.com/DimitrisAndreou/memory-measurer/blob/master/ElementCostInDataStructures.txt)）
* 可以被当作常量使用，正如期望的那样，它将会固定保留。

创建一个对象的不可变拷贝是一种很好的防御编程技术。Guava提供简单、容易使用的每个标准`Collection` 类型的不可变版本，包括Guava自己的`Collection`变体。


在JDK中提供了`Collections.unmodifiableXXX`方法，但是在我们看来，这些会
* 笨重冗长；当你在任何地方想创建一个防御拷贝都是不爽的
* 不安全：当没有任何其他对象持有原始集合的引用时，返回的集合才是真正的不可变对象
* 低效的：在数据结构上依然包括可变对象的所有开销，包括并发修改检查，哈希表中的额外空间，等等

**当你不期望去修改一个集合，或者期望一个集合保持不变，那么将这个集合防御性的拷贝到一个不可变的集合中是一个非常好的实践.**

**重要：** 每个Guava的不可变集合实现都拒绝`null`值. 我们在Google内部代码上做了一个详尽的调研，结果表明在集合中允许`null`元素，大约有5%的时间，以及95%的例子服务于在nulls上快速失败。如果你需要使用null值，可以考虑使用`Collections.unmodifiableList`,它友好的支持在集合实现中允许null。更多详细建议可以访问[这里](https://github.com/google/guava/wiki/UsingAndAvoidingNullExplained)


## 如何？

一个`ImmutableXXX`集合可以通过多种方式创建：
* 使用`copyOf`方法，比如，`ImmutableSet.copyOf(set)`
* 使用`of`方法，比如，`ImmutableSet.of("a", "b", "c")` 或者 `ImmutableMap.of("a", 1, "b", 2)`
* 使用`Builder`,比如，

```java
public static final ImmutableSet<Color> GOOGLE_COLORS =
   ImmutableSet.<Color>builder()
       .addAll(WEBSAFE_COLORS)
       .add(new Color(0, 191, 255))
       .build();

```

除了排序集合，**集合中元素的顺序将保持创建时的顺序**。比如，
```java
ImmutableSet.of("a", "b", "c", "a", "d", "b")

```
将以"a","b","c","d"的顺序依此迭代集合中的元素。

## copyOf比你想象更加聪明

这是非常有用的去记住`ImmutableXXX.copyOf`会尝试在当数据是安全的时候避免拷贝数据--更多细节是不确定的，但是其实现是典型的"智能"。比如，
```java
ImmutableSet<String> foobar = ImmutableSet.of("foo", "bar", "baz");
thingamajig(foobar);

void thingamajig(Collection<String> collection) {
   ImmutableList<String> defensiveCopy = ImmutableList.copyOf(collection);
   ...
}
```

在这段代码中，`ImmutableList.copyOf(foobar)`将会足够智能仅返回`foobar.asList()`,这是 `Immutable`的一个不变视图。

作为常见的启发式方法，`ImmutableXXX.copyOf(ImmutableCollection)`尝试避免线性时间拷贝，如果满足以下条件：
* 如果可能在常数时间下使用数据结构。比如，`ImmutableSet.copyOf(ImmutableList)`不能在常数时间内完成.
* 不会造成内存泄漏--比如，如果你有一个`ImmutableList<String> hugeList`,同时你将做`ImmutableList.copyOf(hugeList.subList(0,10))`,一个明确的拷贝是性能优化的，因此要避免偶然的持有一个`hugeList`的引用，这是不必要的
* 不会改变语义的--因此`ImmutableSet.copyOf(myImmutableSortedSet)`将会执行一个显式的拷贝，因为`ImmutableSet`使用的`hashCode()`和`equals`与`ImmutableSortedSet`的相应方法具有的不用的语义.

这种方法帮助优秀的防御程序设计最小化性能开销.


## asList

所有的不可变集合都能通过`asList()`提供一个`ImmutableList`视图,因此即使你将数据存储在有序集合中`ImmutableSortedSet`,依然可以通过`sortedSet.asList().get(k)`来获取第k小元素。


返回`ImmutableList`是频繁的--但不是总是这样的，而是经常的--一个固定开销的视图，而不是一个显式的拷贝。也就是说，要比你的普通的`List`更加智能--比如，它将非常高效的使用背后的集合的`contains`方法.
## 详情

### 位置？

Interface                  | JDK or Guava? | Immutable Version
:------------------------- | :------------ | :------------------------------
`Collection`               | JDK           | [`ImmutableCollection`]
`List`                     | JDK           | [`ImmutableList`]
`Set`                      | JDK           | [`ImmutableSet`]
`SortedSet`/`NavigableSet` | JDK           | [`ImmutableSortedSet`]
`Map`                      | JDK           | [`ImmutableMap`]
`SortedMap`                | JDK           | [`ImmutableSortedMap`]
[`Multiset`]               | Guava         | [`ImmutableMultiset`]
`SortedMultiset`           | Guava         | [`ImmutableSortedMultiset`]
[`Multimap`]               | Guava         | [`ImmutableMultimap`]
`ListMultimap`             | Guava         | [`ImmutableListMultimap`]
`SetMultimap`              | Guava         | [`ImmutableSetMultimap`]
[`BiMap`]                  | Guava         | [`ImmutableBiMap`]
[`ClassToInstanceMap`]     | Guava         | [`ImmutableClassToInstanceMap`]
[`Table`]                  | Guava         | [`ImmutableTable`]

[using-and-avoiding-null]: UsingAndAvoidingNullExplained
[`ImmutableCollection`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableCollection.html
[`ImmutableList`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableList.html
[`ImmutableSet`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableSet.html
[`ImmutableSortedSet`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableSortedSet.html
[`ImmutableMap`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableMap.html
[`ImmutableSortedMap`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableSortedMap.html
[`Multiset`]: NewCollectionTypesExplained#Multiset
[`ImmutableMultiset`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableMultiset.html
[`ImmutableSortedMultiset`]: http://google.github.io/guava/releases/12.0/api/docs/com/google/common/collect/ImmutableSortedMultiset.html
[`Multimap`]: NewCollectionTypesExplained#Multimap
[`ImmutableMultimap`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableMultimap.html
[`ImmutableListMultimap`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableListMultimap.html
[`ImmutableSetMultimap`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableSetMultimap.html
[`BiMap`]: NewCollectionTypesExplained#BiMap
[`ImmutableBiMap`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableBiMap.html
[`ClassToInstanceMap`]: NewCollectionTypesExplained#ClassToInstanceMap
[`ImmutableClassToInstanceMap`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableClassToInstanceMap.html
[`Table`]: NewCollectionTypesExplained#Table
[`ImmutableTable`]: http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/ImmutableTable.html
