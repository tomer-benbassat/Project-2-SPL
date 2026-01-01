package scheduling;

import org.junit.jupiter.api.AfterEach; //after every @test
import org.junit.jupiter.api.BeforeEach; //before every @test
import org.junit.jupiter.api.Test;
//these 2 will help for thread checking. we want to make sure we test the created tiredThread and not the main thread which run the test(so paradoxical and philosofical haha :))
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*; //helping us check if actual output equal the expected

public class TiredThreadTest {

    private TiredThread tiredThread;
    private final double FATIGUE_FACTOR = 1.0; //deterministic FF for tests

    @BeforeEach
    void init() {
        //initialize new thread before test
        tiredThread = new TiredThread(0, FATIGUE_FACTOR);
    }

    @AfterEach
    void shutDown() throws InterruptedException {
        //making sure the thread has shut down
        if (tiredThread.isAlive()) {
            tiredThread.shutdown();
            tiredThread.join(1000); 
        }
    }


    @Test
    //when we start the thread run method has started and while hes allive he will take tasks from the queue
    //in this test we check if the statistcs update well while submitting tasks to thread
    void testThreadTasksExecution() throws InterruptedException {
        tiredThread.start();
        CountDownLatch lock = new CountDownLatch(1);
       //simulate task that takes 50ms
        tiredThread.newTask(() -> { //lambada
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.countDown();
            }
        });
        //waiting for the task to end
        boolean done = lock.await(2, TimeUnit.SECONDS);
        assertTrue(done, "Task should be done by now");
        //wait for statistics to update
        Thread.sleep(10);
        assertTrue(tiredThread.getTimeUsed() > 0, "TimeUsed should increase after task");
        assertTrue(tiredThread.getFatigue() > 0, "Fatigue should increase based on timeUsed");
        assertFalse(tiredThread.isBusy(), "Thread should not be busy after task completion");
    }




    @Test
    void testRejectTaskWhenBusy() throws InterruptedException {
        tiredThread.start();
        CountDownLatch startLock = new CountDownLatch(1);
        CountDownLatch finishLock = new CountDownLatch(1);

        
        tiredThread.newTask(() -> {
            startLock.countDown(); 
            try {
                //were waiting to the thread is busy
                finishLock.await(); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        //waiting for task to run
        assertTrue(startLock.await(1, TimeUnit.SECONDS));
        //waiting a bit to make sure is busy updated
        Thread.sleep(10);
        assertTrue(tiredThread.isBusy(), "Thread should be marked as busy while running task");
        //trying to submit other task and checking if we get exception
        //lambada of empty task
        assertThrows(IllegalStateException.class, () -> {
            tiredThread.newTask(() -> {} );
        }, "Should throw exception when trying to assign task to a busy worker");
        //now we finish to check so we can let it go
        finishLock.countDown();
    }



    @Test
    void testShutdown() throws InterruptedException {
        tiredThread.start();
        assertTrue(tiredThread.isAlive(), "Thread should be alive after start");
        tiredThread.shutdown();
        //waiting for thread to shutdown
        tiredThread.join(2000);
        assertFalse(tiredThread.isAlive(), "Thread shouldn't be alive after shutdown");
    }


    @Test
    void testCompareTo() throws InterruptedException {
        TiredThread t0 = new TiredThread(0, 1.0);
        TiredThread t1 = new TiredThread(1, 10.0); 
        t0.start();
        t1.start();
        //some random task that tasks 20ms to test
        Runnable task = () -> {
            try { Thread.sleep(20); } catch (InterruptedException e) {}
        };
        //submitting both threads task that taking the same time and waiting for them to execute
        t0.newTask(task);
        t1.newTask(task);
        Thread.sleep(100);
        //checking results:
        assertTrue(t1.getFatigue() > t0.getFatigue(), "t2 should be more fatigued than t1");
        
     
        assertTrue(t1.compareTo(t0) > 0, "compareTo should return 1 when this > other");
        assertTrue(t0.compareTo(t1) < 0, "compareTo should return -1 when this < other");
        assertTrue(t0.compareTo(t0) == 0,"compareTo should return 0 when this == other");
        t0.shutdown();
        t1.shutdown();
    }

    @Test
    void testTimeIdleUpdates() throws InterruptedException {
        tiredThread.start();
        //idle time:
        Thread.sleep(50);
        CountDownLatch lock = new CountDownLatch(1);
        tiredThread.newTask(()-> lock.countDown());
        lock.await();
        assertTrue(tiredThread.getTimeIdle() > 0, "TimeIdle should be > 0 after waiting");
    }


    @Test
    void testNullTaskSubmission() {
        //edge case: empty task
        assertThrows(NullPointerException.class, () -> {
            tiredThread.newTask(null);
        }, "Should throw NullPointerException when submitting a null task");
    }
}