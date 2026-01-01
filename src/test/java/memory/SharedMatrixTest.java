package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedMatrixTest {

    private final double DELTA = 1e-9;

    private void assertMatrixEquals(double[][] expected, double[][] actual) {
        assertNotNull(actual);
        assertEquals(expected.length, actual.length);
        if (expected.length > 0) {
            assertEquals(expected[0].length, actual[0].length);
        }
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], DELTA);
        }
    }

    @Test
    void testConstructorAndLoadRowMajor() {
        double[][] data = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0}
        };

        SharedMatrix matrix = new SharedMatrix(data);

        assertEquals(2, matrix.length());
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());

        double[][] result = matrix.readRowMajor();
        assertMatrixEquals(data, result);
    }

    @Test
    void testLoadRowMajorDirectly() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        
        matrix.loadRowMajor(data);
        
        assertEquals(2, matrix.length());
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
        assertMatrixEquals(data, matrix.readRowMajor());
    }

    @Test
    void testLoadColumnMajorSquare() {
        double[][] data = {
            {1.0, 2.0}, 
            {3.0, 4.0}
        };

        SharedMatrix matrix = new SharedMatrix();
        matrix.loadColumnMajor(data);

        assertEquals(2, matrix.length());
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());

        double[][] result = matrix.readRowMajor();
        assertMatrixEquals(data, result);
    }

    @Test
    void testLoadColumnMajorNonSquare() {
        double[][] data = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0}
        };

        SharedMatrix matrix = new SharedMatrix();
        matrix.loadColumnMajor(data);

        assertEquals(3, matrix.length());
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());

        double[][] result = matrix.readRowMajor();
        assertMatrixEquals(data, result);
    }

    @Test
    void testNullInputs() {
        SharedMatrix matrix = new SharedMatrix();

        assertThrows(IllegalArgumentException.class, () -> new SharedMatrix(null));
        assertThrows(IllegalArgumentException.class, () -> matrix.loadRowMajor(null));
        assertThrows(IllegalArgumentException.class, () -> matrix.loadColumnMajor(null));
        
        double[][] badData = {null, {1.0}};
        assertThrows(IllegalArgumentException.class, () -> matrix.loadColumnMajor(badData));
    }

    @Test
    void testEmptyMatrix() {
        double[][] empty = new double[0][0];
        SharedMatrix matrix = new SharedMatrix();
        
        matrix.loadRowMajor(empty);
        assertEquals(0, matrix.length());
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation()); 
        
        double[][] result = matrix.readRowMajor();
        assertEquals(0, result.length);

        matrix.loadColumnMajor(empty);
        assertEquals(0, matrix.length());
    }

    @Test
    void testGetVector() {
        double[][] data = {{1.0}, {2.0}};
        SharedMatrix matrix = new SharedMatrix(data);

        SharedVector v0 = matrix.get(0);
        assertNotNull(v0);
        assertEquals(1.0, v0.get(0), DELTA);

        assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(2));
    }

    @Test
    void testOrientationChanges() {
        SharedMatrix matrix = new SharedMatrix();
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());

        matrix.loadRowMajor(new double[][]{{1.0}});
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());

        matrix.loadColumnMajor(new double[][]{{1.0}});
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());
    }

@Test
    void testUnevenBehavior() {
        double[][] uneven = {
            {1.0, 2.0},
            {3.0} 
        };
        SharedMatrix matrix = new SharedMatrix();
        assertThrows(IndexOutOfBoundsException.class, () -> matrix.loadColumnMajor(uneven));
        matrix.loadRowMajor(uneven);
        assertThrows(IndexOutOfBoundsException.class, () -> matrix.readRowMajor());
    }
}