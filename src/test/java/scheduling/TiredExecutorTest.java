package scheduling;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

public class TiredExecutorTest {
    private TiredExecutor executor;
    private final int NUM_THREADS = 3;

    @BeforeEach
    void init() {
        executor = new TiredExecutor(NUM_THREADS);
    }

    @AfterEach
    void shutDown() throws InterruptedException {
        if (executor != null) {
            executor.shutdown();
        }
    }


    @Test
    void testSubmitSingleTask() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);
        executor.submit(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.countDown();
            }
        });
        boolean completed = lock.await(2, TimeUnit.SECONDS);
        assertTrue(completed, "Single task should complete successfully");
    }

   @Test
   //we want to test differnet number of tasks for 3 threads
    void testSubmitAllWithVariousCountsLoop() {
        int[] taskCounts = {1, 3, 10, 50, 100}; 
        for (int count : taskCounts) {
            AtomicInteger counter = new AtomicInteger(0);
            List<Runnable> tasks = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                tasks.add(() -> {
                    try {
                        Thread.sleep(5);
                        counter.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            //wait for all tasks to finish
            executor.submitAll(tasks);
            //check that all tasks actually done
            assertEquals(count, counter.get(), "Failed when count is " + count);
        }
    }


    @Test
    void testFairnessDistribution() {
        int numThreads = 3;
        int numTasks = 50; 
        if (executor != null) try { executor.shutdown(); } catch (Exception e) {}
        executor = new TiredExecutor(numThreads);
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(2); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        executor.submitAll(tasks);
        String report = executor.getWorkerReport();
        assertFalse(report.contains("Fatigue: 0.0 "), 
            "Distribution failed: Some workers did not work at all (Fatigue is 0)");
    }
    

    @Test
    //since the formula and ns measure make life complex i just check if threads are waiting and working more-or-less the same times
    void testTimeDurations() throws Exception {
        int numThreads = 3;
        int numTasks = 150;
        if (executor != null) try { executor.shutdown(); } catch (Exception e) {}
        executor = new TiredExecutor(numThreads);
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            tasks.add(() -> {
                try { Thread.sleep(2); } catch (InterruptedException e) {}
            });
        }
        executor.submitAll(tasks);
        TiredThread[] workers = (TiredThread[]) getPrivateField(executor, "workers");
        List<Long> usedTimes = new ArrayList<>();
        List<Long> idleTimes = new ArrayList<>();
        for (TiredThread worker : workers) {
            long timeUsed = worker.getTimeUsed();
            long timeIdle = worker.getTimeIdle();
            usedTimes.add(timeUsed);
            idleTimes.add(timeIdle);
        }

        //used:
        long maxUsed = usedTimes.stream().mapToLong(v->v).max().orElse(0);
        long minUsed = usedTimes.stream().mapToLong(v->v).min().orElse(0);
        double avgUsed = usedTimes.stream().mapToLong(v->v).average().orElse(0);
        long diffUsed = maxUsed - minUsed;
        assertTrue(diffUsed < (avgUsed * 0.5), 
            "Used Time is not balanced! Gap is too big: " + diffUsed);
        //idle
        long maxIdle = idleTimes.stream().mapToLong(v->v).max().orElse(0);
        long minIdle = idleTimes.stream().mapToLong(v->v).min().orElse(0);
        double avgIdle = idleTimes.stream().mapToLong(v->v).average().orElse(0);
        long diffIdle = maxIdle - minIdle;
        assertTrue(diffIdle < (avgIdle * 0.5), 
            "Idle Time is not balanced! Gap is too big: " + diffIdle);
    }

    
    //heleper function to get private fields
    private Object getPrivateField(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }


    //check shutdown
    @Test
    void testShutdown() throws InterruptedException {
        assertDoesNotThrow(() -> {
            executor.shutdown();
        }); 
    }
}

