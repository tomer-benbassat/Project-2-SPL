package spl.lae;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      /*args[0]=number of threads
        args[1]=inputPath
        args[2]=outputPath
      */
     

      //Parsing the input file and writing the output file.
      InputParser parser = new InputParser();
      
      //Building the computation tree of ComputationNode objects:
      try{
      ComputationNode root = parser.parse(args[1]);
      
      /*all of this happen in lae.run
        1.Loading operand matrices into the two SharedMatrix instances M1 and M2(loadAndCompute)
        2.Creating and submitting Runnable tasks to TiredExecutor(loadAndCompute)
        3.Waiting until all tasks for the current node have completed(executor.submitAll)
        4.Shutting down the executor cleanly once the entire computation finishes(executor.shutdown)
        */
        int numOfThreads = Integer.parseInt(args[0]);
        LinearAlgebraEngine lae = new LinearAlgebraEngine(numOfThreads);
        ComputationNode result = lae.run(root);
        OutputWriter.write(result.getMatrix(),args[2]);
        
        //addition from the forum - need to print report
        System.out.println(lae.getWorkerReport());
      }
      catch(Exception e){
        /* examples possible exceptions:
        1.ParseException from the input file
        2.RuntimeException from lae.executor.shutdown(join Interrupted exception which we convert to Runtime)
        3.IllegalArgumentException(unsuitable dimensions for add or multiply etc)
        4.a lot more since in this assigment I used a more defensive programming 
         */
        OutputWriter.write("Illegal operation: " +  e.getMessage(),args[2]);
      }
    }
}