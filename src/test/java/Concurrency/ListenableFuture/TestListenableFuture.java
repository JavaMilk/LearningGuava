package Concurrency.ListenableFuture;

import com.google.common.util.concurrent.*;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author mindawei
 * @date 2018/3/20
 *
 * https://github.com/google/guava/wiki/ListenableFutureExplained#listenablefuture
 */
public class TestListenableFuture {

    @Test
    public void test(){

        CountDownLatch countDownLatch = new CountDownLatch(1);

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
        ListenableFuture<String> explosion = executorService.submit(() -> "finished");

        ExecutorService callBackService =  Executors.newFixedThreadPool(1);
        Futures.addCallback(explosion, new FutureCallback<String>() {
            public void onSuccess(String explosion) {
                System.out.println("onSuccess");
                countDownLatch.countDown();
            }

            public void onFailure(Throwable thrown) {
                System.out.println("onFailure");
                countDownLatch.countDown();
            }
        },callBackService);

        try {
            countDownLatch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            executorService.shutdown();
            callBackService.shutdown();
        }

    }

}
