package spl.lae;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import memory.SharedMatrix;
import memory.VectorOrientation;

import static org.junit.jupiter.api.Assertions.*;

class LinearAlgebraEngineTest {

    private LinearAlgebraEngine lae;
    private final int NUM_THREADS = 4;
    private final double DELTA = 0.0001;

    @BeforeEach
    void setUp() {
        lae = new LinearAlgebraEngine(NUM_THREADS);
    }


    @Test
    void testSimpleAddition() {
        double[][] dataA = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] dataB = {{5.0, 6.0}, {7.0, 8.0}};
        double[][] expected = {{6.0, 8.0}, {10.0, 12.0}};
        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        List<ComputationNode> children = new ArrayList<>();
        children.add(nodeA);
        children.add(nodeB);
        ComputationNode addNode = new ComputationNode("+", children);
        ComputationNode resultNode = lae.run(addNode);
        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    
    @Test
    void testMultiplication() {
        double[][] dataA = {{1.0, 2.0}, {3.0, 4.0}}; // 2x2
        double[][] dataB = {{2.0, 0.0}, {1.0, 2.0}}; // 2x2
        double[][] expected = {{4.0, 4.0}, {10.0, 8.0}};
        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        List<ComputationNode> children = new ArrayList<>();
        children.add(nodeA);
        children.add(nodeB);
        ComputationNode multNode = new ComputationNode("*", children);
        ComputationNode resultNode = lae.run(multNode);
        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    @Test
    void testTranspose() {
        double[][] data = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}}; // 2x3
        double[][] expected = {{1.0, 4.0}, {2.0, 5.0}, {3.0, 6.0}}; // 3x2
        ComputationNode node = new ComputationNode(data);
        List<ComputationNode> children = new ArrayList<>();
        children.add(node);
        ComputationNode transNode = new ComputationNode("T", children);
        ComputationNode resultNode = lae.run(transNode);
        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    
    @Test
    void testNegate() {
        double[][] data = {{5.0, -3.0}, {0.0, 1.0}};
        double[][] expected = {{-5.0, 3.0}, {-0.0, -1.0}};
        ComputationNode node = new ComputationNode(data);
        List<ComputationNode> children = new ArrayList<>();
        children.add(node);
        ComputationNode negNode = new ComputationNode("-", children);
        ComputationNode resultNode = lae.run(negNode);
        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    
    @Test
    void testAssociativeAddition() {
        double[][] A = {{1.0}};
        double[][] B = {{2.0}};
        double[][] C = {{3.0}};
        double[][] expected = {{6.0}};

        ComputationNode nodeA = new ComputationNode(A);
        ComputationNode nodeB = new ComputationNode(B);
        ComputationNode nodeC = new ComputationNode(C);
        List<ComputationNode> children = new ArrayList<>();
        children.add(nodeA);
        children.add(nodeB);
        children.add(nodeC);
        ComputationNode rootNode = new ComputationNode("+", children);
        ComputationNode resultNode = lae.run(rootNode);
        assertMatrixEquals(expected, resultNode.getMatrix());
    }


    @Test
    void testComplexTree() {
        double[][] valA = {{1.0}};
        double[][] valB = {{2.0}};
        double[][] valC = {{3.0}};
        ComputationNode nodeA = new ComputationNode(valA);
        ComputationNode nodeB = new ComputationNode(valB);
        ComputationNode nodeC = new ComputationNode(valC);
        List<ComputationNode> addChildren = new ArrayList<>();
        addChildren.add(nodeA);
        addChildren.add(nodeB);
        ComputationNode addNode = new ComputationNode("+", addChildren);
        List<ComputationNode> multChildren = new ArrayList<>();
        multChildren.add(addNode);
        multChildren.add(nodeC);
        ComputationNode root = new ComputationNode("*", multChildren);
        ComputationNode result = lae.run(root);
        assertMatrixEquals(new double[][]{{9.0}}, result.getMatrix());
    }

  
    @Test
    void testDimensionMismatch() {
        double[][] dataA = {{1.0, 2.0}}; // 1x2
        double[][] dataB = {{1.0}, {2.0}}; // 2x1

        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        
        List<ComputationNode> children = new ArrayList<>();
        children.add(nodeA);
        children.add(nodeB);
        ComputationNode root = new ComputationNode("+", children);
        assertThrows(IllegalArgumentException.class, () -> {
            lae.run(root);
        });
    }

    
    @Test
    void testAddWithSingleOperand() {
        double[][] data = {{1.0, 2.0}};
        ComputationNode node = new ComputationNode(data);
        
        List<ComputationNode> children = new ArrayList<>();
        children.add(node); 
        ComputationNode invalidNode = new ComputationNode("+", children);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lae.run(invalidNode);
        });
        assertTrue(exception.getMessage().contains("binary operator with less than 2 kids"));
    }

   

   
    @Test
    void testNegateWithTwoOperands() {
        double[][] data1 = {{1.0}};
        double[][] data2 = {{2.0}};
        
        ComputationNode node1 = new ComputationNode(data1);
        ComputationNode node2 = new ComputationNode(data2);
        
        List<ComputationNode> children = new ArrayList<>();
        children.add(node1);
        children.add(node2);
        ComputationNode invalidNode = new ComputationNode("-", children);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lae.run(invalidNode);
        });
        assertTrue(exception.getMessage().contains("unary operator with more than 1 kid"));
    }


  


    @Test
    void testEmptyMatrixError() {
        double[][] emptyData = new double[0][0]; 
        double[][] validData = {{1.0}};
        ComputationNode emptyNode = new ComputationNode(emptyData);
        ComputationNode validNode = new ComputationNode(validData);
        List<ComputationNode> children = new ArrayList<>();
        children.add(emptyNode);
        children.add(validNode);
        ComputationNode root = new ComputationNode("+", children);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lae.run(root);
        });
        
        assertTrue(exception.getMessage().contains("empty matrices"));
    }


     
    @Test
    void testOrientationMismatchForAddViaReflection() throws Exception {
        java.lang.reflect.Field leftField = lae.getClass().getDeclaredField("leftMatrix");
        java.lang.reflect.Field rightField = lae.getClass().getDeclaredField("rightMatrix");
        leftField.setAccessible(true);
        rightField.setAccessible(true);
        SharedMatrix left = (SharedMatrix) leftField.get(lae);
        SharedMatrix right = (SharedMatrix) rightField.get(lae);
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        left.loadRowMajor(data);
        right.loadColumnMajor(data);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lae.createAddTasks();
        });
        assertTrue(exception.getMessage().contains("orientation mismatch"), 
            "Should throw exception when orientations differ in ADD");
    }

 
    private void assertMatrixEquals(double[][] expected, double[][] actual) {
        assertNotNull(actual, "Result matrix is null");
        assertEquals(expected.length, actual.length, "Rows mismatch");
        assertEquals(expected[0].length, actual[0].length, "Cols mismatch");

        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], DELTA, "Mismatch at row " + i);
        }
    }
}