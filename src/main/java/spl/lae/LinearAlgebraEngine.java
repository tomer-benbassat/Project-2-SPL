package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        computationRoot.associativeNesting();
        while (computationRoot.getMatrix()!=null){
            ComputationNode curr = computationRoot.findResolvable();
            loadAndCompute(curr);
        }
        return null;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor



        node.setType();
    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        if(leftMatrix.length()!=rightMatrix.length() ||
         leftMatrix.get(0).length()!=rightMatrix.get(0).length()){
            throw new IllegalArgumentException("matrix's dont match in size");
         }
         List<Runnable> result = new ArrayList<>();
         for (int i = 0; i < leftMatrix.length();i++){
            SharedVector a = leftMatrix.get(i);
            SharedVector b = rightMatrix.get(i);
            result.add(new task(() -> a.add(b)));
         }
        return result;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        return null;
    }

    public List<Runnable> createNegateTasks() {
         List<Runnable> result = new ArrayList<>();
         for (int i = 0; i < leftMatrix.length() ; i++){
            SharedVector a = leftMatrix.get(i);
            result.add(new task(() -> a.negate()));
         }
        return result;
    }

    public List<Runnable> createTransposeTasks() {
        List<Runnable> result = new ArrayList<>();
         for (int i = 0; i < leftMatrix.length() ; i++){
            SharedVector a = leftMatrix.get(i);
            result.add(new task(() -> a.transpose()));
         }
        return result;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return null;
    }

    private static class task implements Runnable {
        private final Runnable action;

        public task(Runnable action) {
            this.action = action;
        }

        @Override
        public void run() {
            action.run();
        }
    }
}
