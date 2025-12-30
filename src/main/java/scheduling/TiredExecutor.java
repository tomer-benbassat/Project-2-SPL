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
        //@pre numThreads > 0
        //@post workers.length == numThreads
        if(numThreads <= 0){
            throw new IllegalArgumentException("numThreads must be greater than 0");
        }
        workers = new TiredThread[numThreads];
        for (int i = 0 ; i<numThreads ; i++){
            TiredThread curr = new TiredThread(i, 0.5 + (double) Math.random()); //change fatigue factor
            idleMinHeap.add(curr);
            workers[i]=curr;
            curr.start();
        }
    }

    public void submit(Runnable task) {
        //@pre task != null
        if(task == null){
            throw new IllegalArgumentException("task is null");
        }
        try {
            TiredThread worker = idleMinHeap.take(); 
            worker.newTask(task);
            inFlight.set(inFlight.get() + 1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        for (Runnable task : tasks){
            submit(task);
        }
        // Wait for all tasks to complete 
        synchronized (inFlight) {
            while (inFlight.get() > 0) {
                try {
                    inFlight.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


    public void shutdown() throws InterruptedException {
        //@post for all workers: worker.isAlive() == false
        for (TiredThread thread : idleMinHeap){
            thread.shutdown();
        }
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        for (int i = 0 ; i < workers.length ; i++){
            
        }
    }
}
