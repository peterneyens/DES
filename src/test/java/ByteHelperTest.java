/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import helpers.ByteHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


/**
 *
 * @author arno
 */
public class ByteHelperTest {
    
    
    public ByteHelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void testPermutate1() {
        int[] testExpansionTabel = new int[]{
                1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12,
                13, 14, 15, 16, 17, 18,
                19, 20, 21, 22, 23, 24,
                25, 26, 27, 28, 29, 30,
                31, 32, 1, 2, 3, 4,
                5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16
        };
        byte[] block = new byte[]{1, 2, 3, 4};
        byte[] expected = new byte[]{1, 2, 3, 4, 1, 2};
        
        byte[] result = ByteHelper.permutate(block, testExpansionTabel);
        
        for (int i = 0; i < block.length; i++) {
            assertEquals(result[i], expected[i]);
        }
    }
    
        @Test
    public void testPermutate2() {
        int[] testExpansionTabel = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        byte[] block = new byte[]{10, 12, 11, 13};
        byte[] expected = new byte[]{10, 12, 11,13, 10, 12};
        
        byte[] result = ByteHelper.permutate(block, testExpansionTabel);
        
        for (int i = 0; i < block.length; i++) {
            assertEquals(result[i], expected[i]);
        }
    }
}
