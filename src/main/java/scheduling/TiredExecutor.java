package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {
    //@INV: Inflight >=0 && workers!=null

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        workers = new TiredThread[numThreads];
        for(int i=0; i<numThreads;i++){
            double fatigueFactor = Math.random() * (1.5 - 0.5) + 0.5;
            workers[i] = new TiredThread(i,fatigueFactor);
            idleMinHeap.add(workers[i]);
            workers[i].start();
        }
    }

    //@PRE:none
    //@POST:task has submited, the worker who executed it, has returned to priority queue
    public void submit(Runnable task) {
        inFlight.incrementAndGet();
        boolean taskHasSubmitted = false;
        while(!taskHasSubmitted){ // due to assigment restrictions the thread can be back to pool even though its bust therfore throw exception so thats how we handle 
            try{
                TiredThread currentThread = idleMinHeap.take(); //take aka blocking if none are current idle
                //make sure thread is back to priority queue after finishing task:
                Runnable wrapper = new Runnable(){ //anonymous
                    @Override
                    public void run(){
                        try{
                            task.run();
                        }
                        finally{ //suppose task.run failed, make sure thread is not lost in space&counter is up-to-date
                            idleMinHeap.add(currentThread);
                            synchronized(inFlight){ //you have to synchronized in order to use wait&notify
                                inFlight.decrementAndGet();
                                if(inFlight.get()==0){
                                inFlight.notifyAll();
                                }
                            } 
                        }
                    }
                };
                //end of anonymous
                try{
                    currentThread.newTask(wrapper);
                    taskHasSubmitted = true;
                }catch(IllegalStateException e){//the thread went back to pool even though its still busy due to wrapper 
                    idleMinHeap.add(currentThread);
                }
            }
            catch(InterruptedException e){  //we couldnt take thread of priority queue so counter-- (the task is not executed...)
                inFlight.decrementAndGet();
                return; //interrupted.....
            }
        }
    }

    //@PRE:none
    //@POST:all tasks submited to threads and have been executed
    public void submitAll(Iterable<Runnable> tasks) {
        for (Runnable task : tasks) {
            submit(task);
        }
        try{
            synchronized(inFlight){
                while(inFlight.get()!=0){
                inFlight.wait(); //executor sleeps, inflight unlocked in order for threads to update it.
                }
            }
        }catch(InterruptedException e){
            return;
        }
    }

    //@PRE:none
    //@POST:all threads has been shutdown
    public void shutdown() throws InterruptedException { //if join throws InterruptedException, throw it back to the calling method
        for(TiredThread worker : workers){
            worker.shutdown();
        }
        for(TiredThread worker : workers){
            worker.join(); //waits for the thread to shutdown completelly
        }
    }
    
    //@PRE:none
    //@@POST:strinn of statistics on each thread has returned
    public synchronized String getWorkerReport() {
        String report = "";
        for(TiredThread worker : workers){
            report = report + "Thread ID: " + worker.getWorkerId() + "," + "Fatigue: " + worker.getFatigue()+ "," + "Is Busy: " + worker.isBusy() + "," + "Time Used: " + worker.getTimeUsed() + "," + "Time Idle: "+ worker.getTimeIdle() + "\n";
        }
        return report;
    }
}
