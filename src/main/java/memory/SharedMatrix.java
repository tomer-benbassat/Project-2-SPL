package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
                vectors = null;
    }

    public SharedMatrix(double[][] matrix) {
        if (matrix == null) throw new IllegalArgumentException("matrix is null");
        int n = matrix.length;
        if (n==0) throw new IllegalArgumentException("matrix is empty");
        SharedVector[] vectorList = new SharedVector[n];
        for(int i = 0;i<n;i++){
            if (matrix[i].length != n) {
                throw new IllegalArgumentException("inconsistent row size");
            }
            SharedVector vector = new SharedVector
                (matrix[i], VectorOrientation.ROW_MAJOR);
            vectorList[i]=vector;
        }
        vectors = vectorList;
    }

    public void loadRowMajor(double[][] matrix) {
        if (matrix == null) throw new IllegalArgumentException("matrix is null");
        int n = matrix.length;
        if (n==0) throw new IllegalArgumentException("matrix is empty");
        if (n!=vectors.length) throw new IllegalArgumentException("new matrix is not"+
        " the same size as the current one");
        for(int i = 0;i<n;i++){
            if (matrix[i].length != n) {
                throw new IllegalArgumentException("inconsistent row size");
            }
            vectors[i] = new SharedVector
                (matrix[i], VectorOrientation.ROW_MAJOR);
        }
    }

    public void loadColumnMajor(double[][] matrix) {
        if (matrix == null)
            throw new IllegalArgumentException("matrix is null");
        int n = matrix.length;
        if (n == 0)
            throw new IllegalArgumentException("matrix is empty");
        if (n != vectors.length)
            throw new IllegalArgumentException("new matrix is not the same size as the current one");
        for (int i = 0; i < n; i++) {
            if (matrix[i].length != n)
                throw new IllegalArgumentException("inconsistent row size");
        }
        for (int col = 0; col < n; col++) {
            double[] column = new double[n];
            for (int row = 0; row < n; row++) {
                column[row] = matrix[row][col];
            }
            vectors[col] = new SharedVector(column, VectorOrientation.COLUMN_MAJOR);
        }
    }

    public double[][] readRowMajor() {
        int n =vectors[0].length();
        double  [][] result = new double[vectors.length][n];
        for (int i = 0; i < vectors.length; i++) {
            if (vectors[i].length() != n) 
                throw new IllegalStateException("Inconsistent vector lengths");
            for (int j = 0; j < vectors[i].length(); j++) {
                result[i][j] = vectors[i].get(j);
            }
        }
        return result;
    }

    public SharedVector get(int index) {
        return vectors[index];
    }

    public int length() {
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        if (vectors.length==0) throw new IllegalStateException
            ("Matrix has no vectors");
        // TODO: return orientation
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
    }
}
