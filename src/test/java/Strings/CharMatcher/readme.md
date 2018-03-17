[CharMatcher](https://github.com/google/guava/wiki/StringsExplained#charmatcher)
---
以下参考：
* [官方文档](https://github.com/google/guava/wiki/StringsExplained#charmatcher)。
* [Guava 是个风火轮之基础工具(3)](http://www.importnew.com/15230.html)。

# 概览
之前，Guava 中的 `StringUtil` 在无节制地增长，具有很多方法，如：
* `allAscii`
* `collapse`
* `collapseControlChars`
* `collapseWhitespace`
* `lastIndexNotOf`
* `numSharedChars`
* `removeChars`
* `removeCrLf`
* `retainAllChars`
* `strip`
* `stripAndCollapse`
* `stripNonDigits`

这些函数本质上是以下两个方面的乘积（M x N 种情况）：
1. 何如定义一个“匹配”的字符？ （M 种情况）
2. 对“匹配”的字符进行怎样的操作？ （N 种情况）

为了解决这样的爆炸式增长，Guava 提供了 `CharMatcher`。一个 `CharMacher` 实例本身，界定了一个匹配字符的集合，而 CharMacher 实例的方法，解决了要对匹配字符做什么的问题。然后我们就可以用最小化的 API 来处理字符匹配和字符操作，__把 M×N 的复杂度下降到了 M+N__。

直观地说，你可以把 `CharMatcher` 看做是一些特别字符串的表示，例如：数字、空格等。而事实上，`CharMatcher` 只是一个针对字符串的布尔断言（它实现了 `Predicate<Character>`），但考虑到“所有空白字符串”、“所有小写单词”等相关需求是很普遍的，Guava 还是为字符串提供了专门的语法和 API。

`CharMatcher` 的功能主要在于对特定类或字符串执行这些操作：`trimming`、`collapsing`、`removing`、`retaining` 等。

# 使用示例

## 创建 CharMatcher
很多需求都可以被 `CharMatcher ` 的工厂方法满足：
* [any()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#any--)
* [none()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#none--)
* [whitespace()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#whitespace--)
* [breakingWhitespace()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#breakingWhitespace--)
* [invisible()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#invisible--)
* [digit()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#digit--)
* [javaLetter()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#javaLetter--)
* [javaDigit()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#javaDigit--)
* [javaLetterOrDigit()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#javaLetterOrDigit--)
* [javaIsoControl()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#javaIsoControl--)
* [javaLowerCase()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#javaLowerCase--)
* [javaUpperCase()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#javaUpperCase--)
* [ascii()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#ascii--)
* [singleWidth()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#singleWidth--)

其它一些常用的获得一个 `CharMatcher` 的方法包括：

方法|描述
---|---
[anyOf(CharSequence)](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#anyOf-java.lang.CharSequence-)|表明你想匹配的所有字符，例如：`CharMatcher.anyOf("aeiou")` 可以匹配小写元音字母。
[is(char)](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#is-char-)| 表明你想匹配的一个确定的字符。
[inRange(char, char)](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#inRange-char-char-)| 表明你想匹配的一个字符范围，例如：`CharMatcher.inRange('a', 'z')`。

此外，`CharMatcher` 还有这些方法：[ negate()](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#negate--)、[and(CharMatcher)](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#and-com.google.common.base.CharMatcher-)、[or(CharMatcher)](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#or-com.google.common.base.CharMatcher-)。这些方法可以为 `CharMatcher` 提供方便的布尔运算。

## 使用 CharMatcher
`CharMatcher` 提供了很多方法来对匹配的字符序列 `CharSequence` 进行操作。以下只是列出了一些常用方法。

方法|描述
---|---
[`collapseFrom(CharSequence, char)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#collapseFrom-java.lang.CharSequence-char-)| 将一组连续匹配的字符串替换为一个指定的字符。例如：`WHITESPACE.collapseFrom(string, ' ')` 可以将连续的空字符串替换为单个字符。
[`matchesAllOf(CharSequence)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#matchesAllOf-java.lang.CharSequence-)|测试字符序列是否全部匹配。例如：`ASCII.matchesAllOf(string)` 可以测试字符是否全部是 ASCII。
[`removeFrom(CharSequence)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#removeFrom-java.lang.CharSequence-)|将匹配的字符序列移除
[`retainFrom(CharSequence)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#retainFrom-java.lang.CharSequence-)|将没有匹配的字符序列移除
[`trimFrom(CharSequence)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#trimFrom-java.lang.CharSequence-)|去除开头和结尾匹配的部分
[`replaceFrom(CharSequence, CharSequence)`](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/CharMatcher.html#replaceFrom-java.lang.CharSequence-java.lang.CharSequence-)|将匹配的字符替换为给定的序列

## 方法分类
根据函数的返回值和名称将这些方法分为三类。

第一类是判定型函数，判断 `CharMacher` 和入参字符串的匹配关系。
```
CharMatcher.is('a').matchesAllOf("aaa");//true
CharMatcher.is('a').matchesAnyOf("aba");//true
CharMatcher.is('a').matchesNoneOf("aba");//true
```

第二类是计数型函数，查找入参字符串中第一次、最后一次出现目标字符的位置，或者目标字符出现的次数，比如 `indexIn`，`lastIndexIn` 和 `countIn`。
```
CharMatcher.is('a').countIn("aaa"); // 3
CharMatcher.is('a').indexIn("java"); // 1
```

第三类就是对匹配字符的操作。包括 `removeFrom`、`retainFrom`、`replaceFrom`、`trimFrom`、`collapseFrom` 等。
```
CharMatcher.is('a').retainFrom("bazaar"); // "aaa"
CharMatcher.is('a').removeFrom("bazaar"); // "bzr"
CharMatcher.anyOf("ab").trimFrom("abacatbab"); // "cat"
```

# 源码分析
源码有很多行，主要的逻辑是这样的：
* `CharMatcher` 是个抽象类，内部有一些私有的实现类，通过一些工厂函数 `is`、`anyOf` 等工厂函数创建对应的实例（固定的是单例，变化的会创建一个具体实例）。
* 不同的实例主要实现的是 `CharMatcher` 中的 `matches` 方法，这样就实现了不同策略的匹配器。
* 基于上述匹配方法 `matches`，可以进行统计工作（`countIn`等）、查找工作（`indexIn`等）、修改工作（`trimFrom`）等。

这样的设计最基础的工作就是：把匹配部分进行抽象。此外，在具体实现过程中也有较多的优化，就不一一列出来了。
