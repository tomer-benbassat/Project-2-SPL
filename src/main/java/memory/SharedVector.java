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

        if (index < 0 || index >= vector.length) {
            throw new IndexOutOfBoundsException("Index: " + index + "not legal for vector of length " + vector.length);
        }
        return vector[index];
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

        if (this.orientation == VectorOrientation.ROW_MAJOR) {
            this.orientation = VectorOrientation.COLUMN_MAJOR;
        } else {
            this.orientation = VectorOrientation.ROW_MAJOR;
        }
    }

    public void add(SharedVector other) {
        //@pre this.length() == other.length()

        if (this.length() != other.length()) {
            throw new IllegalArgumentException("Incompatible vector lengths");
        }
        for (int i = 0; i < this.length(); i++) {
            this.vector[i] += other.get(i);
        }
    }

    public void negate() {
        for (int i = 0 ; i<vector.length ; i++){
            vector[i] = -1*vector[i];
        }
    }

    public double dot(SharedVector other) {
        //@pre this.length() == other.length()
        if (vector.length != other.vector.length){
            throw new IllegalArgumentException("Incompatible vector lengths");
        }
        double sum = 0;
        for (int i = 0; i < vector.length; i++) {
            sum += vector[i] * other.vector[i];
        }
        return sum;
    }

    public void vecMatMul(SharedMatrix matrix) {
        //@pre this.length() == matrix.length()
        if (this.length() != matrix.length()) {
            throw new IllegalArgumentException("Incompatible vector and matrix sizes");
        }
        double[] result = new double[vector.length];
        for (int j = 0; j < matrix.length(); j++) {
            SharedVector curr = matrix.get(j);
            double sum = 0;
            for (int i = 0; i < this.length(); i++) {
                sum += this.get(i) * curr.get(i);
            }
            result[j] = sum;
        }
        this.vector = result;
    }
}
