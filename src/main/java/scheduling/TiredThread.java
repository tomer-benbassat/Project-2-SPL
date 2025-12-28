package scheduling;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TiredThread extends Thread implements Comparable<TiredThread> {
    //@INV: timeused.get >=0 && timeIdle.get >=0 && id>=0

    
    //lambada of empty function
    private static final Runnable POISON_PILL = () -> {}; // Special task to signal shutdown

    private final int id; // Worker index assigned by the executor
    private final double fatigueFactor; // Multiplier for fatigue calculation

    private final AtomicBoolean alive = new AtomicBoolean(true); // Indicates if the worker should keep running

    // Single-slot handoff queue; executor will put tasks here
    private final BlockingQueue<Runnable> handoff = new ArrayBlockingQueue<>(1);

    private final AtomicBoolean busy = new AtomicBoolean(false); // Indicates if the worker is currently executing a task

    private final AtomicLong timeUsed = new AtomicLong(0); // Total time spent executing tasks
    private final AtomicLong timeIdle = new AtomicLong(0); // Total time spent idle
    private final AtomicLong idleStartTime = new AtomicLong(0); // Timestamp when the worker became idle

    public TiredThread(int id, double fatigueFactor) {
        this.id = id;
        this.fatigueFactor = fatigueFactor;
        this.idleStartTime.set(System.nanoTime());
        setName(String.format("FF=%.2f", fatigueFactor));
    }

    public int getWorkerId() {
        return id;
    }

    public double getFatigue() {
        return fatigueFactor * timeUsed.get();
    }

    public boolean isBusy() {
        return busy.get();
    }

    public long getTimeUsed() {
        return timeUsed.get();
    }

    public long getTimeIdle() {
        return timeIdle.get();
    }

    /**
     * Assign a task to this worker.
     * This method is non-blocking: if the worker is not ready to accept a task,
     * it throws IllegalStateException.
     */
    //@PRE:the worker is ready to accept a task
    //@POST:the task has been inserted to this thread handoff Queue
    public void newTask(Runnable task) {
        if(isBusy() || !handoff.offer(task)){
            //first condition:thread is currently executing task, so its not optimized to give it to hime. we would like to give it to other available thread
            //second conition:queue is full,dont have place for other task
            throw new IllegalStateException();
        }
    }

    /**
     * Request this worker to stop after finishing current task.
     * Inserts a poison pill so the worker wakes up and exits.
     */
    //@PRE:current thread is alive
    //@POST:handoff.take() == poison_pill
    public void shutdown() {
        //put poison pill in the queue and if it's full, wait patiently
        //if the thread was already cancelled(we caught Interruption exception) we ignore it
        try{
        handoff.put(POISON_PILL);
        }catch(InterruptedException e){
            alive.set(false);
        }
       }
    

    @Override
    //@PRE:alive==true
    //@POST: alive ==false
    public void run() {
        while(alive.get()){
            try{
                Runnable task = handoff.take();
                if(task == POISON_PILL){
                    alive.set(false);
                    return;
                }
                //calculate timeIdle
                timeIdle.set(timeIdle.get() + (System.nanoTime() - idleStartTime.get()));
                //save the time we started the task
                long taskStarted = System.nanoTime();
                //run the task
                busy.set(true);
                task.run();
                busy.set(false);
                //calculate time used
                timeUsed.set(timeUsed.get() + (System.nanoTime() - taskStarted));
                //track idle new starting time(finish the task so now we idle again)
                idleStartTime.set(System.nanoTime());
        }catch(InterruptedException e) {
            alive.set(false);
            return;
            }
        }
    }   

    @Override
    //@PRE:none
    //@POST:none
    public int compareTo(TiredThread o) {
        if(getFatigue() > o.getFatigue()){
            return 1;
        }
        if(getFatigue() < o.getFatigue()){
            return -1;
        }
        return 0;
    }
}

