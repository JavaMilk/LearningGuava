[Splitter](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html)
---

# 使用示例
以下参考：[官方文档](https://github.com/google/guava/wiki/StringsExplained#splitter)。

## Splitter
### 概述
Java 中关于分词的工具类会有一些古怪的行为。例如：`String.split` 函数会悄悄地丢弃尾部分割符，而 `StringTokenizer` 处理5个空格字符串，结果将会什么都没有。 

问题：`",a,,b,".split(",")` 的结果是什么？
1. "", "a", "", "b", ""
2. `null`, "a", `null`, "b", `null`
3. "a", `null`, "b"
4. "a", "b"
5. 以上都不是

正确答案是：5 以上都不是，应该是 `"", "a", "", "b"`。只有尾随的空字符串被跳过。这样的结果很令人费解。

Splitter 可以让你使用一种非常简单流畅的模式来控制这些令人困惑的行为。
```
Splitter.on(',')
    .trimResults()
    .omitEmptyStrings()
    .split("foo,bar,,   qux");
```

以上代码将会返回 `Iterable<String>` ，包含 "foo"、 "bar"、 "qux"。一个 `Splitter`  可以通过这些来进行划分：`Pattern`、`char`、 `String`、`CharMatcher`。

如果你希望返回的是 `List` 的话，可以使用这样的代码 `Lists.newArrayList(splitter.split(string))`。

### 工厂函数

方法|描述|例子
---|---|---
[`Splitter.on(char)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html#on-char-) | 基于特定字符划分 | `Splitter.on(';')`
[`Splitter.on(CharMatcher)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html#on-com.google.common.base.CharMatcher-) | 基于某些类别划分 | `Splitter.on(';')`
[`Splitter.on(String)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html#on-java.lang.String-) | 基于字符串划分 | `Splitter.on(CharMatcher.BREAKING_WHITESPACE)`<br>`Splitter.on(CharMatcher.anyOf(";,."))`
[`Splitter.on(Pattern)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html#on-java.util.regex.Pattern-)<br>[`Splitter.onPattern(String)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html#onPattern-java.lang.String-)| 基于正则表达式划分 | `Splitter.on(", ")`
[`Splitter.fixedLength(int)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html#fixedLength-int-) | 按指定长度划分，最后部分可以小于指定长度但不能为空 | `Splitter.fixedLength(3)`

### 修改器

方法|描述|例子
---|---|---
[`omitEmptyStrings()`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html#omitEmptyStrings--)|移去结果中的空字符串| `Splitter.on(',').omitEmptyStrings().split("a,,c,d")` 返回 `"a", "c", "d"`
[`trimResults()`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html#trimResults--)|将结果中的空格删除，等价于`trimResults(CharMatcher.WHITESPACE)`|`Splitter.on(',').trimResults().split("a, b, c, d")` 返回 `"a", "b", "c", "d"`
[`trimResults(CharMatcher)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html#trimResults-com.google.common.base.CharMatcher-)|移除匹配字符|`Splitter.on(',').trimResults(CharMatcher.is('_')).split("_a ,_b_ ,c__")` 返回 `"a ", "b_ ", "c"`
[`limit(int)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Splitter.html#limit-int-)|达到指定数目后停止字符串的划分|`Splitter.on(',').limit(3).split("a,b,c,d")` 返回 `"a", "b", "c,d"`

## Splitter.MapSplitter
以下参考：[Guava 是个风火轮之基础工具(2)](http://www.importnew.com/15227.html)。

过 `Splitter` 的 `withKeyValueSeparator` 方法可以获得 `MapSplitter` 对象。 `MapSplitter` 是 `Splitter` 的内部类，需要表示成 `Joiner.MapJoiner`。

`MapSpliter` 只有一个公共方法，如下所示。可以看到返回的对象是 `Map<String, String>`。
```
public Map<String, String> split(CharSequence sequence)
```

以下代码将返回这样的 `Map`: `{"1":"2", "3":"4"}`。
```
Splitter.on("#").withKeyValueSeparator(":").split("1:2#3:4");
```

需要注意的是，`MapSplitter` 对键值对个格式有着严格的校验。下面的拆分会抛出异常 `java.lang.IllegalArgumentException`。
```
Splitter.on("#").withKeyValueSeparator(":").split("1:2#3:4:5"); 
```

因此，如果希望使用 `MapSplitter` 来拆分 KV 结构的字符串，需要保证键-值分隔符和键值对之间的分隔符不会称为键或值的一部分。也许是出于类似方面的考虑，MapSplitter 被加上了 `@Beta` 注解（未来不保证兼容，甚至可能会移除）。所以一般推荐使用 `JSON` 而不是 `MapJoiner` + `MapSplitter`。

# 源码分析
以下参考：[Guava 是个风火轮之基础工具(2)](http://www.importnew.com/15227.html)。

`Splitter` 的实现中有十分明显的策略模式和模板模式，有各种神乎其技的方法覆盖，还有 Guava 久负盛名的迭代技巧和惰性计算。

## 成员变量
`Splitter` 类有 4 个成员变量:
* `CharMatcher trimmer`：用于描述删除拆分结果的前后指定字符的策略。
* `boolean omitEmptyStrings`：用于控制是否删除拆分结果中的空字符串。
* `Strategy strategy`：用于帮助实现策略模式。
* `int limit`：用于控制拆分的结果个数。

## 策略模式
`Splitter` 可以根据字符、字符串、正则、长度还有 Guava 自己的字符匹配器 `CharMatcher` 来拆分字符串，基本上每种匹配模式的查找方法都不太一样，但是字符拆分的基本框架又是不变的，所以策略模式正好合用。

策略接口的定义很简单，就是传入一个 `Splitter` 和一个待拆分的字符串，返回一个迭代器。
```
  private interface Strategy {
    Iterator<String> iterator(Splitter splitter, CharSequence toSplit);
  }
```

每个工厂函数创建最后都需要去调用基本的私有函数：
```
  private Splitter(Strategy strategy, boolean omitEmptyStrings, CharMatcher trimmer, int limit)；
```

这个创建过程中，最基本的部分就是实现一个 `Strategy` 中的 `Iterator<String>`。以 `Splitter on(final CharMatcher separatorMatcher)` 创建函数为例：
```
  public static Splitter on(final CharMatcher separatorMatcher) {
    checkNotNull(separatorMatcher);
    return new Splitter(
        new Strategy() {
          @Override
          public SplittingIterator iterator(Splitter splitter, final CharSequence toSplit) {
            return new SplittingIterator(splitter, toSplit) {
              @Override
              int separatorStart(int start) {
                return separatorMatcher.indexIn(toSplit, start);
              }

              @Override
              int separatorEnd(int separatorPosition) {
                return separatorPosition + 1;
              }
            };
          }
        });
  }
```

这里返回的是 `SplittingIterator` (它是个抽象类，继承了 `AbstractIterator`，而 `AbstractIterator` 继承了 `Iterator`），需要覆盖实现 `separatorStart` 和 `separatorEnd` 两个方法才能实例化。这两个方法是 `SplittingIterator` 用到的模板模式的重要组成。

## 使用 continue 跳转到指定位置
以下代码是找到分隔符第一个起始的地方，其中使用了 `continue positions` 跳到了指定地方。
```
	public int separatorStart(int start) {
        int separatorLength = separator.length();

        positions:
        for (int p = start, last = toSplit.length() - separatorLength; p <= last; p++) {
            for (int i = 0; i < separatorLength; i++) {
                if (toSplit.charAt(i + p) != separator.charAt(i)) {
                    continue positions;
                }
            }
            return p;
        }
        return -1;
    }
```

## 惰性迭代器与模板模式
[惰性计算](https://baike.baidu.com/item/%E6%83%B0%E6%80%A7%E8%AE%A1%E7%AE%97/3081216)目的是要最小化计算机要做的工作，即把计算推迟到不得不算的时候进行。Java中的惰性计算可以参考[《你应该更新的 Java 知识之惰性求值：Supplier 和 Guava》](https://my.oschina.net/bairrfhoinn/blog/142985)。

Guava 中的迭代器使用了惰性计算的技巧，它不是一开始就算好结果放在列表或集合中，而是在调用 `hasNext` 方法判断迭代是否结束时才去计算下一个元素。

### AbstractIterator
为了看懂 Guava 的惰性迭代器实现，我们要从 `AbstractIterator` 开始。

`AbstractIterator` 使用一个私有的枚举变量 `state` 来记录当前的迭代进度，比如是否找到了下一个元素，迭代是否结束等等。
```
  private enum State {
    READY,
    NOT_READY,
    DONE,
    FAILED,
  }
```

`AbstractIterator` 给出了一个抽象方法 `computeNext`，计算下一个元素。由于 `state` 是私有变量，而迭代是否结束只有在调用 `computeNext` 的过程中才知道，于是我们有了一个保护的 `endOfData` 方法，允许 `AbstractIterator` 的子类将 `state` 设置为 `State.DONE`。

`AbstractIterator` 实现了迭代器最重要的两个方法，`hasNext` 和 `next`。
```
  @Override
  public final boolean hasNext() {
    checkState(state != State.FAILED);
    switch (state) {
      case READY:
        return true;
      case DONE:
        return false;
      default:
    }
    return tryToComputeNext();
  }
  
   @Override
    public final T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      state = State.NOT_READY;
      T result = next;
      next = null;
      return result;
    }
```

`hasNext` 很容易理解，一上来先判断迭代器当前状态，如果已经结束，就返回 `false`；如果已经找到下一个元素，就返回 `true`，不然就试着找找下一个元素。

`next` 则是先判断是否还有下一个元素，属于防御式编程，先对自己做保护；然后把状态复原到还没找到下一个元素，然后返回结果。至于为什么要把 `next` 置为 `null`，可能是帮助 JVM 回收对象。

```
private boolean tryToComputeNext() {
    state = State.FAILED; // 暂时悲观
    next = computeNext();
    if (state != State.DONE) {
      state = State.READY;
      return true;
    }
    return false;
  }
 ```
 
`tryToComputeNext` 可以认为是对模板方法 `computeNext` 的包装调用，首先把状态置为失败，然后才调用 computeNext。这样一来，如果计算下一个元素的过程中发生 `RuntimeException`，整个迭代器的状态就是 `State.FAILED`，一旦收到任何调用都会抛出异常。
 
`AbstractIterator` 的代码就这些，我们现在知道了它的子类需要覆盖实现 `computeNext` 方法，然后在迭代结束时调用 `endOfData`。接下来看看 `SplittingIterator` 的实现。
 
### SplittingIterator 
`SplittingIterator` 还是一个抽象类，虽然实现了 `computeNext` 方法，但是它又定义了两个虚函数:
* `separatorStart`： 返回分隔符在指定下标之后第一次出现的下标
* `separatorEnd`： 返回分隔符在指定下标后面第一个不包含分隔符的下标。

之前的策略模式中我们可以看到，这两个函数在不同的策略中有各自不同的覆盖实现，在 `SplittingIterator` 中，这两个函数就是模板函数。
 
接下来我们看看 `SplittingIterator` 的核心函数 `computeNext`，注意这个函数一直在维护的两个内部全局变量: `offset` 和 `limit`。
```
  @Override
    protected String computeNext() {
      /*
	   * 返回的字符串介于上一个分隔符和下一个分隔符之间。
	   * nextStart 是返回子串的起始位置，offset 是下次开启寻找分隔符的地方。 
       */
      int nextStart = offset;
      while (offset != -1) {
        int start = nextStart;
        int end;

		// 找 offset 之后第一个分隔符出现的位置
        int separatorPosition = separatorStart(offset);
        if (separatorPosition == -1) {
		  // 处理没找到的情况
          end = toSplit.length();
          offset = -1;
        } else {
		  // 处理找到的情况
          end = separatorPosition;
          offset = separatorEnd(separatorPosition);
        }
		
		// 处理的是第一个字符就是分隔符的特殊情况
        if (offset == nextStart) {
          /*
		   * 发生情况：空字符串 或者 整个字符串都没有匹配。
           * offset 需要增加来寻找这个位置之后的分隔符，
		   * 但是没有改变接下来返回字符串的 start 的位置，
		   * 所以此时它们二者相同。
           */
          offset++;
          if (offset > toSplit.length()) {
            offset = -1;
          }
          continue;
        }

		// 根据 trimmer 来对找到的元素做前处理，比如去除空白符之类的。
        while (start < end && trimmer.matches(toSplit.charAt(start))) {
          start++;
        }
		// 根据 trimmer 来对找到的元素做后处理，比如去除空白符之类的。
        while (end > start && trimmer.matches(toSplit.charAt(end - 1))) {
          end--;
        }
		// 根据需要去除那些是空字符串的元素，trim完之后变成空字符串的也会被去除。
        if (omitEmptyStrings && start == end) {
          // Don't include the (unused) separator in next split string.
          nextStart = offset;
          continue;
        }

		// 判断 limit，
        if (limit == 1) {
          // The limit has been reached, return the rest of the string as the
          // final item. This is tested after empty string removal so that
          // empty strings do not count towards the limit.
          end = toSplit.length();
		  // 调整 end 指针的位置标记 offset 为 -1，下一次再调用 computeNext 
		  // 的时候就发现 offset 已经是 -1 了，然后就返回 endOfData 表示迭代结束。
          offset = -1;
          // Since we may have changed the end, we need to trim it again.
          while (end > start && trimmer.matches(toSplit.charAt(end - 1))) {
            end--;
          }
        } else {
		  // 还没到 limit 的极限，就让 limit 自减
          limit--;
        }

        return toSplit.subSequence(start, end).toString();
      }
      return endOfData();
    }
  }
```


