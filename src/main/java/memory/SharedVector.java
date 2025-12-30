package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // @pre vector != null
        // @post this.vector == vector && this.orientation == orientation
        if (vector == null) {
            throw new IllegalArgumentException("Vector data cannot be null");
        }
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        //@pre index >= 0 && index < vector.length
        //@post result == this.vector[index]
        readLock();
        try {
            if (index < 0 || index >= vector.length) {
                throw new IndexOutOfBoundsException(index + " is not a legal index");
            }
            return vector[index];
        } finally {
            readUnlock();
        }
    }

    public int length() {
        //@pre true
        //@post result == this.vector.length
        readLock();
        try {
            return vector.length;
        } finally {
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        //@pre true
        //@post result == this.orientation
        readLock();
        try {
            return orientation;
        } finally {
            readUnlock();
        }
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        //@pre lock.isWriteLockedByCurrentThread()
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();       
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        //@post this.orientation != old(this.orientation)
        writeLock();
        try {
            if (orientation == VectorOrientation.ROW_MAJOR) {
                orientation = VectorOrientation.COLUMN_MAJOR;
            } else {
                orientation = VectorOrientation.ROW_MAJOR;
            }
        } finally {
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        //@pre this.length() == other.length()
        //@post for all i: this.vector[i] == old(this.vector[i]) + other.vector[i]
        writeLock();
        try{
            if (this.length() != other.length()) {
                throw new IllegalArgumentException("Incompatible vector lengths");
            }
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] += other.get(i); // other.get(i) locks other for reading 
            }
        }
        finally{
            writeUnlock();
        }
    }

    public void negate() {
        //@post for all i: this.vector[i] == -1 * old(this.vector[i])
        writeLock();
        try{
            for (int i = 0 ; i<vector.length ; i++){
                vector[i] = -1*vector[i];
            }
        }
        finally{
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        //@per vector orientation == ROW_MAJOR && other.orientation == COLUMN_MAJOR
        //@pre this.length() == other.length()
        //@post result == sum for all i: this.vector[i] * other.vector[i]
        readLock();
        try{
            if (length() != other.length()){
                throw new IllegalArgumentException("Incompatible vector lengths");
            }
            if( this.getOrientation() != VectorOrientation.ROW_MAJOR ||
                other.getOrientation() != VectorOrientation.COLUMN_MAJOR){
                throw new IllegalArgumentException("Incompatible vector orientations");
            }
            double sum = 0;
            for (int i = 0; i < vector.length; i++) {
                sum += vector[i] * other.get(i); // other.get(i) locks other for reading
            }
            return sum;
        }
        finally{
            readUnlock();
        }
    }

    public void vecMatMul(SharedMatrix matrix) {
        //@pre Vector is ROW_MAJOR, Matrix is COLUMN_MAJOR , this.length() == matrix.length()
        //@post this.vector == result of multiplying this vector by the matrix
        writeLock();
        try {
            int matrixRows = matrix.get(0).length();
            int matrixCols = matrix.length();
            // Validation of dimensions
            if (this.length() != matrixRows) {
                throw new IllegalArgumentException("Incompatible vector and matrix sizes");
            }
            // Validation of orientations
            if (this.getOrientation() != VectorOrientation.ROW_MAJOR ||
                matrix.getOrientation() != VectorOrientation.COLUMN_MAJOR) {
                throw new IllegalArgumentException("Incompatible vector and matrix orientations");
            }
            // Initialize result array based on the number of columns in the matrix

            double[] result = new double[matrixCols];

        for (int j = 0; j < matrixCols; j++) {

            // i didnt use dot to avoid deadlocks aka a vector trying to readLock while holding a writeLock
            SharedVector colVector = matrix.get(j);
            double sum = 0;

            for (int i = 0; i < matrixRows; i++) {
                sum += this.vector[i] * colVector.get(i);
            }
            result[j] = sum;
        }
            this.vector = result;
        } finally {
            writeUnlock();
        }
    }
}
