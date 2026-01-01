package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import memory.SharedMatrix;
import memory.SharedVector;
import memory.VectorOrientation;

class SharedVectorTest {

    private final double DELTA = 1e-9;

    
    @Test
    void testConstructorValid() {
        double[] data = {1.0, 2.0};
        SharedVector v = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        assertNotNull(v);
        assertEquals(2, v.length());
    }

    @Test
    void testConstructorNullParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SharedVector(null, VectorOrientation.ROW_MAJOR);
        }, "Should throw exception when data array is null");

        assertThrows(IllegalArgumentException.class, () -> {
            new SharedVector(new double[]{1.0}, null);
        }, "Should throw exception when orientation is null");
    }

    @Test
    void testGetAndLengthValid() {
        double[] data = {10.0, 20.0, 30.0};
        SharedVector v = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        assertEquals(3, v.length());
        assertEquals(10.0, v.get(0), DELTA);
        assertEquals(30.0, v.get(2), DELTA);
    }

    @Test
    void testGetOutOfBounds() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IndexOutOfBoundsException.class, () -> v.get(-1), 
            "Should throw exception for negative index");

        assertThrows(IndexOutOfBoundsException.class, () -> v.get(2), 
            "Should throw exception for index >= length");
    }



    @Test
    void testTranspose() {
        SharedVector v = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
        v.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }



    @Test
    void testAddValid() {
        SharedVector v1 = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3.0, 4.0}, VectorOrientation.ROW_MAJOR);
        v1.add(v2);
        assertEquals(4.0, v1.get(0), DELTA); // 1+3
        assertEquals(6.0, v1.get(1), DELTA); // 2+4
    }

    @Test
    void testAddExceptions() {
        SharedVector vBase = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        SharedVector vDiffLength = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        SharedVector vDiffOrient = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> vBase.add(vDiffLength),
            "Should throw exception when adding vectors of different lengths");
        assertThrows(IllegalArgumentException.class, () -> vBase.add(vDiffOrient),
            "Should throw exception when adding vectors with different orientation");
    }

    

    @Test
    void testNegate() {
        SharedVector v = new SharedVector(new double[]{1.0, -2.0}, VectorOrientation.ROW_MAJOR);
        v.negate();
        assertEquals(-1.0, v.get(0), DELTA);
        assertEquals(2.0, v.get(1), DELTA);
    }

  

    @Test
    void testDotValid() {
        SharedVector v1 = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3.0, 4.0}, VectorOrientation.ROW_MAJOR);
        assertEquals(11.0, v1.dot(v2), DELTA);
    }

    @Test
    void testDotExceptions() {
        SharedVector v1 = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v1.dot(v2),
            "Should throw exception when dot product lengths mismatch");
    }

   

    @Test
    void testVecMatMulValid() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m = new SharedMatrix();
        double[][] data = {{3.0, 4.0}, {5.0, 6.0}};
        m.loadColumnMajor(data); 
        v.vecMatMul(m);
        assertEquals(2, v.length()); 
        assertEquals(13.0, v.get(0), DELTA);
        assertEquals(16.0, v.get(1), DELTA);
    }

    @Test
    void testVecMatMulExceptions() {
        SharedVector v = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(null),
            "Should throw if matrix is null");
        SharedMatrix emptyMatrix = new SharedMatrix(); 
        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(emptyMatrix),
            "Should throw if matrix is empty");
        SharedMatrix rowMatrix = new SharedMatrix();
        rowMatrix.loadRowMajor(new double[][]{{1.0}, {2.0}});
        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(rowMatrix),
            "Should throw if matrix is not COLUMN_MAJOR");
        SharedMatrix dimMismatchMatrix = new SharedMatrix();
        dimMismatchMatrix.loadColumnMajor(new double[][]{{1.0, 2.0, 3.0}}); 
        
        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(dimMismatchMatrix),
            "Should throw if vector length matches matrix dimension (inner dimension mismatch)");
    }
}