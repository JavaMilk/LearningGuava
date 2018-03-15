# 使用示例
以下参考：[官方文档](https://github.com/google/guava/wiki/StringsExplained#joiner)。

开发过程中，用分隔符连接字符串序列可能是一个比较繁琐的过程，但本不应该如此。`Joiner` 可以简化这个操作。

如果序列中包含 `null` 值，那么可以使用 `Joiner` 跳过 `null` 值： 
```
    // 跳过 null 值
    result = Joiner.on("; ").skipNulls().join("Harry", null, "Ron", "Hermione");
    Assert.assertEquals(result, "Harry; Ron; Hermione");
```

也可以通过 `useForNull(String)` 来将 `null` 值替换为指定的字符串。
```
    // 替换 null 值
    result = Joiner.on("; ").useForNull("null").join("Harry", null, "Ron", "Hermione");
    Assert.assertEquals(result, "Harry; null; Ron; Hermione");
```

同样可以在对象上使用 `Joiner`,最终会调用对象的 `toString()` 方法。
```
    // 使用在对象上，会调用对象的 toString() 函数
    result = Joiner.on(",").join(Arrays.asList(1, 5, 7));
    Assert.assertEquals(result, "1,5,7");
```

对于 `Map` ,可以使用这样的代码：
```
    // MapJoiner 的使用，将 map 转换为字符串
    Map map = ImmutableMap.of("k1", "v1", "k2", "v2");
    result = Joiner.on("; ").withKeyValueSeparator("=").join(map);
    Assert.assertEquals(result, "k1=v1; k2=v2");
```

# 源码分析
以下参考：[Guava 是个风火轮之基础工具(1)](http://www.importnew.com/15221.html)。

## 初始化方法
`Joiner` 的构造方法被设置成了私有，需要通过静态的 `on(String separator)` 或者 `on(char separator)` 函数初始化。 

## 拼接基本函数
`Joiner` 了中最为核心的函数就是 `<A extends Appendable> A appendTo(A appendable, Iterator<?> parts) `。作为全功能函数，其它所有的字符串拼接最终都会调用这个函数。

```
  public <A extends Appendable> A appendTo(A appendable, Iterator<?> parts) throws IOException {
    checkNotNull(appendable);
    if (parts.hasNext()) {
      appendable.append(toString(parts.next()));
      while (parts.hasNext()) {
        appendable.append(separator);
        appendable.append(toString(parts.next()));
      }
    }
    return appendable;
  }
```

这段代码的分析如下：
* 这里的 `Appendable` 源码中传入的是实现该接口的 `StringBuilder`。
* 因为是公共方法，无法保证 `appendable` 值不为空，所以要先检查该值是否为空。
* `if ... while ...` 的结构确保末尾不会添加多余的分隔符。
* 通过本地 `toString` 方法，而不是直接调用对象的 `toString` 方法，这种做法提供了空指针保护。

##  不可能发生的异常
在源码中，有个地方的处理值得关注一下：
```
   public StringBuilder appendTo(StringBuilder builder, Iterator<? extends Entry<?, ?>> entries) {
      try {
        appendTo((Appendable) builder, entries);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
      return builder;
    }
```

这里之所以 `IOException` 的变量名取名为 `impossible` 是因为：虽然 `Appendable` 接口的 `append` 方法会抛出 `IOException`，但是传入的 `StringBuilder` 在实现的时候并不会抛出改异常，所以为了适应这个接口，这里不得不捕捉异常。这样捕捉后的断言处理也就可以理解了。

## 巧妙的可变长参数转换
有一个添加的重载函数如下所示：
```
 public final <A extends Appendable> A appendTo(
      A appendable, @NullableDecl Object first, @NullableDecl Object second, Object... rest)
      throws IOException {
    return appendTo(appendable, iterable(first, second, rest));
  }
```

其中 `iterable` 函数将参数变为一个可以迭代的序列，该函数如下所示。
```
private static Iterable<Object> iterable(
      final Object first, final Object second, final Object[] rest) {
    checkNotNull(rest);
    return new AbstractList<Object>() {
      @Override
      public int size() {
        return rest.length + 2;
      }

      @Override
      public Object get(int index) {
        switch (index) {
          case 0:
            return first;
          case 1:
            return second;
          default:
            return rest[index - 2];
        }
      }
    };
  }
```

通过实现 `AbstractList` 的方式，巧妙地复用了编译器生成的数组，减少了对象创建的开销。这样的实现需要对迭代器有深入的了解，因为要确保实现能够满足迭代器接口各个函数的语义。

##  Joiner 二次创建
因为 `Joiner` 创建后就是不可更改的了，所以为了实现 `useForNull` 和 `skipNulls` 等语义，源码会再次创建一个匿名类，并覆盖相应的方法。 

`useForNull` 函数汇中为了防止重复调用 `useForNull` 和 `skipNulls`，还特意覆盖了这两个方法，一旦调用就抛出运行时异常。为什么不能重复调用 `useForNull` ？因为覆盖了 `toString` 方法，而覆盖实现中需要调用覆盖前的 `toString`。
```
  public Joiner useForNull(final String nullText) {
    checkNotNull(nullText);
    return new Joiner(this) {
      @Override
      CharSequence toString(@NullableDecl Object part) {
        return (part == null) ? nullText : Joiner.this.toString(part);
      }

      @Override
      public Joiner useForNull(String nullText) {
        throw new UnsupportedOperationException("already specified useForNull");
      }

      @Override
      public Joiner skipNulls() {
        throw new UnsupportedOperationException("already specified useForNull");
      }
    };
  }
```

`skipNulls` 函数实现如下所示。个人比较奇怪的是 `skipNulls` 中为什么不禁止重复调用 `skipNulls` 函数。
```
  public Joiner skipNulls() {
    return new Joiner(this) {
      @Override
      public <A extends Appendable> A appendTo(A appendable, Iterator<?> parts) throws IOException {
        checkNotNull(appendable, "appendable");
        checkNotNull(parts, "parts");
        while (parts.hasNext()) {
          Object part = parts.next();
          if (part != null) {
            appendable.append(Joiner.this.toString(part));
            break;
          }
        }
        while (parts.hasNext()) {
          Object part = parts.next();
          if (part != null) {
            appendable.append(separator);
            appendable.append(Joiner.this.toString(part));
          }
        }
        return appendable;
      }

      @Override
      public Joiner useForNull(String nullText) {
        throw new UnsupportedOperationException("already specified skipNulls");
      }

      @Override
      public MapJoiner withKeyValueSeparator(String kvs) {
        throw new UnsupportedOperationException("can't use .skipNulls() with maps");
      }
    };
  }
```



