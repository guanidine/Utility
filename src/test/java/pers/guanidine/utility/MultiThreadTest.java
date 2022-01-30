package pers.guanidine.utility;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;
import pers.guanidine.utility.thread.NamedThreadFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MultiThreadTest {
    @Test
    public void testThreadFactory() {
        ThreadPoolExecutor executor1 = new ThreadPoolExecutor(
                10,
                15,
                100,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(20),
                new NamedThreadFactory("demo1")
        );
        executor1.execute(() -> log.info("Log: demo1"));
        executor1.execute(() -> log.info("Log: demo1"));
        ThreadPoolExecutor executor2 = new ThreadPoolExecutor(
                10,
                15,
                100,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(20),
                new NamedThreadFactory("demo2")
        );
        executor2.execute(() -> log.info("Log: demo2"));
    }
}
