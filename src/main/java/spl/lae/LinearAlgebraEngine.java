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
        while (computationRoot.getNodeType()!= ComputationNodeType.MATRIX) {
            ComputationNode curr = computationRoot.findResolvable();
            if (curr == null) {
                throw new IllegalStateException("No resolvable node found, but computation not complete");
            }
            loadAndCompute(curr);
        }
        try {
            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        throw new RuntimeException("Executor shutdown interrupted", e);
        }
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        //@pre node != null && all children of node have their matrices resolved
        if(node == null){
            throw new IllegalArgumentException("node is null");
        }
        List<ComputationNode> children = node.getChildren();
        double[][] matrixA = children.get(0).getMatrix();
            leftMatrix.loadRowMajor(matrixA);
        List<Runnable> allTasks = new ArrayList<>();
        ComputationNodeType type = node.getNodeType();

        if (type == ComputationNodeType.ADD) {
            if (children.size() < 2) {
                throw new IllegalArgumentException("ADD operation requires two matrixs");
            } else {
                double[][] matrixB = children.get(1).getMatrix();
                rightMatrix.loadRowMajor(matrixB);
            }
            allTasks = createAddTasks();
        } 
        else if (type == ComputationNodeType.MULTIPLY) {
            if (children.size() < 2) {
                throw new IllegalArgumentException("MULTIPLY operation requires two matrixs");
            } else {
                double[][] matrixB = children.get(1).getMatrix();
                rightMatrix.loadColumnMajor(matrixB);
            }
            allTasks = createMultiplyTasks();
        } 
        else if (type == ComputationNodeType.NEGATE) {
            if (children.size() != 1) {
                throw new IllegalArgumentException("NEGATE operation requires one matrix");
            }
            allTasks = createNegateTasks();
        } 
        else if (type == ComputationNodeType.TRANSPOSE) {
            if (children.size() != 1) {
                throw new IllegalArgumentException("TRANSPOSE operation requires one matrix");
            }
            allTasks = createTransposeTasks();
        } 
        else {
            throw new UnsupportedOperationException("Unknown node type: " + type);
        }
        executor.submitAll(allTasks);
        node.resolve(leftMatrix.readRowMajor());
    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        //@pre leftMatrix.length() == rightMatrix.length()
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
        //@pre leftMatrix.numColumns() == rightMatrix.numRows()
        List<Runnable> result = new ArrayList<>();
    for (int i = 0; i < leftMatrix.length(); i++) {
        SharedVector row = leftMatrix.get(i);
        result.add(new task(() -> row.vecMatMul(rightMatrix)));
    }
    return result;
    }

    public List<Runnable> createNegateTasks() {
        //@pre leftMatrix.orientation == ROW_MAJOR
        //@post for all i: leftMatrix.get(i) == -1 * old(leftMatrix.get(i))
         List<Runnable> result = new ArrayList<>();
         for (int i = 0; i < leftMatrix.length() ; i++){
            SharedVector a = leftMatrix.get(i);
            result.add(new task(() -> a.negate()));
         }
        return result;
    }

    public List<Runnable> createTransposeTasks() {
        //@pre leftMatrix.orientation == ROW_MAJOR
        List<Runnable> result = new ArrayList<>();
         for (int i = 0; i < leftMatrix.length() ; i++){
            SharedVector a = leftMatrix.get(i);
            result.add(new task(() -> a.transpose()));
         }
        return result;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        try {
            return executor.getWorkerReport();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get worker report", e);
        }
    }

    private class task implements Runnable {
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
