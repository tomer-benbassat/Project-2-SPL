package memory;

public class SharedMatrix {
    // @INV: vectors!= null && for each 0<=i<vectors.length: vectors[i] != null

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // @PRE:marix!=null
        // @POST:this.vectors represents ROW_MAJOR matrix with matrix data
        if (matrix == null) {
            throw new IllegalArgumentException("matrix is null");
        }
        this.loadRowMajor(matrix);
    }

    public void loadRowMajor(double[][] matrix) {
        // basic command
        // no vectors lock needed since no other thread knows matrix yet. only current
        // thread.
        // @PRE: matrix!=null
        // @POST: this.vectors represents ROW_MAJOR SharedMatrix with matrix data
        if (matrix == null) {
            throw new IllegalArgumentException("matrix is null");
        }
        // temp help us for concurrency. if i'd use vectors array other thread could got
        // to partial true data(while im building it)
        SharedVector[] temp = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            temp[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
        this.vectors = temp;
    }

    public void loadColumnMajor(double[][] matrix) {
        // basic command
        // no vectors lock needed since no other thread knows matrix yet. only current
        // thread.
        // @PRE: matrix!=null
        // @POST: this.vectors represents COLUMN_MAJOR SharedMatrix with matrix data
        if (matrix == null) {
            throw new IllegalArgumentException("matrix is null");
        }
        if (matrix.length == 0) {
            this.vectors = new SharedVector[0];
            return;
        }
        if (matrix[0] == null) {
            throw new IllegalArgumentException("first line is null");
        }
        SharedVector[] temp = new SharedVector[matrix[0].length];
        for (int j = 0; j < matrix[0].length; j++) {
            double[] col = new double[matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                col[i] = matrix[i][j];
            }
            temp[j] = new SharedVector(col, VectorOrientation.COLUMN_MAJOR);
        }
        this.vectors = temp;
    }

    public double[][] readRowMajor() {
        //basic query since we didnt use this.get()\length() etc.. since we chose to use the copy for safety
        //@PRE:none
        //@POST:none
        //@Returns matrix contents as a row-major double[][]
        //using readLocks since we currently reading this matrix vectors info and we dont want it to change by other threads
        SharedVector[] vectorsCopy = this.vectors; //since this class isnt synchronized 
        acquireAllVectorReadLocks(vectorsCopy);
        //copy belongs to current thread only and isnt shared resource. 
        //vectors inside are shared therfore we use readlocks 
        try{
            if(vectorsCopy.length==0){
                return new double[0][0];
            }
            if(vectorsCopy[0] == null){
                throw new IllegalArgumentException("first vector is null");
            }
            int rows, columns;
            double[][] matrix = new double[0][0];
            if(vectorsCopy[0].getOrientation()==VectorOrientation.ROW_MAJOR){
                rows = vectorsCopy.length;
                columns = vectorsCopy[0].length();
                matrix = new double[rows][columns];
                for(int i=0;i<rows;i++){
                    SharedVector currentRow = vectorsCopy[i];
                    for(int j=0;j<columns;j++){
                        matrix[i][j] = currentRow.get(j);
                    }
                }
            }
            //else-->orientation == COLUMN_MAJOR
            else{
                rows = vectorsCopy[0].length();
                columns = vectorsCopy.length;
                matrix = new double[rows][columns];
                for(int i=0;i<columns;i++){
                    SharedVector currentColumn = vectorsCopy[i];
                    for(int j=0;j<rows;j++){
                        matrix[j][i] = currentColumn.get(j);
                    }
                }
            }
        return matrix;
        }finally{
            releaseAllVectorReadLocks(vectorsCopy);
        }
    }

    public SharedVector get(int index) {
        //basic query
        //@PRE:0<=index<vectors.length
        //@POST:none
        //@Returns vactor at index
        SharedVector[] tempVectors = this.vectors; //make sure weve got valid info 
        if(index<0 || index>=tempVectors.length){
            throw new IndexOutOfBoundsException("index invalid");
        }
        return(tempVectors[index]);
    }

    public int length() {
        //basic query
        //@PRE:none
        //@POST:none
        //@Returns: current number of stored vectors
        return(vectors.length);
    }

    public VectorOrientation getOrientation() {
        //basic query
        //@PRE:none
        //@POST: none
        //@Returns: current orientation of the matrix
        SharedVector[] tempVectors = this.vectors;
        //return ROW_MAJOR in case of emoty matrix just bc i didnt really know what to do. 
        //asked in the forum. waiting for answer
        if(tempVectors.length==0){
           return VectorOrientation.ROW_MAJOR;
        }
        //might be unnecessary
        if(tempVectors[0]==null){
            throw new IllegalArgumentException("first vector is null");
        }
        return tempVectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        if(vecs == null){
            throw new IllegalArgumentException("vecs is null");
        }
        for(SharedVector vector:vecs){
            if(vector!=null){
                vector.readLock();
            }
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        if(vecs == null){
            throw new IllegalArgumentException("vecs is null");
        }
        for(SharedVector vector:vecs){
            if(vector!=null){
                vector.readUnlock();
            }
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        if(vecs == null){
            throw new IllegalArgumentException("vecs is null");
        }
        for(SharedVector vector:vecs){
            if(vector!=null){
                vector.writeLock();
            }
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        if(vecs == null){
            throw new IllegalArgumentException("vecs is null");
        }
        for(SharedVector vector:vecs){
            if(vector!=null){
                vector.writeUnlock();
            }
        }
    }
}
