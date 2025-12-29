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
        workers = new TiredThread[numThreads];
        for (int i = 0 ; i<numThreads ; i++){
            TiredThread curr = new TiredThread(i, 0); //change fatigue factor
            idleMinHeap.add(curr);
            workers[i]=curr;
        }
    }

    public void submit(Runnable task) {
        try {
            // take() עוצר ומחכה אוטומטית אם התור ריק, ללא צורך ב-wait/notify
            TiredThread worker = idleMinHeap.take(); 
            worker.newTask(task);
            inFlight.incrementAndGet(); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        for (Runnable task : tasks){
            submit(task);
        }  
    }

    public void shutdown() throws InterruptedException {
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
