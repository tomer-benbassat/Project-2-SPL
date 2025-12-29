package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        return null;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        if(leftMatrix.length()!=rightMatrix.length() ||
         leftMatrix.get(0).length()!=rightMatrix.get(0).length()){
            throw new IllegalArgumentException("matrix's dont match in size");
         }

        return null;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        return null;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        return null;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        return null;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return null;
    }

    public class task implements Runnable {
        private final Runnable realTask;
        private final TiredThread worker;
        private final TiredExecutor executor;

        public task(Runnable realTask, TiredThread worker, TiredExecutor executor) {
            this.realTask = realTask;
            this.worker = worker;
            this.executor = executor;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            try {
                // ביצוע המשימה האמיתית
                realTask.run();
            } finally {
                long end = System.currentTimeMillis();
                long duration = end - start;

                executor.decrementInFlightCAS();
                
                executor.returnToHeap(worker);
            }
        }
    }
}
