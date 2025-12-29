package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector data cannot be null");
        }
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        //@pre index >= 0 && index < vector.length

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
        return vector.length; 
    }

    public VectorOrientation getOrientation() {
        return orientation;
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
        /*maybe do 2 times try and catch? */
        writeLock();
        other.readLock();
        try{
            if (this.length() != other.length()) {
                throw new IllegalArgumentException("Incompatible vector lengths");
            }
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] += other.get(i);
            }
        }
        finally{
            other.readUnlock();
            writeUnlock();
        }
    }

    public void negate() {
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
        //@pre this.length() == other.length()
        /* maybe 2 try & finally */
        readLock();
        other.readLock();
        try{
            if (length() != other.length()){
                throw new IllegalArgumentException("Incompatible vector lengths");
            }
            double sum = 0;
            for (int i = 0; i < vector.length; i++) {
                sum += vector[i] * other.vector[i];
            }
            return sum;
        }
        finally{
            other.readUnlock();
            readUnlock();
        }
    }

    public void vecMatMul(SharedMatrix matrix) {
        //@pre this.length() == matrix.length()
        writeLock();
        try{
            if (this.length() != matrix.length()) {
                throw new IllegalArgumentException("Incompatible vector and matrix sizes");
            }
            double[] result = new double[vector.length];
            for (int j = 0; j < matrix.length(); j++) {
                SharedVector curr = matrix.get(j);
                curr.readLock();
                try{
                    double sum = 0;
                    for (int i = 0; i < this.length(); i++) {
                        sum += vector[i] * curr.get(i);
                    }
                    result[j] = sum;
                }finally{
                    curr.readUnlock();
                }
            }
            this.vector = result;
        }finally{
            writeUnlock();
        }
    }
}
