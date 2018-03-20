[ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained#listenablefuture)
---

处理并发是一个很困难的问题，但是我们可以通过使用功能强大的抽象来简化这个工作。为了简化这个问题，Guava 提供了 [ListenableFuture](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/ListenableFuture.html)，它继承了 JDK 中的 `Future` 接口。 

__我们强烈建议：在你的代码中，使用 `ListenableFuture ` 来替代 `Future `__，因为 
* 很多 `Future` 相关的方法需要它。
* 一开始就使用 `ListenableFuture` 会省事很多。
* 这样工具方法提供者就不需要针对 `Future` 和 `ListenableFuture` 都提供方法。

## 接口
`Future` 代表了异步执行的结果：一个可能还没有产生结果的执行过程。 `Future` 可以正在被执行，但是会保证返回一个结果。

`ListenableFuture` 可以使你注册回调函数，使得在结果计算完成的时候可以回调你的函数。如果结果已经算好，那么将会立即回调。这个简单的功能使得可以完成很多 `Future` 支持不了的操作。

`ListenableFuture` 添加的基本函数是 [`addListener(Runnable, Executor)`](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/ListenableFuture.html#addListener-java.lang.Runnable-java.util.concurrent.Executor-)。通过这个函数，当 `Future` 中的结果执行完成时，传入的 `Runnable` 会在传入的 `Executor` 中执行。

## 添加回调函数
使用者偏向于使用 [`Futures.addCallback(ListenableFuture<V>, FutureCallback<V>, Executor)`](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#addCallback-com.google.common.util.concurrent.ListenableFuture-com.google.common.util.concurrent.FutureCallback-java.util.concurrent.Executor-) , 或者当需要注册轻量级的回调的时候，可以使用默认为 `MoreExecutors.directExecutor()` 的[版本](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#addCallback-com.google.common.util.concurrent.ListenableFuture-com.google.common.util.concurrent.FutureCallback-)。 

`FutureCallback<V>` 实现了两个方法:
* [`onSuccess(V)`]() ：当 future 执行成功时候的反应。
* [`onFailure(Throwable)`]()：当 future 执行失败时候的反应。

## 创建
与 JDK 中 通过 [`ExecutorService.submit(Callable)`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html#submit-java.util.concurrent.Callable-) 来初始化一个异步的任务相似，Guava 提供了一个 `ListeningExecutorService` 接口，这个接口可以返回一个 `ListenableFuture`（`ExecutorService` 只是返回一个普通的 `Future`）。如果需要将一个 `ExecutorService` 转换为 `ListeningExecutorService`，可以使用 [`MoreExecutors.listeningDecorator(ExecutorService)`](https://github.com/google/guava/wiki/ListenableFutureExplained#listenablefuture)。一个使用示例如下：
```
ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
ListenableFuture<Explosion> explosion = service.submit(new Callable<Explosion>() {
  public Explosion call() {
    return pushBigRedButton();
  }
});
Futures.addCallback(explosion, new FutureCallback<Explosion>() {
  // we want this handler to run immediately after we push the big red button!
  public void onSuccess(Explosion explosion) {
    walkAwayFrom(explosion);
  }
  public void onFailure(Throwable thrown) {
    battleArchNemesis(); // escaped the explosion!
  }
});
```
 
如果你想从一个基于 [`FutureTask`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/FutureTask.html) 的 API 转换过来，Guava 提供了 `[ListenableFutureTask.create(Callable<V>)](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/ListenableFutureTask.html#create-java.util.concurrent.Callable-)` 和 `[ ListenableFutureTask.create(Runnable, V)](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/ListenableFutureTask.html#create-java.lang.Runnable-V-)`。和 JDK 不一样，`ListenableFutureTask` 并不意味着可以直接扩展。

如果你更喜欢可以设置 future 值的抽象，而不是实现一个方法来计算结果，那么可以考虑直接扩展 `[AbstractFuture<V>](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/AbstractFuture.html)` 或者 `[SettableFuture](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/SettableFuture.html)`。

如果你一定要将一个基于 `Future` 的 API 转换为基于 `ListenableFuture` 的话，你不得不选择重量级的 `[JdkFutureAdapters.listenInPoolThread(Future)](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/JdkFutureAdapters.html)` 来实现从 `Future` 到 `ListenableFuture` 的转换。所以，尽可能地使用 `ListenableFuture`。

## 应用
使用 `ListenableFuture` 一个最重要的原因就是：可以基于他实现负责的异步执行链。如下所示：
```
ListenableFuture<RowKey> rowKeyFuture = indexService.lookUp(query);
AsyncFunction<RowKey, QueryResult> queryFunction =
  new AsyncFunction<RowKey, QueryResult>() {
    public ListenableFuture<QueryResult> apply(RowKey rowKey) {
      return dataService.read(rowKey);
    }
  };
ListenableFuture<QueryResult> queryFuture =
    Futures.transformAsync(rowKeyFuture, queryFunction, queryExecutor);
```  

很多不能被 `Future` 支持的方法可以通过 `ListenableFuture` 被高效地支持。不同的操作可能被不同的执行器执行，而且一个 `ListenableFuture` 可以有多个响应操作。

当 `ListenableFuture` 有多个后续操作的时候，这样的操作称为：“扇出”。当它依赖多个输入 future 同时完成时，称作“扇入”。可以参考 [`Futures.allAsList`的实现](https://google.github.io/guava/releases/snapshot/api/docs/src-html/com/google/common/util/concurrent/Futures.html#line.1276)。 

方法|描述|参考
--|--|--
[`transformAsync(ListenableFuture<A>, AsyncFunction<A, B>, Executor)`]()|返回新的 `ListenableFuture`，它是给定 `AsyncFunction` 结合的结果|[`transformAsync(ListenableFuture<A>, AsyncFunction<A, B>)`](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#transformAsync-com.google.common.util.concurrent.ListenableFuture-com.google.common.util.concurrent.AsyncFunction-)
[`transform(ListenableFuture<A>, Function<A, B>, Executor)`]()|返回新的 `ListenableFuture`,它是给定 `Function ` 结合的结果|[`transform(ListenableFuture<A>, Function<A, B>)`](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#transform-com.google.common.util.concurrent.ListenableFuture-com.google.common.base.Function-) 
[`allAsList(Iterable<ListenableFuture<V>>)`]()|返回一个 `ListenableFuture`,它的值是一个输入 futures 的值的按序列表，任何一个 future 的失败都会导致最后结果的失败|[`allAsList(ListenableFuture<V>...)`](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#allAsList-com.google.common.util.concurrent.ListenableFuture...-)
[`successfulAsList(Iterable<ListenableFuture<V>>)`]()|返回一个 `ListenableFuture`,它的值是一个输入 futures 的成功执行值的按序列表，对于取消或者失败的任务，对应的值是 `null`|[`successfulAsList(ListenableFuture<V>...)`](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#successfulAsList-com.google.common.util.concurrent.ListenableFuture...-)
 
`AsyncFunction<A, B>` 提供了一个方法：`ListenableFuture<B> apply(A input)`。可以被用来异步转换一个值。
```
List<ListenableFuture<QueryResult>> queries;
// The queries go to all different data centers, but we want to wait until they're all done or failed.

ListenableFuture<List<QueryResult>> successfulQueries = Futures.successfulAsList(queries);

Futures.addCallback(successfulQueries, callbackOnSuccessfulQueries);
```

#  避免嵌套 Future
在使用通用接口返回 `Future` 的代码中，很有可能会嵌套 `Future`。例如：
```
executorService.submit(new Callable<ListenableFuture<Foo>() {
  @Override
  public ListenableFuture<Foo> call() {
    return otherExecutorService.submit(otherCallable);
  }
});
```
上述代码将会返回：`ListenableFuture<ListenableFuture<Foo>>`。这样的代码是不正确的，因为外层 future 的取消操作不能传递到内层的 future。此外，一个常犯的错误是：使用 `get()` 或者 listener 来检测其它 future 的失败。为了避免这样的情况，Guava 所有处理 future 的方法（以及一些来自 JDK 的代码）具有安全解决嵌套的版本。

# CheckedFuture
Guava 也提供 [`CheckedFuture<V, X extends Exception>`](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/CheckedFuture.html) 接口。

`CheckedFuture` 是这样的一个 `ListenableFuture`：具有多个可以抛出受保护异常的 `get` 方法。这使得创建一个执行逻辑可能抛出异常的 future 变得容易。使用 [`Futures.makeChecked(ListenableFuture<V>, Function<Exception, X>)`](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#makeChecked-com.google.common.util.concurrent.ListenableFuture-com.google.common.base.Function-)可以将 `ListenableFuture ` 转换为 `CheckedFuture`。



 
