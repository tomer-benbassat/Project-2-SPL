package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.List;
import java.util.ArrayList;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        executor = new TiredExecutor(numThreads); 
    }
    
    //@POST:leftMatrix has the result of all calculation
    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        computationRoot.associativeNesting();
        while(computationRoot.getNodeType() != ComputationNodeType.MATRIX){
            loadAndCompute(computationRoot.findResolvable());
        }
        return computationRoot;
    }

    //@PRE: node is resolvable aka his children are matrices
    //&& each node has 2 childen(associatveNestins)
    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        //load:
        ComputationNodeType type = node.getNodeType();
        List<ComputationNode> children = node.getChildren();
        leftMatrix.loadRowMajor(children.get(0).getMatrix());
        if(type==ComputationNodeType.ADD){
            rightMatrix.loadRowMajor(children.get(1).getMatrix());
        }
        if(type==ComputationNodeType.MULTIPLY){
            rightMatrix.loadColumnMajor(children.get(1).getMatrix());
        }
        //create compute tasks
        List<Runnable> tasks = new ArrayList<>();
        if(type==ComputationNodeType.ADD){
            tasks = createAddTasks();
        }
        if(type==ComputationNodeType.MULTIPLY){
            tasks = createMultiplyTasks();
        }
         if(type==ComputationNodeType.NEGATE){
            tasks = createNegateTasks();
        }
        if(type==ComputationNodeType.TRANSPOSE){
            tasks = createTransposeTasks();
        }
        //submit
        executor.submitAll(tasks);
        //back to ComputationNode:
        node.resolve(leftMatrix.readRowMajor());
    }


    //@PRE: leftMatrix and rightmatrix has same number of rows and columns, &&they arent empty
    public List<Runnable> createAddTasks() {
        //return tasks that perform row-wise addition
        if(leftMatrix.length()==0|| rightMatrix.length() == 0){
            throw new IllegalArgumentException("empty matrices");
        }
        if(leftMatrix.length()!=rightMatrix.length() || leftMatrix.get(0).length() !=rightMatrix.get(0).length()){
            throw new IllegalArgumentException("unsuitable dimesnsions");
        }
        List<Runnable> result = new ArrayList<>();
        for(int i=0; i< leftMatrix.length();i++){
            SharedVector leftRow = leftMatrix.get(i);
            SharedVector rightRow = rightMatrix.get(i);
            result.add(()->leftRow.add(rightRow));
        }
        return result;
    }

    //@PRE:rightMatrix is column oriented! & matrices arent empty & left.columnsNumber == right.rowsNumber 
    public List<Runnable> createMultiplyTasks() {
        //tasks that perform row × matrix multiplication
        if(rightMatrix.getOrientation()!=VectorOrientation.COLUMN_MAJOR){
            throw new IllegalArgumentException();
        }
        if(leftMatrix.length()==0|| rightMatrix.length() == 0){
            throw new IllegalArgumentException("empty matrices");
        }
        if(leftMatrix.get(0).length() != rightMatrix.get(0).length()){
            throw new IllegalArgumentException("unsuitable demensions");
        }
        List<Runnable> result = new ArrayList<>();
        for(int i=0;i < leftMatrix.length();i++){
            SharedVector row = leftMatrix.get(i);
            result.add(()->row.vecMatMul(rightMatrix));
        }
        return result;
    }

    public List<Runnable> createNegateTasks() {
        //return tasks that negate rows
        List<Runnable> result = new ArrayList<>();
        for(int i=0; i < leftMatrix.length();i++){
            SharedVector row = leftMatrix.get(i);
            result.add(()->row.negate());
        }
        return result;
    }


    public List<Runnable> createTransposeTasks() {
        //return tasks that transpose rows
        List<Runnable> result = new ArrayList<>();
        for(int i =0; i < leftMatrix.length();i++){
            SharedVector row = leftMatrix.get(i);
            result.add(()->row.transpose()); //() tells the compiler we override run()
        }
        return result;
    }

    public String getWorkerReport() {
        return executor.getWorkerReport();
    }
}
