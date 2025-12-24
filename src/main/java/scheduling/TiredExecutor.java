package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        workers = new TiredThread[numThreads]
        for (int i = 0 ; i<numThreads ; i++){
            TiredThread curr = new TiredThread(i, 0); //change fatigue factor
            idleMinHeap.add(curr);
            workers[i]=curr;
        }
    }

    public void submit(Runnable task) {
        TiredThread leastTired = idleMinHeap.poll();
        leastTired.newTask(task);
        leastTired.run();
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
    }

    public void shutdown() throws InterruptedException {
        // TODO
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        return null;
    }
}
